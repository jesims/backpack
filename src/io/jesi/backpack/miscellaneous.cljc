(ns io.jesi.backpack.miscellaneous
  (:refer-clojure :exclude [assoc-in])
  (:require
    [clojure.string :as string]
    [io.jesi.backpack.collection :refer [assoc-in]]
    [io.jesi.backpack.string :refer [uuid-str?]])
  #?(:clj
     (:import (java.util UUID))))

(defmulti ->uuid
  "Coerces a value into a UUID if possible, otherwise returns nil"
  type)

(defmethod ->uuid :default [_] nil)

(defmethod ->uuid UUID [s] s)

#?(:clj  (defmethod ->uuid String [s]
           (when (uuid-str? s)
             (UUID/fromString s)))

   :cljs (defmethod ->uuid js/String [s]
           (when (uuid-str? s)
             (UUID. s nil))))

(defn ->uuid-or-not [id]
  (or (->uuid id) id))

(defn assoc-changed!
  "assoc(-in) the atom when the value has changed"
  [atom & kvs]
  (let [base @atom
        updated (apply assoc-in base kvs)]
    (when (not= updated base)
      (reset! atom updated))))

;https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ
(defn cljs-env?
  "Take the &env from a macro, and tell whether we are expanding into cljs."
  [env]
  (boolean (:ns env)))

(defn namespaced?
  "Returns true if the `named` has a namespace"
  [named]
  (some? (namespace named)))

;TODO support other runtimes
(defn- runtime [env]
  (cond
    (cljs-env? env) :cljs
    :else :default))

(defn- ->cljs [sym]
  (let [ns (namespace sym)]
    (if (and (string/starts-with? ns "clojure.")
             (not= "clojure.core" ns))
      (symbol
        (str "cljs" (subs ns (string/index-of ns \.)))
        (name sym))
      sym)))

(defn env-specific
  "Takes a macro &env and a namespaced symbol, returning the environment specific symbol"
  [env sym]
  {:pre [(symbol? sym)
         (namespaced? sym)]}
  (condp = (runtime env)
    :cljs (->cljs sym)
    :default sym))

;TODO create a macro that take a form and can transform it to the required runtime e.g. cljs ns rename, cljs catch clause
