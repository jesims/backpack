(ns io.jesi.backpack.random
  (:refer-clojure :exclude [uuid])
  #?(:clj
     (:import java.util.UUID)
     :cljs
     (:require
       [cljs-uuid-utils.core :as UUID]))
  #?(:clj
     (:gen-class
       :name io.jesi.backpack.Random
       :prefix ""
       :methods [#^{:static true} [string [int] String]
                 #^{:static true} [string [] String]])))

(defn uuid []
  #?(:clj  (UUID/randomUUID)
     :cljs (UUID/make-random-uuid)))

(defn uuid-str [] (str (uuid)))

(def ^:private range-of-chars
  (->> [8 9 10 13]
       (concat (range 32 256))
       (map char)
       (apply str)))

(defn string
  "Generates a random string of size (default 24)"
  ([] (string 24))
  ([size]
   (apply str (take size (repeatedly #(rand-nth range-of-chars))))))
