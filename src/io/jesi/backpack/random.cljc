(ns io.jesi.backpack.random
  (:refer-clojure :exclude [uuid])
  (:require
    [clojure.set :as set]
    #?(:cljs [cljs-uuid-utils.core :as UUID]))
  #?(:clj
     (:import (java.util UUID)))
  #?(:clj
     (:gen-class
       :name io.jesi.backpack.Random
       :prefix ""
       :methods [#^{:static true} [alphaNumeric [] String]
                 #^{:static true} [alphaNumeric [int] String]
                 #^{:static true} [extendedChars [] String]
                 #^{:static true} [string [] String]
                 #^{:static true} [string [int] String]
                 #^{:static true} [wktLinestring [] String]
                 #^{:static true} [wktLinestring [int int] String]])))

(defn uuid []
  #?(:clj  (UUID/randomUUID)
     :cljs (UUID/make-random-uuid)))

(defn uuid-str [] (str (uuid)))

(def ^:private basic-chars
  (->> (concat
         (range 65 91)                                      ;A-Z
         (range 97 123)                                     ;a-z
         (range 48 58))                                     ;0-9
    (map char)
    (apply str)))

;Refer: https://en.wikipedia.org/wiki/List_of_Unicode_characters and https://clojure.org/reference/reader#_character
(def ^:private extended-chars
  (->>
    (concat
      (range 0 33)                                          ;Exclude c0 control characters
      (range 127 160)                                       ;Exclude c1 control characters
      [173]                                                 ;Exclude soft hyphen #shy
      ;µ to upper = Μ to lower = μ...WTF MATE
      ;ß to lower = lss to upper = LSS
      [181 223 956 924])                                    ; (map int [\µ \ß \μ \Μ ])
    set
    (set/difference (set (range 256)))
    (set/union #{\newline \space \tab \formfeed \backspace \return})
    (map char)
    sort
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

(defn extendedChars
  "Returns all characters used in random string generation"
  []
  extended-chars)

(defn lnglat []
  [(- (rand 360) 180)
   (- (rand 180) 90)])

(defn wkt-linestring
  ([] (wkt-linestring 2 10000))
  ([min max]
   (let [size (+ min (rand-int (- max min)))]
     (->> (repeatedly size lnglat)
       (map (partial clojure.string/join " "))
       (clojure.string/join ",")))))

(defn wktLinestring
  "Generates a random wkt Linestring (with default length between 2 and 10000)"
  ([] (wkt-linestring))
  ([min max] (wkt-linestring min max)))
