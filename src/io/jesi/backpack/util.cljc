(ns io.jesi.backpack.util
  (:require
    [com.rpl.specter :as sp]))

(def infinity-value 2147483647)                             ; From Integer/MAX_VALUE

(defn uuid-str? [s]
  (and (string? s) (re-matches #"(\w{8}(-\w{4}){3}-\w{12}?)$" s)))

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

(def map-walker (sp/recursive-path [] m (sp/if-path map? (sp/continue-then-stay sp/MAP-VALS m))))

(defn no-empty-values [m]
  (not-empty
    (sp/transform
      [map-walker sp/ALL]
      (fn [p]
        (if (safe-empty? (last p))
          sp/NONE
          p))
      m)))

(defn partial-right [f & args]
  (fn [& more]
    (apply f (concat more args))))
