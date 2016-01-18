(ns shopping-cart-demo.datomic-fn
  (:require [datomic.api :as d]))

(def component-crud
  (d/function
    '{:lang   "clojure"
      :params [db entity comp-key]
      :code   (if-let [db-entity (d/pull db '[*] (:db/id entity))]
                (let [db-comps (comp-key db-entity)
                      user-comps (comp-key entity)
                      db-comps-set (into #{} (map :db/id db-comps))
                      user-comps-sets (into #{} (map :db/id user-comps))
                      diffs (clojure.set/difference db-comps-set user-comps-sets)
                      retractions (into [] (map (fn [e] [:db.fn/retractEntity e]) diffs))

                      existing-comps (filter :db/id user-comps)
                      additions (remove :db/id user-comps)
                      datomicized-additions (map #(assoc % :db/id (d/tempid :db.part/user)) additions)
                      comp-entities (into [] (reduce into [existing-comps datomicized-additions]))
                      updated-entity (assoc entity comp-key comp-entities)]
                  (if (empty? retractions)
                    (vector updated-entity)
                    (conj retractions updated-entity))))}))

(defn install-crud-fn [conn]
  @(d/transact conn [{:db/id #db/id [:db.part/user]
                      :db/ident :component/crud
                      :db/doc "Handle CRUD for component entities"
                      :db/fn component-crud}]))
