;TODO move to test utils library
(ns io.jesi.backpack.test.util
  (:require
    [clojure.pprint :as pprint]
    [clojure.string :as string]
    [clojure.test :refer [is]]
    [clojure.walk :refer [postwalk]]))

(defn pprint-str [object]
  (with-out-str
    (pprint/pprint object)))

(defn pprint-str-code [object]
  (with-out-str
    (pprint/with-pprint-dispatch pprint/code-dispatch
      (pprint/pprint object))))

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
    (is (= expected actual))))
