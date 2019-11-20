(ns io.jesi.backpack.test.macros
  #?(:cljs (:require-macros [io.jesi.backpack.test.macros]))
  (:require
    [clojure.core.async]
    [clojure.test]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.miscellaneous :refer [cljs-env? env-specific]]
    [io.jesi.backpack.test.strict :as strict]))

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

(defmacro ^:deprecated is=
  "DEPRECATED: Use `io.jesi.backpack.test.strict` ns"
  [x y & more]
  `(strict/is= ~x ~y ~@more))

(defmacro ^:deprecated testing
  "DEPRECATED: Use `io.jesi.backpack.test.strict` ns

  Like `clojure.test/testing`, but will fail if `body` is empty."
  [string & body]
  `(strict/testing ~string ~@body))
