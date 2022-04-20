(ns io.jesi.backpack.compare
  #?(:cljs (:require-macros [io.jesi.backpack.compare]))
  (:refer-clojure :exclude [< <= = > >=]))

(defmacro ^:private ->compare [sym]
  {:pre [(symbol? sym)]}
  (let [name-str (name sym)
        f (symbol "clojure.core" name-str)
        name-sym (symbol name-str)]
    `(fn ~name-sym
       ([]
        true)
       ([~'x]
        true)
       ([~'x ~'y]
        ;TODO support custom comparator
        (boolean (~f (compare ~'x ~'y) 0)))
       ([~'x ~'y & ~'more]
        (and (~name-sym ~'x ~'y)
             (apply ~name-sym ~'more))))))

(def ^{:arglists '([x y])} < (io.jesi.backpack.compare/->compare <))

(def ^{:arglists '([x y])} <= (io.jesi.backpack.compare/->compare <=))

(def ^{:arglists '([x y])} = (io.jesi.backpack.compare/->compare =))

(def ^{:arglists '([x y])} > (io.jesi.backpack.compare/->compare >))

(def ^{:arglists '([x y])} >= (io.jesi.backpack.compare/->compare >=))
