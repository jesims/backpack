(ns io.jesi.backpack.clojurescript
  (:refer-clojure :exclude [clj->js js->clj])
  (:require
    [clojure.string :as string]
    [clojure.walk :refer [postwalk]]
    [goog.object :as gobj]
    [io.jesi.backpack.collection :refer [trans-reduce transform-keys]]
    [io.jesi.backpack.string :refer [->camelCase ->kebab-case-key]]))

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
