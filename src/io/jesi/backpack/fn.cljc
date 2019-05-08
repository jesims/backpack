(ns io.jesi.backpack.fn
  #?(:cljs
     (:require [cljs.core :refer [IDeref]])
     :clj
     (:import (clojure.lang IDeref))))

(defn apply-when
  "Invokes f when it's truthy"
  [f v]
  (when f (f v)))

(defn- derefable? [x]
  #?(:cljs
     ;protocol
     (satisfies? IDeref x)
     :clj
     ;interface
     (instance? IDeref x)))

(defn d#
  "Derefs a value if it is derefable"
  [a]
  (if (derefable? a) @a a))

(defn map-if [pred f col]
  (map #(if (pred %) (f %) %) col))

(defn partial-right [f & args]
  (fn [& more]
    (apply f (concat more args))))

(defn pass [f]
  (fn [x]
    (f x)
    x))

(defn pass-if [pred f]
  #(if (pred %)
     %
     (f %)))

(def noop (constantly nil))

(defn if-fn
  "Higher-order if function.
  Takes a predicate (`pred`), calling `then` or (optionally) `else` based on the predicate.
  Returns nil if no `else` defined."

  ([pred then]
   (fn if-fn [v]
     (when (pred v)
       (then v))))

  ([pred then else]
   (fn if-fn [v]
     (if (pred v)
       (then v)
       (else v)))))

(def
  ^{:arglists '([& x])}
  p=
  "Partial ="
  #(apply partial = %&))
