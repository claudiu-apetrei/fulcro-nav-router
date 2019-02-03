(ns demo-app.containers.library-page
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro-nav-router.protocols :as p]
   [fulcro.client.data-fetch :as df]
   [fulcro-nav-router.protocols :as fnp]
   [fulcro.client.mutations :as m :refer [defmutation]]
   [fulcro-nav-router.core :as navr :refer [defsc-route]]
   [fulcro-nav-router.utils :as nav-utils]
   #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])
   [fulcro.client.mutations :as m]))

(declare LibraryDisplay)

(defmutation load-library [{:keys [db/id] :as params}]
  (action [{:keys [state] :as env}]
    (df/load-action env [:library/id id] LibraryDisplay {:target   [:PAGE/libraries :singleton :libraries]
                                                         :parallel true}))
  (remote [env]
    (df/remote-load env)))

(defsc LibraryDisplay [this {:keys [db/id library/name library/logo library/description]} {}]
  {:query         [:db/id :library/name :library/logo :library/description]
   :ident         [:library/id :db/id]
   :initial-state {}}
  (dom/section
    (dom/div :.hero-body
      (dom/h1 :.title name)
      (dom/p "from server requested by on-before-enter: " description))))

(def ui-libray-display (prim/factory LibraryDisplay {:keyfn :db/id}))


(defsc LibraryPage [this {:keys [db/id library]} computed]
  {:query         [:db/id {:library (prim/get-query LibraryDisplay)}]
   :ident         [:PAGE/library :db/id]
   :initial-state (fn [params]
                    (let [id (-> params :id int)]
                      {:db/id id}))
   ;:componentWillMount (fn []
   ;                      ;(df/load this :libraries Library {:target [:PAGE/libraries :singleton :libraries]})
   ;                      ;(js/console.log "real this fact" (prim/factory this {}))
   ;                      ;(prim/transact! this `[(m/set-props {:library [:library/id 1]})])
   ;
   ;                      ;(js/console.log (with-meta {:here "a"} {:x 1}))
   ;                      ;(df/load-field this :test {})
   ;                      ;(m/set-value! this :test 1)
   ;                      #_(df/load this :libraries nil {:post-mutation nil
   ;                                                      :target        [:a 1]})
   ;                      #_(df/load this :libraries Library {}))

   :protocols     [static fnp/RoutingOnBeforeEnter
                   (on-before-enter [_this {:keys [::navr/component-initial-state] :as rdata}]
                     (let [id (:db/id component-initial-state)]
                       `[(m/set-props {:library [:library/id ~id]})
                         (load-library {:db/id ~id})])
                     )]}
  (dom/section
    (ui-libray-display library)
    ))
