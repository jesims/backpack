(ns io.jesi.backpack.fn)

(defn partial-right [f & args]
  (fn [& more]
    (apply f (concat more args))))

(defn apply-when
  "Invokes f when it's truthy"
  [f v]
  (when f (f v)))

(defn pass [f]
  (fn [x]
    (f x)
    x))

(defn pass-if [pred f]
  #(if (pred %)
     %
     (f %)))

(defn map-if [pred f col]
  (map #(if (pred %) (f %) %) col))
