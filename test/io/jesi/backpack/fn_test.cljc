(ns io.jesi.backpack.fn-test
  (:refer-clojure :exclude [=])
  (:require
    #?(:cljs [cljs.core :refer [IDeref]])
    [clojure.string :as str]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as random]
    [io.jesi.customs.strict :refer [= deftest is is= testing thrown?]])
  #?(:clj (:import (clojure.lang ArityException IDeref))))


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

  (testing "and-fn"

    (testing "throws an exception if no parameters are given"
      (is (thrown? #?(:clj ArityException :cljs js/Error) (eval `(bp/and-fn)))))

    (testing "returns the predicate function when only given one"
      (is (identical? odd? (bp/and-fn odd?)))
      (is (identical? true? (bp/and-fn true?))))

    (testing "returns a function"
      (is (fn? (bp/and-fn identity)))

      (testing "that returns true if ALL predicate functions evaluate to true for the given value"
        (let [actual (bp/and-fn (partial < 10) odd?)]
          (is (true? (actual 11)))
          (is (false? (actual 12)))
          (is (false? (actual 8)))
          (is (false? (actual 9)))))

      (testing "that short circuits if any return false"
        (let [actual (bp/and-fn odd? even? #(throw (ex-info "I am evaluated" {})))]
          (is (false? (actual 2))))))))

(deftest or-fn-test

  (testing "or-fn"

    (testing "throws an exception if no parameters are given"
      (is (thrown? #?(:clj Exception :cljs js/Error) (eval `(bp/or-fn)))))

    (testing "returns the predicate function when only given one"
      (is (identical? odd? (bp/or-fn odd?)))
      (is (identical? true? (bp/or-fn true?))))

    (testing "returns a function"
      (is (fn? (bp/or-fn identity)))

      (testing "that returns true if ANY predicate function evaluate to true for the given value"
        (let [less-than-ten? (partial > 10)
              is-even-or-under-ten? (bp/or-fn less-than-ten? even?)]
          (is (true? (is-even-or-under-ten? 8)))
          (is (true? (is-even-or-under-ten? 9)))
          (is (true? (is-even-or-under-ten? 12)))
          (is (false? (is-even-or-under-ten? 11)))))

      (testing "that short circuits if any return true"
        (let [actual (bp/or-fn odd? even? #(throw (ex-info "I am thrown" {})))]
          (is (true? (actual 1))))))))
