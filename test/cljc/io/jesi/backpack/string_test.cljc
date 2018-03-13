(ns io.jesi.backpack.string-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.string :as string]))

(deftest uuid-str?-test
  (testing "uuid-str? is a UUID string"
    (is (string/uuid-str? "2c80c3ca-535c-4706-bea2-afd2a2bf374d"))
    (is (not (string/uuid-str? "1234")))
    (is (not (string/uuid-str? 1234)))
    (is (not (string/uuid-str? nil)))))
