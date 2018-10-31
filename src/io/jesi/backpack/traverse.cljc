(ns io.jesi.backpack.traverse
  #?(:cljs
     (:require
       [cljs.core :refer [->MapEntry]])
     :clj
     (:import (clojure.lang MapEntry))))

(defn- create-map-entry [k v]
  #?(:clj  (MapEntry. k v)
     :cljs (->MapEntry k v nil)))

(defn walk
  "Like clojure.walk/walk, but does not convert MapEntry to vector"
  [inner outer form]
  (condp #(%1 %2) form
    map-entry? (outer (create-map-entry (inner (first form)) (inner (second form))))
    coll? (outer (into (empty form) (map inner form)))
    seq? (outer (doall (map inner form)))
    (outer form)))

(defn prewalk
  "Like clojure.walk/prewalk, but uses io.jesi.backpack.walk/walk"
  [f form]
  (walk (partial prewalk f) identity (f form)))

(defn postwalk
  "Like clojure.walk/postwalk, but uses io.jesi.backpack.walk/walk"
  [f form]
  (walk (partial postwalk f) f form))

(defn- demo-fn [x]
  (print "Walked: ") (prn x) x)

(defn postwalk-demo [form]
  (postwalk demo-fn form))

(defn prewalk-demo [form]
  (prewalk demo-fn form))
