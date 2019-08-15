(ns io.jesi.backpack.collection-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.backpack.test.macros :refer [is=]]))

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

  (testing "filter-values:"

    (testing "filter map values based on a predicate"
      (is= {} (bp/filter-values true? {}))
      (is= {:a true} (bp/filter-values true? {:a true :b nil})))))

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
      (is= expected actual))))

(deftest select-non-nil-keys-test

  (testing "select-non-nil-keys: "

    (testing "doesn't return keys that don't exist"
      (is= {:a 1} (bp/select-non-nil-keys {:a 1} [:a :b :c])))

    (testing "doesn't return keys with nil values"
      (is= {:a 1} (bp/select-non-nil-keys {:a 1 :b nil} [:a :b :c])))

    (testing "preserves true and false values values"
      (is= {:a true :b false} (bp/select-non-nil-keys {:a true :b false} [:a :b :c])))))

(deftest contains-any?-test
  (let [m {:a 1 :b 2 :c 3}
        contains? (partial contains? m)
        contains-any? (partial bp/contains-any? m)]

    (testing "Same as contains for one key"
      (let [key :a]
        (is= (contains? key) (contains-any? key)))
      (let [key :b]
        (is= (contains? key) (contains-any? key)))
      (let [key :c]
        (is= (contains? key) (contains-any? key)))
      (let [key :d]
        (is= (contains? key) (contains-any? key)))
      (let [key "d"]
        (is= (contains? key) (contains-any? key))))

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
        (is= (contains? key) (contains-any? key)))
      (let [key :b]
        (is= (contains? key) (contains-any? key)))
      (let [key :c]
        (is= (contains? key) (contains-any? key)))
      (let [key :d]
        (is= (contains? key) (contains-any? key)))
      (let [key "d"]
        (is= (contains? key) (contains-any? key)))))

  (testing "Supports multiple keys"
    (let [contains-any? (partial bp/contains-any? {:a 1 :b 2 :c 3})]
      (is (contains-any? :a))
      (is (contains-any? :a :b))
      (is (contains-any? :a :d))
      (is (not (contains-any? :d :e))))))

(deftest dissoc-all-test

  (testing "Removes multiple keys"
    (let [m {:a 1 :b 2 :c 3}]
      (is= {:a 1} (bp/dissoc-all m :b :c))
      (is= {:a 1} (bp/dissoc-all m :b :c :d))))

  (testing "Removed nested keys"
    (is= {:b {}} (bp/dissoc-all {:a 1 :b {:a 2}} :a))))

(deftest first-some-test

  (testing "Returns nil if all keys are nil"
    (is (nil? (bp/first-some {} :these :do :not :exist))))

  (testing "Returns first non-nil value"
    (let [id (rnd/uuid)
          m {:animal "Lion"
             :fact   "In the wild, usually makes no more than twenty kills a year."
             :name   nil
             :id     id}]
      (is= id (bp/first-some m :id :animal :this))
      (is= "Lion" (bp/first-some m :name :missing :animal :id))))

  (testing "Allows using functions"
    (let [fn1 #(:id %)
          fn2 (constantly false)
          fn3 (constantly "Fact yo self!")
          fn4 (constantly "Oh My!")
          m {:animal "Lion"
             :fact   "The female lion does ninety percent of the hunting."}]
      (is= false (bp/first-some m fn1 fn2 fn3 fn4))
      (is= "Oh My!" (bp/first-some m fn4 fn3 fn2 fn1)))))

(deftest filter-nil-keys-test
  (let [m {:id          (rnd/uuid)
           :animal      "Gorillas"
           :fact        "Can catch human colds and other illnesses."
           :age         nil
           :diet        ["plants"]
           :environment []}]
    (is= (dissoc m :age) (bp/filter-nil-keys m))))

