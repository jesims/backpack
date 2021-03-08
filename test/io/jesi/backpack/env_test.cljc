(ns io.jesi.backpack.env-test
  (:require
    [io.jesi.backpack.env :as env]
    [io.jesi.customs.strict :refer [deftest is= testing]]))

(deftest transform-test

  (testing "transform*"

    (testing "returns the runtime specific version of the passed in form"
      (let [form '(clojure.test/is (clojure.core/some? (clojure.core.async/go 1)))
            cljs '(cljs.test/is (clojure.core/some? (cljs.core.async/go 1)))]
        (is= form
             (env/transform* :clj form)
             (env/transform* :default form)
             (env/transform* nil form))
        (is= cljs
             (env/transform* :cljs form)
             (env/transform* {:ns true} form)))

      (testing "does not convert clojure.core ns"
        (let [form '(clojure.core/= 1 1)]
          (is= form
               (env/transform* :clj form)
               (env/transform* :default form)
               (env/transform* nil form)
               (env/transform* :cljs form))))

      (testing "even if inner symbols are quoted"
        (let [form '(clojure.core/cons 'clojure.core/= (clojure.core/list 1 1))]
          (is= form
               (env/transform* :clj form)
               (env/transform* :default form)
               (env/transform* nil form)
               (env/transform* :cljs form)))))))
