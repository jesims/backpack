(defproject io.jesi/backpack "0.0.7"
  :description "Clojure(Script) cross-project utilities"
  :license "Unlicensed"
  :url "https://github.com/jesims/backpack"
  :plugins [[lein-parent "0.3.4"]
            [lein-cljsbuild "1.1.7"]]
  :parent-project {:path    "build-scripts/parent-clj/project.clj"
                   :inherit [:plugins :repositories :managed-dependencies :dependencies :exclusions [:profiles :dev] :test-refresh]}
  :dependencies [[org.clojure/clojurescript]
                 [com.rpl/specter]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [medley "1.0.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [potemkin "0.4.4"]
                 [camel-snake-kebab "0.4.0"]]
  :test-paths ["test/cljc"]
  :clean-targets ^{:protect false} ["target"]
  :aot [io.jesi.backpack.random]
  :cljsbuild {:builds
              {:test {:source-paths ["src" "test/cljc"]
                      :compiler     {:main           io.jesi.backpack.runner
                                     :output-dir     "target/test"
                                     :output-to      "target/test/test.js"
                                     :optimizations  :none
                                     :pretty-print   true
                                     :process-shim   false
                                     :parallel-build true
                                     :target         :nodejs}}}}
  :doo {:build "test" :alias {:default [:node]}}
  :release-tasks [["deploy"]]
  :aliases {"test-cljs" ["doo" "once"]})
