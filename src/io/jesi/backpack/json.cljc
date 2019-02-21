(ns io.jesi.backpack.json
  (:require
    [clojure.string :as string]
    #?(:clj[cheshire.core :refer[generate-string parse-string] ])
    #?(:cljs[io.jesi.backpack.clojurescript :refer [clj->js]]))

#?(:cljs(defn clj->json [x] )
   (js/JSON.stringify (clj->js x)))

#?(:cljs (defn json->clj [s]
           (when-not (string/blank? s)
                                 (some-> s
                                         js/JSON.parse
                                         js->clj))))
