(ns io.jesi.backpack.macros-test
  (:refer-clojure :exclude [when-let])
  (:require
    [clojure.string :as string]
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [try* catch->nil fn1 when-let shorthand condf defconsts]])
  #?(:clj
     (:import (java.lang ArithmeticException
                         SecurityException))))

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

#?(:clj
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
       (catch Exception _ "Exception"))))

#?(:clj
   (deftest try*-test
     (testing "Doesn't catch if none thrown"
       (is (= "Not Caught" (throw-for -1))))

     (testing "allows catching multiple exception types"
       (is (= "Multi" (throw-for 1)))
       (is (= "Multi" (throw-for 2))))

     (testing "allows catching multiple types and single exceptions"
       (is (= "ArithmeticException" (throw-for 3))))

     (testing "allows catching single exceptions"
       (is (= "Exception" (throw-for 0))))))

(def ^:private shorthand-test-variable "long name is long")

(deftest shorthand-test
  #?(:clj
     (testing "is a macro"
       (is (:macro (meta #'shorthand)))))

  (testing "creates a map with the keywords from the symbol names"
    (let [a 1
          b 2
          c {:cheese false}]
      (is (= {:a 1} (shorthand a)))
      (is (= {:b 2} (shorthand b)))
      (is (= {:a 1 :b 2} (shorthand a b)))
      (is (= {:c {:cheese false}} (shorthand c)))
      (is (= {:c {:cheese false} :a 1 :b 2} (shorthand c a b)))
      (is (= {:a 1 :shorthand-test-variable "long name is long"} (shorthand a shorthand-test-variable))))))

(deftest condf-test
  #?(:clj
     (testing "is a macro"
       (is (:macro (meta #'condf)))))

  (testing "takes functions as condp predicates"
    (let [f #(condf %
               map? "map"
               string? "string"
               nil)]
      (is (= "map" (f {:a 1})))
      (is (= "string" (f "hi")))
      (is (nil? (f 1))))))

(deftest defconsts-test
  #?(:clj
     (testing "is a macro"
       (is (true? (bp/macro? `defconsts)))))

  (testing "transforms the symbol values with the given function"
    (defconsts bp/->snake_case
      'a-snail-can-sleep-for-three-years
      'slugsHaveFourNoses)
    (is (= "a_snail_can_sleep_for_three_years" a-snail-can-sleep-for-three-years))
    (is (= "slugs_have_four_noses" slugsHaveFourNoses))))
