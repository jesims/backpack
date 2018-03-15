(ns io.jesi.backpack.macros-test
  (:require
    [clojure.string :as string]
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack :as bp]
    [io.jesi.backpack.macros #?(:clj :refer :cljs :refer-macros) [catch->nil]]))

(defn- throw-ex []
  (throw (ex-info "Error" {})))

(deftest catch->nil-test
  #?(:clj
     (testing "is a macro"
       (is (bp/macro? `catch->nil))))

  (testing "surrounds with a try catch"
    (let [ex #?(:clj `Throwable :cljs :default)
          expected (->> `(try (throw-ex) (catch ~ex e#))
                        str
                        (re-find #".+?(?=__)"))
          actual (str (macroexpand-1 '(io.jesi.backpack.macros/catch->nil (io.jesi.backpack.macros-test/throw-ex))))]
      (is (some? (seq expected)))
      (is (string/starts-with? actual expected))))

  (testing "returns nil if the body throws an exception"
    (is (nil? (catch->nil (throw-ex))))))
