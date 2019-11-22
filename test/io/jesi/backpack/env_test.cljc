(ns io.jesi.backpack.env-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack.test.strict :refer [= deftest is is= testing thrown?]]
    [io.jesi.backpack.env :as env]))

(deftest transform-test

  (testing "transform*"

    (testing "returns the runtime specific version of the passed in form"
      (let [form '(clojure.test/is (clojure.core/some? (clojure.core.async/go 1)))
            cljs '(cljs.test/is (clojure.core/some? (cljs.core.async/go 1)))]
        (is= form
             (env/transform* :clj form))
        (is= form
             (env/transform* :default form))
        (is= form
             (env/transform* nil form))
        (is= cljs
             (env/transform* :cljs form))
        (is= cljs
             (env/transform* {:ns true} form)))

      (testing "even if inner symbols are quotes")

      (testing "preserves metadata"))))
