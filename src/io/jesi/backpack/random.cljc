(ns io.jesi.backpack.random
  (:refer-clojure :exclude [uuid])
  #?(:clj
     (:import java.util.UUID)
     :cljs
     (:require
       [cljs-uuid-utils.core :as UUID])))

(defn uuid []
  #?(:clj  (UUID/randomUUID)
     :cljs (UUID/make-random-uuid)))

(defn uuid-str [] (str (uuid)))

(defn string
  "Generates a random string of size (default 24)"
  ([] (string 24))
  ([size]
   (apply str (take size (repeatedly #(char (+ (rand 26) 97)))))))
