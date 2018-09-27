(ns io.jesi.backpack.test.runner
  "Based on shadow.test.browser"
  (:require
    [cljs.test :as ct]
    [clojure.string :as string]
    [pjstadig.humane-test-output]
    [shadow.dom :as dom]
    [shadow.test :as st]))

;TODO colours!

(enable-console-print!)

(def log-node (delay (dom/by-id "log")))

(defn- append-log [& more]
  (dom/append @log-node (str (string/join \  more) \newline)))

(set-print-fn! append-log)
(set-print-err-fn! append-log)

(js/window.addEventListener "error"
  (fn [evt]
    (let [msg (.-message evt)
          file (.-filename evt)
          line (.-lineno evt)]
      (append-log "ERROR:" (str msg "(" file ":" line ")")))))

(defmethod ct/report [::ct/default :begin-test-ns] [_])

;TODO show ns and testing string on test failure
;(defmethod ct/report [::ct/default :fail-test-ns] [_])

(defmethod ct/report [::ct/default :error] [{:keys [message actual] :as m}]
  (ct/inc-report-counter! :error)
  (println \newline "ERROR in" (ct/testing-vars-str m))
  (when (seq (:testing-contexts (ct/get-current-env)))
    (println (ct/testing-contexts-str)))
  (when message
    (println message))
  (ct/print-comparison m)
  (when-let [stack (.-stack actual)]
    (js/console.log stack)
    (println stack)))

(defn start []
  (st/run-all-tests))

(defn stop [done]
  (let [log @log-node]
    (set! (.-innerText log) ""))

  ;; FIXME: determine if async tests are still running
  ;; and call done after instead
  ;; otherwise a live reload might interfere with running tests by
  ;; reloading code in the middle
  (done))

(defn ^:export init []
  (start))
