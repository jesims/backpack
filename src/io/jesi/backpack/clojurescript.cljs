(ns io.jesi.backpack.clojurescript
  (:refer-clojure :exclude [clj->js js->clj])
  (:require
    [clojure.walk :refer [postwalk]]
    [io.jesi.backpack.string :refer [->kebab-case-key ->camelCase]]))

; Came from camel-snake-kebab
(defn transform-keys [t coll]
  "Recursively transforms all map keys in coll with t."
  (letfn [(transform [[k v]] [(t k) v])]
    (postwalk (fn [x] (if (map? x) (into {} (map transform x)) x)) coll)))

(defn js->clj
  "Transforms JavaScript to ClojureScript converting keys to kebab-case keywords"
  [x]
  (transform-keys ->kebab-case-key (clojure.core/js->clj x :keywordize-keys true)))

(defn clj->js [x]
  "Transforms ClojureScript to JavaScript converting keys to camelCase"
  (clojure.core/clj->js x :keyword-fn ->camelCase))

(defn clj->json
  [x]
  (js/JSON.stringify (clj->js x)))

(defn json->clj
  [x]
  (js->clj (js/JSON.parse x)))
