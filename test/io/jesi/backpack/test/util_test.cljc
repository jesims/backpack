(ns io.jesi.backpack.test.util-test
  (:require
    [clojure.test :refer :all]
    [io.jesi.backpack.test.macros :refer [is=]]
    [io.jesi.backpack.test.util :as util]))

#?(:clj
   (deftest wait-for-test

     (testing "wait-for"

       (testing "exits the moment a function is truthy"
         (binding [util/*sleep* #(throw (Exception. "Shouldn't be Called"))]
           (let [actual (util/wait-for (constantly true))]
             (is (true? actual)))))

       (testing "invokes the function every interval until truthy"
         (let [sleep-invoke (atom 0)
               f-invoke (atom 0)
               expected-interval (+ 200 (rand-int 100))]
           (binding [util/*sleep* (fn [duration]
                                    (swap! sleep-invoke inc)
                                    (is= expected-interval duration))]
             (let [actual (util/wait-for
                            #(and
                               (= 4 (swap! f-invoke inc))
                               (= 3 @sleep-invoke))
                            expected-interval)]
               (is (true? actual))))))

       (testing "nil if the timeout expires and f is never truthy"
         (let [expected-interval 100]
           (binding [util/*sleep* (fn [duration] (is= expected-interval duration))]
             (let [actual (util/wait-for (constantly false) 100 200)]
               (is (nil? actual)))))))))
