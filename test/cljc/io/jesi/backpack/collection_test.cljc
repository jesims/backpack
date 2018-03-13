(ns io.jesi.backpack.collection-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.collection :as cu]
    [io.jesi.backpack.random :as rnd]))

(deftest safe-empty?-test
  (testing "returns true when empty coll/string/map"
    (is (cu/safe-empty? []))
    (is (cu/safe-empty? '()))
    (is (cu/safe-empty? ""))
    (is (cu/safe-empty? {})))

  (testing "returns false when non empty coll/string/map"
    (is (false? (cu/safe-empty? ["value"])))
    (is (false? (cu/safe-empty? '("value"))))
    (is (false? (cu/safe-empty? "value")))
    (is (false? (cu/safe-empty? {:value true}))))

  (testing "returns true when nil"
    (is (cu/safe-empty? nil)))

  (testing "returns false when non-nil"
    (is (false? (cu/safe-empty? 1234)))
    (is (false? (cu/safe-empty? (rnd/uuid))))))

(deftest safe-empty?-test
  (testing "returns true when empty coll/string/map"
    (is (cu/safe-empty? []))
    (is (cu/safe-empty? '()))
    (is (cu/safe-empty? ""))
    (is (cu/safe-empty? {})))

  (testing "returns false when non empty coll/string/map"
    (is (false? (cu/safe-empty? ["value"])))
    (is (false? (cu/safe-empty? '("value"))))
    (is (false? (cu/safe-empty? "value")))
    (is (false? (cu/safe-empty? {:value true}))))

  (testing "returns true when nil"
    (is (cu/safe-empty? nil)))

  (testing "returns false when non-nil"
    (is (false? (cu/safe-empty? 1234)))
    (is (false? (cu/safe-empty? (rnd/uuid))))))

(deftest distinct-by-test
  (testing "returns true if a collection of maps are distinct by a given keyword"
    (let [generator (fn [level] {:val (rnd/string) :type "animal" :danger-level level})
          maps (map (partial generator) (range 5))]
      (is (true? (cu/distinct-by :val maps)))
      (is (true? (cu/distinct-by :danger-level maps)))
      (is (false? (cu/distinct-by :type maps))))))

(deftest in?-test
  (testing "true if a collection contains a given value"
    (let [col ["An" "ostrich's" "eye" "is" "bigger" "than" "it's" "brain"]]
      (is (true? (cu/in? col "An")))
      (is (true? (cu/in? (set col) "An")))
      (is (false? (cu/in? col "an")))
      (is (false? (cu/in? col "bear"))))))

