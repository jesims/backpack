(ns io.jesi.backpack.string-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.strict :refer [= deftest is is= testing]]))

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
      (is= "The decapitated" (bp/subs-inc " decapitated" input)))))

(deftest subs-to-test
  (let [input "What humans do over the next 50 years will determine the fate of all life on the planet"]

    (testing "returns the whole string if match is not found"
      (is= input (bp/subs-to "." input)))

    (testing "returns the substring up to match"
      (is= "What" (bp/subs-to " humans" input)))))

(deftest true-string?-test

  (testing "returns true for a true string"
    (is (true? (bp/true-string? "true"))))

  (testing "returns false for any other value"
    (is (false? (bp/true-string? "false")))
    (is (false? (bp/true-string? "f")))
    (is (false? (bp/true-string? "")))
    (is (false? (bp/true-string? nil)))))

(deftest ->camelCase-test
  (is= "v2"
       (bp/->camelCase :v2)
       (bp/->camelCase "v2"))

  (is= "baseUrl"
       (bp/->camelCase :base-url)
       (bp/->camelCase "base-url"))

  (is= "_actions"
       (bp/->camelCase :_actions)
       (bp/->camelCase "_actions"))

  (is= "-actionMan"
       (bp/->camelCase :-action-man)
       (bp/->camelCase "-action-man")
       (bp/->camelCase "-Action-Man"))

  (is= "covid19Enabled"
       (bp/->camelCase :covid19-enabled)
       (bp/->camelCase "covid19-enabled"))
  ;TODO what about double - ?
  (comment (is= "-action-Man"
                (bp/->camelCase "-action--man")))

  (is= "-actionMan-"
       (bp/->camelCase "-action-man-"))

  (is= "-actions"
       (bp/->camelCase :-actions)
       (bp/->camelCase "-actions"))

  (is= "namespaced/kebabCase"
       (bp/->camelCase :namespaced/kebab-case)
       (bp/->camelCase "namespaced/kebab-case")))

(deftest ->kebab-case-test
  (is= "kebab-case"
       (bp/->kebab-case :kebab-case)
       (bp/->kebab-case "kebab-case"))

  (is= "namespaced/kebab-case"
       (bp/->kebab-case :namespaced/kebab-case)
       (bp/->kebab-case "namespaced/kebab-case"))

  (is= "v2"
       (bp/->kebab-case :v2)
       (bp/->kebab-case "v2"))

  (is= "base-url"
       (bp/->kebab-case :baseUrl)
       (bp/->kebab-case :baseURL)
       (bp/->kebab-case "baseUrl"))

  (is= "base-url-v2"
       (bp/->kebab-case :baseURLv2)
       (bp/->kebab-case :BaseUrlV2))

  (is= "something-had-spaces"
       (bp/->kebab-case "something had spaces"))

  (is= "something-had--multiple-spaces"
       (bp/->kebab-case "something had  multiple spaces"))

  (is= "-actions"
       (bp/->kebab-case :_actions)
       (bp/->kebab-case "_actions")
       (bp/->kebab-case :-actions)
       (bp/->kebab-case "-actions"))

  (is= "-action-man"
       (bp/->kebab-case :-actionMan)
       (bp/->kebab-case "-actionMan")
       (bp/->kebab-case "-ActionMan"))

  (is= "-lots-of-yelling"
       (bp/->kebab-case "_LOTS_OF_YELLING"))

  (is= "lots-of-yelling"
       (bp/->kebab-case :LOTS_OF_YELLING))

  (is= "covid19-enabled"
       (bp/->kebab-case :covid19Enabled)
       (bp/->kebab-case "covid19Enabled")))

(deftest ->kebab-case-key-test
  (is= :turtles.can.breathe.through/their-anus
       (bp/->kebab-case-key :turtles.can.breathe.through/their-anus)
       (bp/->kebab-case-key "turtles.can.breathe.through/their-anus")
       (bp/->kebab-case-key "turtles.can.breathe.through/theirAnus")))

(deftest ->snake_case-test
  (is= "v2"
       (bp/->snake_case :v2)
       (bp/->snake_case "v2"))

  (is= "base_url"
       (bp/->snake_case :baseUrl)
       (bp/->snake_case :baseURL)
       (bp/->snake_case "baseUrl"))

  (is= "base_url_v2"
       (bp/->snake_case :baseURLv2)
       (bp/->snake_case :BaseUrlV2))

  (is= "something_had_spaces"
       (bp/->snake_case "something had spaces"))

  (is= "something_had__multiple_spaces"
       (bp/->snake_case "something had  multiple spaces")))

