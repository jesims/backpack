(ns io.jesi.backpack.cache-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.cache :as cache]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.backpack.test.macros :refer [is= async-go]])
  #?(:clj
     (:import
       (clojure.lang ILookup))))

(deftest fcache-test

  (testing "fcache"
    (let [captor (atom nil)
          ttl 100
          threshold 3
          cache (->> {}
                     (cache/ttl ttl)
                     (cache/lru threshold))
          test-fn (partial reset! captor)
          cached-test-fn (cache/fcache cache test-fn)]

      (testing "is a function"
        (is (fn? cached-test-fn)))

      (testing "first invocation caches and returns the value"
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

(deftest ->SimpleCacheTest

  (testing "->SimpleCache"

    (testing "without miss fn"
      (let [simple-cache (cache/->SimpleCache (cache/lru 3 {}))]

        (testing "converts a CacheProtocol into a simple cache"
          (is (satisfies? cache/SimpleCache simple-cache)))

        (testing "get"
          (is (nil? (cache/get simple-cache :a)))
          (is (nil? (cache/get simple-cache :a))))

        (testing "set"
          (let [v (rnd/string)]
            (cache/set simple-cache :a v)
            (is= v (cache/get simple-cache :a))

            (testing "supports 'clojure get'"
              (is= v (get simple-cache :a))

              (testing "with not-found support"
                (is= :c (get simple-cache :b :c))))))

        (testing "evict"
          (cache/set simple-cache :b 1)
          (is= 1 (cache/get simple-cache :b))
          (cache/evict simple-cache :b)
          (is (nil? (cache/get simple-cache :b))))))

    (testing "with a miss fn"
      (let [captor (atom nil)
            miss-fn (fn [entry]
                      (reset! captor entry)
                      (if (= :skip entry)
                        nil
                        (str "missed " entry)))
            simple-cache (cache/->SimpleCache (cache/lru 3 {}) miss-fn)]

        (testing "get"
          (is (nil? @captor))
          (is= "missed :a" (cache/get simple-cache :a))
          (reset! captor nil)
          (is= "missed :a" (cache/get simple-cache :a))
          (is (nil? @captor))

          (testing "supports 'clojure get'"
            (is= "missed :b" (get simple-cache :b))

            (testing "with not-found support"
              (reset! captor nil)
              (is= "not found" (get simple-cache :skip "not found"))
              (is= :skip @captor))))))))
