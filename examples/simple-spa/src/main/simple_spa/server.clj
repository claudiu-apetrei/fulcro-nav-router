(ns simple-spa.server
  (:require
   [fulcro.easy-server :refer [make-fulcro-server]]
   ; MUST require these, or you won't get them installed.
   [simple-spa.api.read]
   [simple-spa.api.mutations]))

(defn build-server
  [{:keys [config] :or {config "config/dev.edn"}}]
  (make-fulcro-server
   :parser-injections #{:config}
   :config-path config))



