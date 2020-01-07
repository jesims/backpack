(ns io.jesi.backpack.test.strict-test
  (:require
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])
    [clojure.string :as str]
    [clojure.test :as test :refer [are deftest is testing]]
    [com.rpl.specter :as sp]
    [io.jesi.backpack.atom :as atom]
    [io.jesi.backpack.test.strict :as strict :refer [thrown-with-msg? thrown?]]
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

(defn- is-not-blank-assertion [ex]
  (is (str/starts-with? (ex-message ex) "Assert failed: (io.jesi.backpack.string/not-blank? ")))

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
    (is (= #?(:clj  '(clojure.test/use-fixtures :each identity)
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

    (testing "takes"

      (testing "an optional message string"
        (strict/is true "true")
        (strict/is true (str true))))

    (testing "errors if message is not a non-blank string"
      #?(:clj (-> (is (thrown? Compiler$CompilerException (eval `(strict/is nil))))
                  is-assertion-error-cause))
      (are [msg]
        (-> (is (thrown? #?(:clj AssertionError :cljs js/Error) (strict/is 1 msg)))
            is-not-blank-assertion)
        1
        ""
        \space
        " "
        :message))

    #?(:clj (testing "errors if"

              (testing "`form` is `nil`"
                (are [form]
                  (is-assertion-error-cause (is (thrown? Compiler$CompilerException (eval form))))
                  `(strict/is nil)
                  `(strict/is nil "")))))))

(deftest is=-test

  (testing "is="

    #?(:clj (testing "is a macro "
              (is (macro? `strict/is=))))

    (testing "expands"
      (is-macro= #?(:clj  '(try
                             (clojure.core/let [values# (clojure.core/list 1 2 3)
                                                result# (clojure.core/apply clojure.core/= values#)]
                               (if result#
                                 (clojure.test/do-report {:type     :pass
                                                          :expected '(clojure.core/= 1 2 3)
                                                          :actual   (clojure.core/cons clojure.core/= values#)
                                                          :message  nil})
                                 (clojure.test/do-report {:type     :fail
                                                          :expected '(clojure.core/= 1 2 3)
                                                          :actual   (clojure.core/list 'not (clojure.core/cons 'clojure.core/= values#))
                                                          :message  nil}))
                               result#)
                             (catch java.lang.Throwable t#
                               (clojure.test/do-report {:type     :error
                                                        :expected '(clojure.core/= 1 2 3)
                                                        :actual   t#
                                                        :message  nil})))
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
                                                       :actual   (clojure.core/list 'not (clojure.core/cons 'clojure.core/= values#))
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

(strict/deftest ^:preserved -deftest true)

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

    (testing "is a deftest"
      (let [test (-> #'-deftest meta :test)]
        (when (is (some? test))
          (is (fn? test)))))

    (testing "fails if empty"
      (let [reports (atom [])]
        (with-redefs [test/do-report (partial atom/conj! reports)]
          (ns-unmap 'io.jesi.backpack.test.strict-test 'empty-deftest)
          (strict/deftest empty-deftest)
          (test/test-var #'empty-deftest))
        (let [{:keys [type message] :as report} (sp/select-one! [sp/ALL (comp (partial = :fail) :type)] @reports)]
          (when (is (some? report))
            (is (= :fail type))
            (is (= "Test is empty" message))))))

    (testing "expands"
      (is (= #?(:clj  '(def testing (clojure.core/fn [] (clojure.test/test-var (var testing))))
                :cljs '(do
                         (def testing (clojure.core/fn [] (cljs.test/test-var (.-cljs$lang$var testing))))
                         (set! (.-cljs$lang$var testing) #'testing)))
             (macroexpand '(io.jesi.backpack.test.strict/deftest testing)))))

    (testing "retains metadata"
      (is (true? (-> #'-deftest meta :preserved))))))

(deftest testing-test

  (testing "testing"

    #?(:clj (testing "is a macro"
              (is (macro? `strict/testing))))

    (testing "takes a testing context string"
      (strict/testing "a" true)
      (strict/testing (str "a") true))

    (testing "errors if `string` is not a non-blank string"
      #?(:clj (-> (is (thrown? Compiler$CompilerException (eval `(strict/testing nil))))
                  is-assertion-error-cause))
      (are [x]
        (->> (is (thrown? #?(:clj AssertionError :cljs js/Error) (strict/testing x)))
             is-not-blank-assertion)
        1
        :stuff
        ""
        \space
        " "))

    (testing "fails if empty"
      (let [report (atom nil)]
        (with-redefs [test/do-report (partial reset! report)]
          (strict/testing "test empty"))
        (let [{:keys [type message] :as report} @report]
          (when (is (some? report))
            (is (= :fail type))
            (is (= "Test is empty" message))))))

    (testing "expands"

      (testing "to fail if empty"
        (is (= #?(:clj  '(let* []
                           (clojure.core/push-thread-bindings (clojure.core/hash-map (var clojure.test/*testing-contexts*) (clojure.core/conj clojure.test/*testing-contexts* "testing")))
                           (try
                             (clojure.core/assert (io.jesi.backpack.string/not-blank? "testing"))
                             (clojure.test/try-expr "Test is empty" nil)
                             (finally
                               (clojure.core/pop-thread-bindings))))
                  :cljs '(do
                           (cljs.test/update-current-env! [:testing-contexts] clojure.core/conj "testing")
                           (try
                             (clojure.core/assert (io.jesi.backpack.string/not-blank? "testing"))
                             (cljs.test/try-expr "Test is empty" nil)
                             (finally
                               (cljs.test/update-current-env! [:testing-contexts] clojure.core/rest)))))
               (macroexpand '(io.jesi.backpack.test.strict/testing "testing")))))

      (testing "normally if not empty"
        (is (= #?(:clj  '(let* []
                           (clojure.core/push-thread-bindings (clojure.core/hash-map (var clojure.test/*testing-contexts*) (clojure.core/conj clojure.test/*testing-contexts* "testing")))
                           (try
                             (clojure.core/assert (io.jesi.backpack.string/not-blank? "testing"))
                             (io.jesi.backpack.test.strict/is true)
                             (io.jesi.backpack.test.strict/is= 1 1)
                             (finally
                               (clojure.core/pop-thread-bindings))))
                  :cljs '(do
                           (cljs.test/update-current-env! [:testing-contexts] clojure.core/conj "testing")
                           (try
                             (clojure.core/assert (io.jesi.backpack.string/not-blank? "testing"))
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
