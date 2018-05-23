(ns io.jesi.backpack.clojurescript
  (:refer-clojure :exclude [clj->js js->clj])
  (:require
    [camel-snake-kebab.core :as csk]
    [camel-snake-kebab.extras :refer [transform-keys]]))

(defn js->clj
  "Transforms JavaScript to ClojureScript converting keys to kebab-case keywords"
  [x]
  (transform-keys csk/->kebab-case-keyword (clojure.core/js->clj x :keywordize-keys true) :splitter A-Z))

(defn clj->js [x]
  "Transforms ClojureScript to JavaScript converting keys to camelCase"
  (clojure.core/clj->js x :keyword-fn #(csk/->camelCaseString % :separator #"A-Z")))

(defn clj->json
  [x]
  (js/JSON.stringify (clj->js x)))

(defn json->clj
  [x]
  (js->clj (js/JSON.parse x)))
