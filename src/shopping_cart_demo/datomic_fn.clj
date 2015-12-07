(ns shopping-cart-demo.datomic-fn
  (:require [datomic.api :as d]))

(def component-crud
  (d/function
    '{:lang   "clojure"
      :params [db entity comp-key]
      :code   (if-let [db-entity (d/pull db '[*] (:db/id entity))]
                (let [db-comps (comp-key db-entity)
                      user-comps (comp-key entity)
                      s1 (into #{} (map :db/id db-comps))
                      s2 (into #{} (map :db/id user-comps))
                      diffs (clojure.set/difference s1 s2)
                      retractions (into [] (map (fn [e] [:db.fn/retractEntity e]) diffs))

                      existing-comps (filter :db/id user-comps)
                      new-comps (remove :db/id user-comps)
                      datomicized-comps (map #(assoc % :db/id (d/tempid :db.part/user)) new-comps)
                      comp-entities (into [] (reduce into [existing-comps datomicized-comps]))
                      updated-entity (assoc entity comp-key comp-entities)]
                  (if (empty? retractions)
                    (vector updated-entity)
                    (conj retractions updated-entity))))}))

(defn install-crud-fn [conn]
  @(d/transact conn [{:db/id #db/id [:db.part/user]
                      :db/ident :component/crud
                      :db/doc "Handle CRUD for component entities"
                      :db/fn component-crud}]))
