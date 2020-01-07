(ns io.jesi.backpack.test.runner.node
  {:dev/always true}
  (:require
    [cljs.test :as ct]
    [io.jesi.backpack.spy :as spy]
    [io.jesi.backpack.test.runner.util :refer [convert-event]]
    [pjstadig.humane-test-output]
    [shadow.test :as st]
    [shadow.test.env :as env]))

(enable-console-print!)

(defmethod ct/report [::ct/default :fail] [event]
  (let [[event code] (convert-event event)]
    (ct/inc-report-counter! :fail)
    (println "\nFAIL in" (ct/testing-vars-str event))
    (when (seq (:testing-contexts (ct/get-current-env)))
      (println (ct/testing-contexts-str)))
    (when-let [message (:message event)]
      (println message))
    (when code
      (println code))))

;from shadow.test.node

(defmethod ct/report [::ct/default :end-run-tests] [m]
  (if (ct/successful? m)
    (js/process.exit 0)
    (js/process.exit 1)))

(defn main []
  (-> (env/get-test-data)
      (env/reset-test-data!))
  (st/run-all-tests))
