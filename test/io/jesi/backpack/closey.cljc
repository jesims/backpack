(ns io.jesi.backpack.closey
  #?(:cljs (:require
             [io.jesi.backpack.closer])
     :clj  (:import
             (java.lang AutoCloseable))))

(defprotocol IClosey
  (close [this])
  (closed? [this]))

(deftype Closey [^:volatile-mutable closed?]
  IClosey
  (close [_]
    (set! closed? true))
  (closed? [_]
    closed?))

(defn ->Closey []
  (Closey. false))

#?(:clj
   (do

     (deftype _AutoCloseable [^:volatile-mutable closed?]
       AutoCloseable
       (close [_]
         (set! closed? true))
       IClosey
       (closed? [_]
         closed?))

     (defn ->AutoCloseable []
       (->_AutoCloseable false))))

#?(:cljs
   (defmethod io.jesi.backpack.closer/close Closey [c]
     (close c)))
