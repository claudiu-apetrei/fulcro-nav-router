(ns demo-app.server-components.http-server
  (:require
    [demo-app.server-components.config :refer [config]]
    [demo-app.server-components.middleware :refer [middleware]]
    [mount.core :refer [defstate]]
    [org.httpkit.server :as http-kit]))

(defstate http-server
  :start (http-kit/run-server middleware (:http-kit config))
  :stop (http-server))
