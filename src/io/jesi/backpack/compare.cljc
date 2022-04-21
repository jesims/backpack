(ns io.jesi.backpack.compare
  "Comparison operators based on `compare`"
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
        (->> (list* ~'x ~'y ~'more)
             (partition 2 1)
             (every? (fn [[x# y#]]
                       (~name-sym x# y#))))))))

(def ^{:arglists '([]
                   [x]
                   [x y]
                   [x y & more])
       :doc      "Returns `true` if the `compare`d values are in increasing order (<)"} <
  (io.jesi.backpack.compare/->compare <))

(def ^{:arglists '([]
                   [x]
                   [x y]
                   [x y & more])
       :doc      "Returns `true` if the `compare`d values are in non-decreasing order (<=)"} <=
  (io.jesi.backpack.compare/->compare <=))

(def ^{:arglists '([]
                   [x]
                   [x y]
                   [x y & more])
       :doc      "Returns `true` if the `compare`d values are equal (=)"} =
  (io.jesi.backpack.compare/->compare =))

(def ^{:arglists '([]
                   [x]
                   [x y]
                   [x y & more])
       :doc      "Returns `true` if the `compare`d values are in decreasing order (>)"} >
  (io.jesi.backpack.compare/->compare >))

(def ^{:arglists '([]
                   [x]
                   [x y]
                   [x y & more])
       :doc      "Returns `true` if the `compare`d values are in non-increasing order (>=)"} >=
  (io.jesi.backpack.compare/->compare >=))
