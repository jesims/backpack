(ns io.jesi.backpack.macros
  (:refer-clojure :exclude [when-let])
  #?(:cljs (:require-macros io.jesi.backpack.macros)))

(defmacro import-vars
  "Imports a list of vars (including various metadata) from one namespace into the current namespace.
  Supports Clojure and ClojureScript. Similar to https://github.com/ztellman/potemkin

  ```clojure
  (import-vars
    [io.jesi.backpack.collection
     assoc-in
     ...]

    [io.jesi.backpack.fn
     apply-when
     ...]))
  ```"
  [& imports]
  `(do
     ~@(apply concat
         (for [[ns & names] imports
               name names
               :let [src (symbol (str ns) (str name))
                     meta (meta (resolve src))
                     meta-clone (select-keys meta [:arglists :no-doc :deprecated])
                     doc (get meta :doc "")]]
           `((def ~name ~doc ~src) (alter-meta! #'~name merge '~meta-clone))))))

(defmacro catch->nil
  "Wraps the `body` in a catch block, returning all thrown exceptions as `nil`"
  [& body]
  `(try
     ~@body
     (catch ~(if (:ns &env) :default `Throwable) ~'e)))

(defmacro ns-of
  "Gets the namespace string of the provided function"
  [f]
  `(-> ~f var meta :ns str))

#?(:clj
   (defn macro?
     "True if the provided `sym` is a macro"
     [sym]
     (:macro (meta (find-var sym)))))

(defmacro fn1
  {:deprecated true
   :no-doc     true}
  [& exprs]
  `(fn [_#] ~@exprs))

(defmacro fn2
  {:deprecated true
   :no-doc     true}
  [& exprs]
  `(fn [_# _#] ~@exprs))

(defmacro fn3
  {:deprecated true
   :no-doc     true}
  [& exprs]
  `(fn [_# _# _#] ~@exprs))

(defmacro when-let
  "An enhanced version of `clojure.core/when-let`.
   Evaluates the body only when **all** bindings are truthy"
  ([bindings & body]
   (if (seq bindings)
     `(clojure.core/when-let [~(first bindings) ~(second bindings)]
        (when-let ~(drop 2 bindings) ~@body))
     `(do ~@body))))

(defmacro defkw
  "Defines a symbol as the name of the given keyword in the current namespace"
  [kw]
  `(def ~(symbol (name kw)) ~kw))

;TODO this should not be used by cljs
; Source https://gist.github.com/Gonzih/5814945
(defmacro try*
  "Macro to catch multiple exceptions within a single body

  ```clojure
  (try*
    (condp = x
      0 (throw (Exception. \"Exception\"))
      1 (throw (RuntimeException. \"Runtime\"))
      3 (throw (ArithmeticException. \"Arithmetic\"))
      \"Not Caught\")
    (catch ArithmeticException _ \"ArithmeticException\")
    (catch-any [RuntimeException SecurityException] _ \"Multi\")
    (catch Exception _ \"Exception\"))))
  ```"
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
  "Returns a map with the keywords from the symbol names"
  [& symbols]
  (into (array-map)
    (map
      (fn [sym]
        [(keyword (name sym)) sym])
      symbols)))

(defmacro condf
  "Takes a value, and a set of binary predicate clauses.

  For each clause `(clause v)` is evaluated. If it returns logical true, the clause is a match and the result-expr is returned.
  A single default expression can follow the clauses, and its value will be returned if no clause matches.

  ```clojure
  (condf {:map 1}
    map? \"map\"
    string? \"string\"
    nil)
  ```"
  [v & clauses]
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
