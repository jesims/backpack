(ns io.jesi.backpack.string
  (:refer-clojure :exclude [#?(:cljs type) subs])
  (:require
    [clojure.string :as str]
    [io.jesi.backpack.fn :refer [and-fn or-fn]]
    [io.jesi.backpack.macros :refer [condf]])
  #?(:clj (:import
            (clojure.lang IPersistentCollection Keyword))))

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
    (when-not (nil? index)
      (subs s 0 (+ index (count match))))))

(defn true-string?
  "True if 's' is the string literal 'true'"
  [s]
  (= s "true"))

(defn kw->str [^Keyword k]
  (str #?(:clj  (.-sym k)
          :cljs (.-fqn k))))

#?(:cljs
   ;TODO move to own ns
   (do
     (comment
       (defonce ^:private types (atom []))

       (defn reg-type [pred fq-sym]
         (atom/conj! types [pred fq-sym]))

       (defn unreg-type [pred fq-sym]))

     (defn type [o]
       ;TODO check the type atom
       (condp #(%1 %2) o
         string? 'js/String
         number? 'js/Number
         keyword? `Keyword
         coll? `ICollection
         seq? `ISeq
         nil))))

(defmulti ->str
  "Extensible way to convert a single value to a string. Collection types should return `nil`"
  type)

(defmethod ->str #?(:clj Keyword :cljs `Keyword) [^Keyword k]
  (kw->str k))

(defmethod ->str #?(:clj IPersistentCollection :cljs `ICollection) [_]
  nil)

(defmethod ->str :default [o]
  (some-> o
          (str)))

;; TODO fix cljs hierarchy, then use clj (defmulti) code above
;; according to the global hierarchy, PersistentHashSet is not a ICollection
;; (isa?  PersistentHashSet ICollection) ;=> false
;; using `derive` to add the collection types, throws a compiler error
(comment (defn ->str
           "Convert a single value to a string. Collection types return `nil`"
           [o]
           (condf o
             nil? nil
             keyword? (kw->str o)
             coll? nil
             (str o))))

(defn ->camelCase [s]
  (when-let [s (some-> s (->str))]
    (let [[head & rest] (remove empty? (str/split s #"-|(?=[A-Z])"))
          camel (cons (str/lower-case head) (map str/capitalize rest))
          ;TODO improve adding in - prefix and suffix
          camel (if (= \- (get s 0))
                  (cons \- camel)
                  camel)
          camel (if (= \- (get s (dec (count s))))
                  (concat camel [\-])
                  camel)]
      (str/join camel))))

(defn ->kebab-case [s]
  (when-let [s (some-> s (->str))]
    (-> s
        (str/replace #"([A-Z]{2,})([a-z])" "$1 $2")
        (str/replace #"([a-z])([A-Z])" "$1 $2")
        (str/replace #"([0-9])([A-Z])" "$1 $2")
        (str/replace \_ \-)
        (str/replace #"\s" "-")
        (str/lower-case))))

(defn ->snake_case [s]
  (some-> s
          (->kebab-case)
          (str/replace \- \_)))

(def ->kebab-case-key (comp keyword ->kebab-case))

(def ->camelCase-key (comp keyword ->camelCase))

(def ->snake_case-key (comp keyword ->snake_case))

(defn- create-affix [bipred f]
  (fn [substr s]
    (let [[s :as args] (map str [s substr])]
      (if (apply bipred args)
        s
        (str/join (f args))))))

(def prefix (create-affix str/starts-with? reverse))

(def suffix (create-affix str/ends-with? identity))

(def ^{:doc "Similar to clojure.string/blank? but returns false if given a non-string"}
  blank?
  (and-fn string? str/blank?))

(def not-blank? (and-fn string? (complement str/blank?)))

(defn ->proper-case [s]
  (some-> s
          (->str)
          (str/replace #"\b." str/upper-case)))

(defn kebab->proper-case [kebab-case]
  (some-> kebab-case
          (->str)
          (str/replace \- \space)
          (->proper-case)))

(defn kebab-case->Proper-Kebab-Case [kebab-case]
  (some-> kebab-case
          (kebab->proper-case)
          (str/replace \space \-)))

(defn split-at-first
  "Splits s at the first occurrence of value, returns nil when s is empty"
  [value s]
  (let [value (str value)]
    (cond
      (empty? s) nil
      (empty? value) [s]
      :else (if-let [idx (str/index-of s value)]
              (let [val-length (count value)
                    before-split (subs s 0 idx)
                    after-split (subs s (+ idx val-length))]
                [before-split after-split])
              [s]))))
