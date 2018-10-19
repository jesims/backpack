(ns io.jesi.backpack.specter
  (:require
    [com.rpl.specter :as sp]
    [io.jesi.backpack.collection :refer [safe-empty? in? remove-empty]]))

(def map-walker (sp/recursive-path [] m (sp/if-path map? (sp/continue-then-stay sp/MAP-VALS m))))

(def map-key-walker
  ; https://github.com/nathanmarz/specter/issues/57
  (sp/recursive-path [keys] self
    [(sp/walker map?)
     sp/ALL
     (sp/if-path [sp/FIRST (partial in? keys)]
       sp/LAST
       [sp/LAST self])]))

(def ^{:no-doc     true
       :deprecated true} no-empty-values
  "DEPRECATED: use collections/remove-empty"
  remove-empty)
