(ns io.jesi.backpack.random
  (:refer-clojure :exclude [uuid])
  #?(:clj
     (:require
       [clojure.set :as set]
       [clojure.string :as string])
     :cljs
     (:require
       [clojure.set :as set]
       [clojure.string :as string]
       [cljs-uuid-utils.core :as UUID]
       [goog.string :refer [format]]
       [goog.string.format]))
  #?(:clj
     (:import (java.util UUID))))

(defn uuid
  "Generates a random UUID"
  []
  #?(:clj  (UUID/randomUUID)
     :cljs (UUID/make-random-uuid)))

(defn uuid-str
  "Generates a random UUID string"
  [] (str (uuid)))

(def ^:private char-range (range 97 123))                   ;a-z

(defn character
  {:added "3.0.0"}
  []
  (char (rand-nth char-range)))

(def ^:private basic-chars
  (->> (concat
         (range 65 91)                                      ;A-Z
         char-range
         (range 48 58))                                     ;0-9
       (map char)
       (apply str)))

;Refer: https://en.wikipedia.org/wiki/List_of_Unicode_characters and https://clojure.org/reference/reader#_character
(def extended-chars
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

(defn- remove-exponential-chance [v]
  (if (< -0.1 v 0.1) (+ 0.2 v) v))

(defn lnglat
  "Generates a random [lng lat] value"
  []
  (let [lng (- (rand 360) 180)
        lat (- (rand 180) 90)]
    [(remove-exponential-chance lng)
     (remove-exponential-chance lat)]))

;Todo: Feels hackish
(defn- fmt [val]
  (-> (format "%.8f" val)
      (string/replace #"0+$" "")                            ; Removes trailing zeros
      (string/replace #"\.$" ".0")))                        ; Restores end zero if necessary

(defn wkt-linestring
  "Generates a random WellKnownText linestring value"
  ([] (wkt-linestring 2 10000))
  ([min max]
   (let [size (+ min (rand-int (+ max min)))]
     (->> (repeatedly size lnglat)
          (map (comp (partial string/join " ") #(mapv fmt %)))
          (string/join ",")))))
