(ns io.jesi.backpack.test.util-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack.random :as rnd]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing]]
    [io.jesi.backpack.test.util :as util])
  #?(:clj (:import
            (clojure.lang ExceptionInfo))))

(defn- capture-set [atom k ret & args]
  (swap! atom assoc k args)
  ret)

(defn- capture-clear [atom k id]
  (swap! atom assoc k [id]))

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
                   (is= expected @captor)))))

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
           :cljs (let [captor (atom nil)
                       [interval-id timeout-id] (repeatedly #(rand-int 1000))
                       result (atom false)]
                   (binding [util/*js-set-timeout* (partial capture-set captor :set-timeout timeout-id)
                             util/*js-clear-timeout* (partial capture-clear captor :clear-timeout)
                             util/*js-set-interval* (partial capture-set captor :set-interval interval-id)
                             util/*js-clear-interval* (partial capture-clear captor :clear-interval)]
                     (util/wait-for #(deref result) expected-interval)
                     (is= #{:set-timeout :set-interval} (set (keys @captor)))

                     (testing "a timeout is set"
                       (let [[actual-ms actual-timeout-fn] (:set-timeout @captor)]
                         (is= 10000 actual-ms)
                         (is (fn? actual-timeout-fn))))

                     (testing "an interval is set"
                       (let [[actual-ms actual-interval-fn] (:set-interval @captor)]
                         (is= expected-interval actual-ms)
                         (is (fn? actual-interval-fn))

                         (testing "cleared when truthy"
                           (actual-interval-fn)
                           (is (false? (contains? @captor :clear-interval)))
                           (is (false? (contains? @captor :clear-timeout)))
                           (reset! result true)
                           (actual-interval-fn)
                           (is= [interval-id] (:clear-interval @captor))
                           (is= [timeout-id] (:clear-timeout @captor))))))))))

    (testing "nil if the timeout expires and f is never truthy"
      (let [expected-interval 100
            expected-timeout 200]
        #?(:clj  (binding [util/*sleep* (fn [duration] (is= expected-interval duration))]
                   (is (thrown-with-msg? ExceptionInfo #"Wait timeout"
                         (util/wait-for (constantly false) expected-interval expected-timeout))))
           :cljs (let [captor (atom nil)
                       [interval-id timeout-id] (repeatedly #(rand-int 1000))
                       capture-set (fn [k ret & args]
                                     (swap! captor assoc k args)
                                     ret)
                       capture-clear (fn [k id]
                                       (swap! captor assoc k [id]))]
                   (binding [util/*js-set-timeout*
                             (partial capture-set :set-timeout timeout-id)
                             util/*js-clear-timeout* (partial capture-clear :clear-timeout)
                             util/*js-set-interval* (partial capture-set :set-interval interval-id)
                             util/*js-clear-interval* (partial capture-clear :clear-interval)]
                     (util/wait-for (constantly false) expected-interval expected-timeout)
                     (is= #{:set-timeout :set-interval} (set (keys @captor)))

                     (testing "a timeout is set"
                       (let [[actual-ms actual-timeout-fn] (:set-timeout @captor)]
                         (is= expected-timeout actual-ms)
                         (is (fn? actual-timeout-fn))

                         (testing "clears the interval and throws an exception"
                           (is (thrown-with-msg? ExceptionInfo #"Wait timeout" (actual-timeout-fn)))
                           (is= [interval-id] (:clear-interval @captor))
                           (is (nil? (:clear-timeout @captor)))))))))))))
