(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/backpack VERSION
  :description "Clojure(Script) cross-project utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.6"]]
  :parent-project {:coords  [io.jesi/parent "1.0.0"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies [:profiles :dev] :test-refresh :aliases]}
  :dependencies [[thheller/shadow-cljs :scope "provided"]
                 [org.clojure/clojure]
                 [org.clojure/core.async]
                 [com.rpl/specter]
                 [cheshire "5.8.1"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [medley "1.1.0"]
                 [org.clojure/core.cache "0.7.2"]
                 [org.clojars.mmb90/cljs-cache "0.1.4"]
                 [pjstadig/humane-test-output "0.10.0"]]
  :profiles {:test {:dependencies [[org.clojure/tools.namespace "0.2.11"]]}
             :dev  {:injections [(require 'pjstadig.humane-test-output)
                                 (pjstadig.humane-test-output/activate!)]
                    :plugins    [[lein-codox "0.10.7"]
                                 [lein-auto "0.1.3"]
                                 [lein-shell "0.5.0"]]}}
  :clean-targets ^{:protect false} ["target"]
  :release-tasks [["deploy"]]
  ;FIXME generate docs for cljc, clj and cljs
  :codox {:output-path "docs"
          :namespaces  [io.jesi.backpack
                        io.jesi.backpack.http.codes
                        io.jesi.backpack.http.response
                        io.jesi.backpack.http.status
                        io.jesi.backpack.cache
                        io.jesi.backpack.macros
                        io.jesi.backpack.random]}
  :aliases {"test-refresh" ["auto" "do" ["shell" "clear"] "test"]})
