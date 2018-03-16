(ns io.jesi.backpack.miscellaneous
  (:require
    [io.jesi.backpack.string :refer [uuid-str?]])
  #?(:clj
     (:import java.util.UUID)))

(defn ->uuid [s]
  (cond
    (uuid? s) s
    (uuid-str? s) #?(:clj  (UUID/fromString s)
                     :cljs (UUID. s nil))
    :else nil))

(defn ->uuid-or-not [id]
  (or (->uuid id) id))
