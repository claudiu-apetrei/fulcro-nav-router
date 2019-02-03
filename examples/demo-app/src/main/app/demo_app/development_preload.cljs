(ns demo-app.development-preload
  (:require [fulcro.logging :as log]))

; Add code to this file that should run when the initial application is loaded in development mode.
; shadow-cljs already enables console print and plugs in devtools if they are on the classpath,

(js/console.log "Turning logging to :all (in demo-app.development-preload)")
(log/set-level! :all)
