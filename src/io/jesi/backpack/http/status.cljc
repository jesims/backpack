(ns io.jesi.backpack.http.status
  (:require
    [io.jesi.backpack.http.codes :as codes])
  #?(:cljs (:require-macros
             [io.jesi.backpack.http.status :refer [def-status]])))

(defmacro def-status [status-code quoted-sym]
  (let [sym (last quoted-sym)
        sym (symbol sym)]
    `(def ~sym ~status-code)))

(codes/intern def-status)
