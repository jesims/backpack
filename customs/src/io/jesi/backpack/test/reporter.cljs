(ns io.jesi.backpack.test.reporter
  (:require
    [cljs-test-display.core :as ctd]
    [cljs.test :as ct]
    [io.jesi.backpack.async :as async]))

(defonce done-chan (async/chan))

(def ^:private ^:const dispatch-val [::ctd/default :summary])

(defonce ^:private report-summary (get-method ct/report dispatch-val))

(defmethod ct/report dispatch-val [m]
  (async/put! done-chan true)
  (report-summary m))
