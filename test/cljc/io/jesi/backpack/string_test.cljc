(ns io.jesi.backpack.string-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

(deftest uuid-str?-test
  (testing "uuid-str? is a UUID string"
    (is (bp/uuid-str? "2c80c3ca-535c-4706-bea2-afd2a2bf374d"))
    (is (not (bp/uuid-str? "1234")))
    (is (not (bp/uuid-str? 1234)))
    (is (not (bp/uuid-str? nil)))))
