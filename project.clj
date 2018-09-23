(defproject io.jesi/backpack "0.0.16"
  :description "Clojure(Script) cross-project utilities"
  :license "Unlicensed"
  :url "https://github.com/jesims/backpack"
  :plugins [[lein-parent "0.3.4"]]
  :parent-project {:path    "build-scripts/parent-clj/project.clj"
                   :inherit [:plugins :repositories :managed-dependencies :dependencies :exclusions [:profiles :dev] :test-refresh]}
  :dependencies [[org.clojure/clojurescript]
                 [com.rpl/specter]
                 [thheller/shadow-cljs]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [medley "1.0.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [pjstadig/humane-test-output "0.8.3"]
                 [binaryage/oops "0.6.2"]]
  :clean-targets ^{:protect false} ["target"]
  :aot [io.jesi.backpack.random]
  :release-tasks [["deploy"]])
