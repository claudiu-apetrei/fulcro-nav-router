(ns simple-spa.ui.friends-page
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro.client.data-fetch :as df]

   [fulcro-nav-router.core :as nav-router :refer [defsc-route]]
   #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])))


(defsc Person [this {:keys [db/id person/name person/age]} {}]
  {:query         [:db/id :person/name :person/age]
   :ident         [:person/by-id :db/id]
   :initial-state {}}
  (dom/a :.column {:href (str "/person/" id)
                   :onClick nav-router/hijack-link!}
         (dom/div :.card
           (dom/div :.card-content
             (dom/p :.title (str name " (age: " age ")"))))))

(def ui-person (prim/factory Person {:keyfn :person/name}))

(defsc PersonList [this {:keys [db/id person-list/label person-list/people]}]
  {:query [:db/id :person-list/label {:person-list/people (prim/get-query Person)}]
   :ident [:person-list/by-id :db/id]
   :initial-state
          (fn [{:keys [id label]}]
            {:db/id              id
             :person-list/label  label
             :person-list/people []})}
  (dom/div :.columns
    (map (fn [p] (ui-person (prim/computed p {}))) people)))

(def ui-person-list (prim/factory PersonList))


(defsc-route FriendsPage [this {:keys [ui/loading? friends]}]
   {:query           [:ui/loading? {:friends (prim/get-query PersonList)}]
    :ident           (fn [] [:page/friends :singleton])
    :initial-state   (fn [_]
                       {:friends (prim/get-initial-state PersonList {:id :friends :label "Friends"})})
    :on-before-enter (fn [{:keys [::nav-router/reconciler] :as payload}]
                       (df/load reconciler :my-friends Person {:target [:person-list/by-id :friends :person-list/people]})
                       )}
   (dom/section
    (dom/div :.hero-body
      (dom/h1 :.title "Friends")
      (ui-person-list friends))))

