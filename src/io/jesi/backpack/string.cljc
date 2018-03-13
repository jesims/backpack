(ns io.jesi.backpack.string)

(defn uuid-str? [s]
  (and (string? s) (re-matches #"(\w{8}(-\w{4}){3}-\w{12}?)$" s)))
