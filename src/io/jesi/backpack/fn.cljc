(ns io.jesi.backpack.fn)

(defn partial-right [f & args]
  (fn [& more]
    (apply f (concat more args))))
