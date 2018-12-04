(ns demo-app.client
  (:require [fulcro.client :as fc]
            [demo-app.containers.root :as root]
            [demo-app.routing.routes :refer [routes]]
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


(defmutation set-value [params]
  (action [{:keys [state] :as env}]
          1
          (swap! state assoc-in [:PAGE/libraries :singleton :libraries] [:abcd])
          (swap! state assoc-in [:PAGE/libraries :singleton :sc] [:abcd])
          ;(swap! state ...) ; local optimistic db updates
          ;(js/console.log "env... wtf" (meta env))
          ;(js/console.log "state... wtf" (meta state))
          ;(df/load-action env :other Other) {:remote :other-remote}) ; as many as you need...
          ))

(declare xrec)
(defn ^:export init []
  (reset! app (fc/new-fulcro-client #_{}
               :render-mode :normal
                                    :started-callback (fn [{:keys [reconciler]}]
                                                        (def xrec reconciler)
                                                        (js/setTimeout
                                                         (fn []
                                                           (js/console.log "in started callback timeout")
                                                           #_(prim/transact! reconciler (with-meta `[(set-value {}) (set-value {})] {:aici 1})))
                                                         3000)
                                                        (nav-router/init-router {:reconciler reconciler
                                                                                   :config     {:uri-routing-type :fragment}
                                                                                   :routes     routes})
                                                          (nav-router/nav-to-current-route!))
                                    ;:load-marker-default false
                                    :reconciler-options {:shared {:pi 3.14}}
                                    ;; This ensures your client can talk to a CSRF-protected server.
                                    ;; See middleware.clj to see how the token is embedded into the HTML
                                    :networking {:remote (net/fulcro-http-remote
                                                            {:url                "/api"
                                                             :request-middleware secured-request-middleware})}))
  (start))

;(-> xrec deref :PAGE/libraries)


