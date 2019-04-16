;TODO move to test utils library
(ns io.jesi.backpack.spy
  #?(:clj  (:refer-clojure :exclude [prn])
     :cljs (:refer-clojure :exclude [prn -name]))
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [when-not=]]
    [io.jesi.backpack.test.util :refer [pprint-str]])
  #?(:cljs (:require-macros io.jesi.backpack.spy)))

(def ^:dynamic *enabled* false)

(defmacro with-spy [& body]
  `(binding [*enabled* true]
     ~@body))

(defmacro when-debug [body]
  (if (boolean (:ns &env))                                  ;if compiling JS
    `(when ~(vary-meta 'js/goog.DEBUG assoc :tag 'boolean)
       ~body)
    body))

(defn- -name [form]
  (if (symbol? form)
    (name form)
    (str form)))

(defn- line-number [form]
  (let [f (or (when (and *file*
                         (not= "NO_SOURCE_PATH" *file*)
                         (not= \/ (nth *file* 0)))
                *file*)
              *ns*)
        line (:line (meta form))]
    (str f \: line)))

(defmacro prn [& more]
  `(when-debug
     (when *enabled*
       (println ~@(let [line (line-number &form)]
                    (bp/trans-reduce
                      (fn [col form]
                        (doto col
                          (conj! (str (-name form) \:))
                          (conj! `(pr-str ~form))))
                      [line]
                      more))))))

(defmacro pprint [& more]
  `(when-debug
     (when *enabled*
       (do ~@(let [line (line-number &form)]
               (for [form more]
                 `(println (str ~(str line \space (-name form) \: \newline) (pprint-str ~form)))))))))
