(ns io.jesi.backpack.spy-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.spy :as spy])
  (:import (java.io StringWriter)))

(deftest prn-test

  #?(:clj (testing "is a macro"
            (bp/macro? `spy/prn)))

  (let [a 1 b 2]
    (testing "expands to applying prn"
      (is (= '(clojure.core/println "a:" (clojure.core/pr-str a) "b:" (clojure.core/pr-str b))
             (macroexpand-1 '(io.jesi.backpack.spy/prn a b)))))

    (testing "prns the specified values"
      (is (= "a: 1\n" (with-out-str (spy/prn a))))
      (is (= "a: 1 b: 2\n" (with-out-str (spy/prn a b)))))))
