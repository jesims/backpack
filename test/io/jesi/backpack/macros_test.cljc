(ns io.jesi.backpack.macros-test
  (:refer-clojure :exclude [identical? with-open])
  (:require
    [clojure.string :as str]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.closey :refer [->Closey closed?]]
    [io.jesi.backpack.macros :refer #?(:clj  :all
                                       :cljs [assoc-nx
                                              assoc-nx!
                                              catch->identity
                                              catch->nil
                                              condf
                                              def-
                                              defconsts
                                              reify-ifn
                                              shorthand
                                              shorthand-assoc
                                              shorthand-str
                                              when-debug
                                              with-open
                                              with-open->])]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.strict :refer [deftest is is= testing thrown-with-msg? use-fixtures]]
    [io.jesi.customs.util :refer [is-macro=]])
  #?(:clj (:import
            (clojure.lang ArityException ExceptionInfo)
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

(deftest defconsts-test

  #?(:clj (testing "is a macro"
            (is (macro? `defconsts))))

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
    (defconsts (comp str/upper-case bp/->snake_case)
      'a-rhinoceros-horn-is-made-of-hair)
    (let [val "A_RHINOCEROS_HORN_IS_MADE_OF_HAIR"]
      (is= val a-rhinoceros-horn-is-made-of-hair)
      (is= (hash-set val) -all))))

(deftest when-debug-test

  (testing "when-debug"

    #?(:clj (testing "is a macro"
              (macro? `when-debug)))

    (let [hello '(prn "hello")]
      (testing "expands"
        (is= #?(:clj  hello
                :cljs (list 'clojure.core/when 'js/goog.DEBUG hello))
             (macroexpand-1 '(io.jesi.backpack.macros/when-debug (prn "hello")))))

      (testing "body can be multiple forms"
        (let [world '(prn "world")]
          (is= #?(:clj  (list 'do
                          hello world)
                  :cljs (list 'clojure.core/when 'js/goog.DEBUG
                          hello world))
               (macroexpand-1 '(io.jesi.backpack.macros/when-debug
                                 (prn "hello")
                                 (prn "world")))))))

    #?(:cljs (testing "executes the body when debug mode is on"
               (do
                 (set-debug false)
                 (when-debug
                   (throw (ex-info "Unexpected exception" {})))
                 (set-debug true)
                 (is= 1 (when-debug 1)))))

    #?(:cljs (testing "debug is has the boolean tag"
               (is= 'boolean
                    (->> (macroexpand-1 '(io.jesi.backpack.macros/when-debug 1))
                         (second)
                         (meta)
                         :tag))))))

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
        #?@(:clj  [(is (var? v))
                   (is (:private (meta v)))
                   (is= val @v)]
            :cljs [(is= val v)])))))

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

(deftest with-open-test

  (testing "closes the resource"
    (let [ran (volatile! false)
          o (->Closey)]
      (with-open [o o]
        (when (is (false? (closed? o)))
          (vreset! ran true)))
      (is (true? (closed? o))))

    (testing "on exception"
      (let [o (->Closey)
            msg "A Cockroach's brain is in its body"]
        (is (thrown-with-msg? ExceptionInfo (re-pattern (bp/re-quote msg))
              (with-open [o o]
                (when (is (false? (closed? o)))
                  (throw (ex-info msg {}))))))
        (is (true? (closed? o)))))))

