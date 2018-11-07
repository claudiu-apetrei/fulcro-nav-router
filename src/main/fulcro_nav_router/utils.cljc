(ns fulcro-nav-router.utils
  (:require [fulcro.client.mutations :as m :refer [defmutation]]
            [fulcro.client.primitives :as prim]))


(defmutation set-my-value*
  ""
  [{:keys [location value]}]
  (action [{:keys [state] :as env}]
          (prn "here in transact" location value)
    (swap! state assoc-in location value)))


(defn set-value! [reconciler location value]
  (prim/transact! reconciler `[(set-my-value* {:location ~location :value ~value})]))
