(ns io.jesi.backpack.test.macros
  #?(:cljs (:require-macros [io.jesi.backpack.test.macros]))
  (:require
    [clojure.core.async]
    [clojure.test :as test]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.env :as env]
    [io.jesi.backpack.test.strict :as strict]))

(defmacro async-go [& body]
  (if (env/cljs? &env)
    `(cljs.test/async ~'done
       (cljs.core.async/go
         (try
           ~@body
           (finally
             (~'done)))))
    `(clojure.core.async/<!! (clojure.core.async/go
                               ~@body))))

(defmacro is-nil<? [body]
  `(env/transform
     (test/is (nil? (async/<? ~body)))))

(defmacro ^:deprecated is=
  "DEPRECATED: Use `io.jesi.backpack.test.strict` ns"
  [x y & more]
  `(strict/is= ~x ~y ~@more))

(defmacro ^:deprecated testing
  "DEPRECATED: Use `io.jesi.backpack.test.strict` ns

  Like `clojure.test/testing`, but will fail if `body` is empty."
  [string & body]
  `(strict/testing ~string ~@body))
