(ns io.jesi.backpack.traverse-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [clojure.walk :as cljw]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.test.macros :refer [is=]]))

(defn- go-walking [walk-f col]
  (let [capture (atom [])]
    (walk-f #(do (swap! capture conj %) %) col)
    @capture))

(def ^:private prewalk (partial go-walking bp/prewalk))
(def ^:private postwalk (partial go-walking bp/postwalk))

(def ^:private col {:a [{:b ""}]})

(deftest postwalk-test

  (testing "postwalk"

    (testing "walks as expected"
      (is= [:a :b "" [:b ""] {:b ""} [{:b ""}] [:a [{:b ""}]] col]
           (postwalk col))

      (is= [1 2 [1 2] 3 4 5 6 [5 6] [3 4 [5 6]] 7 8 [7 8] [[1 2] [3 4 [5 6]] [7 8]]]
           (postwalk [[1 2] [3 4 [5 6]] [7 8]]))))

  #?(:clj
     (testing "behaves the same as clojure.walk"
       (is= (with-out-str (cljw/postwalk-demo col))
            (with-out-str (bp/postwalk-demo col))))))

(deftest prewalk-test

  (testing "prewalk"

    (testing "walks as expected"
      (is= [col [:a [{:b ""}]] :a [{:b ""}] {:b ""} [:b ""] :b ""]
           (prewalk col))

      (is= [[[1 2] [3 4 [5 6]] [7 8]] [1 2] 1 2 [3 4 [5 6]] 3 4 [5 6] 5 6 [7 8] 7 8]
           (prewalk [[1 2] [3 4 [5 6]] [7 8]]))))

  #?(:clj
     (testing "behaves the same as clojure.walk"
       (is= (with-out-str (cljw/prewalk-demo col))
            (with-out-str (bp/prewalk-demo col))))))
