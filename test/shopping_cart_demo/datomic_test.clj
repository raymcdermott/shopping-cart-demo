(ns shopping-cart-demo.datomic-test
  (:require [clojure.test :refer :all]
            [datomic.api :as d]
            [shopping-cart-demo.datomic :refer :all]
            [shopping-cart-demo.datomic-fn :refer :all]))

(def skus [{:sku/number   12345
            :sku/quantity 1}
           {:sku/number   54321
            :sku/quantity 2}])

(def schema (read-string (slurp "resources/cart-schema.edn")))

(def test-db "datomic:mem://demo-cart")

(defn set-up-db []
  (if (d/create-database test-db)
    (let [conn (d/connect test-db)]
      @(d/transact conn schema)
      (install-crud-fn conn))))

(defn tear-down-db []
  (d/delete-database test-db))

(defn fixture [f]
  (set-up-db)
  (f)
  (tear-down-db))

(use-fixtures :once fixture)

(deftest empty-cart
  (let [conn (d/connect test-db)
        cart {:cart/name "Cart-1"}
        new-cart (save-cart! conn cart)
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (:cart/name cart) (:cart/name new-cart)))))

(deftest saving-items
  (let [conn (d/connect test-db)
        cart {:cart/name "Cart-2"}
        new-cart (save-cart! conn (assoc cart :cart/skus skus))
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (:cart/name cart) (:cart/name new-cart)))
    (is (= (count (:cart/skus new-cart)) (count skus)))))

(deftest saving-new-items
  (let [conn (d/connect test-db)
        cart {:cart/name "Cart-3"}
        new-cart (save-cart! conn (assoc cart :cart/skus skus))
        updated-cart (save-cart! conn (assoc new-cart :cart/skus skus))
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (:cart/name cart) (:cart/name new-cart)))
    (is (= (count (:cart/skus new-cart)) (count skus)))
    (is (= (count (:cart/skus updated-cart)) (count skus)))
    (is (false? (= (filter :db/id (:cart/skus new-cart))
                   (filter :db/id (:cart/skus updated-cart)))))))

(deftest deleting-items
  (let [conn (d/connect test-db)
        cart {:cart/name "Cart-4"}
        new-cart (save-cart! conn (assoc cart :cart/skus skus))
        updated-cart (save-cart! conn (assoc new-cart :cart/skus []))
        id (:db/id new-cart)]
    (is (< 0 id))
    (is (= (:cart/name cart) (:cart/name new-cart) (:cart/name updated-cart)))
    (is (empty? (:cart/skus updated-cart)))))

