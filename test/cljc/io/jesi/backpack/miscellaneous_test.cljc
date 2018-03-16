(ns io.jesi.backpack.miscellaneous-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]))

(deftest ->uuid-test
  (testing "Converts the first parameter to a UUID object, or returns ::s/invalid"
    (is (uuid? (bp/->uuid "2c80c3ca-535c-4706-bea2-afd2a2bf374d")))
    (is (uuid? (bp/->uuid (rnd/uuid))))
    (is (nil? (bp/->uuid "1234")))
    (is (nil? (bp/->uuid 1234)))
    (is (nil? (bp/->uuid nil)))))

(deftest ->uuid-or-not-test
  (testing "leaves UUIDs as is"
    (let [id (rnd/uuid)]
      (is (= id (bp/->uuid-or-not id)))
      (is (identical? id (bp/->uuid-or-not id)))))

  (testing "converts uuid-str to uuid"
    (let [id (rnd/uuid)]
      (is (= id (bp/->uuid-or-not (str id))))))

  (testing "doesn't convert non uuid strings"
    (let [id "just a string"]
      (is (= id (bp/->uuid-or-not id))))))
