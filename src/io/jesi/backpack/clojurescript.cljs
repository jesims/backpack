(ns io.jesi.backpack.clojurescript
  (:refer-clojure :exclude [clj->js js->clj])
  (:require
    [io.jesi.backpack.string :as string]
    [camel-snake-kebab.extras :refer [transform-keys]]))

(defn js->clj
  "Transforms JavaScript to ClojureScript converting keys to kebab-case keywords"
  [x]
  (transform-keys string/->kebab-case-key (clojure.core/js->clj x :keywordize-keys true)))

(defn clj->js [x]
  "Transforms ClojureScript to JavaScript converting keys to camelCase"
  (clojure.core/clj->js x :keyword-fn string/->camelCase))

(defn clj->json
  [x]
  (js/JSON.stringify (clj->js x)))

(defn json->clj
  [x]
  (js->clj (js/JSON.parse x)))
