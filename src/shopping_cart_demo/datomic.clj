(ns shopping-cart-demo.datomic
  (:require [datomic.api :as d]))

; get via mount
(def uri "datomic:dev://localhost:4334/demo-cart")
(def conn (d/connect uri))

(defn- save-new-cart [cart]
  "New: any embedded skus will be created as component entities"
  (let [temp-id (d/tempid :db.part/user)
        tx-data (conj [] (assoc cart :db/id temp-id))
        tx @(d/transact conn tx-data)
        {:keys [db-after tempids]} tx
        cart-id (d/resolve-tempid db-after tempids temp-id)]
    (d/pull db-after '[*] cart-id)))

(defn- save-updated-cart [cart]
  "Update: embedded skus will be handled by the DB CRUD function"
  (let [tx-data [[:component/crud cart :cart/skus]]
        tx @(d/transact conn tx-data)
        db-after (:db-after tx)]
    (d/pull db-after '[*] (:db/id cart))))

(defn save-cart! [cart]
  (if (:db/id cart)
    (save-updated-cart cart)
    (save-new-cart cart)))

(defn get-cart [name]
  (let [db (d/db conn)
        id (d/q '[:find ?cart-name .
                  :in $ ?name
                  :where [?cart-name :cart/name ?name]] db name)]
    (d/pull db '[*] id)))