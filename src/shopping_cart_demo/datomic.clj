(ns shopping-cart-demo.datomic
  (:require [datomic.api :as d]))

(defn- save-new-cart [conn cart]
  "Save the cart to Datomic - any embedded skus will be created as component entities.
  Return the persisted version of the cart from new copy of the database"
  (let [temp-id (d/tempid :db.part/user)
        tx-data (conj [] (assoc cart :db/id temp-id))
        tx @(d/transact conn tx-data)
        {:keys [db-after tempids]} tx
        cart-id (d/resolve-tempid db-after tempids temp-id)]
    (d/pull db-after '[*] cart-id)))

(defn- save-updated-cart [conn cart]
  "Save the cart to Datomic - any embedded skus will be handled by the DB
   CRUD function - inserted, updated or deleted"
  (let [tx-data [[:component/crud cart :cart/sku-counts]]
        tx @(d/transact conn tx-data)
        db-after (:db-after tx)]
    (d/pull db-after '[*] (:db/id cart))))

(defn save-cart! [conn cart]
  (if (:db/id cart)
    (save-updated-cart conn cart)
    (save-new-cart conn cart)))