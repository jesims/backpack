;TODO move to test utils library
(ns io.jesi.backpack.test.util
  (:require
    [clojure.pprint :as pprint]
    [clojure.string :as string]
    [clojure.walk :refer [postwalk]]
    [io.jesi.backpack.test.macros :refer [is=]]))

(defn pprint-str [object]
  (pprint/write object
    :pretty true
    :stream nil))

(defn pprint-str-code [object]
  (pprint/write object
    :pretty true
    :stream nil
    :dispatch pprint/code-dispatch))

(defn is-macro= [expected expanded]
  (let [actual (->> expanded
                    (postwalk
                      (fn [form]
                        (if (symbol? form)
                          (let [form-str (str form)
                                replaced (string/replace-first form-str #"__\d+(__auto__)?" "")]
                            (if (not= form-str replaced)
                              (symbol (str replaced \#))
                              form))
                          form)))
                    pprint-str-code)
        expected (pprint-str-code expected)]
    (is= expected actual)))
