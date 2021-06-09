(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/backpack VERSION
  :description "Clojure(Script) cross-project utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.8"]]
  :parent-project {:coords  [io.jesi/parent "4.11.0"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :test-refresh :aliases :codox]}
  :managed-dependencies [;[io.jesi/backpack "6.3.0"]
                         [io.jesi/backpack "7.1.0"]]
                         ;FIXME once CLJS is working, update in parent
                         ;[thheller/shadow-cljs "2.14.4"]
                         ;[org.clojure/clojurescript "1.10.866"]
                         ;[com.google.javascript/closure-compiler-unshaded "v20210505"]
                         ;[org.clojure/google-closure-library "0.0-20201211-3e6c510d"]
                         ;[org.clojure/google-closure-library-third-party "0.0-20201211-3e6c510d"]]
  :dependencies [[org.clojure/core.async]
                 [com.rpl/specter]
                 ;[com.taoensso/encore "2.117.0"]
                 [com.taoensso/encore "3.19.0"]

                 ;CLJ
                 [org.clojure/clojure :scope "provided"]
                 [org.clojure/core.cache "1.0.207"]         ;TODO move to separate cache project

                 ;CLJS
                 [org.clojure/clojurescript :scope "provided"]
                 [org.clojars.mmb90/cljs-cache "0.1.4"]]    ;TODO move to separate cache project
  :profiles {:dev [:parent/dev {:dependencies [[io.jesi/customs]
                                               [org.clojure/tools.namespace "1.1.0"]
                                               [thheller/shadow-cljs]]}]}
  :clean-targets ^{:protect false} [".shadow-cljs" ".cljs_node_repl" "out" :target-path]
  :codox {:metadata   {:doc/format :markdown}
          :namespaces [io.jesi.backpack
                       #"^io\.jesi\.backpack\.(?!walk)"]})
