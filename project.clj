(def VERSION (.trim (slurp "VERSION")))

(defproject io.jesi/backpack VERSION
  :description "Clojure(Script) cross-project utilities"
  :url "https://github.com/jesims/backpack"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}
  :plugins [[lein-parent "0.3.8"]]
  :parent-project {:coords  [io.jesi/parent "4.12.0"]
                   :inherit [:plugins :managed-dependencies :deploy-repositories :dependencies :profiles :test-refresh :aliases :codox]}
  :managed-dependencies [[io.jesi/backpack "7.1.0"]
                         [com.google.guava/guava "30.1.1-jre"]] ;FIXME remove when we're using customs 1.3.2
  :dependencies [[org.clojure/core.async]
                 [com.rpl/specter]

                 ;CLJ
                 [org.clojure/clojure :scope "provided"]
                 [org.clojure/core.cache "1.0.207"]         ;TODO move to separate cache project

                 ;CLJS
                 [org.clojure/clojurescript :scope "provided"]
                 [org.clojars.mmb90/cljs-cache "0.1.4"]]    ;TODO move to separate cache project
  :profiles {:dev [:parent/dev {:dependencies [[io.jesi/customs] ;"1.3.2"] ;FIXME update in parent
                                               [org.clojure/tools.namespace "1.1.0"]
                                               [thheller/shadow-cljs]]}]}
  :clean-targets ^{:protect false} [".shadow-cljs" ".cljs_node_repl" "out" :target-path]
  :codox {:metadata   {:doc/format :markdown}
          :namespaces [io.jesi.backpack
                       #"^io\.jesi\.backpack\.(?!walk)"]})
