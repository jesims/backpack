(ns io.jesi.backpack.http.status-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.http.status :as status]
    [io.jesi.backpack.test.macros :refer [is=]]))

(deftest ok-test

  (testing "Returns a 200 status code"
    (is= 200 status/ok))

  (testing "Returns a 418 status code"
    (is= 418 status/im-a-teapot)))

(deftest success?-test

  (testing "true if a response status is between 200 and 299 (inclusive)"
    (is (fn? status/success?))
    (is (true? (status/success? 200)))
    (is (true? (status/success? 299)))
    (is (false? (status/success? 199)))
    (is (false? (status/success? 300)))))
