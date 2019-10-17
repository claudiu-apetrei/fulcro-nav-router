(ns fulcro-nav-router.utils
  (:require
   [lambdaisland.uri :as l-uri]
   [fulcro.client.mutations :as m :refer [defmutation]]
   [clojure.string :as str]
   [fulcro.client.primitives :as prim]))


(defmutation set-my-value*
             ""
             [{:keys [location value]}]
             (action [{:keys [state] :as env}]
                     (swap! state assoc-in location value)))


(defn set-value! [reconciler location value]
  (prim/transact! reconciler `[(set-my-value* {:location ~location :value ~value})]))


(defn process-query-params [query]
  (reduce
   (fn [acc param]
     (let [[k v] (str/split param #"=" 2)]
       (if (and k v)
         (assoc acc (keyword k) v)
         acc)))
   {}
   (str/split query #"&")))

(defn get-uri-details [uri]
  (let [uri-rec  (l-uri/parse uri)
        path     (:path uri-rec)
        query    (:query uri-rec)
        fragment (:fragment uri-rec)]
    {:path          path
     :fragment      fragment
     :query         query
     :relative-path (cond-> ""
                            path (str path)
                            query (str "?" query)
                            fragment (str "#" fragment))
     :query-params  (process-query-params query)}))

;(get-uri-details "http://localhost:3000/#/friends")



