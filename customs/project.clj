(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/backpack-test VERSION
  :description "Clojure(Script) cross-project testing utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.7"]]
  :parent-project {:coords  [io.jesi/parent "2.2.0-SNAPSHOT"] ;FIXME remove snapshot
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :test-refresh :aliases :codox]}
  :dependencies [[org.clojure/clojure :scope "provided"]
                 ;FIXME replace with ~VERISON
                 [io.jesi/backpack "5.0.0-SNAPSHOT"]
                 [pjstadig/humane-test-output "0.10.0"]]
  :profiles {:dev [:parent/dev]}
  :codox {:namespaces [io.jesi.backpack.test.spy
                       io.jesi.backpack.test.macros
                       io.jesi.backpack.test.strict
                       io.jesi.backpack.test.util]})
