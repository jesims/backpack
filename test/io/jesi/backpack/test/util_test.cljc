(ns io.jesi.backpack.test.util-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]]
    [io.jesi.backpack.test.util :as util]
    [io.jesi.backpack.random :as rnd]
    #?(:cljs [cljs.test :refer [async]]))
  #?(:clj (:import
            (clojure.lang ExceptionInfo))))

(deftest wait-for-early-exit-test

  (testing "wait-for"

    (testing "exits the moment a function is truthy"
      #?(:clj  (binding [util/*sleep* #(throw (Exception. "Shouldn't be Called"))]
                 (let [actual (util/wait-for (constantly true))]
                   (is (true? actual))))
         :cljs (do
                 (let [captor (atom nil)
                       expected (rnd/string)]
                   (is (nil? (util/wait-for #(do (reset! captor expected)
                                                 true))))
                   (is= expected @captor)))))))

(deftest wait-for-interval-test

  (testing "wait-for"

    (testing "invokes the function every interval until truthy"
      (let [sleep-invoke (atom 0)
            f-invoke (atom 0)
            expected-interval (+ 200 (rand-int 100))]
        #?(:clj  (binding [util/*sleep* (fn [duration]
                                          (swap! sleep-invoke inc)
                                          (is= expected-interval duration))]
                   (let [actual (util/wait-for
                                  #(and
                                     (= 5 (swap! f-invoke inc))
                                     (= 3 @sleep-invoke))
                                  expected-interval)]
                     (is (true? actual))))
           :cljs (async done
                   (let [actual (util/wait-for
                                  #(and
                                     (= 5 (swap! f-invoke inc))
                                     (= 3 @sleep-invoke))
                                  expected-interval)]
                     (is (true? actual)))
                   (is false)
                   (done)))))))

(deftest wait-for-timeout-test

  (testing "wait-for"

    (testing "nil if the timeout expires and f is never truthy"
      (let [expected-interval 100]
        #?(:clj  (binding [util/*sleep* (fn [duration] (is= expected-interval duration))]
                   (is (thrown-with-msg? ExceptionInfo #"Wait timeout" (util/wait-for (constantly false) 100 200))))
           :cljs (is false))))))
