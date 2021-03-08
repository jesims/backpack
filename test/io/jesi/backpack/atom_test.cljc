(ns io.jesi.backpack.atom-test
  (:require
    [io.jesi.backpack.atom :as atom]
    [io.jesi.customs.strict :refer [deftest is is= testing]]))

(deftest assoc!-test

  (testing "assoc!"

    (testing "assocs into an atom"
      (let [a (atom nil)]
        (is= {:a 1}
             (atom/assoc! a :a 1)
             @a)))))

(deftest assoc-in!-test

  (testing "assoc-in!"

    (testing "assoc-in into an atom"
      (let [a (atom nil)]
        (is= {:a {:b 1}}
             (atom/assoc-in! a [:a :b] 1)
             @a)))))

(deftest assoc-some!-test

  (testing "assoc-some!"

    (testing "assoc-some into an atom"
      (let [a (atom nil)]
        (is= {:a 1}
             (atom/assoc-some! a :a 1 :b nil)
             @a)))))

(deftest dissoc!-test

  (testing "dissoc!"

    (testing "dissoc into an atom"
      (let [a (atom {:a 1 :b 1 :c 3})]
        (is= {:a 1}
             (atom/dissoc! a :b :c)
             @a)))))

(deftest dissoc-in!-test

  (testing "dissoc-in!"

    (testing "dissoc-in into an atom"
      (let [a (atom {:a {:b {:c 1}
                         :d 2
                         :e 3}})]
        (is= {:a {:d 2}}
             (atom/dissoc-in! a [:a :b :c] [:a :e])
             @a)))))

(deftest update!-test

  (testing "update!"

    (testing "update into an atom"
      (let [a (atom {:a 1})]
        (is= {:a 2}
             (atom/update! a :a inc)
             @a)))))

(deftest merge!-test

  (testing "merge!"

    (testing "merge into an atom"
      (let [a (atom {:a 1})]
        (is= {:a 1 :b 2 :c 3}
             (atom/merge! a {:b 2} {:c 3})
             @a)))))

(deftest conj!-test

  (testing "conj!"

    (testing "conj into an atom"
      (let [a (atom [1])]
        (is= [1 2 3]
             (atom/conj! a 2 3)
             @a)))))

(deftest toggle!-test

  (testing "toggle!"

    (testing "toggles the value of an atom"
      (is= true
           (atom/toggle! (atom false))
           (swap! (atom false) not))
      (is= false
           (atom/toggle! (atom true))
           (swap! (atom true) not)))))

(deftest assoc-changed!-test
  (let [a (atom {})
        spy (atom 0)
        path [:this :value]
        reset #(do
                 (reset! a (or % {}))
                 (reset! spy 0))
        is-not-called? #(is (zero? @spy))
        is-called-once? #(is= 1 @spy)
        is-equal? #(is= % @a)]
    (add-watch a :watcher (fn [& _] (swap! spy inc)))

    (testing "is a function"
      (is (fn? atom/assoc-changed!)))

    (testing "works like assoc"
      (testing "swaps the key value if different"
        (reset {})
        (is-not-called?)
        (atom/assoc-changed! a :this "val")
        (is-equal? {:this "val"})
        (is-called-once?)))

    (testing "won't invoke swap if the values are already the same"
      (reset {:this "val"})
      (is-not-called?)
      (atom/assoc-changed! a :this "val")
      (is-equal? {:this "val"})
      (is-not-called?))

    (testing "Can clear a value by setting it to nil"
      (reset {:this "val"})
      (is-not-called?)
      (atom/assoc-changed! a :this nil)
      (is-equal? {:this nil})
      (is-called-once?))

    (testing "works like assoc-in"
      (testing "swaps the key value if different"
        (reset {})
        (is-not-called?)
        (atom/assoc-changed! a path "val")
        (is-equal? {:this {:value "val"}})
        (is-called-once?))

      (testing "won't invoke swap if the values are already the same"
        (let [expected {:this {:value "val"}}]
          (reset expected)
          (is-not-called?)
          (atom/assoc-changed! a path "val")
          (is-equal? expected)
          (is-not-called?)))

      (testing "Can clear a value by setting it to nil"
        (reset {:this {:value "val"}})
        (is-not-called?)
        (atom/assoc-changed! a path nil)
        (is-equal? {:this {:value nil}})
        (is-called-once?))

      (testing "Works with collection values"
        (reset {:this [1 2]})
        (is-not-called?)
        (atom/assoc-changed! a :this [1 2])
        (is-not-called?))

      (testing "Works with nested collection values"
        (reset {:this [1 {:a 2}]})
        (is-not-called?)
        (atom/assoc-changed! a :this [1 2])
        (is-equal? {:this [1 2]})
        (is-called-once?))

      (testing "Works with default empty value"
        (reset {:this []})
        (is-not-called?)
        (atom/assoc-changed! a :this (concat [1] [2]))
        (is-equal? {:this [1 2]})
        (is-called-once?))

      (testing "allows providing multiple kvs"
        (reset {:this []})
        (is-not-called?)
        (atom/assoc-changed! a
          :this :that
          [:something :else] "value"
          :range (range 0 5)
          [:meaning :of :life] 42)
        (is-equal? {:this      :that
                    :range     [0 1 2 3 4]
                    :something {:else "value"}
                    :meaning   {:of {:life 42}}})
        (is-called-once?)))))
