(ns io.jesi.backpack.walk-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [io.jesi.backpack.fn :refer [call]]
    [io.jesi.backpack.walk :refer [walk]]
    [io.jesi.customs.spy :as spy])
  #?(:clj (:import (clojure.lang PersistentTreeSet))))

;FIXME move to backpack.types ?
(defn sorted-set? [x]
  #?(:clj (instance? PersistentTreeSet x)))

(comment (deftest walk-test

           (testing "walk"

             (testing "is a function"
               (is (fn? walk)))

             (testing "takes simple forms"
               (is (= 1 (walk identity 1)))
               (is (nil? (walk identity nil)))
               (let [v [1 2 3]]
                 (is (= v (walk identity v)))))

             (testing "transforms simple forms"
               (is (= 2 (walk inc 1))))

             (testing "transforms"
               (let [inc-er (fn [form]
                              (if (number? form)
                                (inc form)
                                form))]

                 (testing "vectors"
                   (is (= [2 3 4] (walk inc-er [1 2 3]))))

                 (testing "map values"
                   (is (= {:a 2 :b 3}
                          (walk inc-er {:a 1 :b 2}))))

                 (testing "nested collections breadth first (postwalk) order"
                   (is (= {:a 2 :b 3}
                          (walk
                            (fn [form]
                              (condp call form
                                map? (dissoc form :c)
                                number? (inc form)
                                form))
                            {:a 1 :b 2 :c 9001}))))

                 (testing "maintaining type"
                   (spy/enabled
                     (let [actual (walk inc-er (sorted-set 2 3))]
                       (is (sorted-set? actual))
                       (is (= (sorted-set 3 4)
                              actual))))))))))
