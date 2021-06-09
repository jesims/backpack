(ns io.jesi.backpack.closey
  #?(:clj (:import
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
       IClosey
       (close [_]
         (set! closed? true))
       (closed? [_]
         closed?)
       AutoCloseable)

     (defn ->AutoCloseable []
       (->_AutoCloseable false))))
