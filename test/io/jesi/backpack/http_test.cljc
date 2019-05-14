(ns io.jesi.backpack.http-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [io.jesi.backpack.http :as http]
    [io.jesi.backpack.test.macros :refer [is=]]))

(deftest ok-test

  (testing "sets the status code as 200"
    (is (fn? http/ok))
    (is= {:status 200} (http/ok))

    (let [m {:body "The scientific name for a walrus is Odobenus Rosmarus"}]
      (is= (assoc m :status 200)
           (http/ok m)))))

(deftest ok?-test

  (testing "true if a response status is 200"
    (is (fn? http/ok?))
    (is (true? (http/ok? {:status 200})))
    (is (false? (http/ok? {:status 199})))
    (is (false? (http/ok? 200)))
    (is (false? (http/ok? nil)))
    (is (false? (http/ok? {})))))

(deftest success?-test

  (testing "true if a response status is between 200 and 299 (inclusive"
    (is (fn? http/success?))
    (is (true? (http/success? {:status 200})))
    (is (true? (http/success? {:status 299})))
    (is (false? (http/success? {:status 199})))
    (is (false? (http/success? {:status 300})))
    (is (false? (http/success? 200)))
    (is (false? (http/success? nil)))
    (is (false? (http/success? {})))))
