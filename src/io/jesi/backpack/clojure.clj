(ns io.jesi.backpack.clojure
  (:import
    (java.net MalformedURLException URI)))

(defn ->uri [s]
  (cond
    (uri? s) s
    (and (string? s)) (try (URI. s) (catch MalformedURLException _ nil))
    :else nil))
