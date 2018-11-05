(ns simple-spa.ui.home-page
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])))


(defsc HomePage [this {:keys [page/title]} computed]
  {:query         [:page/title]
   :ident         (fn [] [:page/home :singleton])
   :initial-state {:page/title "Home Page"}}
  (dom/section
   (dom/div :.hero-body
     (dom/h1 :.title "Welcome !!")
     )))
