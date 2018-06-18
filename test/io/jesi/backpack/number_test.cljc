(ns io.jesi.backpack.number-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp])
  #?(:clj
     (:import (java.lang Integer))))

(deftest infinity-test

  (testing "is a number"
    (is (some? bp/infinity))
    (is (number? bp/infinity)))

  #?(:clj
     (testing "Is equal to java Integer/MAX_VALUE"
       (is (= Integer/MAX_VALUE bp/infinity)))))

(deftest round-to-test

  (testing "Rounds values as expected"
    (is (= 3.0 (bp/round-to 2 3)))
    (is (= 3.33 (bp/round-to 2 3.3333333)))
    (is (= 1233.67 (bp/round-to 2 1233.667890)))
    (is (= 1233.668 (bp/round-to 3 1233.667890)))
    (is (= 1233.7 (bp/round-to 1 1233.667890)))
    (is (= 1234.0 (bp/round-to 0 1233.667890)))))
