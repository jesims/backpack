(ns io.jesi.backpack.test.spy
  #?(:clj  (:refer-clojure :exclude [peek prn])
     :cljs (:refer-clojure :exclude [-name peek prn]))
  #?(:cljs (:require-macros [io.jesi.backpack.spy :refer [pprint prn]]))
  (:require
    [io.jesi.backpack.collection :refer [trans-reduce]]
    [io.jesi.backpack.macros :refer [when-debug when-not=]]
    [io.jesi.backpack.miscellaneous :refer [pprint-str]]))

(def ^:dynamic *enabled* false)

(defmacro enabled [& body]
  `(binding [*enabled* true]
     ~@body))

(defn- -name [form]
  (if (symbol? form)
    (name form)
    (str form)))

(defn- line-number [file form]
  (let [{:keys [line] meta-file :file} (meta form)
        f (or (when (and file
                         (not= "NO_SOURCE_PATH" file)
                         (not= \/ (nth file 0)))
                file)
              meta-file
              *ns*)]
    (if line
      (str f \: line)
      (str f))))

(defn- -prn [file form & more]
  `(when-debug
     (when *enabled*
       (println ~@(let [line (line-number file form)]
                    (trans-reduce
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
