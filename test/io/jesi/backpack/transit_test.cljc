(ns io.jesi.backpack.transit-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [def-]]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]]
    [io.jesi.backpack.test.util :refer [pprint-str]]))

(def- m {"[\"~#'\",\"foo\"]"      "foo"
         "[\"^ \",\"~:a\",[1,2]]" {:a [1 2]}})

(deftest clj->transit-test

  (testing "clj->transit"
    (doseq [[t o] m]
      (is= t (bp/clj->transit o)))))

(deftest transit->clj-test

  (testing "transit->clj"
    (doseq [[t o] m]
      (is= o (bp/transit->clj t)))))

(def- vs (concat
           [nil
            1
            [1]
            {:a [1]}
            '(1 2 3)
            #{1 2}
            (rnd/uuid)
            {:a (rnd/uuid)}]
           (vals m)))

(deftest end-to-end-test
  (doseq [o vs]
    (is= o (-> o bp/clj->transit bp/transit->clj))))
