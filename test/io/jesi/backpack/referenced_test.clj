(ns io.jesi.backpack.referenced-test
  (:require
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [clojure.tools.namespace.find :as ns-find]
    [io.jesi.backpack]
    [io.jesi.backpack.macros :refer [def-]]))

(def- excluded-vars #{'io.jesi.backpack.specter/path-walker})

(def- excluded-ns #{'io.jesi.backpack.async
                    'io.jesi.backpack.exceptions
                    'io.jesi.backpack.http.response
                    'io.jesi.backpack.http.status
                    'io.jesi.backpack.macros
                    'io.jesi.backpack.random
                    'io.jesi.backpack.clojurescript})

(deftest core-test
  (let [models-ns (->> "src/io/jesi/backpack/"
                       io/file
                       ns-find/find-ns-decls-in-dir
                       (map second)
                       (remove excluded-ns)
                       set)]

    (testing "Loads all the model namespaces"
      (is (seq models-ns))
      (is (not (contains? models-ns 'io.jesi.backpack.random)))
      (is (true? (every? (comp some? find-ns) models-ns))))

    (testing "Aliases all public def's and functions into bp"
      (let [ns 'io.jesi.backpack]
        (doseq [sym (->> models-ns
                         (map ns-publics)
                         (apply merge)
                         (vals)
                         (map symbol)
                         (remove excluded-vars))]
          (is (some? (ns-resolve ns sym)) (str "Could not find symbol: " ns \/ sym)))))))
