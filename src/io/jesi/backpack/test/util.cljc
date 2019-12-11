;TODO move to test utils library
(ns io.jesi.backpack.test.util
  (:require
    [clojure.pprint :as pprint]
    [clojure.string :as string]
    [clojure.test :refer [is]]
    [io.jesi.backpack.macros :refer [shorthand]]
    [clojure.walk :refer [postwalk]]))

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
  (is (= expected (->> expanded
                       (postwalk
                         (fn [form]
                           (if (symbol? form)
                             ;TODO normalise reified objects
                             (let [form-str (str form)
                                   replaced (string/replace-first form-str #"__\d+(__auto__)?" "")]
                               (if (not= form-str replaced)
                                 (symbol (str replaced \#))
                                 form))
                             form)))))))

#?(:clj
   (defn- ^:dynamic *sleep* [ms-duration]
     (Thread/sleep ms-duration)))

;TODO convert to macro does a test report
(defn wait-for
  "Waits for a `f` to resolve to truthy, checking every
  `interval` (in milliseconds; default 1s) or until a
  `timeout` (in milliseconds; default 10s) has expired.
  Throws an exception if `timeout` is exceeded.
  Returns `nil`."
  ([f] (wait-for f 1))
  ([f interval] (wait-for f interval 10000))
  ([f interval timeout]
   (if (f)
     nil
     (let [throw-ex (fn wait-timeout [] (throw (ex-info "Wait timeout" (shorthand timeout f))))]
       #?(:clj  (let [end-time (+ (System/currentTimeMillis) timeout)]
                  (loop []
                    (if (< end-time (System/currentTimeMillis))
                      (throw-ex)
                      (when-not (f)
                        (*sleep* interval)
                        (recur)))))
          :cljs (let [interval-id (atom nil)
                      timeout-id (js/setTimeout
                                   #(do
                                      (js/clearInterval @interval-id)
                                      (throw-ex))
                                   timeout)]
                  (reset! interval-id (js/setInterval
                                        #(when (f)
                                           (js/clearTimeout timeout-id))
                                        interval))))
       nil))))
