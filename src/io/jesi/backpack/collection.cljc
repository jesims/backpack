(ns io.jesi.backpack.collection)

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

;TODO make predicate point-free
(def filter-empty (partial filter-values #(if (or (coll? %) (string? %))
                                            (seq %)
                                            (some? %))))
(defn assoc-when
  "assoc if the given v is not nil"
  ([m k v]
   (if (nil? v)
     m
     (assoc m k v)))
  ([m k v & kvs]
   (let [ret (assoc-when m k v)]
     (if kvs
       (recur ret (first kvs) (second kvs) (nnext kvs))
       ret))))

(defn select-non-nil-keys [m keys]
  (->>
    (select-keys m keys)
    (remove (comp nil? val))
    (into {})))

(defn contains-any? [map & keys]
  (some? (some #(contains? map %) keys)))

(defn dissoc-all [map & keys]
  (postwalk #(if (map? %) (apply dissoc % keys) %) map))
