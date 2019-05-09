(ns io.jesi.backpack.test.macros-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.test.macros]
    [io.jesi.backpack.test.util :refer [is-macro=]]))

(deftest async-go-test

  (testing "is a macro"
    (is (is-macro=
          '(full.async.env/if-cljs
             (cljs.test/async done
               (io.jesi.backpack.async/go
                 (try
                   (is true)
                   (finally
                     (done)))))
             nil)
          (macroexpand-1 '(io.jesi.backpack.test.macros/async-go (is true)))))))
