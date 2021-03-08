(ns io.jesi.backpack.clojurescript-test
  (:require
    [io.jesi.backpack :as bp]
    [io.jesi.customs.strict :refer [deftest is= testing]]))

(deftype TestClass [f1 f2]
  Object)

(deftest class->clj-test
  (let [m {:f1 1 :f2 2}
        o (clj->js m)]

    (testing "converts simple JS objects"
      (is= m (bp/class->clj o)))

    (testing "converts JS classes"
      (is= m (bp/class->clj (TestClass. 1 2))))

    (testing "converts properties from prototype"
      (is= (assoc m :f3 3)
           (bp/class->clj (js/Object.create o (clj->js {:f3 {:value 3 :enumerable true}})))))))
