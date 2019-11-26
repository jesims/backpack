(ns io.jesi.backpack.clojure-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.test.strict :refer :all])
  (:import
    (java.net URI)
    (java.util HashMap)))

(deftest ->uri-test
  (testing "Converts URI and strings into URI objects"
    (let [uri "https://www.thefactsite.com/2010/09/300-random-animal-facts.html"
          u (new URI uri)]
      (is= u (bp/->uri u))
      (is= u (bp/->uri uri))
      (is (identical? u (bp/->uri u)))
      (is (uri? (bp/->uri "asdf")))))

  (testing "Returns nil if the URI is invalid"
    (is (nil? (bp/->uri 123)))
    (is (nil? (bp/->uri true)))))

(deftype TestType [val loud?])
(def ^:private def-test-type (partial bp/defkw-type ->TestType))
(def-test-type ::tested false)
(def-test-type ::and-worked false)
(def-test-type :LOTS_OF_YELLING true)

(deftest defkw-type-test
  (testing "Registers keyword as type"
    ;FIXME fails in test refresh (but not the first time it runs)
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
