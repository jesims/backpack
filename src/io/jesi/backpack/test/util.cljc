;TODO move to test utils library
(ns io.jesi.backpack.test.util
  (:require
    [clojure.pprint :as pprint]
    [clojure.string :as string]
    [clojure.walk :refer [postwalk]]
    [io.jesi.backpack.test.strict :refer [is=]]))

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

#?(:clj
   (defn- ^:dynamic *sleep* [ms-duration]
     (Thread/sleep ms-duration)))

#?(:clj
   (defn wait-for
     "Waits for a `f` to resolve or truthy, checking every `interval` (in milliseconds; default 1s) or until a
      `timeout` (in milliseconds; default 10s) has expired.

      WARNING: Involves thread sleeping. Should NOT be used in production"
     ([f] (wait-for f 1000))
     ([f interval] (wait-for f interval 10000))
     ([f interval timeout]
      (let [end-time (+ (System/currentTimeMillis) timeout)]
        (loop []
          (if (< end-time (System/currentTimeMillis))
            nil
            (if-let [result (f)]
              result
              (do
                (*sleep* interval)
                (recur)))))))))
