(ns io.jesi.backpack.closer)                                ;"closer" since we can't call it "close"

(defmulti close type)

#?(:clj (defmethod close :default [o]
          (-> o (.close))))
