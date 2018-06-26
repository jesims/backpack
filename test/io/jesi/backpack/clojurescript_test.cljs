(ns io.jesi.backpack.clojurescript-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

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
    (is (= clj (-> clj bp/clj->js bp/js->clj)))))

(deftest clj->js-test

  (testing "is a function"
    (is (fn? bp/clj->js)))

  (testing "converts all keys to camelCase"
    (is (json= js (-> clj bp/clj->js))))

  (testing "end-to-end js->clj->js"
    (is (json= js (-> js bp/js->clj bp/clj->js)))))

(deftest clj->json-test

  (testing "is a function"
    (is (fn? bp/clj->json)))

  (testing "converts ClojureScript to JSON string"
    (is (= (js/JSON.stringify js)
           (bp/clj->json js)))
    (is (= clj
           (-> clj bp/clj->json bp/json->clj)))))

(deftest json->clj-test

  (testing "is a function"
    (is (fn? bp/json->clj)))

  (testing "converts JSON strings to ClojureScript"
    (is (= clj
           (-> clj bp/clj->js js/JSON.stringify bp/json->clj)))
    (let [json (js/JSON.stringify js)]
      (is (= json
             (-> json bp/json->clj bp/clj->json))))))

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
