(ns io.jesi.backpack.test.macros
  #?(:cljs (:require-macros [io.jesi.backpack.test.macros]))
  (:require
    [clojure.core.async]
    [clojure.test :refer [is]]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.macros :refer [if-cljs]]
    #?(:cljs [cljs.test])))

(defmacro async-go [& body]
  `(if-cljs
     (cljs.test/async ~'done
       (async/go
         (try
           ~@body
           (finally
             (~'done)))))
     (clojure.core.async/<!! (async/go
                               ~@body))))

(defmacro is-nil<? [body]
  `(is (nil? (async/<? ~body))))

(defmacro is= [& body]
  `(is (= ~@body)))
