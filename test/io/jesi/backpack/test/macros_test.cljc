(ns io.jesi.backpack.test.macros-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.test.macros]
    [io.jesi.backpack.test.util :refer [is-macro=]]
    #?(:clj [io.jesi.backpack.macros :refer [macro?]])))

(deftest async-go-test
  (testing "async-go")

  #?(:clj (testing "is a macro"
            (is (macro? `io.jesi.backpack.test.macros/async-go))))

  (testing "expands to a `async` `go` block"
    (is (is-macro= '(full.async.env/if-cljs
                      (cljs.test/async done
                        (io.jesi.backpack.async/go
                          (try
                            (is true)
                            (finally
                              (done)))))
                      (clojure.core.async/<!!
                        (io.jesi.backpack.async/go
                          (is true))))
                   (macroexpand-1 '(io.jesi.backpack.test.macros/async-go (is true)))))))
