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

(deftest true-string?-test
  (testing "returns true for a true string"
    (is (true? (bp/true-string? "true"))))

  (testing "returns false for any other value"
    (is (false? (bp/true-string? "false")))
    (is (false? (bp/true-string? "f")))
    (is (false? (bp/true-string? "")))
    (is (false? (bp/true-string? nil)))))

(deftest ->camelCase-test
  (is (= "v2"
         (bp/->camelCase :v2)
         (bp/->camelCase "v2")))

  (is (= "baseUrl"
         (bp/->camelCase :base-url)
         (bp/->camelCase "base-url")))

  (is (= "_actions"
         (bp/->camelCase :_actions)
         (bp/->camelCase "_actions"))))

(deftest ->kebab-case-test
  (is (= "v2"
         (bp/->kebab-case :v2)
         (bp/->kebab-case "v2")))

  (is (= "base-url"
         (bp/->kebab-case :baseUrl)
         (bp/->kebab-case :baseURL)
         (bp/->kebab-case "baseUrl")))

  (is (= "something-had-spaces"
         (bp/->kebab-case "something had spaces")))

  (is (= "something-had--multiple-spaces"
         (bp/->kebab-case "something had  multiple spaces")))

  (is (= "_actions"
         (bp/->kebab-case :_actions)
         (bp/->kebab-case "_actions"))))
