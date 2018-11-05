(ns simple-spa.ui.root
  (:require
    [fulcro.client.mutations :as m]
    [fulcro.client.data-fetch :as df]
    #?(:cljs [fulcro.client.dom :as dom] :clj [fulcro.client.dom-server :as dom])
    [simple-spa.api.mutations :as api]
    [fulcro-nav-router.core :as nav-router]
    [fulcro.client.primitives :as prim :refer [defsc]]))

;; The main UI of your application

(defsc Root [this {:keys [router]}]
  {:initial-state (fn [p]
                    {:router (prim/get-initial-state nav-router/RouterComponent {})})
   :query         [{:router (prim/get-query nav-router/RouterComponent)}]}
  (dom/div
    (dom/nav :.navbar.has-shadow
      (dom/div :.container
        (dom/div :.navbar-brand
          (dom/a :.navbar-item {:href "/" :onClick nav-router/hijack-link!}
                 "Home"))
        (dom/div :.navbar-menu.is-active
          (dom/div :.navbar-start
            (dom/a :.navbar-item {:href "/friends" :onClick nav-router/hijack-link!} "Friends")
            (dom/a :.navbar-item {:href "/about" :onClick nav-router/hijack-link!} "About")
            (dom/div :.navbar-item
              (dom/button :.button {:onClick #(nav-router/nav-to! "/about")} "About button"))
            (dom/a :.navbar-item {:href "/account/settings" :onClick nav-router/hijack-link!} "Account - module")))
        ))

    (dom/main :.bd-main
      (dom/div :.container.bd-main-container {:style {:minHeight "400px"}}
        (nav-router/ui-router router)))
   ))
