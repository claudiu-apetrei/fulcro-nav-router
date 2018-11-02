(ns user
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [com.stuartsierra.component :as component]
    [fulcro-spec.suite :as suite]
    [fulcro-spec.selectors :as sel]
    simple-spa.server))
;; === SHADOW REPL ===

(comment
  ;; evaluate any one of these in your nREPL to
  ;; choose a (running and connected) shadown-CLJS nREPL
  (do
    (require '[shadow.cljs.devtools.api :as shadow])
    (shadow/nrepl-select :main))


  (do
    (require '[shadow.cljs.devtools.api :as shadow])
    (shadow/nrepl-select :test))

  (do
    (require '[shadow.cljs.devtools.api :as shadow])
    (shadow/nrepl-select :cards)))


;; ==================== SERVER ====================

(set-refresh-dirs "src/dev" "src/main" "src/test")

(defn started? [sys]
  (-> sys :config :value))

(defonce system (atom nil))

(defn- refresh [& args]
  {:pre [(not @system)]}
  (apply tools-ns/refresh args))

(defn- init []
  {:pre [(not (started? @system))]}
  (when-let [new-system (simple-spa.server/build-server {:config "config/dev.edn"})]
    (reset! system new-system)))

(defn- start []
  {:pre [@system (not (started? @system))]}
  (swap! system component/start))

(defn stop
  "Stop the server."
  []
  (when (started? @system)
    (swap! system component/stop))
  (reset! system nil))

(defn go
  "Initialize the server and start it."
  []
  {:pre [(not @system) (not (started? @system))]}
  (init)
  (start))

(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (refresh :after 'user/go))

; Run (start-server-tests) in a REPL to start a runner that can render results in a browser
(suite/def-test-suite start-server-tests
  {:config       {:port 8888}
   :test-paths   ["src/test"]
   :source-paths ["src/main"]}
  {:available #{:focused :unit :integration}
   :default   #{::sel/none :focused :unit}})

