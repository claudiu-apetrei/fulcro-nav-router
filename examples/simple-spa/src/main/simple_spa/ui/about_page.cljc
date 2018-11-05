(ns simple-spa.ui.about-page
  (:require
   [fulcro.client.primitives :as prim :refer [defsc]]
   #?(:clj [fulcro.client.dom-server :as dom] :cljs [fulcro.client.dom :as dom])))

(defsc AboutPage [this {:keys [page/title page/description]}]
  {:query         [:page/title :page/description]
   :ident         (fn [] [:page/about :singleton])
   :initial-state {:page/title       "About Page"
                   :page/description "lorem ipsum about ..."}}
  (dom/section
   (dom/div :.hero-body
     (dom/h1 :.title title)
     (dom/h2 :.subtitle description))))
