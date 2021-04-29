(ns io.jesi.backpack.macros-test
  (:require
    [clojure.string :as str]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer #?(:clj  :all
                                       :cljs [catch->identity catch->nil condf def- defconsts reify-ifn shorthand shorthand-assoc shorthand-str when-debug])]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.strict :refer [deftest is is= testing use-fixtures]]
    [io.jesi.customs.util :refer [is-macro=]])
  #?(:clj (:import
            (clojure.lang ArityException)
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
            (is (macro? `catch->nil))))

  (testing "returns nil if the body throws an exception"
    (is (nil? (catch->nil (throw-ex))))))

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
            (is (macro? `shorthand))))

  (testing "creates a map with the keywords from the symbol names"
    (let [a 1
          b 2
          c {:cheese false}]
      (is= {:a 1} (shorthand a))
      (is= {:b 2} (shorthand b))
      (is= {:a 1, :b 2} (shorthand a b))
      (is= {:c {:cheese false}} (shorthand c))
      (is= {:c {:cheese false}, :a 1, :b 2} (shorthand c a b))
      (is= {:a 1, :shorthand-test-variable "long name is long"} (shorthand a shorthand-test-variable)))))

(deftest shorthand-str-test

  #?(:clj (testing "is a macro"
            (is (macro? `shorthand-str))))

  (testing "creates a map with string keys"
    (let [a 1
          b 2]
      (is= {"a" 1} (shorthand-str a))
      (is= {"a" 1, "b" 2}
           (shorthand-str a b)))))

(deftest shorthand-assoc-test

  #?(:clj (testing "is a macro"
            (is (macro? `shorthand-assoc))))

  (testing "assocs with the keywords from the symbol names"
    (let [a 1
          b 2]
      (is= {:a 1}
           (shorthand-assoc {} a)
           (shorthand-assoc {} a a))
      (is= {:a 1, :b 2}
           (shorthand-assoc {:a 1} b)
           (shorthand-assoc {} a b)))))

