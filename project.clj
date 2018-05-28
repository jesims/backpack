(defproject io.jesi/backpack "0.0.8"
  :description "Clojure(Script) cross-project utilities"
  :license "Unlicensed"
  :url "https://github.com/jesims/backpack"
  :plugins [[lein-parent "0.3.4"]]
  :parent-project {:path    "build-scripts/parent-clj/project.clj"
                   :inherit [:plugins :repositories :managed-dependencies :dependencies :exclusions [:profiles :dev] :test-refresh]}
  :dependencies [[org.clojure/clojurescript]
                 [com.rpl/specter]
                 [thheller/shadow-cljs "2.3.23"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [medley "1.0.0"]
                 [org.clojure/tools.namespace "0.2.11"]]
  :source-paths ["src"]
  :test-paths ["test"]
  :clean-targets ^{:protect false} ["target"]
  :aot [io.jesi.backpack.random]
  :release-tasks [["deploy"]])
