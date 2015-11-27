(ns shopping-cart-demo.core)

; basic functions - done

; unit tests - done

; prismatic schema checking - done, in the test code

; save cart to datomic

; wire up om next

(defn add-item! [cart item]
  (swap! cart assoc
         :sku-counts (if (contains? (:items @cart) item)
                       (let [sku (first (filter #(= (:sku item) (:sku %)) (:sku-counts @cart)))
                             other-skus (into #{} (remove #(= (:sku item) (:sku %)) (:sku-counts @cart)))
                             updated-sku (assoc sku :count (inc (:count sku)))]
                         (conj other-skus updated-sku))
                       ;else
                       (conj (:sku-counts @cart) {:sku (:sku item) :count 1}))
         :items (conj (:items @cart) item)))

(defn remove-item! [cart item]
  (let [sku (:sku item)]
    (swap! cart assoc
           :items (into #{} (remove #(= item %) (:items @cart)))
           :sku-counts (into #{} (remove #(= (:sku %) sku) (:sku-counts @cart))))))

(defn find-sku [cart sku]
  (let [sku-list (filter #(= (:sku %) sku) (:items @cart))]
    (if (not-empty sku-list)
      (first sku-list)
      '())))

(defn find-item [cart item]
  (find-sku cart (:sku item)))

(defn count-sku [cart sku]
  (let [sku-count (filter #(= sku (:sku %)) (:sku-counts @cart))]
    (if (not-empty sku-count)
      (:count (first sku-count))
      0)))

(defn count-item [cart item]
  (count-sku cart (:sku item)))

; needed - maybe on the client?
(defn list-items [cart]
  (frequencies (:items @cart)))


(defn store-cart []

  )

(defn fetch-cart []

  )


