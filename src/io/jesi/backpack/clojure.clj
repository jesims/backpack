(ns io.jesi.backpack.clojure
  (:require
    [camel-snake-kebab.core :as csk])
  (:import
    (java.net MalformedURLException URI)))

(defn ->uri [s]
  (cond
    (uri? s) s
    (and (string? s)) (try (URI. s) (catch MalformedURLException _ nil))
    :else nil))

(defn kw-type [type kw & args]
  (intern *ns*
    (symbol (csk/->kebab-case (name kw)))
    (apply type (concat [kw] args))))

