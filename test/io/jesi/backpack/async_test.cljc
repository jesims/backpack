(ns io.jesi.backpack.async-test
  (:require
    [clojure.core.async :as core-async]
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.test.macros :refer [async-go]]))

(deftest closed?-test

  (testing "closed?"

    (testing "is a function"
      (is (fn? async/closed?)))

    (testing "returns"

      (testing "false if the channel is open"
        (let [chan (async/chan)]
          (is (false? (async/closed? chan)))))

      (testing "true if the channel is"

        (testing "nil"
          (is (true? (async/closed? nil))))

        (testing "closed"
          (let [chan (async/close! (async/chan))]
            (is (true? (async/closed? chan)))))))))

(deftest when-open-test
  (async-go

    (testing "when-open executes the body when the channel is open"
      (let [channel (async/chan)]
        (is (false? (async/closed? channel)))
        (async/when-open channel
          (core-async/put! channel true))
        (is (false? (async/closed? channel)))
        (is (true? (async/<? channel)))))))

(deftest when-open-test-closed
  (async-go

    (testing "when-open does nothing when the channel is closed"
      (let [channel (async/chan)]
        (async/close! channel)
        (is (async/closed? channel))
        (async/when-open channel
          (core-async/put! channel true))
        (is (async/closed? channel))
        (is (nil? (async/<? channel)))))))
