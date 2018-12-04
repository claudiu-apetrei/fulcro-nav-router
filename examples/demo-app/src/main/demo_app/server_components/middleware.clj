(ns demo-app.server-components.middleware
  (:require
   [demo-app.server-components.config :refer [config]]
   [mount.core :refer [defstate]]
   [fulcro.server :as server :refer [defquery-entity defquery-root]]
   [ring.middleware.defaults :refer [wrap-defaults]]
   [ring.middleware.gzip :refer [wrap-gzip]]
   [ring.util.response :refer [response file-response resource-response]]
   [ring.util.response :as resp]
   [taoensso.timbre :as timbre]
   [hiccup.page :refer [html5]]))

(def ^:private not-found-handler
  (fn [req]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "NOPE"}))



(def people-db (atom {1 {:db/id 1 :person/name "Bert" :person/age 55 :person/about "aaaaa"}
                      2 {:db/id 2 :person/name "Sally" :person/age 22 :person/about "bbbbbbbbbbbbbbbb"}
                      3 {:db/id 3 :person/name "Allie" :person/age 76 :person/about "ccccccccccccccc"}
                      4 {:db/id 4 :person/name "Zoe" :person/age 32 :person/about "ddddddddddddddddddd"}}))

(def libraries-db (atom {1 {:db/id 1 :library/name "Fulcro" :library/description "a1234" :library/logo "http://fulcro.fulcrologic.com/assets/img/logo.svg"}
                         2 {:db/id 2 :library/name "Pathom" :library/description "b1234" :library/logo "http://www.fulcrologic.com/_/rsrc/1506833826525/config/customLogo.gif?revision=5"}
                         3 {:db/id 3 :library/name "Shadow-cljs" :library/description "c1234" :library/logo "https://raw.githubusercontent.com/thheller/shadow-cljs/master/src/main/shadow/cljs/devtools/server/web/resources/img/shadow-cljs.png"}}))

(defn get-libraries [kind keys]
  (->> @libraries-db
       vals
       vec))

(def server-parser (server/fulcro-parser))

(defquery-entity :library/id
  "Returns the meaning of life."
  (value [{:keys [query]} id params]
         (let [library (get @libraries-db id)]
           (timbre/info "getting" query "for library/id" id)
           (Thread/sleep 500)
           (select-keys library query))))

;; ================================================================================
;; Replace this with a pathom Parser once you get past the beginner stage.
;; This one supports the defquery-root, defquery-entity, and defmutation as
;; defined in the book, but you'll have a much better time parsing queries with
;; Pathom.
;; ================================================================================
(defquery-root :libraries
  "Queries for friends and returns them to the client"
  (value [{:keys [query]} params]
         (Thread/sleep 1000)
         (->> @libraries-db
              vals
              (map #(select-keys % query))
              vec)))

(defn wrap-api [handler uri]
  (fn [request]
    (if (= uri (:uri request))
      (server/handle-api-request
       ;; Sub out a pathom parser here if you want to use pathom.
       server-parser
       ;; this map is `env`. Put other defstate things in this map and they'll be
       ;; in the mutations/query env on server.
       {:config config}
       (:transit-params request))
      (handler request))))

;; ================================================================================
;; Dynamically generated HTML. We do this so we can safely embed the CSRF token
;; in a js var for use by the client.
;; ================================================================================
(defn index [csrf-token]
  (html5
   [:html {:lang "en"}
    [:head {:lang "en"}
     [:title "Application"]
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
     [:link {:href "/bulma.min.css"
             :rel  "stylesheet"}]
     [:link {:rel "shortcut icon" :href "data:image/x-icon;," :type "image/x-icon"}]
     [:script (str "var fulcro_network_csrf_token = '" csrf-token "';")]]
    [:body
     [:div#app]
     [:script {:src "js/main/main.js"}]
     [:script "demo_app.client.init();"]]]))

;; ================================================================================
;; Workspaces can be accessed via shadow's http server on http://localhost:8023/workspaces.html
;; but that will not allow full-stack fulcro cards to talk to your server. This
;; page embeds the CSRF token, and is at `/wslive.html` on your server (i.e. port 3000).
;; ================================================================================
(defn wslive [csrf-token]
  (html5
   [:html {:lang "en"}
    [:head {:lang "en"}
     [:title "devcards"]
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"}]
     [:link {:href "https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.4.1/semantic.min.css"
             :rel  "stylesheet"}]
     [:link {:rel "shortcut icon" :href "data:image/x-icon;," :type "image/x-icon"}]
     [:script (str "var fulcro_network_csrf_token = '" csrf-token "';")]]
    [:body
     [:div#app]
     [:script {:src "js/workspaces/main.js"}]]]))

(defn wrap-html-routes [ring-handler]
  (fn [{:keys [uri anti-forgery-token] :as req}]
    (cond
      (#{"/" "/index.html"} uri)
      (-> (resp/response (index anti-forgery-token))
          (resp/content-type "text/html"))

      ;; See note above on the `wslive` function.
      (#{"/wslive.html"} uri)
      (-> (resp/response (wslive anti-forgery-token))
          (resp/content-type "text/html"))

      :else
      (ring-handler req))))

(defstate middleware
          :start
          (let [defaults-config (:ring.middleware/defaults-config config)
                legal-origins   (get config :legal-origins #{"localhost"})]
            (-> not-found-handler
                (wrap-api "/api")
                server/wrap-transit-params
                server/wrap-transit-response
                (server/wrap-protect-origins {:allow-when-origin-missing? false
                                              :legal-origins              legal-origins})
                (wrap-html-routes)
                ;; If you want to set something like session store, you'd do it against
                ;; the defaults-config here (which comes from an EDN file, so it can't have
                ;; code initialized).
                ;; E.g. (wrap-defaults (assoc-in defaults-config [:session :store] (my-store)))
                (wrap-defaults defaults-config)
                wrap-gzip)))
