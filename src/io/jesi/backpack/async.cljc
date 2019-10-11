(ns io.jesi.backpack.async
  #?(:cljs (:require-macros [io.jesi.backpack.async :refer [go go-try when-open]]))
  (:require
    [clojure.core.async :as async]
    [clojure.core.async.impl.protocols :as proto]
    [io.jesi.backpack.exceptions :as ex]
    [io.jesi.backpack.macros :refer [catch->identity]]
    [io.jesi.backpack.miscellaneous :refer [cljs-env? env-specific]]))

(defn closed?
  "returns true if the channel is nil or closed"
  [chan]
  (or (nil? chan)
      (proto/closed? chan)))

(def
  ^{:arglists '([chan])}
  open?
  "returns true if the channel is open. The complement of `closed?`"
  (complement closed?))

(defmacro when-open [chan & body]
  `(when-not (closed? ~chan)
     ~@body))

(def
  ^{:arglists '([] [buf-or-n] [buf-or-n xform] [buf-or-n xform ex-handler])}
  chan
  "Creates a channel with an optional buffer, an optional transducer
  (like (map f), (filter p) etc or a composition thereof), and an
  optional exception-handler.  If buf-or-n is a number, will create
  and use a fixed buffer of that size. If a transducer is supplied a
  buffer must be specified. ex-handler must be a fn of one argument -
  if an exception occurs during transformation it will be called with
  the Throwable as an argument, and any non-nil return value will be
  placed in the channel."
  async/chan)

(defmacro go
  "Asynchronously executes the body, returning immediately to the
  calling thread. Additionally, any visible calls to <!, >! and alt!/alts!
  channel operations within the body will block (if necessary) by
  'parking' the calling thread rather than tying up an OS thread (or
  the only JS thread when in ClojureScript). Upon completion of the
  operation, the body will be resumed.

  Returns a channel which will receive the result of the body when
  completed"
  [& body]
  (let [go* (env-specific &env 'clojure.core.async/go)]
    `(~go* ~@body)))

(defn close!
  "Closes a channel. The channel will no longer accept any puts (they
  will be ignored). Data in the channel remains available for taking, until
  exhausted, after which takes will return nil. If there are any
  pending takes, they will be dispatched with nil. Closing a closed
  channel is a no-op. Returns the channel.

  Logically closing happens after all puts have been delivered. Therefore, any
  blocked or parked puts will remain blocked/parked until a taker releases them."
  [chan]
  (when chan
    (async/close! chan)
    chan))

(defn put!
  "Asynchronously puts a val into a channel if it's open. `nil` values are
   ignored. Returns the channel."
  [chan val]
  (when (and (open? chan) (some? val))
    (async/put! chan val)
    chan))

(defmacro go-try
  "Asynchronously executes the body in a go block. Returns a channel which
   will receive the result of the body when completed or an exception if one
   is thrown."
  [& body]
  `(go
     (catch->identity ~@body)))

;From https://github.com/fullcontact/full.async
(defmacro go-retry
  "Attempts to evaluate a go block and retries it if `should-retry-fn`
   which is invoked with block's evaluation result evaluates to false.
   `should-retry-fn` is optional and by default it will simply check if
   result is an exception. If the evaluation still fails after given
   retries, the last failed result will be returned in channel.
   Parameters:
   * retries - how many times to retry (default 5 times)
   * delay - how long to wait in seconds between retries (default 1s)
   * should-retry-fn - function that is invoked with result of block's
                       evaluation and should indicate whether to retry
                       (if it returns true) or not (returns false)"
  [{:keys [retries delay should-retry-fn]
    :or   {retries 5, delay 1, should-retry-fn `ex/exception?}}
   & body]
  (let [go-loop* (env-specific &env 'clojure.core.async/go-loop)
        <!* (env-specific &env 'clojure.core.async/<!)
        timeout (env-specific &env 'clojure.core.async/timeout)]
    `(let [delay# (* ~delay 1000)]
       (~go-loop* [retries# ~retries]
         (let [res# (catch->identity ~@body)]
           (if (and (~should-retry-fn res#)
                    (pos? retries#))
             (do
               (when (pos? delay#)
                 (~<!* (~timeout delay#)))
               (recur (dec retries#)))
             res#))))))

;From https://github.com/fullcontact/full.async
(defmacro <?
  "Same as core.async <! but throws an exception if the channel returns a
  throwable object. Also will not crash if channel is nil."
  [ch]
  (let [<!* (env-specific &env 'clojure.core.async/<!)]
    `(ex/throw-if-throwable
       (when-let [ch# ~ch]
         (~<!* ch#)))))

(defmacro go-call
  "Takes a function and a channel. Takes the value of the chanel using `<?` and applies it to `f`.
  Returns a channel which contains the result (or exception)."
  [f chan]
  `(go-try
     (~f (<? ~chan))))
