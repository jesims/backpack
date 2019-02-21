(ns io.jesi.backpack.json
  (:require
    [clojure.string :as string]
    #?(:clj [cheshire.core :refer [generate-string parse-string]])
    #?(:cljs [io.jesi.backpack.clojurescript :refer [clj->js js->clj]])))

(defn clj->json [x]
  #?(:cljs (js/JSON.stringify (clj->js x))))

(defn json->clj [s]
  #?(:cljs (when-not (string/blank? s)
             (some-> s
                     js/JSON.parse
                     js->clj))))
