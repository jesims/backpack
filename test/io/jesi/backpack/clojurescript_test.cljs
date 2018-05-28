(ns io.jesi.backpack.clojurescript-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

(defn- json= [& args]
  (apply = (map js/JSON.stringify args)))

(def ^:private js (clj->js {:baseUrl  "https://"
                            :v2       "is better than v1"
                            :_actions [{:name "Next" :type "GET"}]
                            :aCat     {:aHat true}}))
(def ^:private clj {:base-url "https://"
                    :v2       "is better than v1"
                    :-actions [{:name "Next" :type "GET"}]
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
