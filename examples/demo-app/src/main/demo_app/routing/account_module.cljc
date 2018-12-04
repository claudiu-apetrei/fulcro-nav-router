(ns demo-app.routing.account-module
  (:require
   [demo-app.containers.account-settings-page :refer [AccountSettingsPage]]
   [fulcro-nav-router.core :as nav-router]))

(nav-router/init-module-routes :account {:account-settings AccountSettingsPage})