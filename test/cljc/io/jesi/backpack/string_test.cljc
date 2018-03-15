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

(deftest subs-inc-test
  (let [input "The decapitated head of a dead snake can still bite, even hours after death"]

    (testing "returns the nil if the sub-string isn't found"
      (is (nil? (bp/subs-inc "." input))))

    (testing "returns the substring including the match"
      (is (= "The decapitated" (bp/subs-inc " decapitated" input))))))

(deftest subs-to-test
  (let [input "What humans do over the next 50 years will determine the fate of all life on the planet"]

    (testing "returns the whole string if match is not found"
      (is (= input (bp/subs-to "." input))))

    (testing "returns the substring up to match"
      (is (= "What" (bp/subs-to " humans" input))))))
