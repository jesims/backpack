(ns io.jesi.backpack.specter-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [com.rpl.specter :as sp]
    [io.jesi.backpack :as bp]))

(deftest map-walker-test
  (let [select-one #(sp/select-one bp/map-walker %)]

    (testing "walks over maps"
      (is (= {} (select-one {})))
      (is (= {:a 1} (select-one {:a 1}))))

    (testing "does not include non maps"
      (is nil? (select-one []))
      (is nil? (select-one 1))
      (is nil? (select-one [1 [2] "3" nil {}])))

    (testing "walks over nested maps"
      (let [c {:c 2}
            m {:a 1 :b c :d [1]}]
        (is (= #{m c} (set (sp/select bp/map-walker m))))))

    (testing "walks over maps in other collections"
      (is (= {} (sp/select-one [sp/ALL bp/map-walker] [1 [2] "3" nil {}]))))))

(deftest no-empty-values-test
  (testing "returns the map without empty values"
    (is (= {:str "value" :int 123 :vec ["value"]}
           (bp/no-empty-values {:str       "value"
                                :empty-str ""
                                :vec       ["value"]
                                :empty-vec []
                                :int       123
                                :nil       nil}))))
  (testing "runs on nested maps"
    (is (= {:map {:str-vec ["string"]}}
           (bp/no-empty-values {:empty-map {}
                                :map       {:str-vec ["string"]
                                            :nil     nil
                                            :map     {:empty-str ""}}}))))
  (testing "returns nil when empty"
    (is (nil? (bp/no-empty-values {:str ""})))))

