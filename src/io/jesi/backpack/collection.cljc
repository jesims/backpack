(ns io.jesi.backpack.collection
  (:refer-clojure :exclude [assoc-in conj!])
  (:require
    [clojure.core :as clj]
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

;TODO isn't this the same at set/rename-keys ?
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
      (let [path (if (vector? path) path [path])]
        (clojure.core/assoc-in m path v)))
    m
    (partition 2 kvs)))

;based on clojure.core.incubator/dissoc-in
(defn- dissoc-in1 [m [k & ks :as path]]
  (if (and path (map? m))
    (if ks
      (if-let [nextmap (get m k)]
        (let [newmap (dissoc-in1 nextmap ks)]
          (if (or (nil? newmap)
                  (and (map? newmap)
                       (empty? newmap)))
            ;TODO use transient map
            (dissoc m k)
            (assoc m k newmap)))
        m)
      (dissoc m k))
    m))

(defn dissoc-in
  "Dissociates paths from a map.
  Any empty maps produced will be removed"
  [m path & paths]
  (if path
    (recur (dissoc-in1 m path) (first paths) (rest paths))
    m))

(defn trans-reduce-kv [f init coll]
  (->> coll
       (reduce-kv f (transient init))
       persistent!))

(defn trans-reduce
  ([f [c & coll]]
   (trans-reduce f c coll))

  ([f init coll]
   (->> coll
        (reduce f (transient init))
        persistent!)))

(defn rename-keys!
  "Returns the transient map with the keys in kmap renamed to the vals in kmap"
  [tmap kmap]
  (let [tmap (reduce
               (fn [tmap [old-key new-key]]
                 (if (contains? tmap old-key)
                   (assoc! tmap new-key (get tmap old-key))
                   tmap))
               tmap
               kmap)]
    (apply dissoc! tmap (keys kmap))))

(defn update!
  "'Updates' a value in an transient associative structure, where k is a
  key and f is a function that will take the old value and any supplied args
  and return the new value, and returns a new structure.
  If the key does not exist, nil is passed as the old value."
  ([tcoll k f]
   (assoc! tcoll k (f (get tcoll k))))
  ([tcoll k f x]
   (assoc! tcoll k (f (get tcoll k) x)))
  ([tcoll k f x y]
   (assoc! tcoll k (f (get tcoll k) x y)))
  ([tcoll k f x y z]
   (assoc! tcoll k (f (get tcoll k) x y z)))
  ([tcoll k f x y z & more]
   (assoc! tcoll k (apply f (get tcoll k) x y z more))))

;from cljs.core/conj!
(defn conj!
  "Adds val to the transient collection, and return tcoll. The 'addition'
  may happen at different 'places' depending on the concrete type."
  ([] (transient []))
  ([tcoll] tcoll)
  ([tcoll val]
   (when tcoll
     (clj/conj! tcoll val)))
  ([tcoll val & more]
   (let [ntcoll (conj! tcoll val)]
     (if more
       (recur ntcoll (first more) (next more))
       ntcoll))))

;TODO DRY up. create macro that creates the `& more` overload (could also be used in conj! and dissoc! and disj!)
(defn concat!
  "Adds the values to the transient collection, returning tcoll. Concatenates of the elements in the supplied sequences"
  ([] (transient []))
  ([tcoll] tcoll)
  ([tcoll seq]
   (apply conj! tcoll seq))
  ([tcoll seq & more]
   (let [ntcoll (concat! tcoll seq)]
     (if more
       (recur ntcoll (first more) (next more))
       ntcoll))))

; Came from camel-snake-kebab
(defn transform-keys [f coll]
  "Recursively transforms all map keys in coll with f"
  (letfn [(transform [[k v]] [(f k) v])]
    (postwalk (fn [x] (if (map? x) (into {} (map transform x)) x)) coll)))
