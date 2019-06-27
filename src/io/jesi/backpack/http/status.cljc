(ns io.jesi.backpack.http.status
  (:require
    [io.jesi.backpack.http.codes :as codes])
  #?(:cljs (:require-macros
             [io.jesi.backpack.http.status :refer [def-status def-status-range]])))

(defmacro def-status [status-code quoted-sym]
  (let [sym (last quoted-sym)
        sym (symbol sym)]
    `(def ~sym ~status-code)))

(codes/intern def-status)

(defmacro def-status-range [start end quoted-sym]
  (let [sym (last quoted-sym)
        status-f-name? (symbol (str sym \?))]
    `(defn ~status-f-name? [status#]
       (boolean (and status#
                     (<= ~start status# ~end))))))

(def-status-range 200 299 'success)
(def-status-range 300 399 'redirection)
(def-status-range 400 499 'client-error)
(def-status-range 500 599 'server-error)
(def-status-range 400 599 'error)
