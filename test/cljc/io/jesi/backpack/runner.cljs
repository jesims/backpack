(ns io.jesi.backpack.runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [io.jesi.backpack.random-test]
    [io.jesi.backpack.util-test]))

(enable-console-print!)

(doo-tests
  'io.jesi.backpack.random-test
  'io.jesi.backpack.util-test)
