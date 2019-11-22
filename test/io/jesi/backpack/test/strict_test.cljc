(ns io.jesi.backpack.test.strict-test
  (:require
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])
    [clojure.string :as str]
    [io.jesi.backpack.test.strict :as strict :refer [thrown-with-msg? thrown?]]
    [clojure.test :refer [are deftest is testing]]
    [io.jesi.backpack.test.util :refer [is-macro=]])
  #?(:clj (:import
            (clojure.lang Compiler$CompilerException))))

#?(:clj (defn- is-assertion-error-cause [ex]
          (when ex
            (let [cause (ex-cause ex)]
              (when (is (some? cause))
                (is (instance? AssertionError cause))
                (is (str/starts-with? (ex-message cause) "Assert failed: ")))
              cause))))

(deftest =-test

  (testing "="

    (testing "is the same as ="
      (is (= (strict/= 1 1 1)
             (strict/= true true)
             (= 1 1 1)
             (= true true))))

    (testing "takes at least 2 args"
      (is (thrown? #?(:cljs js/Error :clj IllegalArgumentException) (apply strict/= 1))))))

(deftest use-fixtures-test

  (testing "use-fixtures"
    (is (= #?(:clj  nil
              :cljs '(def cljs-test-each-fixtures [identity]))
           (macroexpand '(io.jesi.backpack.test.strict/use-fixtures :each identity))))))

(deftest is-test

  (testing "is"

    #?(:clj (testing "is a macro"
              (is (macro? `strict/is))))

    (testing "expands to a `clojure.test/is` statement"
      (is-macro= #?(:clj  '(try
                             (clojure.core/let [value# true]
                               (if value#
                                 (clojure.test/do-report {:type     :pass
                                                          :expected 'true
                                                          :actual   value#
                                                          :message  nil})
                                 (clojure.test/do-report {:type     :fail
                                                          :expected 'true
                                                          :actual   value#
                                                          :message  nil}))
                               value#)
                             (catch java.lang.Throwable t#
                               (clojure.test/do-report {:type     :error
                                                        :expected 'true
                                                        :actual   t#
                                                        :message  nil})))
                    ;TODO use env/transform once it supports converting catch clauses
                    :cljs '(try
                             (clojure.core/let [value# true]
                               (if value#
                                 (cljs.test/do-report {:type     :pass
                                                       :expected 'true
                                                       :actual   value#
                                                       :message  nil})
                                 (cljs.test/do-report {:type     :fail
                                                       :expected 'true
                                                       :actual   value#
                                                       :message  nil}))
                               value#)
                             (catch :default t#
                               (cljs.test/do-report {:type     :error
                                                     :expected 'true
                                                     :actual   t#
                                                     :message  nil}))))
                 (macroexpand '(io.jesi.backpack.test.strict/is true))))

    #?(:clj (testing "errors if"

              (testing "`form` is `nil`"
                (are [form]
                  (is-assertion-error-cause (is (thrown? Compiler$CompilerException (eval form))))
                  `(strict/is nil)
                  `(strict/is nil "")))

              (testing "`msg` is is not a non-blank string"
                (are [form]
                  (is-assertion-error-cause (is (thrown? Compiler$CompilerException (eval form))))
                  `(strict/is 1 nil)
                  `(strict/is 1 1)
                  `(strict/is 1 "")
                  `(strict/is 1 \space)
                  `(strict/is 1 " ")
                  `(strict/is 1 :message)))))))

(deftest is=-test

  (testing "is="

    #?(:clj (testing "is a macro "
              (is (macro? `strict/is=))))

    (testing "expands"
      (is-macro= #?(:clj  1
                    ;TODO use env/transform once it supports converting catch clauses
                    :cljs '(try
                             (clojure.core/let [values# (clojure.core/list 1 2 3)
                                                result# (clojure.core/apply clojure.core/= values#)]
                               (if result#
                                 (cljs.test/do-report {:type     :pass
                                                       :expected '(clojure.core/= 1 2 3)
                                                       :actual   (clojure.core/cons clojure.core/= values#)
                                                       :message  nil})
                                 (cljs.test/do-report {:type     :fail
                                                       :expected '(clojure.core/= 1 2 3)
                                                       :actual   (clojure.core/list 'not (clojure.core/cons 'clojure.core/=) values#)
                                                       :message  nil}))
                               result#)
                             (catch :default t#
                               (cljs.test/do-report {:type     :error
                                                     :expected '(clojure.core/= 1 2 3)
                                                     :actual   t#
                                                     :message  nil}))))
                 (macroexpand '(io.jesi.backpack.test.strict/is= 1 2 3))))

    (testing "is the same as `(is (=`"
      (is (= (is (= 1 1))
             (strict/is= 1 1))))))

(deftest deftest-test

  (testing "deftest"

    #?(:clj (testing "is a macro"
              (is (macro? `strict/deftest))))

    #?(:clj (testing "errors if `name` is not a symbol"
              (are [x]
                (when-let [cause (is-assertion-error-cause (is (thrown? Compiler$CompilerException (eval `(strict/deftest ~x)))))]
                  (strict/is= "Assert failed: (symbol? name)" (ex-message cause)))
                nil
                "name"
                :name)))

    (testing "expands"

      (testing "to fail if empty"
        ;FIXME get :test meta from `def`ed symbol
        (is (= #?(:cljs '(do
                           (def testing (clojure.core/fn [] (cljs.test/test-var (.-cljs$lang$var testing))))
                           (set! (.-cljs$lang$var testing) #'testing))
                  :clj  nil)
               (macroexpand '(io.jesi.backpack.test.strict/deftest testing)))))

      (testing "normally if not empty"
        (is (= #?(:clj  nil
                  :cljs '(do
                           (def testing (clojure.core/fn [] (cljs.test/test-var (.-cljs$lang$var testing))))
                           (set! (.-cljs$lang$var testing) (var testing))))
               (macroexpand '(io.jesi.backpack.test.strict/deftest testing
                               (clojure.test/is true)
                               (clojure.test/is (clojure.core/= 1 1))))))))))

(deftest testing-test

  (testing "testing"

    #?(:clj (testing "is a macro"
              (is (macro? `strict/testing))))

    #?(:clj (testing "errors if `string` is not a non-blank string"
              (are [x]
                (is-assertion-error-cause (is (thrown? Compiler$CompilerException (eval `(strict/testing ~x)))))
                nil
                1
                :stuff
                ""
                \space
                " ")))

    (testing "expands"

      (testing "to fail if empty"
        (is (= #?(:clj  nil
                  :cljs '(do
                           (cljs.test/update-current-env! [:testing-contexts] clojure.core/conj "testing")
                           (try
                             (cljs.test/try-expr "Test is empty" nil)
                             (finally
                               (cljs.test/update-current-env! [:testing-contexts] clojure.core/rest)))))
               (macroexpand '(io.jesi.backpack.test.strict/testing "testing")))))

      (testing "normally if not empty"
        (is (= #?(:clj  nil
                  :cljs '(do
                           (cljs.test/update-current-env! [:testing-contexts] clojure.core/conj "testing")
                           (try
                             (io.jesi.backpack.test.strict/is true)
                             (io.jesi.backpack.test.strict/is= 1 1)
                             (finally
                               (cljs.test/update-current-env! [:testing-contexts] clojure.core/rest)))))
               (macroexpand '(io.jesi.backpack.test.strict/testing "testing"
                               (io.jesi.backpack.test.strict/is true)
                               (io.jesi.backpack.test.strict/is= 1 1)))))))))

(deftest thrown?-test

  (testing "thrown?"

    (testing "takes a class, and a body"
      (is (thrown? #?(:cljs js/Error :clj IllegalArgumentException) (apply thrown? 1)))

      (testing "and throws an assertion error if not called in an `is` block"
        (is (thrown? #?(:cljs js/Error :clj AssertionError) (thrown? :default 1)))))))

(deftest thrown-with-msg?-test

  (testing "thrown-with-msg?"

    (testing "takes a class, regular expression, and a body"
      (is (thrown? #?(:cljs js/Error :clj IllegalArgumentException) (apply thrown? 1)))
      (is (thrown? #?(:cljs js/Error :clj IllegalArgumentException) (apply thrown? 1 1))))

    (testing "and throws an assertion error if not called in an `is` block"
      (is (thrown? #?(:cljs js/Error :clj AssertionError) (thrown-with-msg? :default #".*" 1))))))
