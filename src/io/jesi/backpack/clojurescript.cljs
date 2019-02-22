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
  "Transforms JavaScript to ClojureScript. Converting keys to kebab-case keywords by default"

  ([js]
   (js->clj js ->kebab-case-key))

  ([js key-fn]
   (some->> js
            clojure.core/js->clj
            (transform-keys key-fn))))

(defn clj->js
  "Transforms ClojureScript to JavaScript. Converting keys to camelCase by default"

  ([x]
   (clj->js x ->camelCase))

  ([o key-fn]
   (some->> o
            (transform-keys key-fn)
            clojure.core/clj->js)))

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
