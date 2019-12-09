(ns io.jesi.backpack.json-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]]))

(def clj {:base-url "https://"
          :v2       "is better than v1"
          :actions  [{:name "Next" :method "GET"}]
          :-links   [{:rel "view/user"}]
          :geojson  #:geojson {:type :LineString}
          :a-cat    {:a-hat true}})

(def json "{\"baseUrl\":\"https://\"}")

(def simple {:base-url "https://"})

(deftest clj->json-test

  (testing "is a function"
    (is (fn? bp/clj->json)))

  (testing "converts Clojure to JSON string"
    (is= json
         (bp/clj->json simple))))

(deftest json->clj-test

  (testing "is a function"
    (is (fn? bp/json->clj)))

  (testing "converts JSON strings to Clojure"
    (is= simple
         (bp/json->clj json)))

  (testing "parses nil and blank"
    (is (nil? (bp/json->clj nil)))
    (is (nil? (bp/json->clj "")))
    (is (nil? (bp/json->clj " ")))
    (is (nil? (bp/json->clj (str \tab \  \newline)))))

  (testing "parses empty and literal values"
    (let [assert-eq (fn [expected s]
                      (is= expected (bp/json->clj s)))]
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
    ;Keyword values are not converted
    (is= (update-in clj [:geojson :geojson/type] name)
         (-> clj bp/clj->json bp/json->clj))))
