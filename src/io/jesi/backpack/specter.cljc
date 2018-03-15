(ns io.jesi.backpack.specter
  (:require
    [io.jesi.backpack.collection :refer [safe-empty?]]
    [com.rpl.specter :as sp]))

(def map-walker (sp/recursive-path [] m (sp/if-path map? (sp/continue-then-stay sp/MAP-VALS m))))

(def map-key-walker
  ; https://github.com/nathanmarz/specter/issues/57
  (sp/recursive-path [keys] self
                     [(sp/walker map?)
                      sp/ALL
                      (sp/if-path [sp/FIRST (partial u/in? keys)]
                                  sp/LAST
                                  [sp/LAST self])]))

(defn no-empty-values [m]
  (not-empty
    (sp/transform
      [map-walker sp/ALL]
      (fn [p]
        (if (safe-empty? (last p))
          sp/NONE
          p))
      m)))
