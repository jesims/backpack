(ns io.jesi.backpack.number-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]])
  #?(:clj
     (:import (java.lang Integer))))

(deftest infinity-test

  (testing "is a number"
    (is (some? bp/infinity))
    (is (number? bp/infinity)))

  #?(:clj
     (testing "Is equal to java Integer/MAX_VALUE"
       (is= Integer/MAX_VALUE bp/infinity))))

(deftest round-to-test

  (testing "Rounds values as expected"
    (is= 3.0 (bp/round-to 2 3))
    (is= 3.33 (bp/round-to 2 3.3333333))
    (is= 1233.67 (bp/round-to 2 1233.667890))
    (is= 1233.668 (bp/round-to 3 1233.667890))
    (is= 1233.7 (bp/round-to 1 1233.667890))
    (is= 1234.0 (bp/round-to 0 1233.667890))))

(deftest mod-test

  (testing "returns modulus of value"

    (testing "when non 0"
      (is= 4M (bp/mod 10 6))
      (is= 6M (bp/mod 6 10))
      (is= 0.1M (bp/mod 5.3 1.3))
      (is= 0.1M (bp/mod 0.1 0.3)))

    (testing "when 0"
      (is (zero? (bp/mod 1.0 1.0)))
      (is (zero? (bp/mod 18 1.5)))
      (is (zero? (bp/mod 5.2 1.3)))
      (is (zero? (bp/mod 0.3 0.1))))))
