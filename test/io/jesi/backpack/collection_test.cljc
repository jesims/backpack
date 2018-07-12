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

(deftest first-some-test
  (testing "Returns nil if all keys are nil"
    (is (nil? (bp/first-some {} :these :do :not :exist))))

  (testing "Returns first non-nil value"
    (let [id (rnd/uuid)
          m {:animal "Lion"
             :fact   "In the wild, usually makes no more than twenty kills a year."
             :name   nil
             :id     id}]
      (is (= id (bp/first-some m :id :animal :this)))
      (is (= "Lion" (bp/first-some m :name :missing :animal :id)))))

  (testing "Allows using functions"
    (let [fn1 #(:id %)
          fn2 (constantly false)
          fn3 (constantly "Fact yo self!")
          fn4 (constantly "Oh My!")
          m {:animal "Lion"
             :fact   "The female lion does ninety percent of the hunting."}]
      (is (= false (bp/first-some m fn1 fn2 fn3 fn4)))
      (is (= "Oh My!" (bp/first-some m fn4 fn3 fn2 fn1))))))

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

(deftest remove-empty-test

  (testing "returns nil for empty values"
    (is (nil? (bp/remove-empty nil)))
    (is (nil? (bp/remove-empty "")))
    (is (nil? (bp/remove-empty {})))
    (is (nil? (bp/remove-empty '())))
    (is (nil? (bp/remove-empty #{})))
    (is (nil? (bp/remove-empty []))))

  (testing "returns simple values"
    (is (= 1 (bp/remove-empty 1)))
    (is (= {:a 1} (bp/remove-empty {:a 1})))
    (is (= " " (bp/remove-empty " "))))

  (testing "removes empty values"
    (is (= [1]
           (bp/remove-empty [1 nil "" [] '() #{}])))
    (is (= {:a 1}
           (bp/remove-empty {:a 1 :b nil :c "" :d [] :e '() :f #{}}))))

  (testing "returns nil when empty"
    (is (nil? (bp/remove-empty {:str ""})))
    (is (nil? (bp/remove-empty [nil]))))

  (testing "returns the collection without empty values"
    (is (nil? (bp/remove-empty [nil [nil] []])))
    (is (= {:str "value" :int 123 :vec ["value"]}
           (bp/remove-empty {:str       "value"
                             :empty-str ""
                             :vec       ["value"]
                             :empty-vec []
                             :nil-vec   [nil]
                             :int       123
                             :nil       nil})))
    (is (= {:map {:str-vec ["string"]}}
           (bp/remove-empty {:empty-map {}
                             :map       {:str-vec ["string"]
                                         :nil     nil
                                         :map     [{:empty-str ""}]}})))))

(deftest assoc-in-test
  (let [m {}]

    (testing "Same ass assoc in"
      (is (= (assoc-in m [:a] 1)
             (bp/assoc-in m [:a] 1)))
      (is (= (assoc-in m [:a :b] 1)
             (bp/assoc-in m [:a :b] 1))))

    (testing "Takes multiple path value pairs"
      (is (= {:a 1 :b 2}
             (bp/assoc-in m
               [:a] 1
               [:b] 2))))
    (is (= {:a {:b 1}
            :c {:d 2}}
           (bp/assoc-in m
             [:a :b] 1
             [:c :d] 2)))))

(deftest trans-reduce-kv-test
  (testing "Works like reduce-kv, but takes a function that expects a transient"
    (let [values (sorted-map :a 1 :b 2 :c 3 :d 4 :e 5)
          reducer (fn [modifier coll _ v]
                    (modifier coll (+ (count coll) v)))]
      (is (= [1 3 5 7 9]
             (reduce-kv (partial reducer conj) [] values)
             (bp/trans-reduce-kv (partial reducer conj!) [] values))))))

(deftest trans-reduce-test
  (testing "Works like reduce, but takes a function that expects a transient"
    (let [values [[1] [2] [3]]]
      (testing "taking only a function and collection"
        (is (= [1 [2] [3]]
               (reduce conj values)
               (bp/trans-reduce conj! values))))

      (testing "taking a function, initial value and collection"
        (is (= [[1] [2] [3]]
               (reduce conj [] values)
               (bp/trans-reduce conj! [] values)))))))
