(ns io.jesi.backpack.walk-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [clojure.walk :as cljw]
    [io.jesi.backpack.walk :as bp]))

(def ^:private col {:a [{:b ""}]})

(deftest postwalk-test

  (testing "postwalk"

    (testing "walks as expected - using demo col"
      (let [capture (atom [])
            col [[1 2] [3 4 [5 6]] [7 8]]]
        (bp/postwalk #(do (swap! capture conj %) %) col)
        (is (= [1 2 [1 2] 3 4 5 6 [5 6] [3 4 [5 6]] 7 8 [7 8] [[1 2] [3 4 [5 6]] [7 8]]]
               @capture))))

    (testing "walks as expected"
      (let [capture (atom [])]
        (bp/postwalk #(do (swap! capture conj %) %) col)
        (is (= [:a :b "" [:b ""] {:b ""} [{:b ""}] [:a [{:b ""}]] col]
               @capture))))

    #?(:clj
       (testing "behaves the same as clojure.walk"
         (is (= (with-out-str (clw/postwalk-demo col))
                (with-out-str (bp/postwalk-demo col))))))))

(deftest prewalk-test

  (testing "prewalk"

    (testing "walks as expected"
      (let [capture (atom [])]
        (bp/prewalk #(do (swap! capture conj %) %) col)
        (is (= [col [:a [{:b ""}]] :a [{:b ""}] {:b ""} [:b ""] :b ""]
               @capture)))))

  #?(:clj
     (testing "behaves the same as clojure.walk"
       (is (= (with-out-str (cljw/prewalk-demo col))
              (with-out-str (bp/prewalk-demo col)))))))
