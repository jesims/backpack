(ns io.jesi.backpack.macros
  (:refer-clojure :exclude [when-let])
  #?(:cljs (:require-macros [io.jesi.backpack.macros]))
  (:require
    [clojure.core]
    [io.jesi.backpack.fn :refer [noop]]
    [io.jesi.backpack.miscellaneous :refer [cljs-env?]])
  #?(:cljs (:require [cljs.core :refer [IFn]]))
  #?(:clj
     (:import
       (clojure.lang IFn))))

(defmacro import-vars
  [& imports]
  `(do
     ~@(apply concat
         (for [[ns & names] imports
               name names
               :let [src (symbol (str ns) (str name))
                     meta (meta (resolve src))
                     arglists (get meta :arglists)
                     doc (get meta :doc "")]]
           `(
             (def ~name ~doc ~src)
             (alter-meta! #'~name assoc :arglists '~arglists))))))

(defmacro if-cljs
  "Return `then` if we are generating cljs code, and `else` for Clojure code"
  [then else]
  (if (cljs-env? &env)
    then
    else))

(defmacro catch-> [handle & body]
  `(try
     ~@body
     (catch ~(if (cljs-env? &env) :default `Throwable) ~'ex
       (~handle ~'ex))))

(defmacro catch->identity [& body]
  `(catch-> identity ~@body))

(defmacro catch->nil [& body]
  `(catch-> noop ~@body))

(defmacro ns-of [f]
  `(-> ~f var meta :ns str))

#?(:clj (defn macro? [sym]
          (:macro (meta (find-var sym)))))

(defmacro fn1 [& exprs]
  `(fn [_#] ~@exprs))

(defmacro fn2 [& exprs]
  `(fn [_# _#] ~@exprs))

(defmacro fn3 [& exprs]
  `(fn [_# _# _#] ~@exprs))

(defmacro when-let
  ([bindings & body]
   (if (seq bindings)
     `(clojure.core/when-let [~(first bindings) ~(second bindings)]
        (when-let ~(drop 2 bindings) ~@body))
     `(do ~@body))))

(defmacro defkw [kw]
  `(def ~(symbol (name kw)) ~kw))

;TODO this should not be used by cljs
; Source https://gist.github.com/Gonzih/5814945
(defmacro try*
  [& body]
  (letfn [(catch-any? [form]
            (and (seq form)
                 (= (first form) 'catch-any)))
          (expand [[_catch* classes & catch-tail]]
            (map #(list* 'catch % catch-tail) classes))
          (transform [form]
            (if (catch-any? form)
              (expand form)
              [form]))]
    (cons 'try (mapcat transform body))))

(defmacro shorthand
  "returns a map with the keywords from the symbol names"
  [& symbols]
  (into (array-map)
    (map
      (fn [sym]
        [(keyword (name sym)) sym])
      symbols)))

(defmacro condf [v & clauses]
  `(condp apply [~v]
     ~@clauses))

(defmacro defconsts
  "Defines a collection of string constant values as individual symbols transforming their values using body-fn."
  [body-fn & symbols]
  (let [names (map second symbols)]
    `(do
       ~@(for [name names
               :let [body (str name)]]
           `(def ~name (~body-fn ~body)))
       (def ~'-all (hash-set ~@names)))))

(defmacro when-not= [test body]
  `(when-not (= ~test ~body)
     ~body))

(defmacro when-debug [body]
  (if (cljs-env? &env)
    `(when ~(vary-meta 'js/goog.DEBUG assoc :tag 'boolean)
       ~body)
    body))

(defmacro reify-ifn
  "Defines IFn invoke implementations to call as `(invoke-fn this [args])`"
  [invoke-fn & more]
  (let [arg (comp symbol (partial str "arg"))
        cljs? (cljs-env? &env)
        sym (if cljs? '-invoke 'invoke)
        protocol (if cljs? 'IFn 'clojure.lang.IFn)
        code `(reify
                ~@more
                ~protocol
                ~@(for [arity (range 1 21)
                        :let [args (mapv arg (range arity))]]
                    `(~sym [this# ~@args]
                       (~invoke-fn this# ~args)))
                ~(let [args (mapv arg (range 1 21))]
                   (if cljs?
                     ; ShadowCLJS warns if using & to define more. But more than 20 args can be used
                     ; CLJS throws invocation exceptions if invoking with more than 20 args
                     ; See:
                     ; - https://clojure.atlassian.net/browse/CLJS-364
                     ; - https://github.com/hoplon/hoplon/issues/192
                     ; - https://github.com/reagent-project/reagent/issues/358
                     ; - https://clojure.atlassian.net/browse/CLJS-2710
                     `(~sym [this# ~@args more#]
                        (if (seq? more#)
                          (~invoke-fn this# (list* ~@args more#))
                          (~invoke-fn this# (concat ~args (list more#)))))
                     `(~sym [this# ~@args more#]
                        (~invoke-fn this# (list* ~@args more#))))))]
    (if cljs?
      code
      (concat code `(
                     (applyTo [this# args#]
                       (~invoke-fn this# args#)))))))
