(ns simple-spa.client
  (:require [fulcro.client :as fc]
            [simple-spa.ui.root :as root]
            [fulcro.i18n :as i18n]
            [fulcro-nav-router.core :as nav-router]
            [simple-spa.routing.main-module]
            [simple-spa.routing.routes :refer [routes]]
            ["intl-messageformat" :as IntlMessageFormat]))

(defn message-format [{:keys [::i18n/localized-format-string ::i18n/locale ::i18n/format-options]}]
  (let [locale-str (name locale)
        formatter  (IntlMessageFormat. localized-format-string locale-str)]
    (.format formatter (clj->js format-options))))

(defonce app (atom nil))

(defn mount []
  (reset! app (fc/mount @app root/Root "app")))

(defn start []
  (mount))

(defn ^:export init []
  (reset! app (fc/new-fulcro-client
               :started-callback (fn [{:keys [reconciler]}]
                                     (nav-router/init-router {:reconciler reconciler
                                                              :config     {:uri-routing-type :fragment}
                                                              :routes     routes
                                                               })
                                     (nav-router/nav-to! "/"))
               :reconciler-options {:shared      {::i18n/message-formatter message-format}
                                    :render-mode :keyframe  ; Good for beginners. Remove to optimize UI refresh
                                    :shared-fn   ::i18n/current-locale}))
  (start))
