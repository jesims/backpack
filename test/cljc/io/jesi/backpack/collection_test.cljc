(ns io.jesi.backpack.collection-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]))

(deftest safe-empty?-test
  (testing "returns true when empty coll/string/map"
    (is (bp/safe-empty? []))
    (is (bp/safe-empty? '()))
    (is (bp/safe-empty? ""))
    (is (bp/safe-empty? {})))

  (testing "returns false when non empty coll/string/map"
    (is (false? (bp/safe-empty? ["value"])))
    (is (false? (bp/safe-empty? '("value"))))
    (is (false? (bp/safe-empty? "value")))
    (is (false? (bp/safe-empty? {:value true}))))

  (testing "returns true when nil"
    (is (bp/safe-empty? nil)))

  (testing "returns false when non-nil"
    (is (false? (bp/safe-empty? 1234)))
    (is (false? (bp/safe-empty? (rnd/uuid))))))

(deftest safe-empty?-test
  (testing "returns true when empty coll/string/map"
    (is (bp/safe-empty? []))
    (is (bp/safe-empty? '()))
    (is (bp/safe-empty? ""))
    (is (bp/safe-empty? {})))

  (testing "returns false when non empty coll/string/map"
    (is (false? (bp/safe-empty? ["value"])))
    (is (false? (bp/safe-empty? '("value"))))
    (is (false? (bp/safe-empty? "value")))
    (is (false? (bp/safe-empty? {:value true}))))

  (testing "returns true when nil"
    (is (bp/safe-empty? nil)))

  (testing "returns false when non-nil"
    (is (false? (bp/safe-empty? 1234)))
    (is (false? (bp/safe-empty? (rnd/uuid))))))

(deftest distinct-by-test
  (testing "returns true if a collection of maps are distinct by a given keyword"
    (let [generator (fn [level] {:val (rnd/string) :type "animal" :danger-level level})
          maps (map (partial generator) (range 5))]
      (is (true? (bp/distinct-by :val maps)))
      (is (true? (bp/distinct-by :danger-level maps)))
      (is (false? (bp/distinct-by :type maps))))))

(deftest in?-test
  (testing "true if a collection contains a given value"
    (let [col ["An" "ostrich's" "eye" "is" "bigger" "than" "it's" "brain"]]
      (is (true? (bp/in? col "An")))
      (is (true? (bp/in? (set col) "An")))
      (is (false? (bp/in? col "an")))
      (is (false? (bp/in? col "bear"))))))

(deftest filter-values-test
  (testing "filter-values: "
    (testing "filter map values based on a predicate"
      (is (= {} (bp/filter-values true? {})))
      (is (= {:a true} (bp/filter-values true? {:a true :b nil}))))))

(deftest filter-empty-test
  (testing "filters out any empty values"
    (let [original {:value  "123"
                    :key    :123
                    :number 1
                    :false  false
                    :empty  ""
                    :nil    nil
                    :vec    []
                    :seq    ()}
          expected {:value  "123"
                    :key    :123
                    :number 1
                    :false  false}
          actual (bp/filter-empty original)]
      (is (= expected actual)))))

(deftest assoc-when-test
  (testing "assoc-when: "
    (testing "assoc when the value is not nil"
      (is (= {:k 1} (bp/assoc-when {} :k 1)))
      (is (= {} (bp/assoc-when {} :k nil))))

    (testing "supports multiple key value pairs"
      (is (= {:k 1 :l 2} (bp/assoc-when {} :k 1 :l 2)))
      (is (= {:l 2} (bp/assoc-when {} :k nil :l 2))))))

(deftest select-non-nil-keys-test
  (testing "select-non-nil-keys: "
    (testing "doesn't return keys that don't exist"
      (is (= {:a 1} (bp/select-non-nil-keys {:a 1} [:a :b :c]))))
    (testing "doesn't return keys with nil values"
      (is (= {:a 1} (bp/select-non-nil-keys {:a 1 :b nil} [:a :b :c]))))
    (testing "preserves true and false values values"
      (is (= {:a true :b false} (bp/select-non-nil-keys {:a true :b false} [:a :b :c]))))))

(deftest contains-any?-test
  (let [m {:a 1 :b 2 :c 3}
        contains? (partial contains? m)
        contains-any? (partial bp/contains-any? m)]

    (testing "Same as contains for one key"
      (let [key :a]
        (is (= (contains? key) (contains-any? key))))
      (let [key :b]
        (is (= (contains? key) (contains-any? key))))
      (let [key :c]
        (is (= (contains? key) (contains-any? key))))
      (let [key :d]
        (is (= (contains? key) (contains-any? key))))
      (let [key "d"]
        (is (= (contains? key) (contains-any? key)))))

    (testing "Supports multiple keys"
      (is (contains-any? :a))
      (is (contains-any? :a :b))
      (is (contains-any? :a :d))
      (is (not (contains-any? :d :e)))))

  (testing "Same as contains for one key"
    (let [m {:a 1 :b 2 :c 3}
          contains? (partial contains? m)
          contains-any? (partial bp/contains-any? m)]
      (let [key :a]
        (is (= (contains? key) (contains-any? key))))
      (let [key :b]
        (is (= (contains? key) (contains-any? key))))
      (let [key :c]
        (is (= (contains? key) (contains-any? key))))
      (let [key :d]
        (is (= (contains? key) (contains-any? key))))
      (let [key "d"]
        (is (= (contains? key) (contains-any? key))))))

  (testing "Supports multiple keys"
    (let [contains-any? (partial bp/contains-any? {:a 1 :b 2 :c 3})]
      (is (contains-any? :a))
      (is (contains-any? :a :b))
      (is (contains-any? :a :d))
      (is (not (contains-any? :d :e))))))