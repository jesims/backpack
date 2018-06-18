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

(deftest assoc-changed!-test
  (let [a (atom {})
        spy (atom 0)
        path [:this :value]
        reset #(do
                 (reset! a (or % {}))
                 (reset! spy 0))
        is-not-called? #(is (zero? @spy))
        is-called-once? #(is (= 1 @spy))
        is-equal? #(is (= % @a))]
    (add-watch a :watcher (fn [& _] (swap! spy inc)))

    (testing "is a function"
      (is (fn? bp/assoc-changed!)))

    (testing "works like assoc"
      (testing "swaps the key value if different"
        (reset {})
        (is-not-called?)
        (bp/assoc-changed! a :this "val")
        (is-equal? {:this "val"})
        (is-called-once?)))

    (testing "won't invoke swap if the values are already the same"
      (reset {:this "val"})
      (is-not-called?)
      (bp/assoc-changed! a :this "val")
      (is-equal? {:this "val"})
      (is-not-called?))

    (testing "Can clear a value by setting it to nil"
      (reset {:this "val"})
      (is-not-called?)
      (bp/assoc-changed! a :this nil)
      (is-equal? {:this nil})
      (is-called-once?))

    (testing "works like assoc-in"
      (testing "swaps the key value if different"
        (reset {})
        (is-not-called?)
        (bp/assoc-changed! a path "val")
        (is-equal? {:this {:value "val"}})
        (is-called-once?))

      (testing "won't invoke swap if the values are already the same"
        (let [expected {:this {:value "val"}}]
          (reset expected)
          (is-not-called?)
          (bp/assoc-changed! a path "val")
          (is-equal? expected)
          (is-not-called?)))

      (testing "Can clear a value by setting it to nil"
        (reset {:this {:value "val"}})
        (is-not-called?)
        (bp/assoc-changed! a path nil)
        (is-equal? {:this {:value nil}})
        (is-called-once?)))))