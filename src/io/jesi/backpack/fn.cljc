(ns io.jesi.backpack.fn
  (:refer-clojure :exclude [any?])
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

(defn compr
  "Composes functions left to right, the opposite of `comp`"
  ([] identity)
  ([f] f)
  ([f g]
   ;TODO optimize by not always using apply
   (fn [& args]
     (g (apply f args))))
  ([f g & more]
   (reduce compr (list* f g more))))

(defn call
  "Calls the function `f` with a value `v`"
  [f v]
  (f v))

(defn- apply-predicates
  [op pred & more]
  (let [more (seq more)]
    (when (and (nil? pred) (nil? more))
      (throw (ex-info "Invalid arity 0" {})))
    (if more
      (fn [x]
        (op #(% x) (cons pred more)))
      pred)))

(def ^{:arglists '([pred & more])
       :doc      "Higher order `and`.
       Takes any number of predicates and returns a function that takes a value
       and returns true if ALL individual predicates return true, else return false."}
  and-fn
  (partial apply-predicates every?))

(def ^{:arglists '([pred & more])
       :doc      "Higher order `or`.
       Takes any number of predicates and returns a function that takes a value
       and returns true if ANY individual predicates return true, else return false."}
  or-fn
  (partial apply-predicates (comp boolean some)))

(defn ->comparator
  "Returns a comparator where values returning from a value function are compared against"
  [val-fn]
  (fn [x y]
    (compare (val-fn x) (val-fn y))))

(def ^{:arglists '([pred coll])
       :doc      "Returns true if any item in coll returns true for pred"}
  any?
  (comp boolean some))

