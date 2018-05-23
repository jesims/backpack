(ns io.jesi.backpack.clojure
  (:require
    [io.jesi.backpack.string :refer [->kebab-case]])
  (:import
    (java.net MalformedURLException URI)))

(defn ->uri [s]
  (cond
    (uri? s) s
    (and (string? s)) (try (URI. s) (catch MalformedURLException _ nil))
    :else nil))

(defn defkw-type [type kw & args]
  (intern *ns*
          (symbol (->kebab-case kw))
          (apply type (concat [kw] args))))

