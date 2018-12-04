(ns demo-app.workspaces
  (:require
    [nubank.workspaces.core :as ws]
    [demo-app.demo-ws]))

(defonce init (ws/mount))
