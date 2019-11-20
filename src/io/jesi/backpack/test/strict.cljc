;TODO move to test utils library
(ns io.jesi.backpack.test.strict
  (:refer-clojure :exclude [=])
  #?(:cljs (:require-macros [io.jesi.backpack.test.strict]))
  (:require
    [clojure.test]
    [clojure.string :as str]
    [io.jesi.backpack.miscellaneous :refer [env-specific]])
  #?(:clj (:import
            (java.util.regex Pattern))))

(defn =
  #?(:clj
     "Equality. Takes at least 2 args. Returns `true` if `x` equals `y`, `false` if not.
     Same as Java `x.equals(y)` except it also works for `nil`, and compares numbers
     and collections in a type-independent manner. Clojure's immutable data structures
     define `equals()` (and thus `=`) as a value, not an identity, comparison."
     :default
     "Equality. Takes at least 2 args. Returns `true` if `x` equals `y`, `false` if not.
     Compares numbers and collections in a type-independent manner. Clojure's immutable
     data structures define `-equiv` (and thus `=`) as a value, not an identity, comparison.")
  [x y & more]
  (if (seq more)
    (apply clojure.core/= x y more)
    (clojure.core/= x y)))

(defn- default-body [env body]
  (let [try-expr* (env-specific env 'clojure.test/try-expr)]
    (if (seq body)
      body
      [`(~try-expr* "Test is empty" nil)])))

(defmacro deftest
  "Like `clojure.test/deftest`, but will fail if `body` is empty."
  [name & body]
  {:pre [(symbol? name)]}
  (let [deftest* (env-specific &env 'clojure.test/deftest)]
    `(~deftest* ~name
       ~@(default-body &env body))))

(defmacro testing
  "Like `clojure.test/testing`, but will fail if `body` is empty."
  [string & body]
  {:pre [(string? string)
         (not (str/blank? string))]}
  (let [testing* (env-specific &env 'clojure.test/testing)]
    `(~testing* ~string
       ~@(default-body &env body))))

(defn thrown?
  "Checks that an instance of `c` is thrown from `body`, fails if not;
  then returns the thing thrown. Must be used in an `is` block."
  [c body]
  {:pre [#?(:clj     (instance? Class c)
            :default (some? c))
         (some? body)]}
  (assert nil "`thrown?` used not in `is` block"))

(defmacro is= [x y & more]
  (let [is* (env-specific &env 'clojure.test/is)
        =* (env-specific &env 'clojure.core/=)]
    `(~is* (~=* ~x ~y ~@more))))

(defn thrown-with-msg?
  "Checks that an instance of `c` is thrown AND that the message
  on the exception matches (with re-find) the regular expression `re`.
  Must be used in an `is` block."
  [c re body]
  {:pre [#?(:clj     (instance? Class c)
            :default (some? c))
         #?(:cljs (regexp? re)
            :clj  (instance? Pattern re))
         (some? body)]}
  (assert nil "`thrown-with-msg?` used not in `is` block"))

(defmacro is
  "Generic assertion macro. `form` is any predicate test.
  `msg` is an optional message to attach to the assertion.

  Example: `(is (= 4 (+ 2 2)) \"Two plus two should be 4\")`

  Special forms:

  `thrown?` checks that an instance of `c` is thrown from
  body, fails if not; then returns the thing thrown.

  `thrown-with-msg?` checks that an instance of `c` is
  thrown from `body` and that the message on the exception
  matches (with `re-find`) the regular expression `re`."
  ([form]
   {:pre [(some? form)]}
   (let [is* (env-specific &env 'clojure.test/is)]
     `(~is* ~form)))
  ([form msg]
   {:pre [(some? form)
          (some? msg)]}
   (let [is* (env-specific &env 'clojure.test/is)]
     `(~is* ~form ~msg))))

(defmacro are [argv expr & args]
  {:pre [(seq argv)
         (some? expr)
         (seq args)]}
  (let [are* (env-specific &env 'clojure.test/are)]
    `(~are* ~argv ~expr ~@args)))
