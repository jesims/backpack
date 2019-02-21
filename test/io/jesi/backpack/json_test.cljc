(ns io.jesi.backpack.json-test
  (:require
   [clojure.test :refer [deftest testing is]
   [io.jesi.backpack :as bp]))

(deftest clj->json-test

  (testing "is a function"
    (is (fn? bp/clj->json)))

  (testing "converts ClojureScript to JSON string"
    (is (= (js/JSON.stringify js)
           (bp/clj->json js)))
    (is (= from-js->clj
           (-> clj bp/clj->json bp/json->clj)))))


(deftest json->clj-test

  (testing "is a function"
    (is (fn? bp/json->clj)))

  (testing "converts JSON strings to ClojureScript"
    (is (= from-js->clj
           (-> clj bp/clj->js js/JSON.stringify bp/json->clj)))
    (let [json (js/JSON.stringify js)]
      (is (= json
             (-> json bp/json->clj bp/clj->json)))))

  (testing "parses nil and blank"
    (is (nil? (bp/json->clj nil)))
    (is (nil? (bp/json->clj "")))
    (is (nil? (bp/json->clj " ")))
    (is (nil? (bp/json->clj (str \tab \  \newline)))))

  (testing "parses empty and literal values"
    (let [assert-eq (fn [expected s]
                      (is (= expected (bp/json->clj s))))]
      (assert-eq {} "{}")
      (assert-eq [] "[]")
      (assert-eq nil "null")
      (assert-eq true "true")
      (assert-eq false "false")
      (assert-eq "" "\"\"")
      (assert-eq 3.14 "3.14")
      (assert-eq 42 "42"))))
