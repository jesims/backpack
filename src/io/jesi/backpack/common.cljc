;NOTE: This NS should not require anything
(ns io.jesi.backpack.common
  #?(:clj
     (:import
       (clojure.lang Named))))

;TODO doesn't work in Clojurescript macros
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
