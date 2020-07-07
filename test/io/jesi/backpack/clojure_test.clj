(ns io.jesi.backpack.clojure-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.customs.strict :refer :all])
  (:import
    (java.util HashMap)))

(deftype TestType [val loud?])
(def ^:private def-test-type (partial bp/defkw-type ->TestType))
(def-test-type ::tested false)
(def-test-type ::and-worked false)
(def-test-type :LOTS_OF_YELLING true)

(deftest defkw-type-test

  (testing "Registers keyword as type"
    ;TODO fails in test refresh (but not the first time it runs)
    (is (instance? TestType tested))
    (is (instance? TestType and-worked))
    (is (instance? TestType lots-of-yelling)))

  (testing "Applies the arguments to the type constructor"
    (is= ::tested (.val tested))
    (is= ::and-worked (.val and-worked))
    (is= :LOTS_OF_YELLING (.val lots-of-yelling))
    (is (.loud? lots-of-yelling))))

(deftest java->clj-test

  (testing "converts keys in the maps to keywords"
    (let [map-1 {"a" 1}
          expected-1 {:a 1}
          map-2 {"camelCasedKey" 123
                 "a"             {"b" {"c" 1}}}
          expected-2 {:camel-cased-key 123
                      :a               {:b {:c 1}}}
          java-map (doto (HashMap.)
                     (.put "camelCasedKey" 123)
                     (.put "a" (doto (HashMap.)
                                 (.put "b" (doto (HashMap.)
                                             (.put "c" 1))))))]
      (is= nil (bp/java->clj nil))
      (is= expected-1 (bp/java->clj map-1))
      (is= expected-2 (bp/java->clj map-2))
      (is= expected-2 (bp/java->clj java-map)))))
