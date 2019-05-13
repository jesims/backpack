(ns io.jesi.backpack.exceptions)

(defn exception?
  "Returns `true` if x is a Clojure Throwable or ClojureScript js/Error"
  [x]
  (and (some? x)
       #?(:clj  (instance? Throwable x)
          :cljs (instance? js/Error x))))

(defn throw-if-throwable
  "Throw ex if it's an exception. Retains the message, data, and cause"
  [ex]
  (if (exception? ex)
    (throw (ex-info
             (or (ex-message ex) (str ex))
             (or (ex-data ex) {})
             ex))
    ex))
