(ns io.jesi.backpack.env
  (:refer-clojure :exclude [symbol])
  #?(:cljs (:require-macros [io.jesi.backpack.env]))
  (:require
    [clojure.string :as str]
    [com.rpl.specter :as sp]
    [io.jesi.backpack.fn :refer [or-fn]]
    [io.jesi.backpack.miscellaneous :refer [named? namespaced?]]))

(defn cljs?
  "Take the &env from a macro, and tell whether we are expanding into CLJS."
  [env]
  ;https://groups.google.com/d/msg/clojurescript/iBY5HaQda4A/w1lAQi9_AwsJ
  (boolean (:ns env)))

;TODO better runtime detection that doesn't require predicates

(def runtimes
  "An atom containing the runtime specific predicates. Is an array-map of predicates,
  and their runtime keyword value. The predicate should take a macro `&env` and return a
  boolean. The runtime keyword should be a reader conditional platform tag.
  See https://clojure.org/guides/reader_conditionals"
  (atom (array-map cljs? :cljs)))

(defn- runtime [env]
  (if (keyword? env)
    env
    (or (->> @runtimes
             (filter (fn [[pred]]
                       (pred env)))
             first
             second)
        :default)))

(defmulti converter "Runtime specific converters"
  (fn [runtime] runtime))

(defmethod converter :cljs [_]
  ;TODO use (-> &env :ns :ns-aliases) for converting to cljs?
  ;TODO convert `catch` clause also?
  (fn ->cljs [sym]
    (let [ns (namespace sym)]
      (if (and (str/starts-with? ns "clojure.")
               (not= "clojure.core" ns))
        (with-meta
          (clojure.core/symbol
            (str "cljs" (subs ns (str/index-of ns \.)))
            (name sym))
          (meta sym))
        sym))))

(defmethod converter :default [_]
  identity)

(defn- symbol [env sym]
  {:pre [(symbol? sym)
         (namespaced? sym)]}
  (let [converter (converter (runtime env))]
    (converter sym)))

(def ^:private list-walker
  (sp/recursive-path [] l
    (sp/if-path (or-fn list? seq?)
      (sp/continue-then-stay sp/ALL-WITH-META l))))

(defn- quoted? [[fn :as form]]
  (and (named? fn)
       (= "quote" (name fn))))

(defn transform*
  "Transforms the symbols in a quoted form to runtime specific symbols.
  Takes an `env` (from `&env` or runtime keyword), and a `quoted-form`
  to be transformed."
  [env quoted-form]
  {:pre [(not (symbol? quoted-form))]}
  (let [converter (converter (runtime env))]
    (if (identical? identity converter)
      quoted-form
      (sp/transform [list-walker (sp/if-path quoted? (sp/nthpath 1) sp/FIRST) symbol? namespaced?] converter quoted-form))))

;TODO figure how to keep line numbers. macros are removing line numbers, by removing &from metadata

(defmacro transform
  "Transforms the symbols in a quoted form to runtime specific symbols.
  Takes a `quoted-form` to be transformed. Use this in macros.
  e.g. `(defmacro go [& body] `(env/transform (async/go ~@body)))`"
  ([quoted-form]
   (transform* &env quoted-form)))
