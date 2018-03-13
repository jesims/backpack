(ns io.jesi.backpack.runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [io.jesi.backpack.collection-test]
    [io.jesi.backpack.fn-test]
    [io.jesi.backpack.number-test]
    [io.jesi.backpack.random-test]
    [io.jesi.backpack.specter-test]
    [io.jesi.backpack.string-test]))

(enable-console-print!)

(doo-tests
  'io.jesi.backpack.collection-test
  'io.jesi.backpack.fn-test
  'io.jesi.backpack.number-test
  'io.jesi.backpack.random-test
  'io.jesi.backpack.specter-test
  'io.jesi.backpack.string-test)
