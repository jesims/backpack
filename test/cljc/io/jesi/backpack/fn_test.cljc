(ns io.jesi.backpack.fn-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.fn :as fu]))

(deftest partial-right-test
  (testing "partial-right"

    (testing "is a function"
      (is (ifn? fu/partial-right)))

    (testing "returns a function"
      (is (ifn? (fu/partial-right nil))))

    (let [identity (fn [& args] args)]
      (testing "returns the provided function if not args"
        (is (= [1 2 3] ((fu/partial-right identity) 1 2 3))))

      (testing "partially applies parameters from the right"
        (is (= [1 2 3] ((fu/partial-right identity 3) 1 2)))
        (is (= [1 2 3] ((fu/partial-right identity 2 3) 1)))
        (is (= [1 2 3] ((fu/partial-right identity 1 2 3))))))))
