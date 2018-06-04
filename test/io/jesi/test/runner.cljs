(ns io.jesi.test.runner
  "Based on shadow.test.browser"
  (:require
    ;FIXME humane-test-output in browser tests
    [pjstadig.humane-test-output]
    [shadow.dom :as dom]
    [shadow.test :as st]))

(defonce log-node (dom/by-id "log"))
(when log-node
  (set-print-fn!
    (fn [s]
      (dom/append log-node (str s "\n")))))

(defn start []
  (st/run-all-tests))

(defn stop [done]
  (set! (.-innerText log-node) "")

  ;; FIXME: determine if async tests are still running
  ;; and call done after instead
  ;; otherwise a live reload might interfere with running tests by
  ;; reloading code in the middle
  (done))

(defn ^:export init []
  (start))
