(ns io.jesi.backpack
  (:refer-clojure :exclude [js->clj clj->js])
  (:require
    [io.jesi.backpack.collection]
    [io.jesi.backpack.fn]
    [io.jesi.backpack.miscellaneous]
    [io.jesi.backpack.number]
    [io.jesi.backpack.specter]
    [io.jesi.backpack.string]
    [io.jesi.backpack.traverse]
    [io.jesi.backpack.macros :refer [import-vars]]
    #?(:cljs [io.jesi.backpack.clojurescript]))
  #?(:clj
     (:require [io.jesi.backpack.clojure])))

(import-vars
  [io.jesi.backpack.collection
   contains-any?
   dissoc-all
   distinct-by
   filter-empty
   filter-nil-keys
   filter-values
   first-some
   in?
   remove-empty
   safe-empty?
   select-non-nil-keys
   translate-keys]

  [io.jesi.backpack.fn
   apply-when
   d#
   map-if
   noop
   partial-right
   pass
   pass-if]

  [io.jesi.backpack.miscellaneous
   ->uuid
   ->uuid-or-not
   assoc-changed!]

  [io.jesi.backpack.number
   infinity]

  [io.jesi.backpack.specter
   map-walker
   map-key-walker
   no-empty-values]

  [io.jesi.backpack.string
   ->kebab-case
   ->kebab-case-key
   ->camelCase
   ->camelCase-key
   subs-inc
   subs-to
   true-string?
   uuid-str?]

  [io.jesi.backpack.traverse
   walk
   prewalk
   postwalk
   prewalk-demo
   postwalk-demo])

#?(:clj
   (import-vars
     [io.jesi.backpack.clojure
      defkw-type
      ->uri]

     [io.jesi.backpack.macros
      macro?])

   :cljs
   (import-vars
     [io.jesi.backpack.clojurescript
      clj->js
      clj->json
      js->clj
      json->clj]))
