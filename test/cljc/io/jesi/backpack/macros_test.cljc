(ns io.jesi.backpack.macros-test
  (:refer-clojure :exclude [when-let])
  (:require
    [clojure.string :as string]
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros #?(:clj :refer :cljs :refer-macros) [try*
                                                                  catch->nil
                                                                  fn1
                                                                  when-let]]))

(defn- throw-ex []
  (throw (ex-info "Error" {})))

(deftest catch->nil-test
  #?(:clj
     (testing "is a macro"
       (is (bp/macro? `catch->nil))))

  (testing "surrounds with a try catch"
    (let [ex #?(:clj `Throwable :cljs :default)
          expected (->> `(try (throw-ex) (catch ~ex e#))
                        str
                        (re-find #".+?(?=__)"))
          actual (str (macroexpand-1 '(io.jesi.backpack.macros/catch->nil (io.jesi.backpack.macros-test/throw-ex))))]
      (is (some? (seq expected)))
      (is (string/starts-with? actual expected))))

  (testing "returns nil if the body throws an exception"
    (is (nil? (catch->nil (throw-ex))))))

(deftest fn1-test
  (testing "fn1 returns a function"
    (is (fn? (fn1))))

  (testing "fn1 returns an arity 1 function"
    (is (true? ((fn1 true) false)))))

(deftest when-let-test
  (testing "Acts as standard when-let"
    (when-let [x true]
      (is (true? x))))

  (testing "Allows binding multiple forms"
    (when-let [x "Almost half the pigs"
               y "in the world are kept"
               z "by farmers in China."]
      (is (= "Almost half the pigs" x))
      (is (= "in the world are kept" y))
      (is (= "by farmers in China." z))))

  (testing "doesn't evaluate when false values"
    (when-let [x true
               y false
               z true]
      (is (true? false))))

  (testing "Won't evaluate block if any assign fails"
    (when-let [x true
               y nil
               z true]
      (is (true? false)))

    (when-let [x true
               y nil
               z nil]
      (is (true? false)))

    (when-let [x nil
               y nil
               z nil]
      (is (true? false)))))

(deftest defkw-test
  (testing "expands to def a namespaced keyword with the same name"
    (is (= '(def crocodile ::crocodile)
           (macroexpand-1 '(io.jesi.backpack.macros/defkw ::crocodile))))))

(defn- throw-for [x]
  (try*
    (condp = x
      0 (throw (Exception. "Exception"))
      1 (throw (RuntimeException. "Runtime"))
      2 (throw (SecurityException. "Security"))
      3 (throw (ArithmeticException. "Arithmetic"))
      "Not Caught")
    (catch ArithmeticException _ "ArithmeticException")
    (catch-any [RuntimeException SecurityException] _ "Multi")
    (catch Exception _ "Exception")))

(deftest try*-test
  (testing "Doesn't catch if none thrown"
    (is (= "Not Caught" (throw-for -1))))

  (testing "allows catching multiple exception types"
    (is (= "Multi" (throw-for 1)))
    (is (= "Multi" (throw-for 2))))

  (testing "allows catching multiple types and single exceptions"
    (is (= "ArithmeticException" (throw-for 3))))

  (testing "allows catching single exceptions"
    (is (= "Exception" (throw-for 0)))))
