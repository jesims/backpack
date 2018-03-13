(ns io.jesi.backpack.util-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [com.rpl.specter :as sp]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.backpack.util :as u]))

(deftest uuid-str?-test
  (testing "uuid-str? is a UUID string"
    (is (u/uuid-str? "2c80c3ca-535c-4706-bea2-afd2a2bf374d"))
    (is (not (u/uuid-str? "1234")))
    (is (not (u/uuid-str? 1234)))
    (is (not (u/uuid-str? nil)))))

(deftest safe-empty?-test
  (testing "returns true when empty coll/string/map"
    (is (u/safe-empty? []))
    (is (u/safe-empty? '()))
    (is (u/safe-empty? ""))
    (is (u/safe-empty? {})))

  (testing "returns false when non empty coll/string/map"
    (is (false? (u/safe-empty? ["value"])))
    (is (false? (u/safe-empty? '("value"))))
    (is (false? (u/safe-empty? "value")))
    (is (false? (u/safe-empty? {:value true}))))

  (testing "returns true when nil"
    (is (u/safe-empty? nil)))

  (testing "returns false when non-nil"
    (is (false? (u/safe-empty? 1234)))
    (is (false? (u/safe-empty? (rnd/uuid))))))

(deftest map-walker-test
  (let [select-one #(sp/select-one u/map-walker %)]

    (testing "walks over maps"
      (is (= {} (select-one {})))
      (is (= {:a 1} (select-one {:a 1}))))

    (testing "does not include non maps"
      (is nil? (select-one []))
      (is nil? (select-one 1))
      (is nil? (select-one [1 [2] "3" nil {}])))

    (testing "walks over nested maps"
      (let [c {:c 2}
            m {:a 1 :b c :d [1]}]
        (is (= #{m c} (set (sp/select u/map-walker m))))))

    (testing "walks over maps in other collections"
      (is (= {} (sp/select-one [sp/ALL u/map-walker] [1 [2] "3" nil {}]))))))

(deftest no-empty-values-test
  (testing "returns the map without empty values"
    (is (= {:str "value" :int 123 :vec ["value"]}
           (u/no-empty-values {:str       "value"
                               :empty-str ""
                               :vec       ["value"]
                               :empty-vec []
                               :int       123
                               :nil       nil}))))
  (testing "runs on nested maps"
    (is (= {:map {:str-vec ["string"]}}
           (u/no-empty-values {:empty-map {}
                               :map       {:str-vec ["string"]
                                           :nil     nil
                                           :map     {:empty-str ""}}}))))
  (testing "returns nil when empty"
    (is (nil? (u/no-empty-values {:str ""})))))

(deftest distinct-by-test
  (testing "returns true if a collection of maps are distinct by a given keyword"
    (let [generator (fn [level] {:val (rnd/string) :type "animal" :danger-level level})
          maps (map (partial generator) (range 5))]
      (is (true? (u/distinct-by :val maps)))
      (is (true? (u/distinct-by :danger-level maps)))
      (is (false? (u/distinct-by :type maps))))))

(deftest in?-test
  (testing "true if a collection contains a given value"
    (let [col ["An" "ostrich's" "eye" "is" "bigger" "than" "it's" "brain"]]
      (is (true? (u/in? col "An")))
      (is (true? (u/in? (set col) "An")))
      (is (false? (u/in? col "an")))
      (is (false? (u/in? col "bear"))))))

(deftest partial-right-test
  (testing "partial-right"

    (testing "is a function"
      (is (ifn? u/partial-right)))

    (testing "returns a function"
      (is (ifn? (u/partial-right nil))))

    (let [identity (fn [& args] args)]
      (testing "returns the provided function if not args"
        (is (= [1 2 3] ((u/partial-right identity) 1 2 3))))

      (testing "partially applies parameters from the right"
        (is (= [1 2 3] ((u/partial-right identity 3) 1 2)))
        (is (= [1 2 3] ((u/partial-right identity 2 3) 1)))
        (is (= [1 2 3] ((u/partial-right identity 1 2 3))))))))
