(ns io.jesi.backpack.string
  (:refer-clojure :exclude [subs])
  (:require
    [clojure.string :as str]
    [io.jesi.backpack.fn :refer [and-fn if-fn or-fn]]))

(defn- normalize-str-idx [length i]
  (if (neg? i)
    (+ length i)
    i))

(defn subs
  ([s start] (subs s start nil))
  ([s start end]
   {:pre [(or (nil? start) (int? start))
          (or (nil? end) (int? end))]}
   (when (seq s)
     (let [length (count s)
           start (or start 0)
           end (or end length)
           normalize-idx (partial normalize-str-idx length)
           pos-start (normalize-idx start)
           pos-end (normalize-idx end)
           invalid-idx? (or-fn neg? (partial < length))]
       (cond
         (invalid-idx? pos-end)
         (throw (ex-info "End out of bounds" {:s s :start start :end end}))

         (invalid-idx? pos-start)
         (throw (ex-info "Start out of bounds" {:s s :start start :end end}))

         (< pos-end pos-start)
         ""

         :else
         (clojure.core/subs s pos-start pos-end))))))

(defn uuid-str?
  "True if 's' is a string and matches the UUID format"
  [s]
  (and (string? s) (re-matches #"(\w{8}(-\w{4}){3}-\w{12}?)$" s)))

(defn remove-prefix
  "Removes the prefix if the string starts with it otherwise ignores, is case sensitive"
  ([prefix s]
   (remove-prefix prefix ", " s))

  ([prefix separator s]
   (let [match (str prefix separator)]
     (if (str/starts-with? s match)
       (subs s (count match))
       s))))

(defn subs-to
  "Returns the substring of 's' up until the 'match'"
  [match s]
  (let [index (str/index-of s match)]
    (if (nil? index)
      s
      (subs s 0 index))))

(defn subs-inc
  "Returns the substring of 's' up to and including the 'match' or nil"
  [match s]
  (let [index (str/index-of s match)]
    (if (nil? index)
      nil
      (subs s 0 (+ index (count match))))))

(defn true-string?
  "True if 's' is the string literal 'true'"
  [s]
  (= s "true"))

(defn- kw->str [k]
  (if-let [ns (namespace k)]
    (str ns \/ (name k))
    (name k)))

(def ^:private ->str (if-fn keyword? kw->str str))

(defn ->camelCase [s]
  (when s
    (let [[head & tail] (str/split (->str s) #"-|(?=[A-Z])")]
      (str/join (cons (str/lower-case head) (map str/capitalize tail))))))

(defn ->kebab-case [s]
  (some-> s
          ->str
          (str/replace #"([A-Z]{2,})([a-z])" "$1 $2")
          (str/replace #"([a-z])([A-Z])" "$1 $2")
          (str/replace \_ \-)
          (str/replace #"\s" "-")
          str/lower-case))

(defn ->snake_case [s]
  (some-> s
          ->kebab-case
          (str/replace \- \_)))

(def ->kebab-case-key (comp keyword ->kebab-case))

(def ->camelCase-key (comp keyword ->camelCase))

(def ->snake_case-key (comp keyword ->snake_case))

(defn- create-affix [bipred f]
  (fn [substr s]
    (let [[s :as args] (map str [s substr])]
      (if (apply bipred args)
        s
        (apply str (f args))))))

(def prefix (create-affix str/starts-with? reverse))

(def suffix (create-affix str/ends-with? identity))

(def not-blank? (and-fn string? (complement str/blank?)))
