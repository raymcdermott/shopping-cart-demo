(ns shopping-cart-demo.core-test
  (:require [clojure.test :refer :all]
            [schema.core :as s]
            [shopping-cart-demo.core :refer :all]))

; Schema definitions

(def item
  "A schema for items in the catalogue"
  {(s/required-key :name)        s/Str
   (s/required-key :description) s/Str
   (s/required-key :sku)         s/Num
   (s/required-key :cost)        s/Num
   (s/required-key :currency)    s/Keyword})

(def sku-count
  "A schema for sku-counts in the cart"
  {(s/required-key :sku)   s/Num
   (s/required-key :count) s/Int})

(def cart
  "A schema for a shopping cart"
  (s/atom {(s/required-key :name)       s/Str
           (s/required-key :sku-counts) #{sku-count}
           (s/required-key :items)      #{item}}))

; Some test data

(def leffe-blonde {:name        "Leffe Blond"
                   :description "Exquisite Belgian Beer"
                   :sku         12345
                   :cost        1.75
                   :currency    :euros})

(s/validate item leffe-blonde)

(def leffe-bruin {:name        "Leffe Bruin"
                  :description "Exquisite Dark Belgian Beer"
                  :sku         54321
                  :cost        1.75
                  :currency    :euros})

(s/validate item leffe-blonde)

(def test-cart (atom {:name "Shopping Cart" :sku-counts #{} :items #{}}))

(s/validate cart test-cart)

(deftest adding-items
  (add-item! test-cart leffe-blonde)
  (is (= leffe-blonde (find-item test-cart leffe-blonde)))
  (is (= 1 (count-item test-cart leffe-blonde)))

  (add-item! test-cart leffe-bruin)
  (is (= leffe-bruin (find-item test-cart leffe-bruin)))
  (is (= 1 (count-item test-cart leffe-bruin)))

  (add-item! test-cart leffe-bruin)
  (is (= leffe-bruin (find-item test-cart leffe-bruin)))
  (is (= 2 (count-item test-cart leffe-bruin))))

(deftest deleting-items
  (remove-item! test-cart leffe-blonde)
  (is (= 0 (count-item test-cart leffe-blonde)))

  (remove-item! test-cart leffe-bruin)
  (is (= 0 (count-item test-cart leffe-bruin))))
