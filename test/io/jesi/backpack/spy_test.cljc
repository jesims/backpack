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

  #?(:clj
     (testing "is a macro"
       (bp/macro? `spy/when-debug)))

  (testing "expands"
    #?(:clj  (is (= '(prn "hello")
                    (macroexpand-1 '(io.jesi.backpack.spy/when-debug (prn "hello")))))

       :cljs (is (= '(clojure.core/when js/goog.DEBUG (prn "hello"))
                    (macroexpand-1 '(io.jesi.backpack.spy/when-debug (prn "hello"))))))))


(deftest prn-test

  #?(:clj
     (testing "is a macro"
       (bp/macro? `spy/prn)))

  (testing "expands to applying println"
    (is (= '(io.jesi.backpack.spy/when-debug (clojure.core/println "a:" (clojure.core/pr-str a) "b:" (clojure.core/pr-str b)))
           (macroexpand-1 '(io.jesi.backpack.spy/prn a b)))))

  #?(:clj  (testing "prns the specified values"
             (is (= "a: 1\n" (with-out-str (spy/prn a))))
             (is (= "a: 1 b: 2\n" (with-out-str (spy/prn a b)))))

     :cljs (testing "prns nothing"
             (is (false? js/goog.DEBUG))
             (is (= "" (with-out-str (spy/prn a))))
             (is (= "" (with-out-str (spy/prn a b)))))))

(deftest pprint-test

  #?(:clj
     (testing "is a macro"
       (bp/macro? `spy/pprint)))

  (testing "expands to many pprint"
    (is (= '(io.jesi.backpack.spy/when-debug
              (do
                (clojure.core/println "a:")
                (io.jesi.backpack.spy/-pprint a)
                (clojure.core/println "b:")
                (io.jesi.backpack.spy/-pprint b)))
           (macroexpand-1 '(io.jesi.backpack.spy/pprint a b)))))

  #?(:clj  (testing "pprints the specified values"
             (is (= "a:\n1\n" (with-out-str (spy/pprint a))))
             (is (= "a:\n1\nb:\n2\n" (with-out-str (spy/pprint a b))))
             (is (= "c:\n{:a 1, :b 2}\n" (with-out-str (spy/pprint c)))))

     :cljs (testing "pprints the specified values"
             (is (false? js/goog.DEBUG))
             (is (= "" (with-out-str (spy/pprint a))))
             (is (= "" (with-out-str (spy/pprint a b))))
             (is (= "" (with-out-str (spy/pprint c)))))))