(deftest translate-keys-test

  (testing "Is a function"
    (is (fn? bp/translate-keys)))

  (testing "Does not modify when kmap is"
    (let [m {:a 1}]

      (testing "empty"
        (is= m (bp/translate-keys {} m)))

      (testing "nil"
        (is= m (bp/translate-keys nil m)))))

  (testing "Returns nil if specified map is nil"
    (is= nil (bp/translate-keys {:a :b} nil)))

  (testing "Returns a empty map if specified map is empty"
    (is= {} (bp/translate-keys {:a :b} {})))

  (testing "Converts map keys"
    (is= {:a 1 :b 2 :new-a 1 :new-b 2}
         (bp/translate-keys {:new-a :a, :new-b :b} {:a 1 :b 2})))

  (testing "Retains existing entries"
    (is= {:a 1 :b 2 :c nil :d 2}
         (bp/translate-keys {:b :d} {:a 1 :c nil :d 2})))

  (testing "Replaces existing entries"
    (is= {:a 1 :b 1}
         (bp/translate-keys {:b :a} {:a 1 :b 2})))

  (testing "Does not add the new key if the key does not exist"
    (is= {:a 1 :b 2}
         (bp/translate-keys {:c :d} {:a 1 :b 2})))

  (testing "Can set multiple keys from a single key"
    (is= {:a 1 :b 1 :c 1}
         (bp/translate-keys {:a :c, :b :c} {:c 1}))))

