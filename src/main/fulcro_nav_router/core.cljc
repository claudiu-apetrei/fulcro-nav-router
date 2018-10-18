(ns fulcro-nav-router.core
  (:require [pushy.core :as pushy]
            [fulcro.client.dom :as dom]
            #?(:cljs [goog.Uri :as goog-uri])
            [fulcro-nav-router.protocols :as p]
            [fulcro.client.mutations :refer [defmutation]]
            [fulcro.util :as futil]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [cljs.loader :as cljs-loader]
            [bidi.bidi :as bidi]))


(defn component-has-method? [component method]
  (case method
    :on-before-enter (implements? p/Routing component)))

(defmulti get-dynamic-router-target (fn [k] k))
(defmethod get-dynamic-router-target :default [k] nil)

(defsc PRouter [this {:keys [::route-key ::route-data]}]
  {:query         (fn [] [::route-key ::route-data])
   :ident         (fn [] [::nav-router :singleton])
   :initial-state (fn [params] {::route-data [:PAGE/settings 1]
                                ::route-key  :home})}
  (let [active-route-comp (get-dynamic-router-target route-key)]
    (when active-route-comp
      ((prim/factory active-route-comp) route-data))))


(declare routes->bidi-routes
         routes-handlers->modules)


(defrecord Router [config state reconciler history routes]
  p/IRouter
  (get-config [_] config)
  (change-route [this {:keys [::reconciler ::component ::route-info ::ident] :as payload}]
    (let [app-state (prim/app-state reconciler)]
      (swap! app-state assoc-in [::nav-router :singleton ::route-data] ident)
      (swap! app-state assoc-in [::nav-router :singleton ::route-key] (:handler route-info))
      (swap! app-state prim/set-query* PRouter {:query [::route-key {::route-data (prim/get-query component)}]})
      payload)
   )
  (call-on-before-enter [this {:keys [::component] :as payload}]
    (let [on-before-enter? (component-has-method? component :on-before-enter)]
      (prn :call-on-before-enter ":" on-before-enter?)
      (when on-before-enter?
        (p/on-before-enter component payload))
      (p/dispatch-next this :call-on-before-enter payload))
   )
  (load-module [this payload]
    (let [handler      (->> payload ::route-info :handler)
          route-module (get (:handler->module routes) handler)
          callback     #(p/dispatch-next this :load-module payload)]
      (cljs-loader/load route-module callback))
   )
  (build-initial-app-state [this {:keys [::reconciler ::route-info] :as payload}]
    (let [component       (-> route-info :handler get-dynamic-router-target)
          app-state       (prim/app-state reconciler)
          initial-state   (prim/get-initial-state component (:route-params route-info))
          ident           (prim/get-ident component initial-state)
          updated-payload (assoc payload ::ident ident
                                         ::component component
                                         ::component-initial-state initial-state)]
      (swap! app-state assoc-in ident initial-state)
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
      :call-on-before-enter (p/change-route this payload)
      )

   )
  (nav-to! [this uri push-uri?]
    (let [bidi-routes      (:bidi-routes routes)
          route-info       (bidi/match-route bidi-routes uri)
          uri-routing-type (:uri-routing-type config)
          route-uri        (if (= :fragment uri-routing-type) (str "#" uri) uri)
          handler          (:handler route-info)
          route-module     (get (:handler->module routes) handler)
          payload          {::reconciler   reconciler
                            ::uuid         (futil/unique-key)
                            ::route-uri    uri
                            ::route-module route-module
                            ::route-info   route-info}]
      (p/dispatch-next this :nav-to payload)
      (when (and push-uri?
                 (not= uri-routing-type :none))
        (pushy/set-token! history route-uri))

      #?(:cljs (js/console.log "nav-to" route-info uri))

      )))


(declare router)

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
                               (p/nav-to! router uri false))

                             (js/console.log "in browser nav callback" uri)))
        history          (if browser?
                           (pushy/pushy identity js/window.location.pathname)
                           (atom {}))
        routes-map       {:bidi-routes     (routes->bidi-routes routes)
                          :handler->module (routes-handlers->modules routes)}
        router-inst      (Router.
                          config
                          (atom {::browser? browser?})
                          reconciler
                          history
                          routes-map)]
    (def router router-inst)                                ; hack ? :(
    #?(:cljs
       (when (and browser? (not= uri-routing-type :none))
         (.addEventListener js/window "popstate" (partial browser-nav router-inst))))
    router-inst))



(defn nav-to! [uri]
  (p/nav-to! router uri true))


(defn hijack-link! [evt]
  #?(:cljs
     (let [uri (-> evt .-currentTarget (.getAttribute "href"))]
       (.preventDefault evt)
       (p/nav-to! router uri true))))


(def ui-p-router (prim/factory PRouter {:qualifier ::route-key}))

(defn init-module-routes [module-key routes]
  (doseq [[k c] routes]
    (defmethod get-dynamic-router-target k [x] c))
  (when-not (= module-key :main)
    (cljs-loader/set-loaded! module-key)))

(defn routes->bidi-routes [routes]
  ["/" (->> routes vals (apply merge))])

(defn routes-handlers->modules [routes]
  (reduce-kv
   #(merge %1 (zipmap (vals %3) (repeat %2)))
   {}
   routes))
