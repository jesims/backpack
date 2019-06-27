(ns io.jesi.backpack.http.status-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.http.status :as status]
    [io.jesi.backpack.test.macros :refer [is=]]))

(deftest ok-test

  (testing "Returns a 200 status code"
    (is= 200 status/ok)))
