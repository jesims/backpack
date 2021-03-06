(ns io.jesi.backpack.clojure
  (:require
    [clojure.pprint :as pprint]
    [io.jesi.backpack.collection :refer [transform-keys]]
    [io.jesi.backpack.string :refer [->kebab-case ->kebab-case-key]]))

(defn defkw-type [type kw & args]
  (intern *ns*
    (symbol (->kebab-case (name kw)))
    (apply type (concat [kw] args))))

(defn java->clj
  "Transforms Java to Clojure. Converting keys to kebab-case keywords by default"
  ([j]
   (java->clj j ->kebab-case-key))
  ([j key-fn]
   (some->> j
            (transform-keys key-fn))))

(defn pprint-str [object]
  (pprint/write object
    :pretty true
    :stream nil))

(defn pprint-str-code [object]
  (pprint/write object
    :pretty true
    :stream nil
    :dispatch pprint/code-dispatch))
