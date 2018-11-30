(ns io.jesi.backpack.spy
  (:refer-clojure :exclude [prn])
  #?(:cljs (:require-macros io.jesi.backpack.macros)))

(defmacro prn [& more]
  ;FIXME don't print var names as strings (no " ")
  `(do
     (doseq [v# ~more]
       (print (name ~v#))
       (print \space)
       (pr ~v#))
     (newline)))
  ;(let [vars (vec (flatten (map (fn [v] [(str (name v) \:) v]) more)))]
  ;  `(apply clojure.core/prn ~vars)))
