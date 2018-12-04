(ns fulcro-nav-router.core
  (:require
   [fulcro-nav-router.protocols :refer [RoutingOnBeforeEnter]]
   [fulcro-nav-router.defsc-extensions :refer [defextended-defsc]]))


(defextended-defsc defsc-route [[`RoutingOnBeforeEnter true]])
