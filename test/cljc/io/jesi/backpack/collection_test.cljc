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

(deftest dissoc-all-test
  (testing "Removes multiple keys"
    (let [m {:a 1 :b 2 :c 3}]
      (is (= {:a 1} (bp/dissoc-all m :b :c)))
      (is (= {:a 1} (bp/dissoc-all m :b :c :d)))))

  (testing "Removed nested keys"
    (is (= {:b {}} (bp/dissoc-all {:a 1 :b {:a 2}} :a)))))

(deftest first-non-nil-test
  (testing "Returns nil if all keys are nil"
    (is (nil? (bp/first-non-nil {} :these :do :not :exist))))

  (testing "Returns first non-nil value"
    (let [id (rnd/uuid)
          m {:animal "Lion"
             :fact   "In the wild, usually makes no more than twenty kills a year."
             :name   nil
             :id     id}]
      (is (= id (bp/first-non-nil m :id :animal :this)))
      (is (= "Lion" (bp/first-non-nil m :name :missing :animal :id))))))

(deftest filter-nil-keys-test
  (let [m {:id          (rnd/uuid)
           :animal      "Gorillas"
           :fact        "Can catch human colds and other illnesses."
           :age         nil
           :diet        ["plants"]
           :environment []}]
    (is (= (dissoc m :age) (bp/filter-nil-keys m)))))

(deftest translate-keys-test
  (testing "Is a function"
    (is (fn? bp/translate-keys)))

  (testing "Does not modify when kmap is"
    (let [m {:a 1}]

      (testing "empty"
        (is (= m (bp/translate-keys {} m))))

      (testing "nil"
        (is (= m (bp/translate-keys nil m))))))

  (testing "Returns nil if specified map is nil"
    (is (= nil (bp/translate-keys {:a :b} nil))))

  (testing "Returns a empty map if specified map is empty"
    (is (= {} (bp/translate-keys {:a :b} {}))))

  (testing "Converts map keys"
    (is (= {:a 1 :b 2 :new-a 1 :new-b 2}
           (bp/translate-keys {:new-a :a, :new-b :b} {:a 1 :b 2}))))

  (testing "Retains existing entries"
    (is (= {:a 1 :b 2 :c nil :d 2}
           (bp/translate-keys {:b :d} {:a 1 :c nil :d 2}))))

  (testing "Replaces existing entries"
    (is (= {:a 1 :b 1}
           (bp/translate-keys {:b :a} {:a 1 :b 2}))))

  (testing "Does not add the new key if the key does not exist"
    (is (= {:a 1 :b 2}
           (bp/translate-keys {:c :d} {:a 1 :b 2}))))

  (testing "Can set multiple keys from a single key"
    (is (= {:a 1 :b 1 :c 1}
           (bp/translate-keys {:a :c, :b :c} {:c 1})))))
