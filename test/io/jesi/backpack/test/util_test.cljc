(ns io.jesi.backpack.test.util-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]]
    [io.jesi.backpack.test.util :as util]))

(deftest wait-for-test

  (testing "wait-for"

    (testing "exits the moment a function is truthy"
      #?(:clj  (binding [util/*sleep* #(throw (Exception. "Shouldn't be Called"))]
                 (let [actual (util/wait-for (constantly true))]
                   (is (true? actual))))
         :cljs (is false)))

    (testing "invokes the function every interval until truthy"
      (let [sleep-invoke (atom 0)
            f-invoke (atom 0)
            expected-interval (+ 200 (rand-int 100))]
        #?(:clj  (binding [util/*sleep* (fn [duration]
                                          (swap! sleep-invoke inc)
                                          (is= expected-interval duration))]
                   (let [actual (util/wait-for
                                  #(and
                                     (= 4 (swap! f-invoke inc))
                                     (= 3 @sleep-invoke))
                                  expected-interval)]
                     (is (true? actual))))
           :cljs (is false))))

    (testing "nil if the timeout expires and f is never truthy"
      (let [expected-interval 100]
        #?(:clj  (binding [util/*sleep* (fn [duration] (is= expected-interval duration))]
                   (let [actual (util/wait-for (constantly false) 100 200)]
                     (is (nil? actual))))
           :cljs (is false))))))


