;TODO move to test utils library
(ns io.jesi.backpack.spy
  #?(:clj  (:refer-clojure :exclude [prn])
     :cljs (:refer-clojure :exclude [prn -name]))
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.test.util :refer [pprint-str]])
  #?(:cljs (:require-macros io.jesi.backpack.spy)))

(defmacro when-debug [body]
  (if (boolean (:ns &env))
    `(when ~(vary-meta 'js/goog.DEBUG assoc :tag 'boolean)
       ~body)
    body))

(defn -name [form]
  (if (symbol? form)
    (name form)
    (str form)))

(defmacro prn [& more]
  `(when-debug
     (println ~@(bp/trans-reduce
                  (fn [col form]
                    (doto col
                      (conj! (str (-name form) \:))
                      (conj! `(pr-str ~form))))
                  []
                  more))))

(defmacro pprint [& more]
  `(when-debug
     (do ~@(for [form more]
             `(print (str ~(str (-name form) \: \newline) (pprint-str ~form)))))))
