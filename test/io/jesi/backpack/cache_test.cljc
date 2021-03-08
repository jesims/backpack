(ns io.jesi.backpack.cache-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack.cache :as cache]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.customs.macros :refer [async-go]]
    [io.jesi.customs.strict :refer [= deftest is is= testing]]))

(deftest ->SimpleTest

  (testing "->Simple"

    (testing "works like a function"
      (let [captor (atom nil)
            ttl 100
            threshold 3
            cache (->> {}
                       (cache/create-ttl ttl)
                       (cache/create-lru threshold))
            test-fn (partial reset! captor)
            cached-test-fn (cache/->Simple cache test-fn)]

        (testing "acts like a function"
          (is (ifn? cached-test-fn)))

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
                           (f)))))))))

    (testing "can be applied and invoked to"
      (let [cached-sum (cache/->Simple (cache/create-default) +)]
        (is= 1 (apply cached-sum [1]))
        (is= 1 (cached-sum 1))))

    (testing "without miss fn"
      (let [default {:c 3}
            simple-cache (cache/->Simple (cache/create-lru 3 default))]

        (testing "converts a CacheProtocol into a simple cache"
          (is (satisfies? cache/SimpleCache simple-cache)))

        (testing "get"
          (is (nil? (cache/get simple-cache :a)))
          (is (nil? (cache/get simple-cache :a)))
          (is (nil? (simple-cache :a))))

        (testing "set"
          (let [v (rnd/string)]
            (cache/set simple-cache :a v)
            (is= v (cache/get simple-cache :a))

            (testing "supports 'clojure get'"
              (is= v (get simple-cache :a))
              (is= v (simple-cache :a))

              (testing "with not-found support"
                (is= :c (get simple-cache :b :c))))))

        (testing "evict"
          (cache/set simple-cache :b 1)
          (is= 1 (cache/get simple-cache :b))
          (cache/evict simple-cache :b)
          (is (nil? (cache/get simple-cache :b))))

        (testing "reset"
          (is= 3 (cache/get simple-cache :c))
          (cache/set simple-cache :c 2)
          (is= 2 (cache/get simple-cache :c))
          (cache/reset simple-cache)
          (is= 3 (cache/get simple-cache :c)))))

    (testing "with a miss fn"
      (let [captor (atom nil)
            miss-fn (fn [entry]
                      (reset! captor entry)
                      (if (= :skip entry)
                        nil
                        (str "missed " entry)))
            simple-cache (cache/->Simple (cache/create-lru 3 {}) miss-fn)]

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

(deftest ->Simple-Fn-Cache-test

  (testing "->Simple-Fn-Cache"
    (let [store (atom {})
          calls (atom {:miss      0
                       :cache-get 0
                       :cache-set 0})
          [key1 val1] (repeatedly rnd/string)
          [key2 val2] (repeatedly #(rand-int 2000))
          impl (cache/->Simple-Fn-Cache {:miss-fn  (fn [key]
                                                     (swap! calls update :miss inc)
                                                     (if (= key1 key)
                                                       val1
                                                       val2))
                                         :cache-fn (fn
                                                     ([key]
                                                      (swap! calls update :cache-get inc)
                                                      (get @store key))
                                                     ([key entry]
                                                      (swap! calls update :cache-set inc)
                                                      (swap! store assoc key entry)))})]
      (doseq [_ (range 10)]
        (is (= val1 (impl key1)))
        (is (= val2 (impl key2))))
      (is= {:miss      2
            :cache-get 20
            :cache-set 2}
           @calls))))
