(ns io.jesi.backpack.json
  (:require
    [clojure.string :as string]
    [cheshire.core :refer :all]))

(defn clj->json
  [x]
  (js/JSON.stringify (clj->js x)))

(defn json->clj
  [s]
  (when-not (string/blank? s)
    (some-> s
            js/JSON.parse
            js->clj)))
