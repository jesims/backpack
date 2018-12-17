(ns io.jesi.backpack.spy
  (:refer-clojure :exclude [prn])
  (:require
    [io.jesi.backpack :as bp]
    [clojure.pprint :as pprint])
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

;;TODO create pprint
#_(defmacro pprint [& more]
    `(do ~@(for [o more]
             (prn (str (name o) \:))
             '(pprint/pprint
                `(pr-str ~o)))))
