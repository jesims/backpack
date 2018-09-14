(ns io.jesi.backpack.test.util
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as string]
    [clojure.walk :refer [postwalk]]))

(defn- pprint-str [x]
  (with-out-str (pprint x)))

(defn macro= [expected expanded]
  (= (pprint-str expected)
     (->> expanded
          (postwalk
            (fn [form]
              (if (symbol? form)
                (let [form-str (str form)
                      replaced (string/replace-first form-str #"__\d+(__auto__)?" "")]
                  (if (not= form-str replaced)
                    (symbol (str replaced \#))
                    form))
                form)))
          pprint-str)))

