(ns io.jesi.backpack.close-test
  (:require
    #?(:clj [io.jesi.backpack.clojure])
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.closey :refer [->Closey #?(:clj ->AutoCloseable) closed?]]
    [io.jesi.customs.strict :refer [deftest is testing]]))

(deftest close-test

  (testing "closes any type"
    (let [o (->Closey)]
      (is (false? (closed? o)))
      (bp/close o)
      (is (true? (closed? o)))))

  (testing "closes channels"
    (let [chan (async/chan)]
      (is (not (async/closed? chan)))
      (bp/close chan)
      (is (async/closed? chan))))

  #?(:clj (testing "closes AutoCloseable"
            (let [o (->AutoCloseable)]
              (is (false? (closed? o)))
              (bp/close o)
              (is (true? (closed? o)))))))
