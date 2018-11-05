(ns simple-spa.routing.account-module
  (:require
   [simple-spa.ui.account-settings-page :refer [AccountSettingsPage]]
   [fulcro-nav-router.core :as nav-router]))

(nav-router/init-module-routes :account {:account-settings AccountSettingsPage})
