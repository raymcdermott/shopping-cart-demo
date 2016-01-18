(ns shopping-cart-demo.upd-fns
  (:require [datomic.api :as d]))

(defn calculate-retractions [list1 list2]
  (let [db-comps-set (into #{} (map :db/id list1))
        user-comps-sets (into #{} (map :db/id list2))
        diffs (clojure.set/difference db-comps-set user-comps-sets)]
    (map (fn [e] [:db.fn/retractEntity e]) diffs)))

(defn calculate-updates [user-comps]
  (let [existing-comps (filter :db/id user-comps)
        additions (remove :db/id user-comps)
        datomicized-additions (map #(assoc % :db/id (d/tempid :db.part/user)) additions)]
    (concat existing-comps datomicized-additions)))

(defn update [entity comp-key]
  (if-let [db-entity (d/pull db '[*] (:db/id entity))]
    (let [db-comps (comp-key db-entity)
          user-comps (comp-key entity)
          retractions (calculate-retractions db-comps user-comps)
          updated-entity (assoc entity comp-key (calculate-updates user-comps))]
      (if (empty? retractions)
        (vector updated-entity)
        (conj retractions updated-entity)))))