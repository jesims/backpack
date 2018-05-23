(ns io.jesi.backpack.runner
  (:require
    [doo.runner :refer-macros [doo-tests]]
    [pjstadig.humane-test-output]

    [io.jesi.backpack.clojurescript-test]
    [io.jesi.backpack.collection-test]
    [io.jesi.backpack.fn-test]
    [io.jesi.backpack.macros-test]
    [io.jesi.backpack.miscellaneous]
    [io.jesi.backpack.random-test]
    [io.jesi.backpack.specter-test]
    [io.jesi.backpack.string-test]
    [io.jesi.backpack.walk-test]))

(enable-console-print!)

(doo-tests
  ;'io.jesi.backpack.clojurescript-test
  ;'io.jesi.backpack.collection-test
  ;'io.jesi.backpack.fn-test
  ;'io.jesi.backpack.macros-test
  ;'io.jesi.backpack.miscellaneous
  ;'io.jesi.backpack.random-test
  ;'io.jesi.backpack.specter-test
  ;'io.jesi.backpack.string-test
  'io.jesi.backpack.walk-test)
