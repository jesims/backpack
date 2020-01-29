(ns io.jesi.backpack.number
  (:refer-clojure :exclude [mod])
  #?(:cljs (:require
             [clojure.string :as str])))

(def infinity "Java's Integer/MAX_VALUE for consistence use in Clojure(Script) projects" 2147483647)

(defn round-to
  "Rounds a given value 'd' to the specified 'precision'"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))

#?(:cljs (defn- decimal-places [v]
           ;TODO support other separators e.g. in France they use , instead of .
           (let [[_ decimal] (str/split (str v) \.)]
             (count decimal))))

(defn mod
  "Modulus of num and div supporting float and decimal values. Truncates toward negative infinity."
  [num div]
  #?(:clj  (clojure.core/mod (bigdec num) (bigdec div))
     :cljs (let [places (max (decimal-places num) (decimal-places div))
                 exp (js/Math.pow 10 places)
                 int-* #(int (* % exp))]
             (/ (clojure.core/mod (int-* num) (int-* div)) exp))))