(deftest prefix-test

  (testing "adds string prefix"
    (is= "#123" (bp/prefix \# "123"))
    (is= "abc123" (bp/prefix "abc" "123")))

  (testing "doesn't prefix if already prefixed"
    (is= "#123" (bp/prefix \# "#123"))
    (is= "abc123" (bp/prefix "abc" "abc123"))))

(deftest suffix-test

  (testing "adds string suffix"
    (is= "stuff/" (bp/suffix \/ "stuff"))
    (is= "stuffed" (bp/suffix "ed" "stuff")))

  (testing "doesn't suffix if already suffixed"
    (is= "stuff/" (bp/suffix \/ "stuff/"))
    (is= "stuffed" (bp/suffix "ed" "stuffed"))))

(deftest remove-prefix-test

  (testing "returns the correct formatted address when name is in the formatted name "
    (let [fact "Meerkats hunt and eat insects, scorpions, small lizards, snakes, eggs."]

      (testing "does nothing if prefix is not found"
        (is= fact
             (bp/remove-prefix (rnd/string) fact)))

      (testing "removes the prefix with a default separator of `, `"
        (is= "scorpions, small lizards, snakes, eggs."
             (bp/remove-prefix "Meerkats hunt and eat insects" fact)))

      (testing "allows specifying a custom separator"
        (is= "insects, scorpions, small lizards, snakes, eggs."
             (bp/remove-prefix "Meerkats hunt and eat" \space fact))))))

(deftest subs-test
  (let [s "Reptiles have scales"]

    (testing "returns nil when given nil"
      (is (nil? (bp/subs nil 1)))
      (is= s (bp/subs s nil)))

    (testing "returns error when out of bounds"
      (is (thrown? #?(:clj Exception :cljs js/Error) (bp/subs s 100)))
      (is (thrown? #?(:clj Exception :cljs js/Error) (bp/subs s -100)))
      (is (thrown? #?(:clj Exception :cljs js/Error) (bp/subs s 100 5)))
      (is (thrown? #?(:clj Exception :cljs js/Error) (bp/subs s -100 5))))

    (testing "invalid data"
      (is (thrown? #?(:clj Error :cljs js/Error) (bp/subs s true))))

    (testing "returns empty string when start is after end"
      (is= ""
           (bp/subs s 5 1))
      (is= ""
           (bp/subs s -1 -5))
      (is= ""
           (bp/subs s -1 2)))

    (testing "returns substring"

      (testing "when start and end are both "

        (testing "positive"
          (is= "epti"
               (bp/subs s 1 5)))

        (testing "negative"
          (is= "cale"
               (bp/subs s -5 -1))))

      (testing "off setting from start is "

        (testing "positive"
          (is= "les have scales"
               (bp/subs s 5)))

        (testing "negative"
          (is= "cales"
               (bp/subs s -5)))

        (testing "nil"
          (is= s (bp/subs s nil))))

      (testing "when start is "

        (testing "positive"
          (is= "epti"
               (bp/subs s 1 5)))

        (testing "negative"
          (is= "ave s"
               (bp/subs s -10 15)))

        (testing "nil"
          (is= "Repti"
               (bp/subs s nil 5)))))))

(deftest ->proper-case-test

  (testing "->proper-case"

    (testing "converts words into proper case"
      (is (nil? (bp/->proper-case nil)))
      (is= "" (bp/->proper-case ""))
      (is= "Whales Are Warm-Blooded Creatures That Nurse Their Young"
           (bp/->proper-case "Whales are warm-blooded creatures that nurse their young"))
      (is= "Blue-Whales-Are-The-Largest-Animals-To-Have-Ever-Existed"
           (bp/->proper-case "blue-whales-are-the-largest-animals-to-have-ever-existed"))
      (is= "The Heart Of A Blue Whale Is As Big As A Small Car"
           (bp/->proper-case "The heart of a Blue Whale is as big as a small car")))))

(deftest kebab->proper-case-test

  (testing "kebab->proper-case"

    (testing "converts kebab cased keywords to proper case"
      (is (nil? (bp/kebab->proper-case nil)))
      (is= "" (bp/kebab->proper-case ""))
      (is= "A Meercat Can Live For 12 14 Years In Captivity"
           (bp/kebab->proper-case "a-meercat-can-live-for-12-14-years-in-captivity")))))
