(ns io.jesi.backpack.miscellaneous
  (:refer-clojure :exclude [assoc-in])
  (:require
    [io.jesi.backpack.collection :refer [assoc-in]]
    [io.jesi.backpack.string :refer [uuid-str?]])
  #?(:clj
     (:import (java.util UUID))))

(defn ->uuid [s]
  (cond
    (uuid? s) s
    (uuid-str? s) #?(:clj  (UUID/fromString s)
                     :cljs (UUID. s nil))
    :else nil))

(defn ->uuid-or-not [id]
  (or (->uuid id) id))

(defn assoc-changed!
  "assoc(-in) the atom when the value has changed"
  [atom & kvs]
  (let [base @atom
        updated (apply assoc-in base kvs)]
    (when (not= updated base)
      (reset! atom updated))))
