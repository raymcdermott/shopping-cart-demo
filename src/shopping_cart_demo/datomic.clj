(ns shopping-cart-demo.datomic
  (:require [datomic.api :as d]))

; get from ENV
(def uri "datomic:dev://localhost:4334/demo-cart")

(defn- save-new-cart [conn cart]
  "New: any embedded skus will be created as component entities"
  (let [temp-id (d/tempid :db.part/user)
        tx-data (conj [] (assoc cart :db/id temp-id))
        tx @(d/transact conn tx-data)
        {:keys [db-after tempids]} tx
        cart-id (d/resolve-tempid db-after tempids temp-id)]
    (d/pull db-after '[*] cart-id)))

(defn- save-updated-cart [conn cart]
  "Update: embedded skus will be handled by the DB CRUD function"
  (let [tx-data [[:component/crud cart :cart/sku-counts]]
        tx @(d/transact conn tx-data)
        db-after (:db-after tx)]
    (d/pull db-after '[*] (:db/id cart))))

(defn save-cart! [cart]
  (let [conn (d/connect uri)]
    (if (:db/id cart)
      (save-updated-cart conn cart)
      (save-new-cart conn cart))))

(defn get-cart [name]
  (let [db (d/db (d/connect uri))
        id (d/q '[:find ?cart-name .
                  :in $ ?name
                  :where [?cart-name :cart/name ?name]] db name)]
    (d/pull db '[*] id)))