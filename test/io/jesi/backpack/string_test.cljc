(ns io.jesi.backpack.string-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]))

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

  (is (= "base-url-v2"
         (bp/->kebab-case :baseURLv2)
         (bp/->kebab-case :BaseUrlV2)))

  (is (= "something-had-spaces"
         (bp/->kebab-case "something had spaces")))

  (is (= "something-had--multiple-spaces"
         (bp/->kebab-case "something had  multiple spaces")))

  (is (= "-actions"
         (bp/->kebab-case :_actions)
         (bp/->kebab-case "_actions")))

  (is (= "-lots-of-yelling"
         (bp/->kebab-case "_LOTS_OF_YELLING")))

  (is (= "lots-of-yelling"
         (bp/->kebab-case :LOTS_OF_YELLING))))

(deftest ->snake_case-test
  (is (= "v2"
         (bp/->snake_case :v2)
         (bp/->snake_case "v2")))

  (is (= "base_url"
         (bp/->snake_case :baseUrl)
         (bp/->snake_case :baseURL)
         (bp/->snake_case "baseUrl")))

  (is (= "base_url_v2"
         (bp/->snake_case :baseURLv2)
         (bp/->snake_case :BaseUrlV2)))

  (is (= "something_had_spaces"
         (bp/->snake_case "something had spaces")))

  (is (= "something_had__multiple_spaces"
         (bp/->snake_case "something had  multiple spaces"))))

(deftest prefix-test

  (testing "adds string prefix"
    (is (= "#123" (bp/prefix \# "123")))
    (is (= "abc123" (bp/prefix "abc" "123"))))

  (testing "doesn't prefix if already prefixed"
    (is (= "#123" (bp/prefix \# "#123")))
    (is (= "abc123" (bp/prefix "abc" "abc123")))))

(deftest suffix-test

  (testing "adds string suffix"
    (is (= "stuff/" (bp/suffix \/ "stuff")))
    (is (= "stuffed" (bp/suffix "ed" "stuff"))))

  (testing "doesn't suffix if already suffixed"
    (is (= "stuff/" (bp/suffix \/ "stuff/")))
    (is (= "stuffed" (bp/suffix "ed" "stuffed")))))

(deftest remove-prefix-test

  (testing "returns the correct formatted address when name is in the formatted name "
    (let [fact "Meerkats hunt and eat insects, scorpions, small lizards, snakes, eggs."]

      (testing "does nothing if prefix is not found"
        (is (= fact
               (bp/remove-prefix (rnd/string) fact))))

      (testing "removes the prefix with a default separator of `, `"
        (is (= "scorpions, small lizards, snakes, eggs."
               (bp/remove-prefix "Meerkats hunt and eat insects" fact))))

      (testing "allows specifying a custom separator"
        (is (= "insects, scorpions, small lizards, snakes, eggs."
               (bp/remove-prefix "Meerkats hunt and eat" \space fact)))))))

