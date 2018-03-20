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
                 #^{:static true} [string [] String]
                 #^{:static true} [alphaNumeric [int] String]
                 #^{:static true} [alphaNumeric [] String]])))

(defn uuid []
  #?(:clj  (UUID/randomUUID)
     :cljs (UUID/make-random-uuid)))

(defn uuid-str [] (str (uuid)))

(def ^:private basic-chars
  (->> [;A-Z
        (range 65 91)
        ;a-z
        (range 97 123)
        ;0-9
        (range 48 58)]
       flatten
       (map char)
       (apply str)))

;Refer: https://en.wikipedia.org/wiki/List_of_Unicode_characters and https://clojure.org/reference/reader#_character
(def ^:private extended-chars
  (->> [\newline \space \tab \formfeed \backspace \return]
       (concat (range 33 127) (range 160 173) (range 174 256))
       (map char)
       (apply str)))

(defn- gen-str [chars size]
  (apply str (take size (repeatedly #(rand-nth chars)))))

(defn string
  "Generates a random string of size (24 character length default)"
  ([] (string 24))
  ([size]
   (gen-str extended-chars size)))

(defn alpha-numeric
  "Generates a random string alpha-numeric characters [A-Za-z0-9] (24 character length default)"
  ([] (alpha-numeric 24))
  ([size]
   (gen-str basic-chars size)))

(defn alphaNumeric
  "Generates a random string alpha-numeric characters [A-Za-z0-9] (24 character length default)

  Note: Needed for gen-class java method (as hyphenated names are not permitted)
  "
  ([] (alpha-numeric 24))
  ([size] (alpha-numeric size)))
