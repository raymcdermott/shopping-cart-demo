(ns shopping-cart-demo.datomic
  (:require [datomic.api :as d]))

(defn- to-datomic-map [m]
  (if (:db/id m)
    m
    (assoc m :db/id (d/tempid :db.part/user))))

(defn- save-new-cart! [conn cart]
  "Save the cart to Datomic - any embedded skus will be created as component entities.
  Return the persisted version of the cart from new copy of the database"
  (let [temp-id (d/tempid :db.part/user)
        datomic-cart (conj [] (assoc cart :db/id temp-id))
        tx @(d/transact conn datomic-cart)
        {:keys [db-after tempids]} tx
        cart-id (d/resolve-tempid db-after tempids temp-id)]
    (d/pull db-after '[*] cart-id)))


; TODO - combine retraction and update cases

; TODO test this then combine with new-update
(defn works-for-retract [db entity-id query-spec comp-key new-comps]
  "Handle CRUD functions on component entities"
  (if-let [component (d/pull db query-spec entity-id)]
    (let [comp-list (comp-key component)
          s1 (into #{} (map :db/id comp-list))
          s2 (into #{} (map :db/id new-comps))
          retraction-list (clojure.set/difference s1 s2)
          ; create a list of retractions
          retraction-tx-list (map (fn [e] [:db.fn/retractEntity e]) retraction-list)]
      (vector retraction-tx-list))))

; TODO add retractions
(defn works-for-new-and-updates [db entity-id comp-key user-comps]
  (if (d/pull db '[*] entity-id)
    (let [existing-comps (filter :db/id user-comps)
          new-comps (remove :db/id user-comps)
          datomicized-comps (map #(assoc % :db/id (d/tempid :db.part/user)) new-comps)
          comps (into [] (reduce into [existing-comps datomicized-comps]))]
      (vector (assoc {} :db/id entity-id comp-key comps)))))

; Can this be made more general using a txn fn?
(defn- retract-any-missing-component-entities! [conn cart]
  (let [entity-list (:cart/sku-counts cart)
        cart-from-db (d/pull (d/db conn) '[*] (:db/id cart))
        entity-list-from-db (:cart/sku-counts cart-from-db)
        id-set (into #{} (map :db/id entity-list))
        id-set-from-db (into #{} (map :db/id entity-list-from-db))
        missing-db-ids (into [] (clojure.set/difference id-set-from-db id-set))]
    (if (empty? missing-db-ids)
      cart
      (let [txns (map (fn [db-id] @(d/transact conn [[:db.fn/retractEntity db-id]])) missing-db-ids)
            last-tx (last txns)]
        (d/pull (:db-after last-tx) '[*] (:db/id cart))))))

(defn- save-updated-cart! [conn cart]
  (let [datomic-cart (conj [] cart)
        tx @(d/transact conn datomic-cart)]
    (d/pull (:db-after tx) '[*] (:db/id cart))))

(defn- update-cart! [conn cart]
  (let [datomicized-skus (into [] (map to-datomic-map (:cart/sku-counts cart)))
        datomic-ready-cart (assoc cart :cart/sku-counts datomicized-skus)]
    (save-updated-cart! conn datomic-ready-cart)
    (retract-any-missing-component-entities! conn cart)))

(defn save-cart! [conn cart]
  (if (:db/id cart)
    (update-cart! conn cart)
    (save-new-cart! conn cart)))