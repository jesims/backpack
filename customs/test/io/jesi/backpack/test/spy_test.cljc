(ns io.jesi.backpack.test.spy-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros :refer [shorthand]]
    [io.jesi.backpack.test.spy :as spy]
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing use-fixtures]]))

(defn- set-debug [v]
  #?(:cljs (set! js/goog.DEBUG v)))

(use-fixtures :each
  (fn [f]
    (f)
    (set-debug false)))

(def file #?(:clj  *file*
             :cljs "io.jesi.backpack.spy-test"))

#?(:clj (def line (atom nil)))

;TODO use macro to get the line number
(defn- set-line [n]
  #?(:clj (str \: (reset! line n))))

(defn- add-line [n]
  #?(:clj (str \: (swap! line + n))))

(def a 1)
(def b 2)
(def c (shorthand a b))

(deftest prn-test

  (testing "spy/prn"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/prn)))

    (testing "prns"
      (spy/enabled
        (set-debug true)

        (testing "the specified values"
          (is= (str file (set-line 46) " a: 1" \newline)
               (with-out-str (spy/prn a)))
          (is= (str file (add-line 2) " a: 1 b: 2" \newline)
               (with-out-str (spy/prn a b))))

        (testing "literal expressions"
          (is= (str file (add-line 4) " 1: 1" \newline)
               (with-out-str (spy/prn 1)))
          (is= (str file (add-line 2) " a: \"a\"" \newline)
               (with-out-str (spy/prn "a")))
          (is= (str file (add-line 2) " (inc 1): 2" \newline)
               (with-out-str (spy/prn (inc 1))))
          (is= (str file (add-line 2) " ((comp inc dec) 1): 1" \newline)
               (with-out-str (spy/prn ((comp inc dec) 1))))))

      (testing "nothing when not"

        #?(:cljs (testing "debug"
                   (set-debug false)
                   (is (empty? (with-out-str (spy/prn a))))
                   (is (empty? (with-out-str (spy/prn a b))))))

        (testing "enabled"
          (is (empty? (with-out-str (spy/prn a)))))))))

(deftest pprint-test

  (testing "spy/pprint"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/pprint)))

    (testing "pprints"
      (spy/enabled
        (set-debug true)

        (testing "the specified values"
          (is= (str file (set-line 84) " a:" \newline
                 "1" \newline)
               (with-out-str (spy/pprint a)))
          (is= (str file (add-line 5) " a:" \newline
                 "1" \newline
                 file #?(:clj (str ":" @line)) " b:" \newline
                 "2" \newline)
               (with-out-str (spy/pprint a b)))
          (is= (str file (add-line 3) " c:" \newline
                 "{:a 1, :b 2}" \newline)
               (with-out-str (spy/pprint c))))

        ;cljs messes up the formatting, it adds a space after the :d line
        #?(:clj (testing "literal expressions"
                  (let [val {:a 0 :b 1 :c 2 :d 3 :e 4}]
                    (is= (str file (add-line 12) \space
                           "{:a val, :b val, :c val, :d val, :e val}:" \newline
                           "{:a {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :b {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :c {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :d {:a 0, :b 1, :c 2, :d 3, :e 4}," \newline
                           " :e {:a 0, :b 1, :c 2, :d 3, :e 4}}" \newline)
                         (with-out-str (spy/pprint {:a val :b val :c val :d val :e val})))))))

      (testing "nothing when not"

        #?(:cljs (testing "debug"
                   (set-debug false)
                   (is (empty? (with-out-str (spy/pprint a))))
                   (is (empty? (with-out-str (spy/pprint a b))))
                   (is (empty? (with-out-str (spy/pprint c))))))

        (testing "enabled"
          (is (empty? (with-out-str (spy/pprint a)))))))))

(deftest peek-test

  (testing "peek"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/peek)))

    (testing "prns (using spy/prn) and return the passed in value"
      (spy/enabled
        (set-debug true)
        (let [result (atom nil)]
          (is= (str file (set-line 129) " a: 1" \newline)
               (with-out-str (reset! result (spy/peek a))))
          (is= a @result)

          (testing "even in a thread macro (no line numbers since the &from metadata is not preserved)"
            (is= (str file " a: 1" \newline)
                 (with-out-str (reset! result (-> a spy/peek inc))))
            (is= (inc a) @result)))))))

(deftest ppeek-test

  (testing "ppeek"

    #?(:clj (testing "is a macro"
              (bp/macro? `spy/ppeek)))

    (testing "pretty prints (using spy/pprint) and return the passed in value"
      (spy/enabled
        (set-debug true)
        (let [result (atom nil)]
          (is= (str file (set-line 149) " a:" \newline "1" \newline)
               (with-out-str (reset! result (spy/ppeek a))))
          (is= a @result)

          (testing "even in a thread macro (no line numbers since the &from metadata is not preserved)"
            (is= (str file " a:" \newline "1" \newline)
                 (with-out-str (reset! result (-> a spy/ppeek inc))))
            (is= (inc a) @result)))))))
