(ns io.jesi.backpack.macros
  (:refer-clojure :exclude [when-let]))

;Refer https://github.com/cmr-exchange/cmr-client/blob/70ca65d0103282906eec57aacfb9e4a98d9eebb3/src/cljc/cmr/client/common/util.cljc
(defmacro import-def
  "Import a single function or var:
  ```
  (import-def a b) => (def b a/b)
  ```"
  [from-ns def-name]
  (let [from-sym# (symbol (str from-ns) (str def-name))]
    `(def ~def-name ~from-sym#)))

(defmacro import-vars
  "Import multiple defs from multiple namespaces.
   This works for vars and functions, but not macros. Uses the same syntax as
   `potemkin.namespaces/import-vars`, namely:
   ```
   (import-vars
     [m.n.ns1 a b]
     [x.y.ns2 d e f])
  ```"
  [& imports]
  (let [expanded-imports (for [[from-ns & defs] imports
                               d defs]
                           `(import-def ~from-ns ~d))]
    `(do ~@expanded-imports)))

(defmacro catch->nil [& body]
  `(try
     ~@body
     (catch ~(if (:ns &env) :default `Throwable) ~'e)))

(defmacro ns-of [f]
  `(-> ~f var meta :ns str))

;TODO combine, macro? is also defined in io.jesi.spec.util-test
#?(:clj
   (defn macro? [sym]
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
