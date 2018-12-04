(ns user
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [fulcro-spec.suite :as suite]
    [fulcro-spec.selectors :as sel]
    [mount.core :as mount]
    ;; this is the top-level dependent component...mount will find the rest via ns requires
    [demo-app.server-components.http-server :refer [http-server]]))

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
(set-refresh-dirs "src/main/server-components" "src/dev" "src/test")

(defn start
  "Start the web server"
  [] (mount/start))

(defn stop
  "Stop the web server"
  [] (mount/stop))

(defn restart
  "Stop, reload code, and restart the server. If there is a compile error, use:

  ```
  (tools-ns/refresh)
  ```

  to recompile, and then use `start` once things are good."
  []
  (stop)
  (tools-ns/refresh :after 'user/start))

;; Run (start-server-tests) in a REPL to start a runner that can render results in a browser
;; See fulcro-spec documentation for more information. NOTE: `specification` is really just
;; a `deftest` underneath, so you can use "Run all tests in this namespace" with your
;; editor/IDE and it should work that way too.  You can also use the fulcro-spec functions
;; like `assertions` and `when-mocking` in a regular `deftest` if you'd rather do that (which
;; gives a slight better REPL integration, while still leveraging some of the helpers).
(suite/def-test-suite start-server-tests
  {:config       {:port 8888}
   :test-paths   ["src/test"]
   :source-paths ["src/main"]}
  {:available #{:focused :unit :integration}
   :default   #{::sel/none :focused :unit}})