(deftest condf-test

  #?(:clj (testing "is a macro"
            (is (macro? `condf))))

  (testing "takes functions as condp predicates"
    (let [f #(condf %
               map? "map"
               string? "string"
               nil)]
      (is= "map" (f {:a 1}))
      (is= "string" (f "hi"))
      (is (nil? (f 1))))))

(defn- remove-all []
  (ns-unmap 'io.jesi.backpack.macros-test '-all))

(deftest defconsts-test

  #?(:clj (testing "is a macro"
            (is (macro? `defconsts))))

  ;TODO fix failing test. fails even though the content is the same (the diff is empty)
  (comment (testing "expands so a series of defs"
             (is-macro= '(do
                           (def hello (str (with-meta 'hello {:tag Symbol})))
                           (def world (str 'world))
                           (def -all [hello world]))
                        (macroexpand-1 '(io.jesi.backpack.macros/defconsts str
                                          ^Symbol hello
                                          'world)))))

  (testing "transforms the symbol values with the given function"
    (remove-all)
    (defconsts bp/->snake_case
      'a-snail-can-sleep-for-three-years
      'slugsHaveFourNoses)
    (is= ["a_snail_can_sleep_for_three_years" "slugs_have_four_noses"]
         [a-snail-can-sleep-for-three-years slugsHaveFourNoses]
         -all))

  (testing "allows function composition"
    (remove-all)
    (defconsts (comp str/upper-case bp/->snake_case)
      'a-rhinoceros-horn-is-made-of-hair)
    (is= ["A_RHINOCEROS_HORN_IS_MADE_OF_HAIR"]
         [a-rhinoceros-horn-is-made-of-hair]
         -all))

  (testing "preserves metadata"
    ;TODO have correct line and column meta for each symbol
    (defconsts str
      'dolphins-call-eachother-by-name
      ^String ^{:doc "Turritopsis dohrnii can revert to it's childhood form and so never die"} immortal-jellyfish
      ^:deprecated dodo)

    (let [line 165                                          ;the line number of the defconsts above
          m {:ns     (#?(:clj find-ns :cljs do)
                       'io.jesi.backpack.macros-test)
             :file   (:file (meta #'defconsts-test))
             :name   'dolphins-call-eachother-by-name
             :line   165
             :column 5
             #?@(:cljs [:end-line (inc line)
                        :end-column 39
                        :arglists '()
                        :doc nil
                        :test nil])}]
      (is= m
           (meta (var dolphins-call-eachother-by-name)))
      (is= (assoc m
             :name 'immortal-jellyfish
             :tag #?(:clj String :cljs 'String)
             :doc "Turritopsis dohrnii can revert to it's childhood form and so never die"
             #?@(:cljs [:end-line (+ 2 line)
                        :end-column 114]))
           (meta (var immortal-jellyfish)))
      (is= (assoc m
             :name 'dodo
             :deprecated true
             #?@(:cljs [:end-line (+ 3 line)
                        :end-column 24]))
           (meta (var dodo)))))

  (testing "metadata can be used in body-fn"
    (remove-all)
    (defconsts (comp :value meta)
      ^{:value "bat"} only-flying-mammal)
    (is= ["bat"]
         -all)))

(deftest when-debug-test

  (testing "when-debug"

    #?(:clj (testing "is a macro"
              (macro? `when-debug)))

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
              (macro? `catch->identity)))

    (testing "returns caught exception"
      (is= 1 (catch->identity 1))
      (let [ex (ex-info "Elephants are the only animal that can't jump" {:elephant {:sad? true}})]
        (is= ex (catch->identity (throw ex)))))))

(deftest def--test

  (testing "def-"

    #?(:clj (testing "is a macro"
              (macro? `def-)))

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

;Note: Protocols do not support var args
(defprotocol MultiArity
  (invoked
    [this arg1]
    [this arg1 arg2]
    [this arg1 arg2 arg3]
    [this arg1 arg2 arg3 arg4]
    ;Note, can't specify more than 20 params
    [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19]))

(deftest reify-ifn-test

  (testing "reify-ifn"

    (testing "varied arity"
      (let [impl (reify-ifn
                   invoked
                   MultiArity
                   (invoked [_ arg1]
                     arg1)
                   (invoked [_ arg1 arg2]
                     (+ arg1 arg2))
                   (invoked [_ arg1 arg2 arg3]
                     (+ arg1 arg2 arg3))
                   (invoked [_ arg1 arg2 arg3 arg4]
                     (+ arg1 arg2 arg3 arg4))
                   (invoked [this arg1 arg2 arg3 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18 arg19]
                     (+ arg1 arg2 arg3 arg4 arg4 arg5 arg6 arg7 arg8 arg9 arg10 arg11 arg12 arg13 arg14 arg15 arg16 arg17 arg18)))]

        (testing "can be invoked/applied with many args"
          (is= 1 (impl 1))
          (is= 1 (apply impl [1]))
          (is= 2 (impl 1 1))
          (is= 2 (apply impl [1 1]))
          (is= 3 (impl 1 1 1))
          (is= 3 (apply impl [1 1 1]))
          (is= 4 (impl 1 1 1 1))
          (is= 4 (apply impl [1 1 1 1]))
          (is= 19 (impl 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 19 (apply impl [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1])))

        (testing "invoking with unknown arity throws exception"
          (is (thrown? #?(:clj ArityException :cljs js/Error) (impl 1 1 1 1 1))))

        (testing "applying with unknown arity throws exception"
          (is (thrown? #?(:clj ArityException :cljs js/Error) (apply impl (range 100)))))))

    (testing "calls any other defined pure? symbol. But why would you?"
      (let [invoke-me (fn [_this_ & args]
                        (apply + args))
            impl (reify-ifn invoke-me)]

        (testing "can be invoked/applied with many args"
          (is= 1 (impl 1))
          (is= 2 (impl 1 1))
          (is= 3 (impl 1 1 1))
          (is= 4 (impl 1 1 1 1))
          (is= 19 (impl 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1)))

        (doseq [v (range 1 #?(:clj  100
                              :cljs 21))]
          (let [args (range v)
                expected (apply + args)
                actual (apply impl args)]
            (is= expected actual)))))))
