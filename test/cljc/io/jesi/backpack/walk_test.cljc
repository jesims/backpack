(ns io.jesi.backpack.walk-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [io.jesi.backpack :as bp]))

(deftest postwalk-test
  (let [col {:a [{:b ""}]}]

    (testing "postwalk"

      (testing "walks as expected"
        (let [capture (atom [])]
          (bp/postwalk #(do (swap! capture conj %) %) col)
          (is (= [:a :b "" [:b ""] {:b ""} [{:b ""}] [:a [{:b ""}]] col]
                 @capture))))

      (testing "behaves the same as clojure.walk"
        (is (= (with-out-str (clojure.walk/postwalk-demo col))
               (with-out-str (bp/postwalk-demo col))))))))

(deftest prewalk-test
  (let [col {:a [{:b ""}]}]

    (testing "prewalk"

      (testing "behaves the same as clojure.walk"
        (is (= (with-out-str (clojure.walk/prewalk-demo col))
               (with-out-str (bp/prewalk-demo col))))))))

