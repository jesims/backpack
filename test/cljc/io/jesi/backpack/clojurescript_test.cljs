(ns io.jesi.backpack.clojurescript-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.clojurescript :as bp]))

(deftest clj->jskw-test

  (testing "should leave numeric values unchanged"
    (let [m {:number/value 1234}
          actual (->> m bp/clj->jskw js/JSON.stringify)]
      (is (= "{\"number/value\":1234}" actual))))

  (testing "should convert a map with nested keys"
    (let [m {:some/ns {:map/with {:nested/keys "has a value"}}}
          actual (->> m bp/clj->jskw js/JSON.stringify)]
      (is (= "{\"some/ns\":{\"map/with\":{\"nested/keys\":\"has a value\"}}}" actual)))))

(defn- json= [& args]
  (apply = (map js/JSON.stringify args)))

(let [js (clj->js {:aCat {:aHat true}})
      clj {:a-cat {:a-hat true}}]

  (deftest js->clj-test

    (testing "converts all keys to kebab-case"
      (is (= clj (-> js bp/js->clj))))

    (testing "end-to-end clj->js->clj"
      (is (= clj (-> clj bp/clj->js bp/js->clj)))))

  (deftest clj->js-test

    (testing "converts all keys to camelCase"
      (is (json= js (-> clj bp/clj->js))))

    (testing "end-to-end js->clj->js"
      (is (json= js (-> js bp/js->clj bp/clj->js))))))
