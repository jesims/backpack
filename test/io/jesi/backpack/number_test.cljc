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
