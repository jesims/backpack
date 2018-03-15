(ns io.jesi.backpack.fn-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

(deftest partial-right-test
  (testing "partial-right"

    (testing "is a function"
      (is (ifn? bp/partial-right)))

    (testing "returns a function"
      (is (ifn? (bp/partial-right nil))))

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
