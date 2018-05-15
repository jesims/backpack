(ns io.jesi.backpack.clojurescript
  (:refer-clojure :exclude [clj->js js->clj])
  (:require
    [camel-snake-kebab.core :as csk]
    [camel-snake-kebab.extras :refer [transform-keys]]
    [clojure.string :as string]))

(declare clj->jskw)

(defn- key->jskw [k]
  (if (satisfies? IEncodeJS k)
    (-clj->js k)
    (if (or (string? k)
            (number? k)
            (keyword? k)
            (symbol? k))
      (clj->jskw k)
      (pr-str k))))

(defn clj->jskw
  "Note: Altered from cljs.core to encode NAMESPACED keywords
  Recursively transforms ClojureScript values to JavaScript.
  sets/vectors/lists become Arrays, Keywords and Symbol become Strings,
  Maps become Objects. Arbitrary keys are encoded to by key->js."
  [x]
  (when-not (nil? x)
    (if (satisfies? IEncodeJS x)
      (-clj->js x)
      (cond
        (keyword? x) (string/replace (str (keyword x)) #"^:" "")
        (symbol? x) (str x)
        (map? x) (let [m (js-obj)]
                   (doseq [[k v] x]
                     (aset m (key->jskw k) (clj->jskw v)))
                   m)
        (coll? x) (let [arr (array)]
                    (doseq [x (map clj->jskw x)]
                      (.push arr x))
                    arr)
        :else x))))

(defn js->clj
  "Transforms JavaScript to ClojureScript converting keys to kebab-case keywords"
  [x]
  (transform-keys csk/->kebab-case-keyword (clojure.core/js->clj x :keywordize-keys true)))

(defn clj->js [x]
  "Transforms ClojureScript to JavaScript converting keys to camelCase"
  (clojure.core/clj->js x :keyword-fn csk/->camelCaseString))

(defn clj->json-str
  [x]
  (js/JSON.stringify (clj->js x)))

(defn json-str->clj
  [x]
  (js->clj (js/JSON.parse x)))
