(ns io.jesi.backpack.test.util
  (:require
    [clojure.string :as str]
    [clojure.test :refer [is]]
    [clojure.walk :refer [postwalk]]
    [io.jesi.backpack.macros :refer [shorthand]]))

(defn is-macro= [expected expanded]
  (is (= expected (->> expanded
                       (postwalk
                         (fn [form]
                           (if (symbol? form)
                             ;TODO normalise reified objects
                             (let [form-str (str form)
                                   replaced (str/replace-first form-str #"__\d+(__auto__)?" "")]
                               (if (not= form-str replaced)
                                 (symbol (str replaced \#))
                                 form))
                             form)))))))

#?(:clj
   (defn- ^:dynamic *sleep* [ms-duration]
     (Thread/sleep ms-duration))
   :cljs
   (do
     (defn- ^:dynamic *js-set-timeout* [ms-duration f]
       (js/setTimeout f ms-duration))

     (defn- ^:dynamic *js-clear-timeout* [timeout-id]
       (js/clearTimeout timeout-id))

     (defn- ^:dynamic *js-set-interval* [ms-duration f]
       (js/setInterval f ms-duration))

     (defn- ^:dynamic *js-clear-interval* [interval-id]
       (js/clearInterval interval-id))))

;TODO convert to macro does a test report
(defn wait-for
  #?(:clj  "Waits for a `f` to resolve to truthy, checking every
  `interval` (in milliseconds; default 1s) or until a
  `timeout` (in milliseconds; default 10s) has expired.
  Throws an exception if `timeout` is exceeded.
  Returns the value of `f`"
     :cljs "Waits for a `f` to resolve to truthy, checking every
  `interval` (in milliseconds; default 1s) or until a
  `timeout` (in milliseconds; default 10s) has expired.
  Throws an exception if `timeout` is exceeded.
  Returns `nil`.")
  ([f] (wait-for f 1))
  ([f interval] (wait-for f interval 10000))
  ([f interval timeout]
   (if-let [v (f)]
     #?(:cljs    nil
        :default v)
     (let [throw-ex (fn wait-timeout [] (throw (ex-info "Wait timeout" (shorthand timeout f))))]
       #?(:clj  (let [end-time (+ (System/currentTimeMillis) timeout)]
                  (loop []
                    (if (< end-time (System/currentTimeMillis))
                      (throw-ex)
                      (if-let [v (f)]
                        v
                        (do
                          (*sleep* interval)
                          (recur))))))
          :cljs (let [interval-id (atom nil)
                      timeout-id (*js-set-timeout*
                                   timeout
                                   #(do
                                      (*js-clear-interval* @interval-id)
                                      (throw-ex)))]
                  (reset! interval-id (*js-set-interval*
                                        interval
                                        #(when (f)
                                           (*js-clear-interval* @interval-id)
                                           (*js-clear-timeout* timeout-id))))
                  nil))))))
