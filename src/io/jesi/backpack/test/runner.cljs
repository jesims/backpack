;TODO move to test utils library
(ns io.jesi.backpack.test.runner
  "Based on shadow.test.browser"
  {:dev/always true}
  (:require
    [cljs-test-display.core :as ctd]
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
  ;FIXME determine if async tests are still pending https://github.com/clojure/clojurescript-site/blob/master/content/tools/testing.adoc#detecting-test-completion--success
  (done))

(defn ^:export init []
  (dom/append [:div#test-root])
  (start))
