;TODO move to test utils library
(ns io.jesi.backpack.spy
  #?(:clj  (:refer-clojure :exclude [prn peek])
     :cljs (:refer-clojure :exclude [prn -name peek]))
  #?(:cljs (:require-macros [io.jesi.backpack.spy :refer [prn pprint]]))
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [when-not= if-cljs when-debug]]
    [io.jesi.backpack.test.util :refer [pprint-str]]))

(def ^:dynamic *enabled* false)

(defmacro with-spy [& body]
  `(binding [*enabled* true]
     ~@body))

(defn- -name [form]
  (if (symbol? form)
    (name form)
    (str form)))

(defn- line-number [file form]
  (let [f (or (when (and file
                         (not= "NO_SOURCE_PATH" file)
                         (not= \/ (nth file 0)))
                file)
              *ns*)
        line (:line (meta form))]
    (if line
      (str f \: line)
      (str f))))

(defn- -prn [file form & more]
  `(when-debug
     (when *enabled*
       (println ~@(let [line (line-number file form)]
                    (bp/trans-reduce
                      (fn [col form]
                        (doto col
                          (conj! (str (-name form) \:))
                          (conj! `(pr-str ~form))))
                      [line]
                      more))))))

(defmacro prn [& more]
  (apply -prn *file* &form more))

(defn- -pprint [file form & more]
  `(when-debug
     (when *enabled*
       (do ~@(let [line (line-number file form)]
               (for [form more]
                 `(println (str ~(str line \space (-name form) \: \newline) (pprint-str ~form)))))))))

(defmacro pprint [& more]
  (apply -pprint *file* &form more))

(defmacro peek [val]
  `(do
     ~(-prn *file* &form val)
     ~val))

(defmacro ppeek [val]
  `(do
     ~(-pprint *file* &form val)
     ~val))
