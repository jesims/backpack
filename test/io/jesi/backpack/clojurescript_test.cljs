(ns io.jesi.backpack.clojurescript-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [cognitect.transit :as t]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]
    [oops.core :refer [oget]]))

(defn- json= [& args]
  (apply = (map js/JSON.stringify args)))

(def ^:private js (clj->js {:baseUrl "https://"
                            :v2      "is better than v1"
                            :actions [{:name "Next" :type "GET"}]
                            :aCat    {:aHat true}}))
(def ^:private clj {:base-url "https://"
                    :v2       "is better than v1"
                    :actions  [{:name "Next" :type "GET"}]
                    :a-cat    {:a-hat true}})

(defn- nillmap [& keys]
  (zipmap keys (repeat nil)))

(deftest js->clj-test

  (testing "is a function"
    (is (fn? bp/js->clj)))

  (testing "converts all keys to kebab-case"
    (is (= clj (-> js bp/js->clj)))
    (is (= (nillmap :base-url :helios-url)
           (bp/js->clj (nillmap :baseURL :heliosURL)))))

  (testing "end-to-end clj->js->clj"
    (is (= clj (-> clj bp/clj->js bp/js->clj))))

  (testing "returns nil"
    (is (nil? (bp/clj->js nil)))))

(deftest clj->js-test

  (testing "is a function"
    (is (fn? bp/clj->js)))

  (testing "converts all keys to camelCase"
    (is (json= js (-> clj bp/clj->js))))

  (testing "end-to-end js->clj->js"
    (is (json= js (-> js bp/js->clj bp/clj->js))))

  (testing "converts UUIDs to strings"
    (is (string? (-> {:id (random-uuid)}
                     bp/clj->js
                     (oget "id"))))
    (is (string? (bp/clj->js (random-uuid))))))

(deftest clj->json-test

  (testing "is a function"
    (is (fn? bp/clj->json)))

  (testing "converts ClojureScript to JSON string"
    (is (= (js/JSON.stringify js)
           (bp/clj->json js)))
    (is (= clj
           (-> clj bp/clj->json bp/json->clj)))))

(deftest uuid-conversion-test
  (let [json-round-trip (comp bp/json->clj bp/clj->json)
        js-round-trip (comp bp/js->clj bp/clj->js)]
    (testing "cljs uuids"
      (let [id (rnd/uuid)]
        (is (= (str id) (js-round-trip id)))
        (is (= (str id) (json-round-trip id)))))

    (testing "transit uuids"
      (let [id (t/uuid (rnd/uuid-str))]
        (is (= (str id) (js-round-trip id)))
        (is (= (str id) (json-round-trip id)))))))

(deftest json->clj-test

  (testing "is a function"
    (is (fn? bp/json->clj)))

  (testing "converts JSON strings to ClojureScript"
    (is (= clj
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

(deftype TestClass [f1 f2]
  Object)

(deftest class->clj-test
  (let [m {:f1 1 :f2 2}
        o (clj->js m)]

    (testing "converts simple JS objects"
      (is (= m (bp/class->clj o))))

    (testing "converts JS classes"
      (is (= m (bp/class->clj (TestClass. 1 2)))))

    (testing "converts properties from prototype"
      (is (= (assoc m :f3 3)
             (bp/class->clj (js/Object.create o (clj->js {:f3 {:value 3 :enumerable true}}))))))))
