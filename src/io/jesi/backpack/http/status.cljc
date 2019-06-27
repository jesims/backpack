(ns io.jesi.backpack.http.status
  #?(:cljs
     (:require-macros [io.jesi.backpack.http.status :refer [def-status]])))

(defmacro def-status [status-code quoted-sym]
  (let [sym (last quoted-sym)
        sym (symbol sym)]
    `(def ~sym ~status-code)))

(load-file "src/io/jesi/backpack/http/codes.clj")
