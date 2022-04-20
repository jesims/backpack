(ns io.jesi.backpack.compare-test
  (:require
    [io.jesi.customs.strict :refer [is are deftest testing]]
    [io.jesi.backpack.compare :as comp])
  #?(:clj (:import
            (java.time Instant)
            (java.time.temporal ChronoUnit))))

(defn- test-op [op f]

  (testing "returns true if no args"
    (is (true? (f))))

  (testing "returns true if single argument"
    (are [x] (f x)
      nil
      0
      1
      #?(:clj (Instant/now))))

  (letfn [(compares? [args]
            (= (apply f args)
               (->> args
                    (partition 2 1)
                    (map (partial apply compare))
                    (every? #(op % 0)))))]
    (let [colls [[nil 1]
                 [0 1]
                 ["A" "B"]
                 [(long 0) (long 1)]
                 [(long 0) 1]
                 [0 1 2]
                 [0 1 2 3 4 5]
                 #?@(:clj  [[BigDecimal/ZERO BigDecimal/ONE]
                            [BigDecimal/ZERO 1]
                            [(Instant/now) (-> (Instant/now)
                                               (.plus 1 ChronoUnit/SECONDS))]])]]

      (testing "same for ascending order"
        (doseq [args colls]
          (is (compares? args))))

      (testing "same for descending order"
        (doseq [args colls]
          (is (compares? (reverse args))))))

    (testing "same for equal"
      (are [args] (compares? args)
        [nil nil]
        [0 0]
        #?@(:clj [(let [now (Instant/now)]
                    [now now])])))))

(deftest <-test
  (test-op < comp/<))

(deftest <=-test
  (test-op <= comp/<=))

(deftest =-test
  (test-op = comp/=))

(deftest >-test
  (test-op > comp/>))

(deftest >=-test
  (test-op >= comp/>=))
