(ns shopping-cart-demo.datomic
  (:require [datomic.api :as d]))

(def uri "datomic:dev://localhost:4334/demo-cart")

(d/create-database uri)

(def conn (d/connect uri))
(def db (d/db conn))

(def cart [{:db/id     #db/id [:db.part/user -1]
            :cart/id   (java.util.UUID/randomUUID)
            :cart/name "My Shopping Cart"
            :cart/sku-counts
                       [
                        {:sku-count/sku   12345
                         :sku-count/count 1}
                        {:sku-count/sku   54321
                         :sku-count/count 2}
                        ]
            }
           ])

(def new-cart [{
                :db/id 17592186045421
                :cart/sku-counts
                       [
                        {:sku-count/sku   12345
                         :sku-count/count 1}
                        {:sku-count/sku   54321
                         :sku-count/count 2}
                        ]
                }
               ])

(def catalog [;; SKUs
              {:db/id           #db/id [:db.part/user -3]
               :sku/number      12345
               :sku/name        "Leffe Blond"
               :sku/description "Exquisite Belgian Beer"
               :sku/cost        1.75
               :sku/currency    :sku.currency/euro
               }

              {:db/id           #db/id [:db.part/user -4]
               :sku/number      54321
               :sku/name        "Leffe Bruin"
               :sku/description "Exquisite Dark Belgian Beer"
               :sku/cost        1.75
               :sku/currency    :sku.currency/euro
               }
              ])

; to make these pure functions, take in data and return the
; data from the db after the changes have been transacted
; as background: see the Datomic Best Practices on using db-after
; just to prove that I'm not making this shit up

(defn create-schema [conn schema]
  @(d/transact conn schema))
;
;(defn cart-entity [db cart-uuid]
;  (d/q '[:find ?e .
;         :in $ ?cart-uuid
;         :where [?e :cart/id ?cart-uuid]]
;       db #uuid cart-uuid))
;
;; Entity ID or nil (the . flattens out the result)
;
(defn save-new-cart [conn cart]
  "Save the cart to Datomic - any embedded skus will be created as component entities.
  Return the persisted version of the cart from new copy of the database"
  (let [temp-id (d/tempid :db.part/user)
        datomic-cart (conj [] (assoc cart :db/id temp-id))
        tx @(d/transact conn datomic-cart)
        {:keys [db-after tempids]} tx
        cart-id (d/resolve-tempid db-after tempids temp-id)]
    (d/pull db-after '[*] cart-id)))

; to remove individual sku-count entries use this,
; then refresh the version of the DB using :db-after
;@(d/transact conn [[:db.fn/retractEntity 17592186045422 ]])
(defn save-cart-sku [conn sku]
  )

; this gets the difference so we can drop the resulting entity IDs
(defn diff-sku-lists [current-cart outdated-cart]
  (let [correct-sku-map (into #{} (map :db/id (:cart/sku-counts current-cart)))
        outdated-sku-map (into #{} (map :db/id (:cart/sku-counts outdated-cart)))]
    (clojure.set/difference outdated-sku-map correct-sku-map)))


(defn save-cart-xx [conn cart]
  ; save the cart map to Datomic without the skus
  ; Datomic will work out if there is any novelty
  (let [datomic-cart (conj [] (dissoc cart :cart/sku-counts))
        tx @(d/transact conn datomic-cart)]
    (d/pull (:db-after tx) '[*] (:db/id cart))))

(defn save-cart-properties [conn cart]
  ; save the cart map to Datomic without the skus
  ; Datomic will work out if there is any novelty
  (let [datomic-cart (conj [] (dissoc cart :cart/sku-counts))
        tx @(d/transact conn datomic-cart)]
    (d/pull (:db-after tx) '[*] (:db/id cart))))

(defn update-cart [conn cart]

  ; TODO: for updates
  ; if the given cart has skus (assoc :db/id (d/tempid :db.part/user)) over the
  ; skus without a db/id then send them to save-cart-xx and let Datomic do the rest

  ; finally, whether the given cart has skus or not, we need to produce a diff of the skus
  ; between the new version of the cart and the given version to detect if we need
  ; to retract any entities

  ; get back the new copy
  )

(defn save-cart [conn cart]
  (if (:db/id cart)
    (update-cart conn cart)
    (save-new-cart conn cart)))


;; Connect to the database when this namespace is loaded
;(def db-map (let [jdbc-url (env :jdbc-database-url)
;                  ssl-params "&ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory"
;                  db-uri (str "datomic:sql://datomic?" jdbc-url ssl-params)
;
;                  ; created! (d/create-database db-uri)
;
;                  conn (d/connect db-uri)
;                  db (d/db conn)
;
;                  ; schema! (create-schema conn)
;                  ; insert! (insert-data conn customer)
;                  ]
;              {:db db :conn conn}))
;
;; work on the query
;(defn get-cart []
;  ((d/pull db [*] cart)))
;
