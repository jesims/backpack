(ns io.jesi.backpack.fn-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as random])
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
        (is (= [1 2 3] ((bp/partial-right identity) 1 2 3))))

      (testing "partially applies parameters from the right"
        (is (= [1 2 3] ((bp/partial-right identity 3) 1 2)))
        (is (= [1 2 3] ((bp/partial-right identity 2 3) 1)))
        (is (= [1 2 3] ((bp/partial-right identity 1 2 3))))))))

(deftest apply-when-test
  (let [quote "We were running dark, yes?"]
    (testing "apply-when: "
      (testing "returns nil when f is nil"
        (is (nil? (bp/apply-when nil quote))))

      (testing "invokes f when it's truthy"
        (is (= quote (bp/apply-when identity quote)))))))

(deftest pass-test
  (testing "pass returns a function"
    (is (fn? (bp/pass +))))

  (testing "pass returns the original parameter"
    (is (= 1 ((bp/pass +) 1)))))

(deftest pass-if-test
  (testing "pass-if returns a function"
    (is (fn? (bp/pass-if nil? +))))

  (testing "pass-if returns the original parameter if the predicate is true"
    (let [inc-odd (bp/pass-if even? inc)]
      (is (= 2 (inc-odd 1)))
      (is (= 2 (inc-odd 2))))))

(deftest map-if-test
  (testing "map-if only maps if predicate is true"
    (is (= [2 2 4] (bp/map-if odd? inc [1 2 3])))))

(deftype Derefable [v]
  IDeref
  #?(:cljs
     (-deref [_] v)
     :clj
     (deref [_] v)))

(deftest d#-test
  (let [assert-deref #(let [val (random/string)]
                        (is (= val (bp/d# (% val)))))]

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
          (is (= 3 (f 2))))

        (testing "returning the result of else if false"
          (is (= 1 (f 1)))

          (testing "or nil if no else defined"
            (let [f (bp/if-fn even? inc)]
              (is (nil? (f 1))))))))))
