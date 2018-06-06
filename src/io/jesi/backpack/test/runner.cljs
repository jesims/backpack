(ns io.jesi.backpack.test.runner
  "Based on shadow.test.browser"
  (:require
    [cljs.test :as ct]
    [pjstadig.humane-test-output]
    [shadow.dom :as dom]
    [shadow.test :as st]))

(enable-console-print!)

;TODO colours!
(defonce log-node (dom/by-id "log"))

(when log-node
  (set-print-fn!
    (fn [s]
      (dom/append log-node (str s "\n"))))

  (js/window.addEventListener "error"
    (fn [evt]
      (let [msg (.-message evt)
            file (.-filename evt)
            line (.-lineno evt)]
        (dom/append log-node (str "ERROR: " msg "(" file ":" line ")\n"))))))

;TODO show ns and testing string on test failure
(defmethod ct/report [::ct/default :begin-test-ns] [_])

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
