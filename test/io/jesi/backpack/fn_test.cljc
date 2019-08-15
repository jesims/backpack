(ns io.jesi.backpack.fn-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as random]
    [io.jesi.backpack.test.macros :refer [is=]])
  #?(:cljs
     (:require [cljs.core :refer [IDeref]])
     :clj
     (:import (clojure.lang IDeref))))

(deftest partial-right-test

  (testing "partial-right"

    (testing "is a function"
      (is (fn? bp/partial-right)))

    (testing "returns a function"
      (is (fn? (bp/partial-right nil))))

    (let [identity (fn [& args] args)]
      (testing "returns the provided function if not args"
        (is= [1 2 3] ((bp/partial-right identity) 1 2 3)))

      (testing "partially applies parameters from the right"
        (is= [1 2 3] ((bp/partial-right identity 3) 1 2))
        (is= [1 2 3] ((bp/partial-right identity 2 3) 1))
        (is= [1 2 3] ((bp/partial-right identity 1 2 3)))))))

(deftest apply-when-test
  (let [quote "We were running dark, yes?"]
    (testing "apply-when: "
      (testing "returns nil when f is nil"
        (is (nil? (bp/apply-when nil quote))))

      (testing "invokes f when it's truthy"
        (is= quote (bp/apply-when identity quote))))))

(deftest pass-test
  (testing "pass returns a function"
    (is (fn? (bp/pass +))))

  (testing "pass returns the original parameter"
    (is= 1 ((bp/pass +) 1))))

(deftest pass-if-test
  (testing "pass-if returns a function"
    (is (fn? (bp/pass-if nil? +))))

  (testing "pass-if returns the original parameter if the predicate is true"
    (let [inc-odd (bp/pass-if even? inc)]
      (is= 2 (inc-odd 1))
      (is= 2 (inc-odd 2)))))

(deftest map-if-test
  (testing "map-if only maps if predicate is true"
    (is= [2 2 4] (bp/map-if odd? inc [1 2 3]))))

(deftype Derefable [v]
  IDeref
  #?(:cljs
     (-deref [_] v)
     :clj
     (deref [_] v)))

(deftest d#-test
  (let [assert-deref #(let [val (random/string)]
                        (is= val (bp/d# (% val))))]

    (testing "derefs when atom"
      (assert-deref atom))

    (testing "derefs when derefable"
      (assert-deref ->Derefable))

    (testing "derefs when not derefable"
      (assert-deref identity))))


(deftest if-fn-test

  (testing "if-fn"

    (testing "is a function"
      (is (fn? bp/if-fn)))

    (testing "returns a function"
      (is (fn? (bp/if-fn some? identity))))

    (testing "takes a predicate function"
      (let [f (bp/if-fn even? inc identity)]

        (testing "returning the result of then if true"
          (is= 3 (f 2)))

        (testing "returning the result of else if false"
          (is= 1 (f 1))

          (testing "or nil if no else defined"
            (let [f (bp/if-fn even? inc)]
              (is (nil? (f 1))))))))))

(deftest p=-test

  (testing "p="

    (testing "is a function"
      (is (fn? bp/p=)))

    (testing "is partial ="
      (let [=1 (partial = 1)]
        (is= (=1 1)
             ((bp/p= 1) 1)))

      (testing "that can take multiple arguments"
        (is ((bp/p= 1 1) 1))
        (is (false? ((bp/p= 1 2) 1)))))))

(deftest compr-test

  (testing "compr"

    (testing "composes functions left to right (the opposite of comp)"
      (is= "2" ((bp/compr inc str) 1))
      (is= ["aaa"] ((bp/compr str/trim str/lower-case vector) " AAA ")))

    (testing "returns identity for no args"
      (is= identity (bp/compr)))

    (testing "returns same function if one arg"
      (let [f (constantly "Kangaroos can't fart")]
        (is= f (bp/compr f))))))

(deftest and-fn-test

  (testing "and-fn-test"

    (testing "Throws an exception if no predicates are given"
      (is (thrown? Exception (bp/and-fn))))

    (testing "Returns a function"
      (is (fn? (bp/and-fn identity))))

    (testing "Will apply the predicate function when only given one"
      (let [is-odd? (bp/and-fn odd?)]
        (is (true? (is-odd? 11)))))

    (testing "Return true if each sub predicate function also returns true"
      (let [greater-than-ten? (partial < 10)
            is-odd-and-over-ten? (bp/and-fn greater-than-ten? odd?)]

        (testing "When all are true"
          (is (true? (is-odd-and-over-ten? 11))))

        (testing "When first predicate is true and second is false"
          (is (false? (is-odd-and-over-ten? 12))))

        (testing "When s is true and right is false"
          (is (false? (is-odd-and-over-ten? 8))))
        (is (false? (is-odd-and-over-ten? 9)))))))

(deftest or-fn-test

    (testing "Throws an exception if no predicates are given"
      (is (thrown? Exception (bp/or-fn))))

    (testing "Returns a function"
      (is (fn? (bp/or-fn identity))))

    (testing "Correctly applied each sub predicate function"
      (let [greater-than-ten? (partial < 10)
            is-odd-or-over-ten? (bp/or-fn greater-than-ten? odd?)]
        (is (true? (is-odd-or-over-ten? 11)))
        (is (true? (is-odd-or-over-ten? 12)))
        (is (false? (is-odd-or-over-ten? 8)))
        (is (false? (is-odd-or-over-ten? 9))))))
