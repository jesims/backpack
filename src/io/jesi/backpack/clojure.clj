(ns io.jesi.backpack.clojure
  (:import
    (java.net MalformedURLException URI)))

(defn ->uri [s]
  (cond
    (uri? s) s
    (and (string? s)) (try (URI. s) (catch MalformedURLException _ nil))
    :else nil))

(defn kw-type [type kw & args]
  (intern *ns* (symbol (name kw)) (apply type (concat [kw] args))))
