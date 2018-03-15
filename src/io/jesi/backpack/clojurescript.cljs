(ns io.jesi.backpack.clojurescript
  (:require
    [clojure.string :as string]))

(declare clj->jskw)

(defn- key->jskw [k]
  (if (satisfies? IEncodeJS k)
    (-clj->js k)
    (if (or (string? k)
            (number? k)
            (keyword? k)
            (symbol? k))
      (clj->jskw k)
      (pr-str k))))

(defn clj->jskw
  "Note: Altered from cljs.core to encode keywords
  Recursively transforms ClojureScript values to JavaScript.
  sets/vectors/lists become Arrays, Keywords and Symbol become Strings,
  Maps become Objects. Arbitrary keys are encoded to by key->js."
  [x]
  (when-not (nil? x)
    (if (satisfies? IEncodeJS x)
      (-clj->js x)
      (cond
        (keyword? x) (string/replace (str (keyword x)) #"^:" "")
        (symbol? x) (str x)
        (map? x) (let [m (js-obj)]
                   (doseq [[k v] x]
                     (aset m (key->jskw k) (clj->jskw v)))
                   m)
        (coll? x) (let [arr (array)]
                    (doseq [x (map clj->jskw x)]
                      (.push arr x))
                    arr)
        :else x))))
