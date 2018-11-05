(ns simple-spa.routing.routes)

(def routes
  {:main    [["/" :home]
             ["/friends" :friends]
             ["/person/:id" :person-page]
             ["/about" :about]]
   :account [["/account/settings" :account-settings]]})

