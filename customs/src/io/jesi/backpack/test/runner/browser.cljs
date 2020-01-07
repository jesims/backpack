(ns io.jesi.backpack.test.runner.browser
  "Based on shadow.test.browser"
  {:dev/always true}
  (:require
    [cljs-test-display.core :as ctd :refer [code contexts-node current-node current-node-parent div n printing span]]
    [cljs.test :as test]
    [clojure.string :as str]
    [goog.dom :as gdom]
    [goog.dom.classlist :as classlist]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.test.reporter :refer [done-chan]]
    [io.jesi.backpack.test.runner.util :refer [convert-event]]
    [pjstadig.util :as util]
    [shadow.dom :as dom]
    [shadow.test :as st]
    [shadow.test.env :as env]))

;TODO colours! in console and html
(enable-console-print!)

(defmethod test/report [::ctd/default :fail] [event]
  ;based on cljs-test-display.core/add-fail-node!
  (let [[event code] (convert-event event)
        curr-node (current-node)]
    (classlist/add curr-node "has-failures")
    (classlist/add (current-node-parent) "has-failures")
    (gdom/appendChild curr-node (div :test-fail
                                  (contexts-node)
                                  (div :fail-body
                                    (when-let [message (:message event)]
                                      (div :test-message message))
                                    (div
                                      (n :pre {}
                                        (n :code {} code))))))
    (if printing
      (util/report- event)
      (test/inc-report-counter! :fail))))

(defn start []
  (js/console.clear)
  (-> (env/get-test-data)
      (env/reset-test-data!))
  (st/run-all-tests (ctd/init! "test-root")))

(defn ^:dev/before-load-async stop [done]
  (async/go
    (println "Waiting for tests to complete...")
    ;TODO cancel async tests
    (async/<? done-chan)
    (done)))

(defn ^:export init []
  (dom/append [:div#test-root])
  (start))
