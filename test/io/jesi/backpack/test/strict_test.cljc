(ns io.jesi.backpack.test.strict-test
  (:refer-clojure :exclude [=])
  (:require
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])
    [clojure.test :as t]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing thrown-with-msg? thrown? are]]
    [io.jesi.backpack.test.util :refer [is-macro=]]))

(deftest deftest-test

  (testing "deftest"

    #?(:clj (testing "is a macro"
              (is (macro? `deftest))))

    #?(:clj (testing "errors if `name` is not a symbol"
              (are [v]
                (is (thrown? AssertionError (eval `(deftest v))))
                nil
                "name"
                :name)))

    (testing "expands"

      (testing "to fail if empty"
        (is-macro= '(#?(:clj clojure.test/deftest :cljs cljs.test/deftest) testing
                      (#?(:clj clojure.test/try-expr :cljs cljs.test/try-expr) "Test is empty" nil))
                   (macroexpand-1 '(io.jesi.backpack.test.strict/deftest testing))))

      (testing "normally if not empty"
        (is-macro= '(#?(:clj clojure.test/deftest :cljs cljs.test/deftest) testing
                      (io.jesi.backpack.test.strict/is true)
                      (io.jesi.backpack.test.strict/is (io.jesi.backpack.test.strict/= 1 1)))
                   (macroexpand-1 '(io.jesi.backpack.test.strict/deftest testing
                                     (io.jesi.backpack.test.strict/is true)
                                     (io.jesi.backpack.test.strict/is (io.jesi.backpack.test.strict/= 1 1)))))))))

(deftest testing-test

  (testing "testing"

    #?(:clj (testing "is a macro"
              (is (macro? `testing))))

    (testing "errors if `string` is `nil`")

    (testing "expands"

      (testing "to fail if empty"
        (is-macro= #?(:clj  `(t/testing "testing"
                               (t/try-expr "Test is empty" nil))
                      :cljs '(cljs.test/testing "testing"
                               (cljs.test/try-expr "Test is empty" nil)))
                   (macroexpand-1 '(io.jesi.backpack.test.strict/testing "testing"))))

      (testing "normally if not empty"
        (is-macro= #?(:clj  `(t/testing "testing"
                               (is true)
                               (is (= 1 1)))
                      :cljs '(cljs.test/testing "testing"
                               (io.jesi.backpack.test.strict/is true)
                               (io.jesi.backpack.test.strict/is (cljs.core/= 1 1))))
                   (macroexpand-1 #?(:clj  `(testing "testing"
                                              (is true)
                                              (is (= 1 1)))
                                     :cljs '(io.jesi.backpack.test.strict/testing "testing"
                                              (io.jesi.backpack.test.strict/is true)
                                              (io.jesi.backpack.test.strict/is (cljs.core/= 1 1))))))))))

(deftest =-test

  (testing "="

    (testing "is the same as ="
      (is=
        (= 1 1 1)
        (= true true)
        (clojure.core/= 1 1 1)
        (clojure.core/= true true)))

    (testing "takes at least 2 args"
      (is (thrown? #?(:cljs js/Error :clj IllegalArgumentException) (apply = 1))))))

(deftest thrown?-test

  (testing "thrown?"

    (testing "takes a class and a body"
      (is (thrown? #?(:cljs js/Error :clj IllegalArgumentException) (apply thrown? 1)))

      (testing "and throws an assertion error if not called in an `is` block"))))

(deftest thrown-with-msg?-test)

(deftest is=-test

  (testing "is="

    #?(:clj (testing "is a macro"
              (is (macro? `is=))))

    (testing "expands"
      (is-macro= #?(:clj  '(clojure.test/is (clojure.core/= 1 2 3))
                    :cljs '(cljs.test/is (clojure.core/= 1 2 3)))
                 (macroexpand-1 '(io.jesi.backpack.test.strict/is= 1 2 3))))

    (testing "is the same as `(is (=`"
      (is (= (is (= 1 1))
             (is= 1 1))))))

(deftest is-test

  (testing "is"

    (testing "errors if `form` or `msg` is `nil`")))

