(ns io.jesi.backpack.clojurescript-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]))

(deftest clj->jskw-test
  (testing "should leave numeric values unchanged"
    (let [m {:number/value 1234}
          actual (->> m bp/clj->jskw js/JSON.stringify)]
      (is (= "{\"number/value\":1234}" actual))))

  (testing "should convert a map with nested keys"
    (let [m {:some/ns {:map/with {:nested/keys "has a value"}}}
          actual (->> m bp/clj->jskw js/JSON.stringify)]
      (is (= "{\"some/ns\":{\"map/with\":{\"nested/keys\":\"has a value\"}}}" actual)))))
