(ns io.jesi.backpack.clojure
  (:require
    [io.jesi.backpack.collection :refer [transform-keys]]
    [io.jesi.backpack.macros :refer [catch->nil]]
    [io.jesi.backpack.string :refer [->kebab-case ->kebab-case-key]])
  (:import
    (java.net URI)))

(defn ->uri [s]
  (cond
    (uri? s) s
    (string? s) (catch->nil (URI. s))
    :else nil))

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
