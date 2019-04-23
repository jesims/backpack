;TODO move to test utils library
(ns io.jesi.backpack.test.runner
  "Based on shadow.test.browser"
  (:require
    [cljs.test :as ct]
    [clojure.string :as string]
    ; commented out until https://github.com/pjstadig/humane-test-output/issues/37 is fixed
    ;[pjstadig.humane-test-output]
    [shadow.dom :as dom]
    [shadow.test :as st]
    [shadow.test.env :as env]))

;TODO colours!

(enable-console-print!)

(defonce ^:private log-node-id "log")

(defn- log-node []
  (dom/by-id log-node-id))

(defn- create-log-node []
  (when (nil? (log-node))
    (let [node (js/document.createElement "pre")]
      (.setAttribute node "id" log-node-id)
      (.appendChild (.-body js/document) node))))

(defn- append-log [& more]
  (some-> (log-node)
          (dom/append (str (string/join \space more) \newline))))

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
  (create-log-node)
  (js/console.clear)
  (-> (env/get-test-data)
      (env/reset-test-data!))
  (st/run-all-tests))

(defn stop [done]
  (when-let [node (log-node)]
    (set! (.-innerText node) ""))

  ;; FIXME: determine if async tests are still running
  ;; and call done after instead
  ;; otherwise a live reload might interfere with running tests by
  ;; reloading code in the middle
  (done))

(defn ^:export init []
  (start))
