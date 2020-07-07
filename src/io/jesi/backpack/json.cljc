(ns io.jesi.backpack.json
  #?(:cljs (:refer-clojure :exclude [clj->js js->clj]))
  (:require
    #?(:clj [cheshire.core :refer [generate-string parse-string]])
    #?(:clj [io.jesi.backpack.macros :refer [shorthand]])
    #?(:cljs [clojure.string :as str])
    #?(:cljs [io.jesi.backpack.clojurescript :refer [clj->js js->clj]])
    [io.jesi.backpack.string :refer [->camelCase ->kebab-case-key]]))

(defn ^:deprecated clj->json
  "DEPRECATED: Use exchange project"
  ([o] (clj->json o ->camelCase))
  ([o key-fn]
   #?(:cljs (js/JSON.stringify (clj->js o key-fn))
      :clj  (generate-string o (shorthand key-fn)))))

(defn ^:deprecated json->clj
  "DEPRECATED: Use exchange project"
  ([s] (json->clj s ->kebab-case-key))
  ([s key-fn]
   #?(:cljs (when-not (str/blank? s)
              (some-> s
                      js/JSON.parse
                      (js->clj key-fn)))
      :clj  (parse-string s key-fn))))
