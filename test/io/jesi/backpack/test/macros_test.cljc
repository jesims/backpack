(ns io.jesi.backpack.test.macros-test
  (:refer-clojure :exclude [=])
  (:require
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.test.macros :refer [async-go]]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]]
    [io.jesi.backpack.test.util :refer [is-macro=]]))

(deftest async-go-test

  (testing "async-go"

    #?(:clj (testing "is a macro"
              (is (macro? `async-go))))

    (testing "expands"
      (is= #?(:clj  '(clojure.core.async/<!!
                       (clojure.core.async/go
                         (is true)))
              :cljs '(cljs.test/async done
                       (cljs.core.async/go
                         (try
                           (is true)
                           (finally (done))))))
           (macroexpand-1 '(io.jesi.backpack.test.macros/async-go (is true)))))

    (testing "is a `clojure.test/async` `go` block"
      (async-go
        (is= 1 (async/<? (async/go 1)))))))
