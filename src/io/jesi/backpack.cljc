(ns io.jesi.backpack
  (:refer-clojure :exclude [js->clj clj->js assoc-in])
  (:require
    [io.jesi.backpack.collection]
    [io.jesi.backpack.fn]
    [io.jesi.backpack.json]
    [io.jesi.backpack.macros :refer [import-vars]]
    [io.jesi.backpack.miscellaneous]
    [io.jesi.backpack.number]
    [io.jesi.backpack.specter]
    [io.jesi.backpack.string]
    [io.jesi.backpack.traverse]
    #?(:cljs [io.jesi.backpack.clojurescript]))
  #?(:clj
     (:require [io.jesi.backpack.clojure])))

(import-vars
  [io.jesi.backpack.collection
   assoc-in
   concat!
   contains-any?
   dissoc-all
   dissoc-in
   distinct-by
   filter-empty
   filter-nil-keys
   filter-values
   first-some
   in?
   remove-empty
   rename-keys!
   safe-empty?
   select-non-nil-keys
   trans-reduce
   trans-reduce-kv
   transform-keys
   translate-keys
   update!]

  [io.jesi.backpack.fn
   apply-when
   d#
   if-fn
   map-if
   noop
   p=
   partial-right
   pass
   pass-if]

  [io.jesi.backpack.miscellaneous
   ->uuid
   ->uuid-or-not
   assoc-changed!
   cljs-env?
   env-specific
   namespaced?]

  [io.jesi.backpack.number
   infinity
   round-to]

  [io.jesi.backpack.specter
   map-walker
   map-key-walker
   no-empty-values]

  [io.jesi.backpack.string
   ->camelCase
   ->camelCase-key
   ->kebab-case
   ->kebab-case-key
   ->snake_case
   ->snake_case-key
   prefix
   remove-prefix
   subs-inc
   subs-to
   suffix
   true-string?
   uuid-str?]

  [io.jesi.backpack.json
   clj->json
   json->clj])

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
      class->clj
      clj->js
      js->clj]))
