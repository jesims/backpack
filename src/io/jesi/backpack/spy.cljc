(ns io.jesi.backpack.spy
  (:refer-clojure :exclude [prn])
  (:require
    [io.jesi.backpack :as bp])
  #?(:cljs (:require-macros io.jesi.backpack.spy)))

;TODO remove if advanced optimization is on (check goog.DEBUG)
(defmacro prn [& more]
  ;(let [debug? #?(:cljs ^boolean js/goog.DEBUG :clj false)]
  `(println ~@(bp/trans-reduce
                (fn [col sym]
                  (doto col
                    (conj! (str (name sym) \:))
                    (conj! `(pr-str ~sym))))
                []
                more)))
