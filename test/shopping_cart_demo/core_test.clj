(ns shopping-cart-demo.core-test
  (:require [clojure.test :refer :all]
            [shopping-cart-demo.core :refer :all]))

;Some test data

(def leffe-blonde {:name               "Leffe Blond"
                   :description        "Exquisite Belgian Beer"
                   :sku                12345
                   :cost               1.75
                   :currency           :euros})

(def leffe-bruin {:name               "Leffe Bruin"
                  :description        "Exquisite Dark Belgian Beer"
                  :sku                54321
                  :cost               1.75
                  :currency           :euros})

(def test-cart (atom {:name "Shopping Cart" :items []}))

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
