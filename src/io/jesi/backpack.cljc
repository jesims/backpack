(ns io.jesi.backpack
  (:refer-clojure :exclude [assoc-in clj->js conj! js->clj mod sorted? subs])
  (:require
    #?(:clj  [io.jesi.backpack.clojure]
       :cljs [io.jesi.backpack.clojurescript])
    [io.jesi.backpack.collection]
    [io.jesi.backpack.exceptions]
    [io.jesi.backpack.fn]
    [io.jesi.backpack.json]
    [io.jesi.backpack.macros :refer [import-vars]]
    [io.jesi.backpack.miscellaneous]
    [io.jesi.backpack.number]
    [io.jesi.backpack.specter]
    [io.jesi.backpack.string]
    [io.jesi.backpack.transit]))

(import-vars
  io.jesi.backpack.collection
  io.jesi.backpack.common
  io.jesi.backpack.exceptions
  io.jesi.backpack.fn
  io.jesi.backpack.json
  io.jesi.backpack.miscellaneous
  io.jesi.backpack.number
  io.jesi.backpack.specter
  io.jesi.backpack.string
  io.jesi.backpack.transit)

#?(:clj  (import-vars
           io.jesi.backpack.clojure
           [io.jesi.backpack.macros macro?])
   :cljs (import-vars
           io.jesi.backpack.clojurescript))