(deftest remove-empty-test

  (testing "returns nil for empty values"
    (is (nil? (bp/remove-empty nil)))
    (is (nil? (bp/remove-empty "")))
    (is (nil? (bp/remove-empty {})))
    (is (nil? (bp/remove-empty '())))
    (is (nil? (bp/remove-empty #{})))
    (is (nil? (bp/remove-empty []))))

  (testing "returns simple values"
    (is= 1 (bp/remove-empty 1))
    (is= {:a 1} (bp/remove-empty {:a 1}))
    (is= " " (bp/remove-empty " ")))

  (testing "removes empty values"
    (is= [1]
         (bp/remove-empty [1 nil "" [] '() #{}]))
    (is= {:a 1}
         (bp/remove-empty {:a 1 :b nil :c "" :d [] :e '() :f #{}})))

  (testing "returns nil when empty"
    (is (nil? (bp/remove-empty {:str ""})))
    (is (nil? (bp/remove-empty [nil]))))

  (testing "returns the collection without empty values"
    (is (nil? (bp/remove-empty [nil [nil] []])))
    (is= {:str "value" :int 123 :vec ["value"]}
         (bp/remove-empty {:str       "value"
                           :empty-str ""
                           :vec       ["value"]
                           :empty-vec []
                           :nil-vec   [nil]
                           :int       123
                           :nil       nil}))
    (is= {:map {:str-vec ["string"]}}
         (bp/remove-empty {:empty-map {}
                           :map       {:str-vec ["string"]
                                       :nil     nil
                                       :map     [{:empty-str ""}]}}))))

(deftest assoc-in-test
  (let [m {}]

    (testing "assoc-in"

      (testing "Same as assoc in"
        (is= (assoc-in m [:a] 1)
             (bp/assoc-in m [:a] 1))
        (is= (assoc-in m [:a :b] 1)
             (bp/assoc-in m [:a :b] 1)))

      (testing "Takes multiple path value pairs"
        (is= {:a 1 :b 2}
             (bp/assoc-in m
               [:a] 1
               [:b] 2))
        (is= {:a 1 :b 2}
             (bp/assoc-in m
               :a 1
               :b 2))
        (is= {:a {:b 1}
              :c {:d 2}}
             (bp/assoc-in m
               [:a :b] 1
               [:c :d] 2))))))


(deftest trans-reduce-kv-test

  (testing "Works like reduce-kv, but takes a function that expects a transient"
    (let [values (sorted-map :a 1 :b 2 :c 3 :d 4 :e 5)
          reducer (fn [modifier coll _ v]
                    (modifier coll (+ (count coll) v)))]
      (is= [1 3 5 7 9]
           (reduce-kv (partial reducer conj) [] values)
           (bp/trans-reduce-kv (partial reducer conj!) [] values)))))

(deftest trans-reduce-test

  (testing "Works like reduce, but takes a function that expects a transient"
    (let [values [[1] [2] [3]]]

      (testing "taking only a function and collection"
        (is= [1 [2] [3]]
             (reduce conj values)
             (bp/trans-reduce conj! values)))

      (testing "taking a function, initial value and collection"
        (is= [[1] [2] [3]]
             (reduce conj [] values)
             (bp/trans-reduce conj! [] values))))))

(deftest dissoc-in-test

  (testing "dissoc-in"

    (testing "dissociates paths from a nested map"
      (is= {:a 1}
           (bp/dissoc-in {:a 1 :b 1} [:b]))
      (is= {:a {:b 1}}
           (bp/dissoc-in {:a {:b 1 :c 1}} [:a :c]))
      (is= {:a {:b 1}
            :d {:e 1}}
           (bp/dissoc-in {:a {:b 1 :c 1}
                          :d {:e 1
                              :f 1}
                          :g 1}
             [:a :c]
             [:d :f]
             [:g])))

    (testing "ignores path if not found"
      (let [m {:a {:b 1}}]
        (is (identical? m (bp/dissoc-in m [])))
        (is (identical? m (bp/dissoc-in m [:a :c])))
        (is (identical? m (bp/dissoc-in m [:a :b :c])))
        (is (identical? m (bp/dissoc-in m [:c])))
        (is (identical? m (bp/dissoc-in m [:a :c] [:a :b :c] [:c] [])))))

    (testing "removes empty collections along the paths"
      (is= {:a 1}
           (bp/dissoc-in {:a 1 :b {:c 1}} [:b :c]))
      (is= {}
           (bp/dissoc-in {:a {:b 1}} [:a :b])))))

(deftest rename-keys!-test

  (testing "rename-keys!"

    (testing "renames keys in a transient map"
      (is= {:a 1 :b 1 :c 1}
           (-> {:x 1 :z 1 :c 1} transient (bp/rename-keys! {:x :a :z :b}) persistent!))

      (testing "if the map contains the old key"
        (is= {:a 1 :b 1}
             (-> {:a 1 :b 1} transient (bp/rename-keys! {:x :a :z :b}) persistent!))))))

(deftest concat!-test

  (testing "concat!"

    (testing "is a function"
      (is (fn? bp/concat!))

      (testing "that concatenates sequences onto a transient collection"
        (let [concat (fn [& seqs]
                       (persistent! (apply bp/concat! (transient []) seqs)))]
          (is= [1 2 3]
               (concat [1 2 3])
               (concat [1] [2 3])
               (concat [1] [2] [3])))))))

(deftest update-some-test
  (let [m {:a 1 :b 2 :c nil :d false}]

    (testing "if the value is already nil, nothing happens"
      (is (identical? m (bp/update-some m :c inc))))

    (testing "if the value can be false"
      (is (some? false))
      (is (= (assoc m :d true)
             (bp/update-some m :d not))))

    (testing "Updates value at a specified key in the map"
      (is (= (assoc m :b 3)
             (bp/update-some m :b inc))))

    (testing "allows extra args where the value is first"
      (is (= (assoc m :a 6)
             (bp/update-some m :a (fn [v & args]
                                    (is= 1 v)
                                    (is= [2 3] args)
                                    (apply + v args))
               2 3))))

    (testing "doesn't append the value if the resulting function is nil"
      (is (= (dissoc m :b)
             (bp/update-some m :b (constantly nil)))))))

(deftest diff-test

  (testing "diff does a nested map diff"

    (testing "taking a left predicate and two maps"
      (is (= {}
             (bp/diff :a {} {}))))

    (testing "returns a map that could contain :added, :changed, :removed, and :same"
      (let [diff (partial bp/diff number?)]
        (is (= {:added {[:a] 1}}
               (diff {} {:a 1})))
        (is (= {:removed [[:a]]}
               (diff {:a 1} {})))
        (is (= {:changed {[:a] 2}}
               (diff {:a 1} {:a 2})))
        (is (= {:same [[:a]]}
               (diff {:a 1} {:a 1})))
        (is (= {:added   {[:b] 1}
                :changed {[:d] 2}
                :same    [[:a]]
                :removed [[:c]]}
               (diff
                 {:a 1 :c 1 :d 1}
                 {:a 1 :b 1 :d 2}))))
      (is (= {:added {nil {:b 1}}}
             (bp/diff :b nil {:b 1})
             (bp/diff :b {} {:b 1})))
      (is= {:changed {[:a :b 1] 3}
            :same    [[:a :b 0]
                      [:a :b 2]]}
           (bp/diff
             {:a {:b [0 1 2]}}
             {:a {:b [0 3 2]}}))
      (is= {:added {[:a :b :cheese] [1 2 3]}}
           (bp/diff
             {:a {:b {:c 1
                      :d 4}
                  :e 3}
              :d 2}
             {:a {:b {:c      1
                      :cheese [1 2 3]
                      :d      4}
                  :e 3}
              :d 2})))

    (testing "works with large datasets"
      (let [stay-when (comp (bp/p= :Feature) :type)
            old nil
            leaf {:type :Feature, :geometry [[0 1] [1 2]]}
            new {"1" {"2" {:marker leaf}
                      "3" {:marker    leaf
                           :route     leaf
                           :waypoints leaf}
                      "4" {:marker    leaf
                           :route     leaf
                           :waypoints leaf}
                      "5" {:marker    leaf
                           :route     leaf
                           :waypoints leaf}
                      "6" {:marker    leaf
                           :route     leaf
                           :waypoints leaf}}}
            expected (hash-set
                       ["1" "2" :marker]
                       ["1" "3" :marker]
                       ["1" "3" :route]
                       ["1" "3" :waypoints]
                       ["1" "4" :marker]
                       ["1" "4" :route]
                       ["1" "4" :waypoints]
                       ["1" "5" :marker]
                       ["1" "5" :route]
                       ["1" "5" :waypoints]
                       ["1" "6" :marker]
                       ["1" "6" :route]
                       ["1" "6" :waypoints])
            actual (keys (:added (bp/diff stay-when old new)))]
        (is (= (count expected) (count actual)))
        (is (every? expected actual))))))


(deftest map-leaves-test

  (testing "map-leaves"

    (testing "is a function"
      (is (fn? bp/map-leaves))
      (let [mapping-fn (comp (juxt first (comp inc second)) vector)]

        (testing "that traverses over a collection, applying f to the leaves"
          (let [map-leaves (partial bp/map-leaves mapping-fn)]
            (is= [[0] 1] (map-leaves [1]))
            (is= [[[:g] 1]
                  [[:a] 2]
                  [[:b :c] 3]
                  [[:b :d] 4]
                  [[:b :e :f] 5]
                  [[:h :f] 23]]
                 (map-leaves {:g 0
                              :a 1
                              :b {:c 2
                                  :d 3
                                  :e {:f 4}}
                              :h {:f 22}}))
            (is= [[[:a] 2]
                  [[:b :c] 3]
                  [[:b :d] 4]
                  [[:b :e :f] 5]]
                 (map-leaves {:a 1
                              :b {:c 2
                                  :d 3
                                  :e {:f 4}}}))
            (is= [[[0] 2]
                  [[1 0] 5]
                  [[1 1] 6]
                  [[1 2 0] 7]
                  [[1 3 0] 9]
                  [[2] 10]]
                 (map-leaves [1 [4 5 [6] [8]] 9])))

          (testing "with a leaf predicate"
            (is= [[[0] 2]
                  [[2] 4]
                  [[3 1] 6]
                  [[3 3 0] 8]
                  [[4] 10]]
                 (bp/map-leaves mapping-fn #(and (number? %) (odd? %)) [1 2 3 [4 5 [6] [7 8]] 9]))
            (is= [[[:b :e] {:f 4}]
                  [[:h] {:f 22}]]
                 (bp/map-leaves (partial vector) :f {:g 0
                                                     :a 1
                                                     :b {:c 2
                                                         :d 3
                                                         :e {:f 4}}
                                                     :h {:f 22}}))))))))

(deftest reduce-leaves-test

  (testing "reduce-leaves"

    (testing "is a function"
      (is (fn? bp/reduce-leaves))

      (testing "that traverses over a collection, reducing over the leaves"
        (is= 6
             (bp/reduce-leaves + [1])
             (bp/reduce-leaves + [1 2 3])
             (bp/reduce-leaves + [1 [2 [3]]]))

        (testing "with an init"
          (is false))

        (testing "with a leaf predicate"
          (is false))))))
