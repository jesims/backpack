(ns io.jesi.backpack.collection
  (:refer-clojure :exclude [assoc-in])
  (:require
    [io.jesi.backpack.traverse :refer [postwalk]]))

(defn distinct-by [key entities]
  (apply distinct? (map key entities)))

(defn in?
  [col el]
  (contains? (set col) el))

(defn safe-empty? [x]
  (or (nil? x)
      (if (seqable? x)
        (empty? x)
        false)))

(defn filter-values
  [pred map]
  (into {} (filter (comp pred val) map)))

(def filter-empty (partial filter-values (comp not safe-empty?)))

(defn select-non-nil-keys [m keys]
  (->>
    (select-keys m keys)
    (remove (comp nil? val))
    (into {})))

(defn contains-any? [map & keys]
  (some? (some (partial contains? map) keys)))

(defn dissoc-all [map & keys]
  (postwalk #(if (map? %) (apply dissoc % keys) %) map))

(defn first-some [m & ks]
  (first (filter some? (map #(% m) ks))))

(defn filter-nil-keys
  "Filters out all nil key values from a map"
  [map]
  (into {} (filter (comp some? val) map)))

(defn translate-keys
  "Updates map with the keys from kmap"
  [kmap map]
  (let [new-entries (flatten (mapv
                               (fn [[new-key key]]
                                 (if (contains? map key)
                                   [new-key (key map)]
                                   []))
                               kmap))]
    (if (seq new-entries)
      (apply assoc map new-entries)
      map)))

(defn remove-empty
  [x]
  (let [x (postwalk
            (fn remove-empty-postwalk [x]
              (condp #(%1 %2) x
                safe-empty? nil
                map-entry? (if (safe-empty? (second x))
                             nil
                             x)
                coll? (into (empty x) (remove safe-empty? x))
                seq? (remove safe-empty? x)
                x))
            x)]
    (if (seqable? x)
      (not-empty x)
      x)))

(defn assoc-in [m & kvs]
  (reduce
    (fn [m [path v]]
      ;TODO create assoc-in! and use transient map
      (clojure.core/assoc-in m path v))
    m
    (partition 2 kvs)))
