(ns shopping-cart-demo.core)

; unit tests - done

; prismatic schema checking

; save cart to datomic

; wire up om next

(defn add-item! [cart item]
  (swap! cart assoc :items (conj (:items @cart) item)))

(defn remove-item! [cart item]
  (let [sku (:sku item)]
    (swap! cart assoc :items (remove #(= (:sku %) sku) (:items @cart)))))

(defn find-sku [cart sku]
  (filter #(= (:sku %) sku) (:items @cart)))

(defn find-item [cart item]
  (first (find-sku cart (:sku item))))

(defn count-sku [cart sku]
  (count (find-sku cart sku)))

(defn count-item [cart item]
  (count-sku cart (:sku item)))

; needed?
(defn list-items [cart]
  (frequencies (:items @cart)))

