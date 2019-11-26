(ns io.jesi.backpack.atom-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack.atom :as atom]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]]))

(deftest assoc!-test

  (testing "assoc!"

    (testing "assocs into an atom"
      (let [a (atom nil)]
        (is= {:a 1}
             (atom/assoc! a :a 1)
             @a)))))

(deftest assoc-in!-test

  (testing "assoc-in!"

    (testing "assoc-in into an atom"
      (let [a (atom nil)]
        (is= {:a {:b 1}}
             (atom/assoc-in! a [:a :b] 1)
             @a)))))

(deftest assoc-some!-test

  (testing "assoc-some!"

    (testing "assoc-some into an atom"
      (let [a (atom nil)]
        (is= {:a 1}
             (atom/assoc-some! a :a 1 :b nil)
             @a)))))

(deftest dissoc!-test

  (testing "dissoc!"

    (testing "dissoc into an atom"
      (let [a (atom {:a 1 :b 1 :c 3})]
        (is= {:a 1}
             (atom/dissoc! a :b :c)
             @a)))))

(deftest dissoc-in!-test

  (testing "dissoc-in!"

    (testing "dissoc-in into an atom"
      (let [a (atom {:a {:b {:c 1}
                         :d 2
                         :e 3}})]
        (is= {:a {:d 2}}
             (atom/dissoc-in! a [:a :b :c] [:a :e])
             @a)))))

(deftest update!-test

  (testing "update!"

    (testing "update into an atom"
      (let [a (atom {:a 1})]
        (is= {:a 2}
             (atom/update! a :a inc)
             @a)))))

(deftest merge!-test

  (testing "merge!"

    (testing "merge into an atom"
      (let [a (atom {:a 1})]
        (is= {:a 1 :b 2 :c 3}
             (atom/merge! a {:b 2} {:c 3})
             @a)))))

(deftest conj!-test

  (testing "conj!"

    (testing "conj into an atom"
      (let [a (atom [1])]
        (is= [1 2 3]
             (atom/conj! a 2 3)
             @a)))))
