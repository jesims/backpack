(ns io.jesi.backpack.spy-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.spy :as spy]))

(def a 1)
(def b 2)
(def c (shorthand a b))

(deftest when-debug-test

  (testing "when-debug"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/when-debug)))

    (testing "expands"
      #?(:clj  (is (= '(prn "hello")
                      (macroexpand-1 '(io.jesi.backpack.spy/when-debug (prn "hello")))))

         :cljs (is (= '(clojure.core/when js/goog.DEBUG (prn "hello"))
                      (macroexpand-1 '(io.jesi.backpack.spy/when-debug (prn "hello")))))))))

(deftest prn-test

  (testing "spy/prn"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/prn)))

    (testing "expands to println"
      (is (= '(io.jesi.backpack.spy/when-debug (clojure.core/when io.jesi.backpack.spy/*enabled* (clojure.core/println "a:" (clojure.core/pr-str a) "b:" (clojure.core/pr-str b))))
             (macroexpand-1 '(io.jesi.backpack.spy/prn a b)))))

    (testing "prns"
      (spy/with-spy
        #?(:clj  (do

                   (testing "the specified values"
                     (is (= "a: 1\n" (with-out-str (spy/prn a))))
                     (is (= "a: 1 b: 2\n" (with-out-str (spy/prn a b)))))

                   (testing "literal expressions"
                     (is (= "1: 1\n" (with-out-str (spy/prn 1))))
                     (is (= "a: \"a\"\n" (with-out-str (spy/prn "a"))))
                     (is (= "(inc 1): 2\n" (with-out-str (spy/prn (inc 1)))))
                     (is (= "((comp inc dec) 1): 1\n" (with-out-str (spy/prn ((comp inc dec) 1)))))))

           :cljs (testing "nothing when debug"
                   (is (false? js/goog.DEBUG))
                   (is (empty? (with-out-str (spy/prn a))))
                   (is (empty? (with-out-str (spy/prn a b)))))))

      (testing "when enabled"
        (is (empty? (with-out-str (spy/prn a))))))))

(deftest pprint-test

  (testing "spy/pprint"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/pprint)))

    (testing "expands to many pprints"
      (is (= '(io.jesi.backpack.spy/when-debug
                (clojure.core/when io.jesi.backpack.spy/*enabled*
                  (do
                    (clojure.core/print (clojure.core/str "a:\n" (io.jesi.backpack.test.util/pprint-str a)))
                    (clojure.core/print (clojure.core/str "b:\n" (io.jesi.backpack.test.util/pprint-str b))))))
             (macroexpand-1 '(io.jesi.backpack.spy/pprint a b)))))

    (testing "pprints"
      (spy/with-spy
        #?(:clj  (do

                   (testing "the specified values"
                     (is (= "a:\n1\n" (with-out-str (spy/pprint a))))
                     (is (= "a:\n1\nb:\n2\n" (with-out-str (spy/pprint a b))))
                     (is (= "c:\n{:a 1, :b 2}\n" (with-out-str (spy/pprint c)))))

                   (testing "literal expressions"
                     (let [val {:a 0 :b 1 :c 2 :d 3 :e 4}]
                       (is (= (str
                                "{:a val, :b val, :c val, :d val, :e val}:\n"
                                "{:a {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                                " :b {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                                " :c {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                                " :d {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                                " :e {:a 0, :b 1, :c 2, :d 3, :e 4}}\n")
                              (with-out-str (spy/pprint {:a val :b val :c val :d val :e val})))))))

           :cljs (testing "nothing when debug"
                   (is (false? js/goog.DEBUG))
                   (is (= "" (with-out-str (spy/pprint a))))
                   (is (= "" (with-out-str (spy/pprint a b))))
                   (is (= "" (with-out-str (spy/pprint c)))))))

      (testing "when enabled"
        (is (empty? (with-out-str (spy/pprint a))))))))
