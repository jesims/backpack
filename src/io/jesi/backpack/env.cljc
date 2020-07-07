(ns io.jesi.backpack.env
  (:refer-clojure :exclude [symbol])
  #?(:cljs (:require-macros [io.jesi.backpack.env]))
  (:require
    [clojure.string :as str]
    [com.rpl.specter :as sp]
    [io.jesi.backpack.fn :refer [or-fn]]
    [io.jesi.backpack.common :refer [named? namespaced?]]))

(defn cljs?
  "Take the &env from a macro, and tell whether we are expanding into CLJS."
  [env]
  ;https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ
  (boolean (:ns env)))

;TODO better runtime detection that doesn't require predicates

(def platforms
  "An atom containing the platform specific predicates. Is an map of predicates,
  and their platform keyword value. The predicate should take a macro `&env` and return a
  boolean. See https://clojure.org/guides/reader_conditionals for platform keywords."
  (atom (hash-map cljs? :cljs)))

(def ^:deprecated runtimes platforms)

(defn- platform [env]
  (if (keyword? env)
    env
    (some->> @platforms
             (filter (fn [e]
                       (let [pred (key e)]
                         (pred env))))
             first
             val)))

;TODO rename to `->converter`
(defmulti converter
  "Platform specific converters.
  Takes the `env` (from `&env` or platform keyword) and returns a converter function.
  The converter function takes a symbol and returns the platform specific version of that symbol."
  platform)

(defmethod converter :cljs [env]
  ;TODO convert `catch` clause also?
  ;TODO make conversion automatic. Use value in `env` or use cljs.analyzer ns
  (fn ->cljs [sym]
    (let [ns (namespace sym)]
      (if (and (str/starts-with? ns "clojure.")
               (not (contains?
                      #{"clojure.core"
                        "clojure.core.protocols"
                        "clojure.core.reducers"
                        "clojure.data"
                        "clojure.datafy"
                        "clojure.edn"
                        "clojure.reflect"
                        "clojure.set"
                        "clojure.string"
                        "clojure.walk"
                        "clojure.zip"}
                      ns)))
        (with-meta
          (clojure.core/symbol
            (str "cljs" (subs ns (str/index-of ns \.)))
            (name sym))
          (meta sym))
        sym))))

(defmethod converter :default [_]
  nil)

(defn symbol
  "Takes the target `env` (from `&env` or platform keyword), and a quoted symbol.
  Transforms the symbol to platform specific symbol.

  `(symbol :cljs 'clojure.core.async)` => `cljs.core.async`"
  [env sym]
  {:pre [(symbol? sym)
         (namespaced? sym)]}
  (if-let [converter (converter env)]
    (converter sym)
    sym))

(def ^:private list-walker
  (sp/recursive-path [] l
    (sp/if-path (or-fn list? seq?)
      (sp/continue-then-stay sp/ALL-WITH-META l))))

(defn- quoted? [[fn]]
  (and (named? fn)
       (= "quote" (name fn))))

(defn- convert [converter quoted-form]
  ;FIXME throws StackOverflowError for deeply nested forms e.g. jesi-web tests
  (sp/transform [list-walker (sp/if-path quoted? (sp/nthpath 1) sp/FIRST) symbol? namespaced?] converter quoted-form))

(defn transform*
  "Transforms the symbols in a quoted form to platform specific symbols.
  Takes an `env` (from `&env` or platform keyword), and a `quoted-form`
  to be transformed.

  WARNING: Will throw StackOverflowError for deeply nested forms
  Use `io.jesi.backpack.env/symbol` to transform individual symbols"
  [env quoted-form]
  {:pre [(not (symbol? quoted-form))]}
  (if-let [converter (converter env)]
    (convert converter quoted-form)
    quoted-form))

;TODO figure how to keep line numbers. macros are removing line numbers, by removing &from metadata
; use https://github.com/ztellman/riddley ?

(defmacro transform
  "Transforms the symbols in a quoted form to platform specific symbols.
  Takes a `quoted-form` to be transformed. Use this in macros.
  e.g. `(defmacro go [& body] `(env/transform (async/go ~@body)))`

  WARNING: Will throw StackOverflowError for deeply nested forms.
  Use `io.jesi.backpack.env/symbol` to transform individual symbols"
  ([quoted-form]
   (transform* &env quoted-form)))
