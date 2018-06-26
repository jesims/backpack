(ns io.jesi.backpack.macros
  (:refer-clojure :exclude [when-let])
  #?(:cljs (:require-macros io.jesi.backpack.macros)))

(defmacro import-vars
  [& imports]
  `(do
     ~@(apply concat
              (for [[ns & names] imports
                    name names
                    :let [src (symbol (str ns) (str name))]]
                `((def ~name ~src))))))

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
