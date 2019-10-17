(ns demo-app.routing.routes
  (:require [demo-app.containers.home-page :refer [HomePage]]
            [demo-app.containers.about-page :refer [AboutPage]]
            [demo-app.containers.libraries-page :refer [LibrariesPage]]
            [demo-app.containers.library-page :refer [LibraryPage]]
            [fulcro-nav-router.core :as nav-router]))

(def routes
  {:main    [["/" :home]
             ["/libraries" :libraries]
             ["/library/:id" :library]
             ["/about" :about]]
   :account [["/account/settings" :account-settings]]})


(def routes*
  [["/" :home HomePage]
   ["/libraries" :libraries LibrariesPage]
   ["/library/:id" :library LibraryPage]
   ["/about" :about AboutPage]
   ["/account/settings" :account-settings]])

(def modules
  {:account [:account-settings]})


(comment
  (def routes*
    [["/" :home :x]
     ["/libraries" :libraries :y]
     ["/library/:id" :library :z]
     ["/about" :about :z]
     ["/account/settings" :account-settings nil]])


  (comment
    (defn process-route [[path handler component]]
      (let [pattern (-> path
                      (clojure.string/replace #"(:[^\/]*)" "([^/]*)")
                      re-pattern)
            params* (->> path
                      (re-find pattern))
            params (if (vector? params*)
                     (->> params* rest (map #(-> % (subs 1) keyword)) vec)
                     [])]
        {:path      path
         :pattern   pattern
         :handler   handler
         :params    params
         :component component}
        ))

    (keyword (subs ":aaa" 1))

    (let [r1 ["/library/:id" :library :xxx]]
      (process-route r1))
    (def x "/library/55")
    (identity re-find)
    (let [route-str "/library/:id"
          pat-str (clojure.string/replace route-str #"(:[^\/]*)" "([^/]*)")
          re (re-pattern pat-str)]
      (clojure.core/re-find re "/library/aa_123-a+:")
      (clojure.core/re-find re route-str)
      )
    (clojure.string/replace "/library/:id/:bb" #"(:[^\/]*)" "([^/]*)")
    (clojure.core/re-find #"^/library/([^\/]*)/([^\/]*)$" "/library/:a-id/:bid")
    (clojure.core/re-find #"^/library/([^\/]*)/([^\/]*)$" "/library/:a-id/:bid")
    (clojure.core/re-find #"^/library/([^\/]*)$" "/library/aa_123-a+:")
    (clojure.core/zipmap [:id]
      (clojure.core/rest (clojure.core/re-find #"/library/(\w+)$" "/library/aa_123-a/"))))
  )
