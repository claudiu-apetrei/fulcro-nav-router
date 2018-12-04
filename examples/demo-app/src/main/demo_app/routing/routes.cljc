(ns demo-app.routing.routes
  (:require [demo-app.containers.home-page :refer [HomePage]]
            [demo-app.containers.about-page :refer [AboutPage]]
            [demo-app.containers.libraries-page :refer [LibrariesPage]]
            [demo-app.containers.library-page :refer [LibraryPage]])
  )

(def routes
  {:main    [["/" :home]
             ["/libraries" :libraries]
             ["/library/:id" :library]
             ["/about" :about]]
   :account [["/account/settings" :account-settings]]})