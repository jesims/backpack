(ns io.jesi.backpack.test.macros
  #?(:cljs (:require-macros [io.jesi.backpack.test.macros]))
  (:require
    [clojure.core.async]
    [clojure.test]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.miscellaneous :refer [cljs-env? env-specific]]))

(defmacro async-go [& body]
  (if (cljs-env? &env)
    `(cljs.test/async ~'done
       (async/go
         (try
           ~@body
           (finally
             (~'done)))))
    `(clojure.core.async/<!! (async/go
                               ~@body))))

(defmacro is-nil<? [body]
  (let [is* (env-specific &env 'clojure.test/is)]
    `(~is* (nil? (async/<? ~body)))))

(defmacro is= [x y & more]
  (let [is* (env-specific &env 'clojure.test/is)]
    `(~is* (= ~x ~y ~@more))))