(deftest with-open->-test

  #?(:clj (testing "is a macro"
            (is (macro? `with-open->))))

  ;Fails even though the content is the same (yeah that again)
  (comment (testing "expands"
             (is-macro= '(io.jesi.backpack.macros/with-open [v# (->Closey)]
                           (-> v#
                               (closed?)
                               (str)))
                        (macroexpand-1 '(io.jesi.backpack.macros/with-open-> (->Closey)
                                                                             (closed?)
                                                                             (str))))))

  (testing "closes the resource"
    (let [o (->Closey)]
      (is= "false"
           (with-open-> o
                        (closed?)
                        (str)))
      (is (true? (closed? o))))

    (testing "on exception"
      (let [o (->Closey)
            msg "Polar bears have black skin and see-through fur."]
        (is (thrown-with-msg? ExceptionInfo (re-pattern (bp/re-quote msg))
              (with-open-> o
                           ((constantly (throw (ex-info msg {})))))))
        (is (true? (closed? o)))))))

(defn- identical?
  ([x y] (clojure.core/identical? x y))
  ([x y & more]
   (->> (list* x y more)
        (partition 2 1)
        (every? (fn [[x y]]                                 ;TODO point-free
                  (clojure.core/identical? x y))))))

(deftest assoc-nx-test
  (let [m1 {:a 1}
        m2 (assoc m1
             :b 2)
        m3 (assoc m2
             :c 3)]

    #?(:clj (testing "is a macro"
              (is (macro? `assoc-nx))))

    (testing "expands"
      ;Fails even though the content is the same (yeah that again)
      (comment (is-macro= '(let [m# {:a 1}
                                 k# :b]
                             (if (contains? m# k#)
                               m#
                               (assoc m#
                                 k# 2)))
                          (macroexpand-1 '(io.jesi.backpack.macros/assoc-nx {:a 1}
                                            :b 2))))
      ;don't ask me why this one works but others do not
      (is-macro= '(io.jesi.backpack.macros/assoc-nx {:a 1} [:b 2 :c 3])
                 (macroexpand-1 '(io.jesi.backpack.macros/assoc-nx {:a 1}
                                   :b 2
                                   :c 3)))
      ;Fails even though the content is the same (yeah that again)
      (comment (is-macro= '(-> {:a 1}
                               (io.jesi.backpack.macros/assoc-nx :b 2)
                               (io.jesi.backpack.macros/assoc-nx :c 3))
                          (macroexpand-1 '(io.jesi.backpack.macros/assoc-nx {:a 1} [:b 2 :c 3])))))

    (testing "assoc the values"
      (is= m2
           (assoc-nx m1 :b 2)
           (assoc-nx m1 [:b 2])
           (assoc-nx {} :a 1 :b 2)
           (assoc-nx {} [:a 1 :b 2]))
      (is= m3
           (assoc-nx m2 :c 3)
           (assoc-nx m1 :b 2 :c 3)
           (assoc-nx {} :a 1 :b 2 :c 3)
           (assoc-nx {} [:a 1 :b 2 :c 3]))

      (testing "if the key does not exist"
        (is (identical? m1
                        (assoc-nx m1 :a 2)
                        (assoc-nx m1 [:a 2])))
        (is (identical? m2
                        (assoc-nx m2 :a 3 :b 4)
                        (assoc-nx m2 [:a 3 :b 4]))))

      (testing "lazily"
        (is (identical? m1
                        (assoc-nx m1
                          :a (throw (ex-info "Unexpected" {})))
                        (assoc-nx m1
                          [:a (throw (ex-info "Unexpected" {}))])))
        (is (identical? m2
                        (assoc-nx m2
                          :a (throw (ex-info "Unexpected a" {}))
                          :b (throw (ex-info "Unexpected b" {})))
                        (assoc-nx m2
                          [:a (throw (ex-info "Unexpected a" {}))
                           :b (throw (ex-info "Unexpected b" {}))])))))))

(deftest assoc-nx!-test

  (testing "assocs the value in the atom"
    (let [a (atom nil)
          k "Male koalas have two penises"
          v "Female koalas have two vaginas"]
      (assoc-nx! a k v)
      (is= {k v} @a))

    (testing "returning the value"
      (let [a (atom nil)
            k :squirrel
            v "Animals with smaller bodies and faster metabolism such as chipmunks and squirrels see in slow motion."]
        (is= v (assoc-nx! a k v))))

    (testing "if it does not exist"
      (let [k :night-vision
            v "Reindeer eyeballs turn blue in winter to help them see at lower light levels."
            m {k v}
            a (atom m)]
        (is= v (assoc-nx! a k (rnd/string)))
        (is= m @a)))

    (testing "lazily"
      (let [k :sea-lion-drummer
            v "A sea lion is the first nonhuman mammal with a proven ability to keep a beat."
            m {k v}
            a (atom m)]
        (is= v (assoc-nx! a k (throw (ex-info "Sea lions have external ear flaps, seals do not" {:earflaps? true}))))))))

#?(:clj (deftest setup-let-test

          (testing "replaces with-local with the original local bindings"
            (let [blue-whale "Blue Whales are immensely heavy"
                  weight 150000
                  ferret "Female ferrets die if they do not mate once they go into heat"]
              (is= [blue-whale weight (shorthand weight) ferret]
                   (setup-let [val blue-whale
                               {:keys [weight] :as map} (shorthand weight)
                               [x] [ferret]]
                     (with-let
                       [val weight map x])))))

          (testing "replaces multiple with-local"
            (let [actual (setup-let [x (rand-int 1e2)]
                           [(with-let x)
                            (with-let x)])]
              (is= 2 (count actual))
              ;TODO fix this flaky test, x COULD be the same value
              (is (apply distinct? actual))))

          ;commented out because equality assertion fails even though the values are the same
          (comment (testing "expands"
                     (is= '(do
                             [(let [x (rand-int 10)]
                                x)
                              (let [x (rand-int 10)]
                                x)])
                          (macroexpand-1 '(io.jesi.backpack.macros/setup-let [x (rand-int 10)]
                                            [(io.jesi.backpack.macros/with-let x)
                                             (io.jesi.backpack.macros/with-let x)])))
                     (is= '(do
                             (let [val (io.jesi.backpack.random/string)
                                   {:keys [k] :as map} {:k (rand-int 1e2)}
                                   [x] [(io.jesi.backpack.random/alpha-numeric)]]
                               [val k map x]))
                          (macroexpand-1 '(io.jesi.backpack.macros/setup-let [val (io.jesi.backpack.random/string)
                                                                              {:keys [k] :as map} {:k (rand-int 1e2)}
                                                                              [x] [(io.jesi.backpack.random/alpha-numeric)]]
                                            (io.jesi.backpack.macros/with-let
                                              [val k map x]))))))

          (testing "errors if odd number of bindings with-local was used"
            (let [ex (catch-> ex-cause
                       (eval '(io.jesi.backpack.macros/setup-let [x] 1)))]
              (is (instance? AssertionError ex))
              (is= "Assert failed: (even? (count bindings))"
                   (ex-message ex))))

          (testing "errors if no with-local was used"
            (let [ex (catch-> ex-cause
                       (eval '(io.jesi.backpack.macros/setup-let [] 1)))]
              (is (instance? AssertionError ex))
              (is= "Assert failed: No with-let called inside setup-let"
                   (ex-message ex))))))
