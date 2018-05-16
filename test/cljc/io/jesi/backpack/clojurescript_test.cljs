(ns io.jesi.backpack.clojurescript-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

(defn- json= [& args]
  (apply = (map js/JSON.stringify args)))

(def ^:private jso (clj->js {:aCat {:aHat true}}))
(def ^:private cljo {:a-cat {:a-hat true}})

(deftest js->clj-test

  (testing "is a function"
    (is (fn? bp/js->clj)))

  (testing "converts all keys to kebab-case"
    (is (= cljo (-> jso bp/js->clj))))

  (testing "end-to-end clj->js->clj"
    (is (= cljo (-> cljo bp/clj->js bp/js->clj)))))

(deftest clj->js-test

  (testing "is a function"
    (is (fn? bp/clj->js)))

  (testing "converts all keys to camelCase"
    (is (json= jso (-> cljo bp/clj->js))))

  (testing "end-to-end js->clj->js"
    (is (json= jso (-> jso bp/js->clj bp/clj->js)))))

(deftest clj->json-test

  (testing "is a function"
    (is (fn? bp/clj->json)))

  (testing "converts ClojureScript to JSON string"))

(deftest json->clj-test

  (testing "is a function"
    (is (fn? bp/json->clj)))

  (testing "converts JSON strings to ClojureScript"))

