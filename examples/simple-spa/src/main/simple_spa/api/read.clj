(ns simple-spa.api.read
  (:require
    [fulcro.server :refer [defquery-entity defquery-root]]
    [fulcro.i18n :as i18n]
    [taoensso.timbre :as timbre]))

;; Server queries can go here


(def people-db (atom {1  {:db/id 1 :person/name "Bert" :person/age 55 :person/about "aaaaa"}
                      2  {:db/id 2 :person/name "Sally" :person/age 22 :person/about "bbbbbbbbbbbbbbbb"}
                      3  {:db/id 3 :person/name "Allie" :person/age 76 :person/about "ccccccccccccccc"}
                      4  {:db/id 4 :person/name "Zoe" :person/age 32 :person/about "ddddddddddddddddddd"}}))

(defn get-people [kind keys]
  (->> @people-db
    vals
    vec))

(defquery-root :my-friends
  "Queries for friends and returns them to the client"
  (value [{:keys [query]} params]
   (Thread/sleep 500)
    (->> @people-db
         vals
         (map #(select-keys % query))
         vec)))

(defquery-entity :person/by-id
  "Returns the meaning of life."
  (value [{:keys [query]} id params]
    (let [person (get @people-db id)]
      (timbre/info "getting" query "for person/by-id" id)
      (Thread/sleep 500)
      (select-keys person query))))

(defquery-entity :meaning/by-id
  "Returns the meaning of life."
  (value [{:keys [query]} id params]
    (let [meanings {:life       42
                    :universe   42
                    :everything 42}]
      (timbre/info "Thinking about the meaning of " query "...hmmm...")
      (Thread/sleep 3000)
      (select-keys meanings query))))

 ; locale serving from PO files
(defquery-root ::i18n/translations
  (value [env {:keys [locale]}]
    (if-let [translations (i18n/load-locale "i18n" locale)]
      translations
      nil)))
