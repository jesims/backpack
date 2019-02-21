(ns io.jesi.backpack.json-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

(def clj {:base-url "https://"
          :v2       "is better than v1"
          :actions  [{:name "Next" :type "GET"}]
          :geojson  {:type :LineString}
          :a-cat    {:a-hat true}})

;Keyword values are not converted
(def from-js->clj (update-in clj [:geojson :type] name))

(def json "{\"baseUrl\":\"https://\"}")

(def simple {:base-url "https://"})

(deftest clj->json-test

  (testing "is a function"
    (is (fn? bp/clj->json)))

  (testing "converts Clojure to JSON string"
    (is (= json
           (bp/clj->json simple)))

    (testing "end to end"
      (is (= from-js->clj
             (-> clj bp/clj->json bp/json->clj))))))

(deftest json->clj-test

  (testing "is a function"
    (is (fn? bp/json->clj)))

  (testing "converts JSON strings to Clojure"
    (is (= simple
           (bp/json->clj json))))

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

(deftest clj->json->clj

  (testing "Can convert Clojure to JSON and back"
    (is (= from-js->clj
           (-> clj bp/clj->json bp/json->clj)))))
