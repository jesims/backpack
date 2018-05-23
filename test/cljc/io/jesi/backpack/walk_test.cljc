(ns io.jesi.backpack.walk-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [io.jesi.backpack.walk :as bp]
    [clojure.walk :as clj-walk]))

(def ^:private col {:a [{:b ""}]})

(deftest postwalk-test
  (let [col [[1 2] [3 4 [5 6]] [7 8]]]

    (testing "postwalk"

      (testing "walks as expected"
        (let [capture (atom [])]
          (bp/postwalk #(do (swap! capture conj %) %) col)
          (is (= [1 2 [1 2] 3 4 5 6 [5 6] [3 4 [5 6]] 7 8 [7 8] [[1 2] [3 4 [5 6]] [7 8]]]
                 @capture))))

      #?(:clj
         (testing "behaves the same as clojure.walk"
           (is (= (with-out-str (clj-walk/postwalk-demo col))
                  (with-out-str (bp/postwalk-demo col)))))))))

(deftest prewalk-test

  (testing "prewalk"

    (testing "walks as expected"
      (let [capture (atom [])]
        (bp/postwalk #(do (swap! capture conj %) %) col)
        (is (= [:a :b "" [:b ""] {:b ""} [{:b ""}] [:a [{:b ""}]] col]
               @capture))))

    #?(:clj
       (testing "behaves the same as clojure.walk"
         (is (= (with-out-str (clj-walk/prewalk-demo col))
                (with-out-str (bp/prewalk-demo col))))))))
