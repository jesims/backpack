(ns io.jesi.backpack.walk
  (:require
    [io.jesi.backpack.fn :refer [call or-fn]]
    [io.jesi.customs.spy :as spy]))

;TODO make stack transient
(defn- walk-recur [f acc stack]
  (spy/pprint acc stack)
  (if (empty? stack)
    acc
    ;FIXME update acc with the result from f
    (let [[[path form] & more] stack
          form (spy/ppeek (f form))
          form (if ((or-fn seq? list?) form)
                 (vec form)
                 form)
          acc (if (nil? path)
                form
                (assoc-in acc path form))
          add-path (fn [[k v]]
                     (let [path (if (nil? path)
                                  [k]
                                  [path k])]
                       [path v]))
          stack (condp call form
                  map? (map add-path form)
                  (or-fn seq? list? vector?) (map-indexed (comp add-path vector) form)
                  more)]
      (recur f acc stack))))

(defn walk [f form]
  {:pre [(some? f)]}
  (if (coll? form)
    (walk-recur f nil [[nil form]])
    (f form)))
