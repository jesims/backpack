(ns io.jesi.backpack.common-test
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.customs.strict :refer [are deftest is is= testing]])
  #?(:clj
     (:import
       (clojure.lang Named))))

(deftest named?-test

  (testing "named?"

    (testing "is a function"
      (is (fn? bp/named?)))

    (testing "returns `true` if value is"

      (testing "a string"
        (is (bp/named? "hello")))

      (testing "implement Named"
        (are [v]
          (is (bp/named? v))
          'hello
          :hello
          #?(:cljs (reify INamed
                     (-name [_]))
             :clj  (reify Named
                     (getName [_])
                     (getNamespace [_]))))))

    (testing "returns `false` for other types"
      (are [x]
        (is (false? (bp/named? x)))
        nil
        1
        {}
        []))))
