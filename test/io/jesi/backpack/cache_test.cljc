(ns io.jesi.backpack.cache-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.backpack.test.macros :refer [is= async-go]]))

(def ^:private caches #'io.jesi.backpack.cache/caches)

(deftest cache-test

  (testing "cache"

    (reset! @caches nil)

    (let [captor (atom nil)
          ttl 100
          threshold 3
          test-fn (partial reset! captor)
          cached-test-fn (bp/cache {:init-fn #(bp/init-cache (shorthand ttl threshold))} test-fn)]

      (testing "the test-fn has no caches on creation"
        (is (nil? (get @caches test-fn))))

      (testing "first invocation is stored in the cache and result returned"
        (is (fn? cached-test-fn))
        (let [v (rnd/string)]
          (is= v (cached-test-fn v))
          (is= v @captor))

        (testing "subsequent hits for the same args do not re-invoke"
          (reset! captor nil)
          (let [v (rnd/string)]
            (is= v (cached-test-fn v))
            (is= v @captor)
            (reset! captor nil)
            (is= v (cached-test-fn v))
            (is (nil? @captor)))))

      (testing "adheres to threshold settings"
        (let [[v1 v2 v3 v4 :as values] (repeatedly (inc threshold) rnd/string)]
          (doseq [v values]
            (is= v (cached-test-fn v))
            (is= v @captor))

          (testing "clearing the oldest value and forcing a new invocation"
            (reset! captor nil)
            (cached-test-fn v1)
            (cached-test-fn v4)
            (is= v1 @captor))))

      (testing "adheres to ttl settings"
        (async-go
          (let [[v1 v2 v3 v4 :as values] (repeatedly threshold rnd/string)
                assert-invoked #(doseq [v values]
                                  (is= v (cached-test-fn v))
                                  (is= v @captor))]
            (assert-invoked)
            (reset! captor nil)
            (let [timeout (+ 100 ttl)
                  f #(assert-invoked)]
              #?(:cljs (js/setTimeout #(do (f) (done)) timeout)
                 :clj  (do
                         (Thread/sleep timeout)
                         (f))))))))))

(deftest keyed-cache-test
  (reset! @caches nil)
  (let [key :test-key
        identifier (rnd/string)]

    (testing "caches are reset"
      (is (nil? (get @caches key))))

    (testing "returns nil when not exists"
      (is (nil? (bp/keyed-cache key identifier))))

    (testing "sets value when given and returns the value when exists"
      (let [value (rnd/string)]
        (bp/keyed-cache key identifier value)
        (is (= value (bp/keyed-cache key identifier)))))))
