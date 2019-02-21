(ns io.jesi.backpack.json
  #?(:cljs (:refer-clojure :exclude [clj->js js->clj]))
  (:require
    #?(:clj [cheshire.core :refer [generate-string parse-string]])
    #?(:clj [io.jesi.backpack.string :refer [->kebab-case-key ->camelCase]])
    #?(:cljs [clojure.string :as string])
    #?(:cljs [io.jesi.backpack.clojurescript :refer [clj->js js->clj]])))

(defn clj->json [x]
  #?(:cljs (js/JSON.stringify (clj->js x))
     :clj  (generate-string x {:key-fn ->camelCase})))

(defn json->clj [s]
  #?(:cljs (when-not (string/blank? s)
             (some-> s
                     js/JSON.parse
                     js->clj))
     :clj  (parse-string s ->kebab-case-key)))
