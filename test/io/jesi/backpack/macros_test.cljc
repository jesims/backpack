(ns io.jesi.backpack.macros-test
  (:refer-clojure :exclude [= when-let])
  (:require
    [clojure.string :as string]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [catch->identity catch->nil condf def- defconsts fn1 shorthand try* when-debug when-let]]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.strict :refer [= deftest is is= testing use-fixtures]]
    [io.jesi.customs.util :refer [is-macro=]])
  #?(:clj (:import
            (java.lang ArithmeticException SecurityException))))

(defn- set-debug [v]
  #?(:cljs (set! js/goog.DEBUG v)))

(use-fixtures :each
  (fn [f]
    (f)
    (set-debug false)))

(defn- throw-ex []
  (throw (ex-info "Error" {})))

(deftest catch->nil-test

  #?(:clj (testing "is a macro"
            (is (bp/macro? `catch->nil))))

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
      (is= "Almost half the pigs" x)
      (is= "in the world are kept" y)
      (is= "by farmers in China." z)))

  (testing "doesn't evaluate when false values"
    (when-let [x true
               y false
               z true]
      (is false "should not be called")))

  (testing "Won't evaluate block if any assign fails"
    (when-let [x true
               y nil
               z true]
      (is false "should not be called"))

    (when-let [x true
               y nil
               z nil]
      (is false "should not be called"))

    (when-let [x nil
               y nil
               z nil]
      (is false "should not be called"))))

(deftest defkw-test

  (testing "expands to def a namespaced keyword with the same name"
    (is-macro= '(def crocodile ::crocodile)
               (macroexpand-1 '(io.jesi.backpack.macros/defkw ::crocodile)))))

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
       (is= "Not Caught" (throw-for -1)))

     (testing "allows catching multiple exception types"
       (is= "Multi" (throw-for 1))
       (is= "Multi" (throw-for 2)))

     (testing "allows catching multiple types and single exceptions"
       (is= "ArithmeticException" (throw-for 3)))

     (testing "allows catching single exceptions"
       (is= "Exception" (throw-for 0)))))

(def ^:private shorthand-test-variable "long name is long")

(deftest shorthand-test

  #?(:clj (testing "is a macro"
            (is (:macro (meta #'shorthand)))))

  (testing "creates a map with the keywords from the symbol names"
    (let [a 1
          b 2
          c {:cheese false}]
      (is= {:a 1} (shorthand a))
      (is= {:b 2} (shorthand b))
      (is= {:a 1 :b 2} (shorthand a b))
      (is= {:c {:cheese false}} (shorthand c))
      (is= {:c {:cheese false} :a 1 :b 2} (shorthand c a b))
      (is= {:a 1 :shorthand-test-variable "long name is long"} (shorthand a shorthand-test-variable)))))

(deftest condf-test

  #?(:clj (testing "is a macro"
            (is (:macro (meta #'condf)))))

  (testing "takes functions as condp predicates"
    (let [f #(condf %
               map? "map"
               string? "string"
               nil)]
      (is= "map" (f {:a 1}))
      (is= "string" (f "hi"))
      (is (nil? (f 1))))))

(deftest defconsts-test

  #?(:clj (testing "is a macro"
            (is (true? (bp/macro? `defconsts)))))

  (testing "expands so a series of defs"
    (is-macro= '(do
                  (def hello (identity "hello"))
                  (def world (identity "world"))
                  (def -all (clojure.core/hash-set hello world)))
               (macroexpand-1 '(io.jesi.backpack.macros/defconsts identity 'hello 'world))))

  (testing "transforms the symbol values with the given function"
    (ns-unmap 'io.jesi.backpack.macros-test '-all)
    (defconsts bp/->snake_case
      'a-snail-can-sleep-for-three-years
      'slugsHaveFourNoses)
    (let [vals ["a_snail_can_sleep_for_three_years" "slugs_have_four_noses"]]
      (is= (first vals) a-snail-can-sleep-for-three-years)
      (is= (second vals) slugsHaveFourNoses)
      (is= (set vals) -all)))

  (testing "allows function composition"
    (ns-unmap 'io.jesi.backpack.macros-test '-all)
    (defconsts (comp string/upper-case bp/->snake_case)
      'a-rhinoceros-horn-is-made-of-hair)
    (let [val "A_RHINOCEROS_HORN_IS_MADE_OF_HAIR"]
      (is= val a-rhinoceros-horn-is-made-of-hair)
      (is= (hash-set val) -all))))

(deftest when-debug-test

  (testing "when-debug"

    #?(:clj (testing "is a macro"
              (bp/macro? `when-debug)))

    (testing "expands"
      #?(:clj  (is= '(prn "hello")
                    (macroexpand-1 '(io.jesi.backpack.macros/when-debug (prn "hello"))))

         :cljs (is= '(clojure.core/when js/goog.DEBUG (prn "hello"))
                    (macroexpand-1 '(io.jesi.backpack.macros/when-debug (prn "hello"))))))

    #?(:cljs (testing "executes the body when debug mode is on"
               (do
                 (set-debug false)
                 (when-debug
                   (throw (ex-info "Unexpected exception" {})))
                 (set-debug true)
                 (is= 1 (when-debug 1)))))))

(deftest catch->identity-test

  (testing "catch->identity"

    #?(:clj (testing "is a macro"
              (bp/macro? `catch->identity)))

    (testing "returns caught exception"
      (is= 1 (catch->identity 1))
      (let [ex (ex-info "Elephants are the only animal that can't jump" {:elephant {:sad? true}})]
        (is= ex (catch->identity (throw ex)))))))

(deftest def--test

  (testing "def-"

    #?(:clj (testing "is a macro"
              (bp/macro? `def-)))

    (testing "defs a private var"
      (let [val (rnd/string)
            v (def- test-var val)]
        (is= test-var val)
        #?(:clj
           (do
             (is (var? v))
             (is (:private (meta v)))
             (is= val @v))
           :cljs
           (is= val v))))))
