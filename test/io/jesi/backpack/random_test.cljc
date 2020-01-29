(ns io.jesi.backpack.random-test
  (:refer-clojure :exclude [=])
  (:require
    [clojure.string :as string]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.strict :refer [= deftest is is= testing]]))

(defn- assert-random [fn]
  (let [actuals (take 1000 (repeatedly fn))]
    (is= (count actuals) (count (set actuals)))
    actuals))

(defn- assert-random-size [size fn]
  (let [actual (fn size)]
    (is= size (count actual))))

(deftest uuid-test
  (testing "UUID's are always random"
    (assert-random #(rnd/uuid))))

(deftest uuid-str-test
  (testing "UUID strings are always random"
    (let [actuals (assert-random #(rnd/uuid-str))
          non-uuid-strs (filter (comp not bp/uuid-str?) actuals)]
      (is (nil? (seq non-uuid-strs))))))

(deftest string-test
  (testing "Strings are always random"
    (assert-random #(rnd/string)))

  (testing "Can create a random string of size"
    (assert-random-size 2000 rnd/string)))

(deftest alpha-numeric-test
  (testing "Strings are always random"
    (assert-random #(rnd/alpha-numeric)))

  (testing "Can create a random string of size"
    (assert-random-size 2000 rnd/alpha-numeric)))

(def extended-chars #?(:clj  #'rnd/extended-chars
                       :cljs rnd/extended-chars))

(deftest extended-chars-test
  (testing "Can be converted to upper/lower still equal"
    (is= (string/lower-case (string/upper-case extended-chars))
         (string/lower-case extended-chars))
    (is= (string/upper-case (string/lower-case extended-chars))
         (string/upper-case extended-chars))))

(deftest wkt-linestring-test
  (testing "Contains no scientific notation"
    (assert-random #(rnd/wkt-linestring 2 500))))
