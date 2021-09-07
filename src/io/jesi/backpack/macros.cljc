(ns io.jesi.backpack.macros
  (:refer-clojure :exclude [when-let with-open])
  #?(:cljs (:require-macros [io.jesi.backpack.macros]))
  (:require
    #?(:cljs [cljs.core :refer [IFn]])
    [clojure.core]
    [io.jesi.backpack.atom :as atom]
    [io.jesi.backpack.closer :refer [close]]
    [io.jesi.backpack.env :as env]
    [io.jesi.backpack.fn :refer [noop]])
  #?(:clj
     (:import
       (clojure.lang IFn))))

(defmacro import-vars
  "Imports a all symbols (including various metadata) from one namespace into the current namespace.
   Supports Clojure and ClojureScript. Similar to https://github.com/ztellman/potemkin
```clojure
(import-vars
  [io.jesi.backpack.collection
   io.jesi.backpack.fn
   ...])
```"
  [& imports]
  #?(:clj `(do
             ~@(apply concat
                 (for [import imports
                       :let [vars (->> (if (symbol? import)
                                         (do
                                           (require import)
                                           (vals (ns-publics import)))
                                         (let [ns (first import)]
                                           (map
                                             (fn [name]
                                               (require ns)
                                               (let [sym (symbol (str ns) (str name))
                                                     var (resolve sym)]
                                                 (when (nil? var)
                                                   (throw (ex-info (str "Could not resolve var " sym) {:symbol sym
                                                                                                       :ns     *ns*
                                                                                                       :env    &env})))
                                                 var))
                                             (rest import))))
                                       (remove (comp :import/exclude meta)))]]
                   (apply concat
                     (for [var vars
                           :let [sym (symbol var)
                                 name (-> sym name symbol)
                                 {:keys [doc]
                                  :or   {doc ""}
                                  :as   sym-meta} (meta var)]]
                       `[(def ~name ~doc ~sym)
                         (alter-meta! #'~name (partial merge (meta #'~sym)))])))))))

(defmacro catch-> [handle & body]
  `(try
     ~@body
     (catch ~(if (env/cljs? &env) :default `Throwable) ~'ex
       (~handle ~'ex))))

(defmacro catch->identity
  "Wraps the `body` in a catch block, returning the result of the `body` or any thrown exception"
  [& body]
  `(catch-> identity ~@body))

(defmacro catch->nil
  "Wraps the `body` in a catch block, returning all thrown exceptions as `nil`"
  [& body]
  `(catch-> noop ~@body))

(defmacro ns-of
  "Gets the namespace (as string) of the provided symbol"
  [sym]
  `(-> ~sym var meta :ns str))

#?(:clj (defn macro?
          "True if the provided `sym` is a macro"
          [sym]
          (:macro (meta (find-var sym)))))

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

(defn shorthand* [keyfn symbols]
  (->> symbols
       (map (fn [sym]
              [(keyfn sym) sym]))
       (into {})))

(defmacro shorthand
  "Returns a map with the keywords from the symbol names"
  [& symbols]
  (shorthand* (comp keyword name) symbols))

(defmacro shorthand-str
  "String `shorthand`. Returns a map with string keys from the symbol names"
  [& symbols]
  (shorthand* name symbols))

(defmacro shorthand-assoc
  ([map sym]
   `(assoc ~map ~(keyword (name sym)) ~sym))
  ([map sym & more]
   `(assoc ~map ~@(->> (cons sym more)
                       ;;TODO make point-free
                       (reduce
                         (fn [s sym]
                           (list* (keyword (name sym)) sym s))
                         '())))))

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

(defmacro when-not= [test body]
  `(when-not (= ~test ~body)
     ~body))

(defmacro when-debug [& body]
  (if (env/cljs? &env)
    `(when ~(vary-meta 'js/goog.DEBUG assoc :tag 'boolean)
       ~@body)
    (if (next body)
      `(do
         ~@body)
      (first body))))

(defmacro reify-ifn
  "Defines IFn invoke implementations to call as `(invoke-fn this [args])`.
  Note: Protocols do not support var args"
  [invoke-fn & more]
  (let [arg (comp symbol (partial str "arg"))
        cljs? (env/cljs? &env)
        sym (if cljs? '-invoke 'invoke)
        protocol (if cljs? 'IFn 'clojure.lang.IFn)
        code `(reify
                ~@more
                ~protocol
                ~@(for [arity (range 1 21)
                        :let [args (mapv arg (range arity))]]
                    `(~sym [this# ~@args]
                       (apply ~invoke-fn this# ~args)))
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
                        (apply ~invoke-fn this# (concat ~args (if (seq? more#)
                                                                more#
                                                                (list more#)))))
                     `(~sym [this# ~@args more#]
                        (apply ~invoke-fn this# (concat ~args more#))))))]
    (if cljs?
      code
      (concat code `(
                     (applyTo [this# args#]
                       (apply ~invoke-fn this# args#)))))))

(defn- private [sym]
  (vary-meta sym assoc :private true))

(defmacro def-
  "Creates and interns a private var with the name of symbol in the
  current namespace (`*ns*`) or locates such a var if it already exists.
  If init is supplied, it is evaluated, and the root binding of the
  var is set to the resulting value. If init is not supplied, the
  root binding of the var is unaffected."
  ([symbol]
   `(def ~(private symbol)))
  ([symbol init]
   `(def ~(private symbol) ~init))
  ([symbol doc-string init]
   `(def ~(private symbol) ~doc-string ~init)))

(defmacro with-open
  "bindings => [sym init ...]

  Evaluates body in a try expression with symbols bound to the values
  of the inits, and a finally clause that calls (bp/close sym) on each
  sym in reverse order."
  [bindings & body]
  {:pre [(vector? bindings)
         (even? (count bindings))]}
  (cond
    (zero? (count bindings))
    `(do ~@body)

    (symbol? (bindings 0))
    `(let ~(subvec bindings 0 2)
       (try
         (with-open ~(subvec bindings 2)
           ~@body)
         (finally
           (close ~(bindings 0)))))

    :else
    (throw (#?(:clj IllegalArgumentException. :cljs js/Error.) "with-open only allows Symbols in bindings"))))

(defmacro with-open-> [x & forms]
  `(with-open [v# ~x]
     (-> v#
         ~@forms)))

(defmacro assoc-nx
  "Lazily assocs each kv if its key doesn't already exist.
   Macro version of taoensso.encore/assoc-nx"
  ([m k v]
   `(let [m# ~m
          k# ~k]
      (if (contains? m# k#)
        m#
        (assoc m#
          k# ~v))))
  ([m k v & kvs]
   {:pre [(even? (count kvs))]}
   (let [kvs (vec (list* k v kvs))]
     `(assoc-nx ~m ~kvs)))
  ([m kvs]
   {:pre [(even? (count kvs))]}
   `(-> ~m
        ~@(for [[k v] (partition 2 kvs)]
            `(assoc-nx ~k ~v)))))

(defmacro assoc-nx!
  "Lazily `atom/assoc!` if the key does not already exist.
  Returns the value in the atom"
  [a k v]
  ;;TODO make this 100% thread-safe
  ;;currently body could be evaluated concurrently between the @ and the swap! (atom/assoc!)
  ;;create own Atom implementation that does lock
  `(let [a# @~a]
     (-> (if (contains? a# ~k)
           a#
           (atom/assoc! ~a ~k ~v))
         (get ~k))))
