(ns io.jesi.backpack.clojurescript
  #?(:cljs (:refer-clojure :exclude [clj->js js->clj]))
  (:require
    #?(:cljs [goog.object :as gobj])
    [clojure.string :as str]
    [clojure.walk :refer [postwalk]]
    [io.jesi.backpack.collection :refer [trans-reduce transform-keys]]
    [io.jesi.backpack.string :refer [->camelCase ->kebab-case-key]]))

#?(:cljs
   (extend-type UUID
     IEncodeJS
     (-clj->js [x]
       (str x))))

(defn js->clj
  "Transforms JavaScript to ClojureScript. Converting keys to kebab-case keywords by default"
  ([js] (js->clj js ->kebab-case-key))
  ([js key-fn]
   #?(:cljs (some->> js
                     clojure.core/js->clj
                     (transform-keys key-fn)))))

(defn clj->js
  "Transforms ClojureScript to JavaScript. Converting keys to camelCase by default"
  ([x] (clj->js x ->camelCase))
  ([o key-fn]
   #?(:cljs (some->> o
                     (transform-keys key-fn)
                     clojure.core/clj->js))))

(defn class->clj [x]
  #?(:cljs (let [m (transient {})]
             (gobj/forEach x (fn [v k] (assoc! m (keyword k) v)))
             (persistent! m))))
