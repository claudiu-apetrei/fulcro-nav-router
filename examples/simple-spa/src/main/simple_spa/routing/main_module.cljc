(ns simple-spa.routing.main-module
  (:require
   [simple-spa.ui.home-page :refer [HomePage]]
   [simple-spa.ui.about-page :refer [AboutPage]]
   [simple-spa.ui.friends-page :refer [FriendsPage]]
   [simple-spa.ui.person-page :refer [PersonPage]]
   [fulcro-nav-router.core :as nav-router]))

(nav-router/init-module-routes :main {:home    HomePage
                                      :friends FriendsPage
                                      :person  PersonPage
                                      :about   AboutPage})



