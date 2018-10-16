(ns io.jesi.backpack.number)

(def infinity "Java's Integer/MAX_VALUE for consistence use in Clojure(Script) projects" 2147483647)

(defn round-to
  "Rounds a given value 'd' to the specified 'precision'"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))
