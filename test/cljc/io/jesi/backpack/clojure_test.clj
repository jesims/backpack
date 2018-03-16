(ns io.jesi.backpack.clojure-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp])
  (:import
    (java.net URI)))

(deftest ->uri-test
  (testing "Converts URI and strings into URI objects"
    (let [uri "https://www.thefactsite.com/2010/09/300-random-animal-facts.html"
          u (new URI uri)]
      (is (= u (bp/->uri u)))
      (is (= u (bp/->uri uri)))
      (is (identical? u (bp/->uri u)))
      (is (uri? (bp/->uri "asdf")))))

  (testing "Returns nil if the URI is invalid"
    (is (nil? (bp/->uri 123)))
    (is (nil? (bp/->uri true)))))

(deftype TestType [val])
(def def-kw-type (partial bp/kw-type ->TestType))
(def-kw-type ::tested)
(def-kw-type ::and-worked)

(deftest kw-type-test
  (testing "Registers keyword as type"
    (is (instance? TestType tested))
    (is (instance? TestType and-worked))))
