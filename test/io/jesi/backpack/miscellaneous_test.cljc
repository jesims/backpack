(ns io.jesi.backpack.miscellaneous-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.strict :refer [= are deftest is is= testing]])
  #?(:clj (:import
            (clojure.lang Named))))

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
      (is= id (bp/->uuid-or-not id))
      (is (identical? id (bp/->uuid-or-not id)))))

  (testing "converts uuid-str to uuid"
    (let [id (rnd/uuid)]
      (is= id (bp/->uuid-or-not (str id)))))

  (testing "doesn't convert non uuid strings"
    (let [id "just a string"]
      (is= id (bp/->uuid-or-not id)))))


(deftest collify-test

  (testing "collify"

    (testing "wraps a value in a vector if it's not a collection"
      (is= [1] (bp/collify 1))
      (is= [1] (bp/collify [1]))
      (is= [1 2] (bp/collify [1 2]))
      (let [fact "Giraffes have no vocal chords"]
        (is= [fact] (bp/collify fact))))

    (testing "returns nil for nil"
      (is (nil? (bp/collify)))
      (is (nil? (bp/collify nil))))))

(deftest named?-test

  (testing "named?"

    (testing "is a function"
      (is (fn? bp/named?)))

    (testing "returns `true` if value is"

      (testing "a string"
        (is (bp/named? "hello")))

      (testing "implement Named"
        (are [v]
          (is (bp/named? v))
          'hello
          :hello
          #?(:cljs (reify INamed
                     (-name [_]))
             :clj  (reify Named
                     (getName [_])
                     (getNamespace [_]))))))

    (testing "returns `false` for other types"
      (are [x]
        (is (false? (bp/named? x)))
        nil
        1
        {}
        []))))
