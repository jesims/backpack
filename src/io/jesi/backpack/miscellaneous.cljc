(ns io.jesi.backpack.miscellaneous
  (:refer-clojure :exclude [assoc-in])
  (:require
    [clojure.pprint :as pprint]
    [io.jesi.backpack.fn :refer [call]]
    [io.jesi.backpack.macros :refer [catch->nil]]
    [io.jesi.backpack.string :refer [uuid-str?]]
    #?(:cljs [goog.Uri :as uri]))
  #?(:clj
     (:import
       (java.net URI)
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

(defn collify
  "Puts value `v` in a vector if it is not a collection. Returns `nil` if no value"
  ([] nil)
  ([v]
   (condp call v
     nil? nil
     coll? v
     [v])))

(defn pprint-str [object]
  (pprint/write object
    :pretty true
    :stream nil))

(defn pprint-str-code [object]
  (pprint/write object
    :pretty true
    :stream nil
    :dispatch pprint/code-dispatch))

(defn ->uri [s]
  (cond
    (uri? s) s
    (string? s) (catch->nil #?(:clj  (URI. s)
                               :cljs (goog.Uri. s)))
    :else nil))
