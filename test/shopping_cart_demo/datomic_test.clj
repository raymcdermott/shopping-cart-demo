(ns shopping-cart-demo.datomic-test
  (:require [clojure.test :refer :all]
            [datomic.api :as d]
            [shopping-cart-demo.datomic :refer :all]))

(def cart {:cart/id   (java.util.UUID/randomUUID)
           :cart/name "Cart"})

(def sku-counts [{:cart/sku-counts [{:sku-count/sku   12345
                                     :sku-count/count 1}
                                    {:sku-count/sku   54321
                                     :sku-count/count 2}]}])

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
               }])

(def uri "datomic:dev://localhost:4334/test-cart")
(def schema (read-string (slurp "resources/cart-schema.edn")))

(defn set-up-db []
  (if (d/create-database uri)
    (let [conn (d/connect uri)]
      @(d/transact conn schema))))

(defn tear-down-db []
  (comment d/delete-database uri))

(defn fixture [f]
  (set-up-db)
  (f)
  (tear-down-db))

(use-fixtures :once fixture)

(deftest test-save-new-empty-cart
  (let [conn (d/connect uri)
        new-cart (save-cart! conn cart)
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (type (:cart/id new-cart)) java.util.UUID))
    (is (= (:cart/name cart) (:cart/name new-cart)))))


; Notes

;
;(defn cart-entity [db cart-uuid]
;  (d/q '[:find ?e .
;         :in $ ?cart-uuid
;         :where [?e :cart/id ?cart-uuid]]
;       db #uuid cart-uuid))
;
;Gives entity ID or nil (the lone '.' flattens the result, obviously)



