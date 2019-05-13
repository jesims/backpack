;TODO move to test utils library
(ns io.jesi.backpack.test.runner
  "Based on shadow.test.browser"
  {:dev/always true}
  (:require
    [cljs-test-display.core :as ctd]
    [io.jesi.backpack.async :as async]
    [io.jesi.backpack.test.reporter :refer [done-chan]]
    [shadow.dom :as dom]
    [shadow.test :as st]
    [shadow.test.env :as env]))

;TODO colours
;TODO better test diff
(enable-console-print!)

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
