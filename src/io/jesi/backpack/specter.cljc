(ns io.jesi.backpack.specter
  (:require
    [com.rpl.specter :as sp]))

(def map-walker (sp/recursive-path [] m (sp/if-path map? (sp/continue-then-stay sp/MAP-VALS m))))

; Taken from io.jesi.backpack.collection/in?
(defn- in?
  [col el]
  (contains? (set col) el))

(def map-key-walker
  ; https://github.com/nathanmarz/specter/issues/57
  (sp/recursive-path [keys] self
    [(sp/walker map?)
     sp/ALL
     (sp/if-path [sp/FIRST (partial in? keys)]
       sp/LAST
       [sp/LAST self])]))

(def ^:private INDEXED
  "A path that visits v and collects k in [[k v], ...].
   This is useful if you want to collect a path to something, see path-walker."
  [sp/ALL (sp/collect-one sp/FIRST) sp/LAST])

(def ^:private INDEXED-SEQ
  "A selector that visits all elements of a seq, and collects their indices.
   This is useful if you want to collect a path to something, see path-walker."
  [(sp/view #(map-indexed vector %)) INDEXED])

;From https://github.com/nathanmarz/specter/issues/201
(def path-walker
  "A spectre recursive path navigator, that collects all paths to the occurrences of leaves that match the given predicate.
   Does not traverse deeper into the matched structures."
  (sp/recursive-path [stay-when] p
    (if (nil? stay-when)
      (sp/cond-path
        map? [INDEXED p]
        vector? [INDEXED-SEQ p]
        :else sp/STAY)
      (sp/cond-path
        (sp/pred stay-when) sp/STAY
        map? [INDEXED p]
        vector? [INDEXED-SEQ p]))))
