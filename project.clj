(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/backpack VERSION
  :description "Clojure(Script) cross-project utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.5"]]
  :parent-project {:coords  [io.jesi/parent "0.0.17"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies [:profiles :dev] :test-refresh]}
  :dependencies [[thheller/shadow-cljs :scope "provided"]
                 [org.clojure/clojure "1.10.0"]
                 [org.clojure/core.async]
                 [com.rpl/specter]
                 [cheshire "5.8.1"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [medley "1.1.0"]]
  :profiles {:test    {:dependencies [[org.clojure/tools.namespace "0.2.11"]]}
             :dev     {:dependencies [];commented out until https://github.com/pjstadig/humane-test-output/issues/37 is fixed
                                      ;[pjstadig/humane-test-output "0.9.0"]]
                       ;commented out until https://github.com/weavejester/codox/issues/166 is fixed
                       :plugins      [[lein-codox "0.10.7"]]}}
  :clean-targets ^{:protect false} ["target"]
  :release-tasks [["deploy"]]
  ;FIXME generate docs for cljc, clj and cljs
  :codox {:output-path "docs"
          :namespaces  [io.jesi.backpack
                        io.jesi.backpack.http
                        io.jesi.backpack.macros
                        io.jesi.backpack.random]})
