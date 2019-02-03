(ns fulcro-nav-router.impl.routes-match
  (:require [fulcro-nav-router.impl.url-parse :as url-parse]))

(defprotocol ROUTES-MATCH
  (match-path [this path] "")
  (match-handler [this handler params] ""))

(defrecord Routes-match [routes-list handler->route-info handler->module]
  ROUTES-MATCH
  (match-path [this path]
    (let [params-fn (fn [params-list params]
                      (if (vector? params)
                        (zipmap
                          params-list
                          (->> (rest params)
                            (map url-parse/url-decode)))
                        []))
          match-fn  (fn [{:keys [:pattern :handler :params-list]}]
                      (let [path-details (url-parse/get-uri-details path)
                            params*      (re-find pattern (:raw-path path-details))]
                        (when params*
                          (merge
                            {:handler  handler
                             :fragment (:raw-fragment path-details)
                             :params   (params-fn params-list params*)}
                            (if (not= pattern "(.*)")
                              {:query-params (:query-params path-details)}
                              {:query-params nil})))))]
      (some
        match-fn
        routes-list)))
  (match-handler [this handler params] params))


(defn- process-route [[path handler component] handler->module]
  (let [pattern-str (clojure.string/replace path #"(:[^\/]*)" "([^/]*)")
        pattern     (re-pattern (str "^" pattern-str "$"))
        params*     (re-find pattern path)
        params-list (if (vector? params*)
                      (->> params*
                        rest
                        (map #(-> % (subs 1) keyword))
                        vec)
                      [])]
    {:path        path
     :pattern     pattern
     :handler     handler
     :params-list params-list
     :module      (get handler->module handler)
     :component   component}
    ))


(defn- process-modules [modules]
  (reduce-kv
    (fn [acc module handlers]
      (merge acc (zipmap handlers (repeat module))))
    {}
    modules))

(defn init-routes-match [routes* modules]
  (let [handler->module     (process-modules modules)
        routes-list         (mapv #(process-route % handler->module) routes*)
        handler->route-info (reduce
                              (fn [acc r]
                                (assoc acc (:handler r) r))
                              {}
                              routes-list)]
    (Routes-match.
      routes-list
      handler->route-info
      handler->module)))


(comment
  (def routes*
    [["/" :home :x]
     ["/libraries" :libraries :y]
     ["/library/:id" :library :z]
     ["/about" :about :z]
     ["/account/settings" :account-settings nil]
     ["(.*)" :not-found :nf]
     ])

  (let [router (init-routes-match routes* modules)]
    (match-path router "/test%3A%21a?p1=qp%2F%25123%21#abcdfrag"))

  (def modules {:account [:account-settings]})

  (process-modules {})

  #_(let [pmodules      (process-modules modules)
          routes-list   (process-routes routes* pmodules)
          handlers-list (reduce (fn [acc r] (assoc acc (:handler r) r)) {} routes-list)]
      handlers-list
      #_(match-path routes-index "/"))

  (re-find #"^/$" "/")

  (let [pat-str (clojure.string/replace "/library/:id/:bb" #"(:[^\/]*)" "([^/]*)")])
  (clojure.string/replace "/library/:id/:bb" #"(:[^\/]*)" "([^/]*)")
  (clojure.core/re-find #"^(.*)$" "/library/dadafda")
  (clojure.core/re-find #"^/library/([^\/]*)/([^\/]*)$" "/library/:a-id/:bid")
  (clojure.core/re-find #"^/library/([^\/]*)$" "/library/aa_123-a+:")
  )