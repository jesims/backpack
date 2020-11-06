(ns io.jesi.backpack.miscellaneous-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.strict :refer [= are deftest is is= testing]])
  #?(:clj
     (:import
       (clojure.lang Named)
       (java.net URI))))

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

(deftest ->uri-test

  (testing "Converts URI and strings into URI objects"
    (let [uri "https://www.thefactsite.com/2010/09/300-random-animal-facts.html"
          expected #?(:clj (new URI uri)
                      :cljs (goog.Uri. uri))]
      (is (uri? expected))
      #?(:clj  (do
                 (is= expected (bp/->uri expected))
                 (is= expected (bp/->uri uri))
                 (is (identical? expected (bp/->uri expected))))
         :cljs (is= uri
                    (-> uri bp/->uri str)
                    (-> expected str)
                    (-> expected bp/->uri str)))
      (is (uri? (bp/->uri "asdf")))))

  (testing "Returns nil if the URI is invalid"
    (is (nil? (bp/->uri 123)))
    (is (nil? (bp/->uri true)))))

(deftest xor-test

  (testing "xor"
    (is (nil? (bp/xor)))
    (is (true? (bp/xor true)))
    (is (false? (bp/xor false)))
    (is (true? (bp/xor false true)))
    (is (true? (bp/xor true false)))
    (is (true? (bp/xor true false false)))
    (is (true? (bp/xor false false false true)))
    (is (false? (bp/xor true false true)))))
