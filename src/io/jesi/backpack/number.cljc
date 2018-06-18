(ns io.jesi.backpack.number)

(def infinity 2147483647)                                   ; From Integer/MAX_VALUE

(defn round-to [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))
