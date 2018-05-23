(ns io.jesi.backpack.walk
  #?(:clj
     (:import clojure.lang.MapEntry)))

(defn walk
  "Like clojure.walk/walk, but does not convert MapEntry to vector"
  [inner outer form]
  (condp #(%1 %2) form
    map-entry? (outer (MapEntry. (inner (first form)) (inner (second form))))
    coll? (outer (into (empty form) (map inner form)))
    seq? (outer (doall (map inner form)))
    (outer form)))

(defn prewalk
  ""
  [f form]
  (walk (partial prewalk f) identity (f form)))

(defn postwalk
  ""
  [f form]
  (walk (partial postwalk f) f form))

(defn- demo-fn [x]
  (print "Walked: ") (prn x) x)

(defn postwalk-demo [form]
  (postwalk demo-fn form))

(defn prewalk-demo [form]
  (prewalk demo-fn form))
