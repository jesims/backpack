(ns io.jesi.backpack.clojurescript
  (:refer-clojure :exclude [clj->js js->clj])
  (:require
    [clojure.string :as string]
    [clojure.walk :refer [postwalk]]
    [goog.object :as gobj]
    [io.jesi.backpack.collection :refer [trans-reduce]]
    [io.jesi.backpack.string :refer [->kebab-case-key ->camelCase]]))

; Came from camel-snake-kebab
(defn transform-keys [t coll]
  "Recursively transforms all map keys in coll with t."
  (letfn [(transform [[k v]] [(t k) v])]
    (postwalk (fn [x] (if (map? x) (into {} (map transform x)) x)) coll)))

(extend-type UUID
  IEncodeJS
  (-clj->js [x]
    (str x)))

(defn js->clj
  "Transforms JavaScript to ClojureScript converting keys to kebab-case keywords"
  [x]
  (when-some [clj (some-> x (clojure.core/js->clj :keywordize-keys true))]
    (transform-keys ->kebab-case-key clj)))

(defn clj->js [x]
  "Transforms ClojureScript to JavaScript converting keys to camelCase"
  (some->> x
    (transform-keys ->camelCase)
    clojure.core/clj->js))

(defn clj->json
  [x]
  (js/JSON.stringify (clj->js x)))

(defn json->clj
  [s]
  (when-not (string/blank? s)
    (some-> s
            js/JSON.parse
            js->clj)))

(defn class->clj [x]
  (let [m (transient {})]
    (gobj/forEach x (fn [v k] (assoc! m (keyword k) v)))
    (persistent! m)))
