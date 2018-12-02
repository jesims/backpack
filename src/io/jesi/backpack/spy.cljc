(ns io.jesi.backpack.spy
  (:refer-clojure :exclude [prn])
  (:require
    [io.jesi.backpack :as bp])
  #?(:cljs (:require-macros io.jesi.backpack.macros)))

(defmacro prn [& more]
  `(println ~@(bp/trans-reduce
                (fn [col sym]
                  (doto col
                    (conj! (str (name sym) \:))
                    (conj! `(pr-str ~sym))))
                []
                more)))
