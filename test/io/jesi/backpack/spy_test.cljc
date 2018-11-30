(ns io.jesi.backpack.spy-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.spy :as spy]
    [io.jesi.backpack :as bp]))

(deftest prn-test

  #?(:clj (testing "is a macro"
            (bp/macro? `spy/prn)))

  (comment (testing "expands to applying prn"
             (let [a 1 b 2]
               (is (= '(clojure.core/apply clojure.core/prn ["a:" a "b:" b])
                      (macroexpand-1 '(io.jesi.backpack.spy/prn a b)))))))

  (testing "prns the specified values"
    (let [a 1]
      (is (= "a: 1\n" (with-out-str (spy/prn a)))))))
