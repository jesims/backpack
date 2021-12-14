(ns io.jesi.backpack.closey
  #?(:cljs (:require
             [io.jesi.backpack.closer])
     :clj  (:import
             (java.lang AutoCloseable))))

(defprotocol IClosey
  (do-close [this])
  (closed? [this]))

(deftype Closey [^:volatile-mutable closed?]
  IClosey
  (do-close [_]
    (set! closed? true))
  (closed? [_]
    closed?))

(defn ->Closey []
  (Closey. false))

(defmethod io.jesi.backpack.closer/close Closey [c]
  (do-close c))

#?(:clj
   (do

     (deftype _AutoCloseable [^:volatile-mutable closed?]
       AutoCloseable
       (close [_]
         (set! closed? true))
       IClosey
       (do-close [_]
         (set! closed? true))
       (closed? [_]
         closed?))

     (defn ->AutoCloseable []
       (->_AutoCloseable false))))

