(ns io.jesi.backpack.close)

(defmulti close type)

(defmethod close :default [o]
  (. o close))
