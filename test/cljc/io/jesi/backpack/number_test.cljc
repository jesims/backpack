(ns io.jesi.backpack.number-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

;Test infinity == Integer/MAX
(deftest infinity-test
  (testing "Is equal to java Integer/MAX_VALUE"
    (is (= Integer/MAX_VALUE bp/infinity))))
