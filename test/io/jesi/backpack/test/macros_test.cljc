(ns io.jesi.backpack.test.macros-test
  (:require
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])
    [clojure.test :refer [deftest is testing try-expr]]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.test.macros :as tm :refer [async-go is=]]
    [io.jesi.backpack.test.util :refer [is-macro=]]))

(deftest async-go-test

  (testing "async-go")

  #?(:clj (testing "is a macro"
            (is (macro? `async-go))))

  (testing "expands "
    #?(:clj  (is-macro= '(clojure.core.async/<!!
                           (io.jesi.backpack.async/go
                             (is true)))
                        (macroexpand-1 '(io.jesi.backpack.test.macros/async-go (is true))))
       :cljs (is-macro= '(cljs.test/async done
                           (io.jesi.backpack.async/go
                             (try
                               (is true)
                               (finally
                                 (done)))))
                        (macroexpand-1 '(io.jesi.backpack.test.macros/async-go (is true))))))

  (testing "is a `cljs.test/async` `go` block"
    (async-go
      (is= 1 (async/<? (async/go-try 1))))))

(deftest is=-test

  (testing "is="

    #?(:clj (testing "is a macro"
              (is (macro? `is=))))

    (testing "expands"
      (is-macro= #?(:clj  '(clojure.test/is (clojure.core/= 1 2 3))
                    :cljs '(cljs.test/is (clojure.core/= 1 2 3)))
                 (macroexpand-1 '(io.jesi.backpack.test.macros/is= 1 2 3))))

    (testing "is the same as (is (="
      (is (= (is (= 1 1))
             (is= 1 1))))))

(deftest testing-test

  (testing "testing"

    #?(:clj (testing "is a macro"
              (is (macro? `tm/testing))))

    (testing "expands"

      (testing "to fail if empty"
        #?(:clj  (is-macro= `(testing "testing"
                               (try-expr "Test is empty" nil))
                            (macroexpand-1 `(tm/testing "testing")))
           :cljs (is-macro= '(cljs.test/testing "testing"
                               (cljs.test/try-expr "Test is empty" nil))
                            (macroexpand-1 '(io.jesi.backpack.test.macros/testing "testing")))))

      (testing "normally if not empty"
        #?(:clj  (is-macro= `(testing "testing"
                               (is true)
                               (is (= 1 1)))
                            (macroexpand-1 `(tm/testing "testing"
                                              (is true)
                                              (is (= 1 1)))))
           :cljs (is-macro= '(cljs.test/testing "testing"
                               (cljs.test/is true)
                               (cljs.test/is (cljs.core/= 1 1)))
                            (macroexpand-1 '(io.jesi.backpack.test.macros/testing "testing"
                                              (cljs.test/is true)
                                              (cljs.test/is (cljs.core/= 1 1))))))))))
