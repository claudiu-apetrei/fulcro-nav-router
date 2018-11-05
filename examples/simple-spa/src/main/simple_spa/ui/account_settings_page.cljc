(ns simple-spa.ui.account-settings-page
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])))

(defsc AccountSettingsPage [this {:keys [page/title]} computed]
       {:query         [:page/title]
   :ident         (fn [] [:page/account :singleton])
   :initial-state {:page/title "Account Page"}}
       (dom/section
   (dom/div :.hero-body
     (dom/h1 :.title "Account Page")
     )))
