(ns io.jesi.backpack.miscellaneous
  (:refer-clojure :exclude [assoc-in])
  (:require
    #?@(:cljs [[goog.Uri]
               [clojure.string :as str]])
    [io.jesi.backpack.macros :refer [#?(:cljs def-) catch->nil]]
    [io.jesi.backpack.string :refer [uuid-str?]])
  #?(:clj
     (:import
       (java.net URI)
       (java.util UUID)
       (java.util.regex Pattern))))

(defmulti ->uuid
  "Coerces a value into a UUID if possible, otherwise returns nil"
  type)

(defmethod ->uuid :default [_] nil)

(defmethod ->uuid UUID [s] s)

#?(:clj  (defmethod ->uuid String [s]
           (when (uuid-str? s)
             (UUID/fromString s)))

   :cljs (defmethod ->uuid js/String [s]
           (when (uuid-str? s)
             (UUID. s nil))))

(defn ->uuid-or-not [id]
  (or (->uuid id) id))

(defn ->uri [s]
  (cond
    (uri? s) s
    (string? s) (catch->nil #?(:clj  (URI. s)
                               :cljs (goog.Uri. s)))
    :else nil))

(defn xor
  "Returns `true` only if one argument is `true`"
  ([] nil)
  ([x] x)
  ([x y]
   (and (or x y)
        (not (and x y))))
  ([x y & more]
   (xor (xor x y) (apply xor more))))

;see https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#escaping
;regex literal does not work here (wants more \\\), but works in lumo...
;#"([.*+?^${}()|[\]\\])"
#?(:cljs (def- re-quote-pattern (re-pattern "([.*+?^${}()|[\\]\\\\])")))

(defn re-quote
  "Quotes the regex string"
  [s]
  #?(:clj  (Pattern/quote s)
     :cljs (str/replace s re-quote-pattern "\\$1")))
