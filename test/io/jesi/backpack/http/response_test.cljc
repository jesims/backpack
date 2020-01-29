(ns io.jesi.backpack.http.response-test
  (:refer-clojure :exclude [=])
  (:require
    [io.jesi.backpack.http.response :as response]
    [io.jesi.customs.strict :refer [= deftest is is= testing]]))

(deftest ok-test

  (testing "sets the status code as 200"
    (is (fn? response/ok))
    (is= {:status 200} (response/ok))

    (let [m {:body "The scientific name for a walrus is Odobenus Rosmarus"}]
      (is= (assoc m :status 200)
           (response/ok m)))))

(deftest ok?-test

  (testing "true if a response status is 200"
    (is (fn? response/ok?))
    (is (true? (response/ok? {:status 200})))
    (is (false? (response/ok? {:status 199})))
    (is (false? (response/ok? 200)))
    (is (false? (response/ok? nil)))
    (is (false? (response/ok? {})))))

(deftest success?-test

  (testing "true if a response status is between 200 and 299 (inclusive"
    (is (fn? response/success?))
    (is (true? (response/success? {:status 200})))
    (is (true? (response/success? {:status 299})))
    (is (false? (response/success? {:status 199})))
    (is (false? (response/success? {:status 300})))
    (is (false? (response/success? 200)))
    (is (false? (response/success? nil)))
    (is (false? (response/success? {})))))
