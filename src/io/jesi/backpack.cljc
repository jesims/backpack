(ns io.jesi.backpack
  (:require
    [io.jesi.backpack.collection]
    [io.jesi.backpack.fn]
    [io.jesi.backpack.number]
    [io.jesi.backpack.specter]
    [io.jesi.backpack.string])
  #?(:clj
           (:require [potemkin :refer [import-vars]])
     :cljs (:require-macros [io.jesi.backpack.macros :refer [import-vars]])))

(import-vars
  [io.jesi.backpack.collection
   assoc-when
   distinct-by
   filter-empty
   filter-values
   in?
   safe-empty?]

  [io.jesi.backpack.fn
   apply-when
   partial-right]

  [io.jesi.backpack.number
   infinity]

  [io.jesi.backpack.specter
   map-walker
   no-empty-values]

  [io.jesi.backpack.string
   uuid-str?])
