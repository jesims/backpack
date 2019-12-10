(ns io.jesi.backpack.transit
  (:require
    [cognitect.transit :as transit]
    [io.jesi.backpack.macros :refer [def-]])
  #?(:clj (:import
            (java.io ByteArrayInputStream ByteArrayOutputStream Writer)
            (java.nio.charset Charset StandardCharsets))))

(def- write-opts {:transform transit/write-meta})

#?(:cljs (do
           (def- transit-writer (transit/writer :json write-opts))
           (def- transit-reader (transit/reader :json)))
   :clj  (do
           (defn- ->transit [o type]
             (let [baos (ByteArrayOutputStream.)
                   ^Writer writer (transit/writer baos type write-opts)]
               (transit/write writer o)
               ;TODO pass in charset
               (.toString baos (.name (StandardCharsets/UTF_8)))))

           (defn- ->clj [^String s type]
             ;TODO pass in charset
             (-> (.getBytes s (StandardCharsets/UTF_8))
                 (ByteArrayInputStream.)
                 (transit/reader type)
                 (transit/read)))))

(defn clj->transit [o]
  #?(:clj  (->transit o :json)
     :cljs (transit/write transit-writer o)))

(defn transit->clj [s]
  #?(:clj  (->clj s :json)
     :cljs (transit/read transit-reader s)))
