(ns demo-app.containers.libraries-page
  (:require [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro.client.data-fetch :as df]
            [fulcro.client.mutations :as m :refer [defmutation]]
            [fulcro-nav-router.protocols :as fnp]
            [fulcro.client.impl.protocols :as fp]
            [fulcro.logging :as log]
            [fulcro.client :as fc]
            [clojure.spec.alpha :as s]
            [fulcro-nav-router.core :as nav-router :refer [defsc-route]]
            #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])))

(declare Library LibrariesPage)


(defmutation load-libraries [params]
  (action [{:keys [state] :as env}]
          (df/load-action env :libraries Library {:target   [:PAGE/libraries :singleton :libraries]
                                                  :parallel true}))
  (remote [env]
          (df/remote-load env)))

(defsc Library [this {:keys [db/id library/name library/logo]} {}]
  {:query         [:db/id :library/name :library/logo]
   :ident         [:library/id :db/id]
   :initial-state {}}
  (dom/a :.column {:href    (str "/library/" id)
                   :onClick nav-router/hijack-link!}
         (dom/div :.card
           (dom/div :.card-content
             (dom/p :.title (str name))))))

(def ui-library (prim/factory Library {:keyfn :db/id}))



(defsc LibrariesPage [this {:keys [test libraries]}]
  {:query              [:test {:libraries (prim/get-query Library)}]
   :ident              (fn [] [:PAGE/libraries :singleton])
   :initial-state      (fn [_]
                         {:libraries [] :test ""})
   :protocols          [static fnp/RoutingOnBeforeEnter
                        (on-before-enter [_this router-data]
                                         `[(load-libraries {})]
                                         )]}
  (dom/section
   (dom/div :.hero-body
     (dom/h1 :.title "Libraries")
     (map (fn [l] (ui-library l)) libraries))))




(comment

 ;(= [(`demo-app.containers.libraries-page/set-value {})] (with-meta [(`demo-app.containers.libraries-page/set-value {})] {:as 1}))

 (js/setTimeout (fn []
                  (t22 {})
                  (js/console.log "why why why ?")), 2000))


