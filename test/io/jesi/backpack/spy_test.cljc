(ns io.jesi.backpack.spy-test
  (:require
    [clojure.test :refer [deftest testing is use-fixtures]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.spy :as spy]))

(def a 1)
(def b 2)
(def c (shorthand a b))

(use-fixtures :each
  (fn [f]
    (f)
    #?(:cljs (set! js/goog.DEBUG false))))

(deftest when-debug-test

  (testing "when-debug"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/when-debug)))

    (testing "expands"
      #?(:clj  (is (= '(prn "hello")
                      (macroexpand-1 '(io.jesi.backpack.spy/when-debug (prn "hello")))))

         :cljs (is (= '(clojure.core/when js/goog.DEBUG (prn "hello"))
                      (macroexpand-1 '(io.jesi.backpack.spy/when-debug (prn "hello")))))))))

(def file #?(:cljs "io.jesi.backpack.spy-test"
             :clj  *file*))

(def line (atom nil))

(defn- set-line [n]
  (reset! line n))

(defn- add-line [n]
  (swap! line + n))

(deftest prn-test

  (testing "spy/prn"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/prn)))

    (testing "prns"
      (spy/with-spy
        #?(:cljs (set! js/goog.DEBUG true))

        (testing "the specified values"
          (is (= (str file ":" (set-line 55) " a: 1\n")
                 (with-out-str (spy/prn a))))
          (is (= (str file ":" (add-line 2) " a: 1 b: 2\n")
                 (with-out-str (spy/prn a b)))))

        (testing "literal expressions"
          (is (= (str file ":" (add-line 4) " 1: 1\n")
                 (with-out-str (spy/prn 1))))
          (is (= (str file ":" (add-line 2) " a: \"a\"\n")
                 (with-out-str (spy/prn "a"))))
          (is (= (str file ":" (add-line 2) " (inc 1): 2\n")
                 (with-out-str (spy/prn (inc 1)))))
          (is (= (str file ":" (add-line 2) " ((comp inc dec) 1): 1\n")
                 (with-out-str (spy/prn ((comp inc dec) 1)))))))

      (testing "nothing when not"

        #?(:cljs (testing "debug"
                   (set! js/goog.DEBUG false)
                   (is (empty? (with-out-str (spy/prn a))))
                   (is (empty? (with-out-str (spy/prn a b))))))

        (testing "enabled"
          (is (empty? (with-out-str (spy/prn a)))))))))

(deftest pprint-test

  (testing "spy/pprint"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/pprint)))

    (testing "pprints"
      (spy/with-spy
        #?(:cljs (set! js/goog.DEBUG true))

        (testing "the specified values"
          (is (= (str
                   file ":" (set-line 94) " a:\n"
                   "1\n")
                 (with-out-str (spy/pprint a))))
          (is (= (str
                   file ":" (add-line 6) " a:\n"
                   "1\n"
                   file ":" @line " b:\n"
                   "2\n")
                 (with-out-str (spy/pprint a b))))
          (is (= (str
                   file ":" (add-line 4) " c:\n"
                   "{:a 1, :b 2}\n")
                 (with-out-str (spy/pprint c)))))

        ;cljs messes up the formatting, it adds a space after the :d line
        #?(:clj (testing "literal expressions"
                  (let [val {:a 0 :b 1 :c 2 :d 3 :e 4}]
                    (is (= (str
                             (str file ":" (add-line 13) \space)
                             "{:a val, :b val, :c val, :d val, :e val}:\n"
                             "{:a {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                             " :b {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                             " :c {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                             " :d {:a 0, :b 1, :c 2, :d 3, :e 4},\n"
                             " :e {:a 0, :b 1, :c 2, :d 3, :e 4}}\n")
                           (with-out-str (spy/pprint {:a val :b val :c val :d val :e val}))))))))

      (testing "nothing when not"

        #?(:cljs (testing "debug"
                   (set! js/goog.DEBUG false)
                   (is (empty? (with-out-str (spy/pprint a))))
                   (is (empty? (with-out-str (spy/pprint a b))))
                   (is (empty? (with-out-str (spy/pprint c))))))

        (testing "enabled"
          (is (empty? (with-out-str (spy/pprint a)))))))))
