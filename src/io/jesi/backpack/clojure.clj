(ns io.jesi.backpack.clojure
  (:require
    [clojure.pprint :as pprint]
    [io.jesi.backpack.closer :refer [close]]
    [io.jesi.backpack.collection :refer [transform-keys]]
    [io.jesi.backpack.string :refer [->kebab-case ->kebab-case-key]])
  (:import
    (clojure.lang IFn)
    (java.lang AutoCloseable)))

(defn defkw-type [type kw & args]
  (intern *ns*
    (symbol (->kebab-case (name kw)))
    (apply type (concat [kw] args))))

(defn java->clj
  "Transforms Java to Clojure. Converting keys to kebab-case keywords by default"
  ([j]
   (java->clj j ->kebab-case-key))
  ([j key-fn]
   (some->> j
            (transform-keys key-fn))))

(defn pprint-str [object]
  (pprint/write object
    :pretty true
    :stream nil))

(defn pprint-str-code [object]
  (pprint/write object
    :pretty true
    :stream nil
    :dispatch pprint/code-dispatch))

(defmethod close AutoCloseable [o]
  (-> ^AutoCloseable o (.close)))

(defn ^Thread add-shutdown-hook-fn
  "Adds a shutdown hook using java.lang.Runtime.addShutdownHook. Takes a function `f`, and returns the created Thread"
  [^IFn f]
  (let [^Thread thread (Thread. f)]
    (-> (Runtime/getRuntime)
        (.addShutdownHook thread))
    thread))

(defmacro add-shutdown-hook
  "Adds a shutdown hook using java.lang.Runtime.addShutdownHook. Returns the created Thread"
  [& body]
  `(add-shutdown-hook-fn (fn [] ~@body)))
