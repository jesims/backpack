(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/backpack VERSION
  :description "Clojure(Script) cross-project utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.7"]]
  :parent-project {:coords  [io.jesi/parent "3.7.0-SNAPSHOT"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :test-refresh :aliases :codox]}
  :javac-options ["--release 14"
                  "-source" "8"
                  "-target" "14"]
  :dependencies [[org.clojure/core.async]
                 [com.rpl/specter]
                 [medley "1.2.0"]
                 [com.taoensso/encore "2.117.0"]

                 ;CLJ
                 [org.clojure/clojure :scope "provided"]
                 [cheshire "5.9.0"]
                 [org.clojure/core.cache "0.8.2"]           ;TODO move to separate cache project
                 [com.cognitect/transit-clj "0.8.319"]      ;TODO move to separate http project

                 ;CLJS
                 [thheller/shadow-cljs :scope "provided"]
                 [org.clojure/clojurescript :scope "provided"]
                 [org.clojars.mmb90/cljs-cache "0.1.4"]     ;TODO move to separate cache project
                 [com.cognitect/transit-cljs "0.8.256"]     ;TODO move to separate http project
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]]
  :profiles {:dev [:parent/dev {:dependencies [[io.jesi/customs]
                                               [org.clojure/tools.namespace "0.3.1"]]}]}
  :clean-targets ^{:protect false} ["target" ".shadow-cljs"])
