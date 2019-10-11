(ns io.jesi.backpack.cache-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [io.jesi.backpack.cache :as cache]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.random :as rnd]
    [io.jesi.backpack.test.macros :refer [async-go is=]])
  #?(:clj
     (:import
       (clojure.lang ILookup))))

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

    (testing "can be applied to"
      (let [cached-sum (cache/->Simple (cache/create-default) +)]
        (doseq [v (range 1 #?(:clj 100
                              :cljs 21))]
          (let [args (range v)
                expected (apply + args)
                actual (apply cached-sum args)]
            (is= expected actual)))

        (testing "can be invoked with many args"
          ;FIXME. Would be nice to have a macro, but I can't get it to spread local args (i.e. args defined with let)
          (comment (defmacro spread [sym args]
                     (cons sym (eval args))))

          (is= 1 (cached-sum 1))
          (is= 2 (cached-sum 1 1))
          (is= 3 (cached-sum 1 1 1))
          (is= 4 (cached-sum 1 1 1 1))
          (is= 5 (cached-sum 1 1 1 1 1))
          (is= 6 (cached-sum 1 1 1 1 1 1))
          (is= 7 (cached-sum 1 1 1 1 1 1 1))
          (is= 8 (cached-sum 1 1 1 1 1 1 1 1))
          (is= 9 (cached-sum 1 1 1 1 1 1 1 1 1))
          (is= 10 (cached-sum 1 1 1 1 1 1 1 1 1 1))
          (is= 11 (cached-sum 1 1 1 1 1 1 1 1 1 1 1))
          (is= 12 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 13 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 14 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 15 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 16 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 17 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 18 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 19 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          (is= 20 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
          ;CLJS does not support arity over 21
          #?(:clj
             (do
               (is= 21 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
               (is= 22 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1))
               (is= 23 (cached-sum 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1)))))))

    (testing "without miss fn"
      (let [simple-cache (cache/->Simple (cache/create-lru 3 {}))]

        (testing "converts a CacheProtocol into a simple cache"
          (is (satisfies? cache/SimpleCache simple-cache)))

        (testing "get"
          (is (nil? (cache/get simple-cache [:a])))
          (is (nil? (cache/get simple-cache [:a])))
          (is (nil? (simple-cache :a))))

        (testing "set"
          (let [v (rnd/string)]
            (cache/set simple-cache [:a] v)
            (is= v (cache/get simple-cache [:a]))

            (testing "supports 'clojure get'"
              (is= v (get simple-cache [:a]))
              (is= v (simple-cache :a))

              (testing "with not-found support"
                (is= :c (get simple-cache [:b] :c))))))

        (testing "evict"
          (cache/set simple-cache [:b] 1)
          (is= 1 (cache/get simple-cache [:b]))
          (cache/evict simple-cache [:b])
          (is (nil? (cache/get simple-cache [:b]))))))

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
          (is= "missed :a" (cache/get simple-cache [:a]))
          (reset! captor nil)
          (is= "missed :a" (cache/get simple-cache [:a]))
          (is (nil? @captor))

          (testing "supports 'clojure get'"
            (is= "missed :b" (get simple-cache [:b]))

            (testing "with not-found support"
              (reset! captor nil)
              (is= "not found" (get simple-cache [:skip] "not found"))
              (is= :skip @captor))))))))
