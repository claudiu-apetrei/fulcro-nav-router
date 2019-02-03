(ns fulcro-nav-router.impl.url-parse
  #?(:cljs (:require
            [goog.Uri :as goog-uri])))


(defn url-decode [s]
  #?(:cljs (js/decodeURIComponent s)
     :clj  (java.net.URLDecoder/decode s)))

#?(:clj
   (defn process-uri [uri]
     (let [uri-obj (new java.net.URI uri)]
       {})))

#?(:cljs
   (defn process-uri [uri]
     (let [uri-obj (goog-uri/parse uri)
           parts   {:raw-path     (.getPath uri-obj)
                    :raw-query    (.getQuery uri-obj)
                    :raw-fragment (.getFragment uri-obj)}]
       parts)))

(defn parse-query-params [q]
  (let [x        (clojure.string/split q #"&")
        split-fn (fn [z]
                   (let [v (clojure.string/split z #"=")]
                     (if (= (count v) 2)
                       (update v 1 url-decode)
                       (conj v nil))))]
    (->> x
      (map split-fn)
      (map #(update % 0 keyword) )
      (into {})
      )))


(defn get-uri-details
  ; TODO jvm implementation
  ([uri]
   (get-uri-details uri nil))
  ([uri fragment-nav]
   (let [uri-details (process-uri uri)]
     uri-details
     (merge {:uri uri}
       uri-details
       {:query-params (parse-query-params (:raw-query uri-details))}
       ))))

;(parse-query-params "/about?a1=b&c1=qp%2F%25123%21&d")
(get-uri-details "/libraries/dsad%2Fdsad?p1=qp%2F%25123%21#abcdfrag")
;(get-uri-details "http://localhost:3051/libraries/dsad%2Fdsad?a1=b&c1=qp%2F%25123%21&d#abcdfrag")


(comment
  (clojure.string/split "a1=b&c1=qp%2F%25123%21&d" #"&")
  (into {} [["a1" "b"] ["c1" "2"] ["d" nil]])


  (update ["aaa" "qp%2F%25123%21"] 1 url-decode)

  (url-decode "qp%2F%25123%21")
  ;(-> uri-obj .getPath js/decodeURIComponent)
  (get-uri-details "http://localhost:3051/libraries/dsad%2Fdsad?p1=qp%2F%25123%21#abcdfrag"))
;(let [uri-details (->> js/document.location goog-uri/parse)
;                                 path        (.getPath uri-details)
;                                 fragment    (.getFragment uri-details)
;                                 uri         (if (not-empty fragment)
;                                               (str path "#" fragment)
;                                               path)]
;                             (if (= uri-routing-type :fragment)
;                               (p/nav-to! router (if-not (empty? fragment) fragment uri) false)
;                               (p/nav-to! router uri false)))
