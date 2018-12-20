(ns io.jesi.backpack.spy
  (:refer-clojure :exclude [prn])
  (:require
    [io.jesi.backpack :as bp]
    #?(:clj  [clojure.pprint :as pprint]
       :cljs [cljs.pprint :as pprint]))
  #?(:cljs (:require-macros io.jesi.backpack.spy)))

(defmacro when-debug [body]
  (if (boolean (:ns &env))
    `(when ~(vary-meta 'js/goog.DEBUG assoc :tag 'boolean)
       ~body)
    body))

(defmacro prn [& more]
  `(when-debug
     (println ~@(bp/trans-reduce
                  (fn [col sym]
                    (doto col
                      (conj! (str (name sym) \:))
                      (conj! `(pr-str ~sym))))
                  []
                  more))))

(def -pprint pprint/pprint)

(defmacro pprint [& more]
  `(when-debug
     (do ~@(apply concat (for [o more]
                           [`(println ~(str (name o) \:))
                            `(-pprint ~o)])))))
