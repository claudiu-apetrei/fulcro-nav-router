(ns demo-app.client
  (:require [fulcro.client :as fc]
            [demo-app.containers.root :as root]
            [demo-app.routing.routes :refer [routes routes* modules]]
            [demo-app.routing.main-module]
            [fulcro.client.mutations :as m :refer [defmutation]]
            [fulcro.client.primitives :as prim :refer [defsc]]
            [fulcro-nav-router.core :as nav-router]
            [fulcro.client.network :as net]))

(defonce app (atom nil))

(defn mount []
  (reset! app (fc/mount @app root/Root "app")))

(defn start []
  (mount))

(def secured-request-middleware
  ;; The CSRF token is embedded in the server_components/html.clj
  (->
   (net/wrap-csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
   (net/wrap-fulcro-request)))


(declare xrec)
(defn ^:export init []
  (reset! app (fc/new-fulcro-client #_{}
                :render-mode :normal
                :started-callback (fn [{:keys [reconciler]}]
                                      (nav-router/init-router {:reconciler reconciler
                                                               :config     {:uri-routing-type :fragment}
                                                               :routes     routes})
                                      (nav-router/nav-to-current-route!))
                :load-marker-default true
                :reconciler-options {:shared {:pi 3.14}}
                ;; This ensures your client can talk to a CSRF-protected server.
                ;; See middleware.clj to see how the token is embedded into the HTML
                :networking {:remote (net/fulcro-http-remote
                                         {:url                "/api"
                                          :request-middleware secured-request-middleware})}))
  (start))

;(-> xrec deref :PAGE/libraries)



(+ 1 2)
(js/console.log "aa")
(prn "aici")