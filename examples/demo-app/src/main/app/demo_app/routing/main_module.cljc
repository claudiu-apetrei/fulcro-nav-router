(ns demo-app.routing.main-module
  (:require
   [demo-app.containers.home-page :refer [HomePage]]
   [demo-app.containers.about-page :refer [AboutPage]]
   [demo-app.containers.libraries-page :refer [LibrariesPage]]
   [demo-app.containers.library-page :refer [LibraryPage]]
   [fulcro-nav-router.core :as nav-router]))

(nav-router/init-module-routes :main {:home      HomePage
                                      :libraries LibrariesPage
                                      :library   LibraryPage
                                      :about     AboutPage})
