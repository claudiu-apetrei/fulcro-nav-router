(ns fulcro-nav-router.protocols)

(defprotocol NAV-ROUTER
  (get-config [this] "")
  (nav-to! [this uri push-uri?] "")
  (dispatch-next [this previous payload] "")
  (load-module [this payload] "")
  (build-initial-app-state [this payload] "")
  (change-route [this payload] "")
  (call-on-before-enter [this payload] ""))

(defprotocol RoutingOnBeforeEnter
  (on-before-enter [this router-data]))

