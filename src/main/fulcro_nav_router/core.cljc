(ns fulcro-nav-router.core
  (:require-macros fulcro-nav-router.core)
  (:require [pushy.core :as pushy]
            #?(:cljs [goog.Uri :as goog-uri])
            [fulcro-nav-router.protocols :as p]
            [reitit.core :as reitit]
            [fulcro.client.mutations :refer [defmutation]]
            [fulcro.util :as futil]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [cljs.loader :as cljs-loader]))



(defn component-has-method? [component method]
  (case method
    :on-before-enter (implements? p/RoutingOnBeforeEnter component)))

(defmulti get-dynamic-router-target (fn [k] k))
(defmethod get-dynamic-router-target :default [k] nil)

(defsc RouterComponent [this {:keys [::route-key ::route-data]}]
       {:query         (fn [] [::route-key ::route-data])
        :ident         (fn [] [::nav-router :singleton])
        :initial-state (fn [params] {::route-data []
                                     ::route-key  nil})}
       (let [active-route-comp (get-dynamic-router-target route-key)]
         (when active-route-comp
           ((prim/factory active-route-comp) route-data))))


(declare
 router
 module-routes->routes
 routes-handler->module)


(defrecord Router [config state reconciler history routing-map]
  p/IRouter
  (change-route [this {:keys [::reconciler ::component ::route-info ::ident] :as payload}]
    (let [app-state (prim/app-state reconciler)]
      (swap! app-state assoc-in [::nav-router :singleton ::route-data] ident)
      (swap! app-state assoc-in [::nav-router :singleton ::route-key] (:handler route-info))
      (swap! app-state prim/set-query* RouterComponent {:query [::route-key {::route-data (prim/get-query component)}]})
      payload))
  (call-on-before-enter [this {:keys [::component] :as payload}]
    (let [on-before-enter? (component-has-method? component :on-before-enter)]
      (when on-before-enter?
        (p/on-before-enter component payload))
      (p/dispatch-next this :call-on-before-enter payload)))
  (load-module [this payload]
    (let [handler      (->> payload ::route-info :handler)
          route-module (get (:handler->module routing-map) handler)
          callback     #(p/dispatch-next this :load-module payload)]
      (cljs-loader/load route-module callback)))
  (build-initial-app-state [this {:keys [::reconciler ::route-info] :as payload}]
    (let [component       (-> route-info :handler get-dynamic-router-target)
          component-query (prim/get-query component)
          initial-state   (prim/get-initial-state component (:route-params route-info))
          ident           (prim/get-ident component initial-state)
          app-state       (prim/app-state reconciler)
          add-route-state (fn [state-map]
                            (let [normalized-state (-> (prim/tree->db [{:tmp/new-route component-query}] {:tmp/new-route initial-state} true)
                                                       (dissoc :tmp/new-route))]
                              (futil/deep-merge state-map normalized-state)))
          updated-payload (assoc payload ::ident ident
                                         ::component component
                                         ::component-initial-state initial-state)]
      (swap! app-state add-route-state)
      (p/dispatch-next this :build-initial-app-state updated-payload)))
  (dispatch-next [this previous payload]
    (case previous
      :nav-to (let [{:keys [::route-module]} payload]
                (if (or (= :main route-module) (cljs-loader/loaded? route-module))
                  (p/build-initial-app-state this payload)
                  (p/load-module this payload))
                )
      :load-module (p/build-initial-app-state this payload)
      :build-initial-app-state (p/call-on-before-enter this payload)
      :call-on-before-enter (p/change-route this payload)))
  (nav-to! [this uri push-uri?]
    (let [route-info       (reitit/match-by-path (:router routing-map) uri)
          uri-routing-type (:uri-routing-type config)
          route-uri        (if (= :fragment uri-routing-type) (str "#" uri) uri)
          handler          (-> route-info :data :name)
          route-module     (get (:handler->module routing-map) handler)
          payload          {::reconciler   reconciler
                            ::uuid         (futil/unique-key)
                            ::route-module route-module
                            ::route-info   {:handler      handler
                                            :route-params (:path-params route-info)
                                            :uri          uri}}]
      (p/dispatch-next this :nav-to payload)
      (when (and push-uri?
                 (not= uri-routing-type :none))
        (pushy/set-token! history route-uri)))))


(defn init-router [{:keys [reconciler routes config]}]
  (let [browser?         (exists? js/window)
        uri-routing-type (-> config :uri-routing-type)
        browser-nav      (fn [router evt]
                           (let [uri-details (->> js/document.location goog-uri/parse)
                                 path        (.getPath uri-details)
                                 fragment    (.getFragment uri-details)
                                 uri         (if (not-empty fragment)
                                               (str path "#" fragment)
                                               path)]
                             (if (= uri-routing-type :fragment)
                               (p/nav-to! router (if-not (empty? fragment) fragment uri) false)
                               (p/nav-to! router uri false))))
        history          (if browser?
                           (pushy/pushy identity js/window.location.pathname)
                           (atom {}))
        routes-flat      (module-routes->routes routes)
        routes-map       {:routes          routes-flat
                          :router          (reitit/router routes-flat)
                          :handler->module (routes-handler->module routes)}
        router-inst      (Router.
                          config
                          (atom {::browser? browser?})
                          reconciler
                          history
                          routes-map)]
    (def router router-inst) ; ????

    #?(:cljs
       (when (and browser? (not= uri-routing-type :none))
         (.addEventListener js/window "popstate" (partial browser-nav router-inst))))
    router-inst))


(defn nav-to! [uri]
  (p/nav-to! router uri true))

(defn nav-to* [uri]
  (p/nav-to! router uri false))

(defn hijack-link! [evt]
  #?(:cljs
     (let [uri (-> evt .-currentTarget (.getAttribute "href"))]
       (.preventDefault evt)
       (p/nav-to! router uri true))))


(def ui-router (prim/factory RouterComponent {:qualifier ::route-key}))

(defn init-module-routes [module-key routes]
  (doseq [[k c] routes]
    (defmethod get-dynamic-router-target k [x] c))
  (when-not (= module-key :main)
    (cljs-loader/set-loaded! module-key)))

(defn module-routes->routes [routes]
  (->> routes vals (apply concat) vec))

(defn routes-handler->module [routes]
  (reduce-kv
   (fn [acc k v]
     (merge acc (zipmap (map second v) (repeat k))))
   {}
   routes))
