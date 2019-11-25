(ns io.jesi.backpack.test.runner.util
  (:require
    [cljs.test :as test]
    [clojure.string :as str]
    [pjstadig.humane-test-output]
    [pjstadig.print :as p]
    [pjstadig.util :as util]))

(defn convert-event [event]
  ;TODO better string diff
  (let [event (p/convert-event event)
        code (->> (with-out-str
                    (binding [test/*current-env* (dissoc (test/get-current-env) :testing-contexts :report-counters)]
                      (util/report- (dissoc event :message))))
                  str/trim
                  ;skip "FAIL in" line
                  str/split-lines
                  rest
                  (str/join \newline))]
    [event code]))
