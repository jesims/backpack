;TODO move to separate project
(ns io.jesi.backpack.cache
  (:refer-clojure :exclude [get set])
  (:require
    #?(:clj  [clojure.core.cache :as cache]
       :cljs [cljs.cache :as cache :refer [CacheProtocol]])
    [io.jesi.backpack.fn :refer [noop]]
    [io.jesi.backpack.macros :refer [reify-ifn]])
  #?(:clj
     (:import
       (clojure.core.cache CacheProtocol)
       (clojure.lang ILookup))))

(def ^:private t-12h (* 12 60 60 1000))
(def ^:private default-seed {})
(def ^:private default-lru-threshold 50)

(defn ^CacheProtocol create-ttl
  "Creates a Time To Live cache with an initial seed (default {}) and maximum TTL in milliseconds (default 12 hours)"
  ([] (create-ttl t-12h default-seed))
  ([seed] (create-ttl t-12h seed))
  ([ttl seed]
   (cache/ttl-cache-factory seed :ttl ttl)))

(defn ^CacheProtocol create-lru
  "Creates a Least Recently Used cache with an initial seed (default {}) and maximum value threshold (default 50)."
  ([] (create-lru default-lru-threshold default-seed))
  ([seed] (create-lru default-lru-threshold seed))
  ([threshold seed]
   (cache/lru-cache-factory seed :threshold threshold)))

(defn ^CacheProtocol create-default
  "Creates a TTL/LRU combination cache with default values and in initial seed (default {})"
  ([] (create-default {}))
  ([seed] (->> seed create-ttl create-lru)))

(defn- hit-or-miss
  ([^CacheProtocol cache ^clojure.lang.Volatile vres entry] (hit-or-miss cache vres entry nil))
  ([^CacheProtocol cache ^clojure.lang.Volatile vres entry miss]
   (if (cache/has? cache entry)
     (do
       (vreset! vres (cache/lookup cache entry))
       (cache/hit cache entry))
     (if miss
       (let [v (miss)]
         (vreset! vres v)
         (cache/miss cache entry v))
       cache))))

(defprotocol SimpleCache
  "A simple, self contained cache protocol"
  (get [this entry]
    "Retrieve the value associated with `entry` if it exists within the cache `impl`, else `nil`.
    May invoke a `miss` function to create the `entry` if not found.")
  (set [this entry value]
    "Sets the `entry` to the specific `value` within the cache `impl`")
  (evict [this entry]
    "Evicts the `entry` from the cache `impl`")
  (reset [this]
    "Resets the cache back to it's initial value"))

(defn ->Simple
  "Converts a `CacheProtocol` into a `SimpleCache`. The `miss` function will be invoked with the entry to determine
  and set the value if not found. Implements IFn as an alternative to invoking `SimpleCache/get`

  WARNING: CLJS has a IFn argument limit of 20 args for both Apply and Invoke"
  ([^CacheProtocol impl] (->Simple impl nil))
  ([^CacheProtocol impl miss]
   (let [cache (atom impl)]
     (reify-ifn
       get
       SimpleCache

       (get [_ entry]
         (let [res (volatile! nil)]
           (swap! cache hit-or-miss res entry (when miss #(miss entry)))
           @res))

       (set [_ entry value]
         (swap! cache cache/miss entry value)
         nil)

       (evict [_ entry]
         (swap! cache cache/evict entry)
         nil)

       (reset [_]
         (reset! cache impl)
         nil)

       ILookup
       #?(:cljs (-lookup [this entry]
                  (get this entry))
          :clj  (valAt [this entry]
                  (get this entry)))

       #?(:cljs (-lookup [this entry not-found]
                  (or (get this entry) not-found))
          :clj  (valAt [this entry not-found]
                  (or (get this entry) not-found)))))))

(defn ->Simple-Fn-Cache
  "Constructs a function-backed `SimpleCache`. The `miss` function will be invoked with the entry to determine
  and set the value if not found. Implements IFn as an alternative to invoking `SimpleCache/get`"
  [{:keys [key-fn cache-fn miss-fn evict-fn reset-fn]
    :or   {key-fn identity}}]
  (let [key-fn (or key-fn identity)
        evict-fn (or evict-fn noop)
        reset-fn (or reset-fn noop)]
    (reify-ifn
      get
      SimpleCache

      (get [this entry]
        (let [key (key-fn entry)]
          (if-some [match (cache-fn key)]
            match
            (let [result (miss-fn entry)]
              (set this entry result)
              result))))

      (set [_ entry value]
        (cache-fn (key-fn entry) value)
        value)

      (evict [_ entry]
        (evict-fn entry)
        nil)

      (reset [_]
        (reset-fn)
        nil))))
