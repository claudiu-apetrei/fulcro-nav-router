(ns simple-spa.ui.person-page
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro-nav-router.protocols :as p]
   [fulcro.client.data-fetch :as df]
   [fulcro-nav-router.core :as nav-router :refer [defsc-route]]
   [fulcro-nav-router.utils :as nav-utils]
   #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])
   [fulcro-nav-router.protocols :as p]))




(defsc PersonDisplay [this {:keys [db/id person/name person/age person/about]} {}]
  {:query         [:db/id :person/name :person/age :person/about]
   :ident         [:person/by-id :db/id]
   :initial-state {}}
  (dom/section
   (dom/div :.hero-body
     (dom/h1 :.title name)
     (dom/p "from server requested by on-before-enter: " about)))
  )

(def ui-person-display (prim/factory PersonDisplay {:keyfn :db/id}))


(defsc-route PersonPage [this {:keys [db/id person]} computed]
  {:query           [:db/id {:person (prim/get-query PersonDisplay)}]
  :ident           [:page/person :db/id]
  :initial-state   (fn [params]
                     (let [id (-> params :id int)]
                       {:db/id id}))
  :on-before-enter (fn [{:keys [::nav-router/reconciler
                                ::nav-router/component-initial-state] :as payload}]
                     (let [id (:db/id component-initial-state)]
                       (nav-utils/set-value! reconciler [:page/person id :person] [:person/by-id id])
                       (df/load reconciler [:person/by-id id] PersonDisplay)))}
  (dom/section
   (ui-person-display person)
   ))