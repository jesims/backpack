(ns io.jesi.backpack.atom
  (:refer-clojure :exclude [assoc! assoc-in conj! dissoc!])
  (:require
    [io.jesi.backpack.collection :refer [assoc-in dissoc-in]]
    [taoensso.encore :refer [assoc-some]]))

(defn assoc! [a k v & kvs]
  (apply swap! a assoc k v kvs))

(defn assoc-in! [a path v & path-vs]
  (apply swap! a assoc-in path v path-vs))

(defn assoc-some! [a k v & kvs]
  (apply swap! a assoc-some k v kvs))

(defn dissoc! [a k & ks]
  (apply swap! a dissoc k ks))

(defn dissoc-in! [a path & paths]
  (apply swap! a dissoc-in path paths))

(defn update! [a k f & args]
  (apply swap! a update k f args))

(defn merge! [a m & maps]
  (apply swap! a merge m maps))

(defn conj! [a x & xs]
  (apply swap! a conj x xs))

(defn assoc-changed!
  "assoc(-in) the atom when the value has changed"
  [atom & kvs]
  (let [base @atom
        updated (apply assoc-in base kvs)]
    (when (not= updated base)
      (reset! atom updated))))
