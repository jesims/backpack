(ns io.jesi.backpack.miscellaneous
  (:refer-clojure :exclude [assoc-in])
  (:require
    [io.jesi.backpack.atom :as atom]
    [io.jesi.backpack.collection :refer [assoc-in]]
    [io.jesi.backpack.fn :refer [call]]
    [io.jesi.backpack.string :refer [uuid-str?]])
  #?(:clj
     (:import (clojure.lang Named)
              (java.util UUID))))

(defmulti ->uuid
  "Coerces a value into a UUID if possible, otherwise returns nil"
  type)

(defmethod ->uuid :default [_] nil)

(defmethod ->uuid UUID [s] s)

#?(:clj  (defmethod ->uuid String [s]
           (when (uuid-str? s)
             (UUID/fromString s)))

   :cljs (defmethod ->uuid js/String [s]
           (when (uuid-str? s)
             (UUID. s nil))))

(defn ->uuid-or-not [id]
  (or (->uuid id) id))

(defn ^:deprecated assoc-changed!
  "DEPRECATED: Use atom ns

   assoc(-in) the atom when the value has changed"
  [atom & kvs]
  (apply atom/assoc-changed! atom kvs))

(defn named?
  "Returns true if `x` is named (can be passed to `name`)"
  [x]
  (or (string? x)
      #?(:cljs (implements? INamed x)
         :clj  (instance? Named x))))

(defn namespaced?
  "Returns true if the `named` has a namespace"
  [named]
  {:pre [(named? named)]}
  (some? (namespace named)))

(defn collify
  "Puts value `v` in a vector if it is not a collection. Returns `nil` if no value"
  ([] nil)
  ([v]
   (condp call v
     nil? nil
     coll? v
     [v])))
