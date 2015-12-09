(ns shopping-cart-demo.datomic-test
  (:require [clojure.test :refer :all]
            [datomic.api :as d]
            [shopping-cart-demo.datomic :refer :all]
            [shopping-cart-demo.datomic-fn :refer :all]))

(def sku-counts [{:sku-count/sku   12345
                  :sku-count/count 1}
                 {:sku-count/sku   54321
                  :sku-count/count 2}])

(def schema (read-string (slurp "resources/cart-schema.edn")))

(defn set-up-db []
  (if (d/create-database uri)
    (let [conn (d/connect uri)]
      @(d/transact conn schema)
      (install-crud-fn conn))))

(defn tear-down-db []
  (d/delete-database uri))

(defn fixture [f]
  (set-up-db)
  (f)
  (tear-down-db))

(use-fixtures :once fixture)

(deftest empty-cart
  (let [cart {:cart/id   (java.util.UUID/randomUUID)
              :cart/name "Cart"}
        new-cart (save-cart! cart)
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (type (:cart/id new-cart)) java.util.UUID))
    (is (= (:cart/name cart) (:cart/name new-cart)))))

(deftest saving-items
  (let [cart {:cart/id   (java.util.UUID/randomUUID)
              :cart/name "Cart"}
        new-cart (save-cart! (assoc cart :cart/sku-counts sku-counts))
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (type (:cart/id new-cart)) java.util.UUID))
    (is (= (:cart/name cart) (:cart/name new-cart)))
    (is (= (count (:cart/sku-counts new-cart)) (count sku-counts)))))

(deftest saving-new-items
  (let [cart {:cart/id   (java.util.UUID/randomUUID)
              :cart/name "Cart"}
        new-cart (save-cart! (assoc cart :cart/sku-counts sku-counts))
        updated-cart (save-cart! (assoc new-cart :cart/sku-counts sku-counts))
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (type (:cart/id new-cart)) java.util.UUID))
    (is (= (:cart/name cart) (:cart/name new-cart)))
    (is (= (count (:cart/sku-counts new-cart)) (count sku-counts)))
    (is (= (count (:cart/sku-counts updated-cart)) (count sku-counts)))
    (is (false? (= (filter :db/id (:cart/sku-counts new-cart))
                   (filter :db/id (:cart/sku-counts updated-cart)))))))

(deftest deleting-items
  (let [cart {:cart/id   (java.util.UUID/randomUUID)
              :cart/name "Cart"}
        new-cart (save-cart! (assoc cart :cart/sku-counts sku-counts))
        updated-cart (save-cart! (assoc new-cart :cart/sku-counts []))
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (type (:cart/id new-cart)) java.util.UUID))
    (is (= (:cart/name cart) (:cart/name new-cart) (:cart/name updated-cart)))
    (is (empty? (:cart/sku-counts updated-cart)))))

