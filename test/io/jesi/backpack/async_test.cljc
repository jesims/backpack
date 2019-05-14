(ns io.jesi.backpack.async-test
  (:require
    [clojure.core.async :as core-async]
    [clojure.test :refer [deftest testing is]]
    [com.rpl.specter :as sp]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.miscellaneous :refer [env-specific namespaced?]]
    [io.jesi.backpack.test.macros :refer [async-go is=]]
    [io.jesi.backpack.test.util :refer [is-macro=]]
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])))

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

(defn ex []
  (ex-info "Exceptional" {}))

(def ^:const ex-type #?(:clj Exception :cljs js/Error))

(def list-walker (sp/recursive-path [] l (sp/if-path list? (sp/continue-then-stay sp/ALL l))))

(defn transform-to-env [env quoted-form]
  (sp/transform [list-walker sp/FIRST symbol? namespaced?] (partial env-specific env) quoted-form))

;FIXME does not compile
(deftest go-retry-test

  (testing "go-retry"

    #?(:clj (testing "is a macro"
              (is (macro? `async/go-retry))))

    (testing "expands"
      (let [expected '(clojure.core.async/go-loop [retries# 5]
                        (clojure.core/let [res# (io.jesi.backpack.macros/catch->identity "foo")]
                          (if (clojure.core/and
                                (io.jesi.backpack.exceptions/exception? res#)
                                (clojure.core/pos? retries#))
                            (do
                              (clojure.core/when (clojure.core/pos? 1000)
                                (clojure.core.async/<! (clojure.core.async/timeout 1000)))
                              (recur (clojure.core/dec retries#)))
                            res#)))
            actual (macroexpand-1 '(io.jesi.backpack.async/go-retry {} "foo"))]
        #?(:clj  (is-macro= expected actual)
           :cljs (is-macro= (transform-to-env {:ns true} expected) actual))))

    (testing "is a go block"
      (let [value "The heart of a shrimp is located in its head"]
        (async-go
          (is= value (async/<? (async/go-retry {} value)))

          (testing "that bubbles exceptions"
            (is (thrown? ex-type (async/<? (async/go-retry
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
                (is (thrown? ex-type (async/<? (async/go-retry
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
                (is= 3 @times)))))))))
