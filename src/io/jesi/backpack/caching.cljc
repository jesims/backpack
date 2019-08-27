(ns io.jesi.backpack.caching
  (:require
    #?(:clj  [clojure.core.cache :as cache]
       :cljs [cljs.cache :as cache])))

(defonce ^:private caches (atom {}))

(def ^:private t-12h (* 12 60 60 1000))
(def ^:private defaults {:ttl       t-12h
                         :seed      {}
                         :threshold 50})

(defn init-cache
  "Creates a cache with the options:

  :ttl
  time in milliseconds that entries are allowed to reside in the cache
  (default 12 hours)

  :threshold
  the maximum number of elements in the cache before the LRU semantics apply
  (default 50)"
  [& [{:keys [threshold ttl seed] :as opts}]]
  (let [{:keys [seed threshold ttl]} (merge defaults opts)]
    ;TODO make ttl and threshold nilable
    ;TODO create soft reference cache
    (-> seed
        (cache/ttl-cache-factory :ttl ttl)
        (cache/lru-cache-factory :threshold threshold))))

(defn clear-cache
  "Completely removes a cache by key from the caches"
  [key]
  (swap! caches dissoc key))

(defn- -cache
  ([cache-key k] (-cache nil cache-key k))
  ([{:keys [miss-fn init-fn] :as cache-opts} cache-key k]
   (let [miss-fn (or miss-fn (fn miss-fn [vres cache] cache))
         init-fn (or init-fn init-cache)
         vres (volatile! nil)]
     (swap! caches assoc cache-key
       (let [cache (get @caches cache-key (init-fn))]
         (if (cache/has? cache k)
           (do
             (vreset! vres (cache/lookup cache k))
             (cache/hit cache k))
           (miss-fn vres cache))))
     @vres)))

(defn- miss [vres cache k result]
  (vreset! vres result)
  (cache/miss cache k result))

(defn cache
  "Creates a generic cache function that takes any number of `args`,
  returning the value of applying `args` to `f`.
  Values are remembered according to the cache from `init-cache`."
  ;FIXME should cache-opts really be passed in or should it be the init-cache opts. `miss-fn` should not be overridden
  ([f] (cache nil f))
  ([{:as cache-opts} f]
   (fn [& args]
     (-cache
       (merge {:miss-fn (fn cached-missed-fn [vres cache]
                          (miss vres cache args (apply f args)))}
         cache-opts)
       f
       args))))

;FIXME docstring
(defn keyed-cache
  ;TODO accept cache-opts
  ([cache-key k]
   (-cache cache-key k))

  ([cache-key k result]
   (-cache
     {:miss-fn (fn [vres cache]
                 (miss vres cache k result))}
     cache-key
     k)))
