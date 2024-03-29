(ns io.jesi.backpack.async-test
  (:refer-clojure :exclude [=])
  (:require
    #?@(:clj  [[io.jesi.backpack.macros :refer [macro?]]]
        :cljs [[com.rpl.specter :as sp]
               [io.jesi.backpack.env :as env]])
    [clojure.core.async :as core-async :refer [<!]]
    [clojure.string :as str]
    [io.jesi.backpack.async :as async]
    [io.jesi.customs.macros :refer [async-go]]
    [io.jesi.customs.strict :refer [= deftest is is= testing thrown?]]
    [io.jesi.customs.util :refer [is-macro=]]))

(deftest go-test
  (async-go

    (testing "go"

      #?(:cljs (testing "is a runtime specific `core.async/go` block"
                 (let [expanded (macroexpand '(io.jesi.backpack.async/go 1))]
                   (is (sp/select-one (sp/codewalker (partial = 'cljs.core.async/chan)) expanded)))))

      (testing "is a go block"
        (is (= 1 (<! (async/go 1))))))))

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

(defn- now []
  #?(:cljs (js/Date.now)
     :clj  (System/currentTimeMillis)))

(defn ex []
  (ex-info "Exceptional" {}))

(deftest go-retry-test

  (testing "go-retry"

    #?(:clj (testing "is a macro"
              (is (macro? `async/go-retry))))

    (testing "expands"
      (let [expected '(let* [delay# (clojure.core/* 1 1000)]
                        (clojure.core.async/go-loop [retries# 5]
                          (clojure.core/let [res# (io.jesi.backpack.macros/catch->identity "foo")]
                            (if (clojure.core/and
                                  (io.jesi.backpack.exceptions/exception? res#)
                                  (clojure.core/pos? retries#))
                              (do
                                (clojure.core/when (clojure.core/pos? delay#)
                                  (clojure.core.async/<!
                                    (clojure.core.async/timeout delay#)))
                                (recur (clojure.core/dec retries#)))
                              res#))))]
        (is-macro= #?(:clj  expected
                      :cljs (env/transform* :cljs expected))
                   (macroexpand '(io.jesi.backpack.async/go-retry {} "foo")))))

    (testing "is a go block"
      (let [value "The heart of a shrimp is located in its head"]
        (async-go
          (is= value (async/<? (async/go-retry {} value)))

          (testing "that bubbles exceptions"
            (is (thrown? #?(:clj Exception :cljs js/Error) (async/<? (async/go-retry
                                                                       {:retries 1
                                                                        :delay   0}
                                                                       (throw (ex)))))))

          (testing "that retries based on"

            (testing "should-retry-fn"
              (is= 4 (let [times (atom 0)]
                       (async/<? (async/go-retry
                                   {:should-retry-fn (fn [res] (= 0 res))
                                    :retries         3
                                    :delay           0}
                                   (swap! times inc)
                                   0))
                       @times)))

            (testing "retries value"
              (let [times (atom 0)]
                (is (thrown? #?(:clj Exception :cljs js/Error) (async/<? (async/go-retry
                                                                           {:delay 0}
                                                                           (swap! times inc)
                                                                           (throw (ex))))))
                (is (= 6 @times)))
              (let [times (atom 0)]
                (is= value (async/<? (async/go-retry
                                       {:delay 0}
                                       (if (< (swap! times inc) 3)
                                         (throw (ex))
                                         value))))
                (is= 3 @times))))

          (testing "can have a delay between retires"
            (let [delay 1
                  start (now)]
              (<! (async/go-retry {:delay           1
                                   :should-retry-fn (constantly true)
                                   :retries         1}
                    (throw (ex))))
              (let [run-time (- (now) start)]
                (is (<= delay (/ run-time 1000)))))))))))

(deftest go-call-test
  (async-go

    (testing "go-call"

      #?(:clj (testing "is a macro"
                (is (macro? `async/go-retry))))

      (testing "returns a channel with the result of passing input through f"
        (let [quote "Lemurs can tell a 1% alcohol solution from a 5% alcohol solution and prefer the solution that contains more alcohol"
              input-chan (async/go quote)
              actual (async/go-call str/capitalize input-chan)]
          (is (true? (async/open? actual)))
          (is (= (str/capitalize quote)
                 (async/<? actual)))
          (is (true? (async/closed? actual)))))

      (testing "returns exceptions if any thrown"
        (is (instance?
              #?(:clj Exception :cljs js/Error)
              (<! (async/go-call str/capitalize (async/go-try (ex))))))))))
