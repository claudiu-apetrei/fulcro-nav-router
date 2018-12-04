(ns demo-app.containers.account-settings-page
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   [fulcro-nav-router.core :as nav-router :refer [defsc-route]]
   #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])))

(defsc AccountSettingsPage [this {:keys [page/title]} computed]
  {:query         [:page/title]
   :ident         (fn [] [:page/account :singleton])
   :initial-state {:page/title "Account Page"}}
  (dom/section
   (dom/div :.hero-body
     (dom/h1 :.title "Account Page")
     )))
