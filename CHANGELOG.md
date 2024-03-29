# 7.6.0

Added:

* `io.jesi.backpack.macros/setup-let` macro for clj
  * and `/with-let` placeholder function

# 7.5.1

Fixed:

* `io.jesi.backpack.compare` functions not working correctly for more than 2 args

# 7.5.0

Added:

* `io.jesi.backpack.compare`

# 7.4.0

Added:

* `io.jesi.backpack.collection/redact`
* `io.jesi.backpack.clojure/`:
  * `add-shutdown-hook-fn`
  * `add-shutdown-hook`

Fixed:

* `io.jesi.backpack.macros/import-vars` now works with marcos

# 7.3.0

Added:

* simple filtering for collections of maps
  * `io.jesi.backpack.collection/`:
    * `filter-by`
    * `filter-by=`

# 7.2.1

Fixed:

* `when-debug` macro now takes multiple args

# 7.2.0

Added:

* `io.jesi.backpack.macros/`:
  * `with-open`
  * `with-open->`
  * `assoc-nx`
  * `assoc-nx!`
* `io.jesi.backpack.closer/close` multimethod
* `io.jesi.backpack.collection/distinct-vals?`
* `io.jesi.backpack.miscellaneous/re-quote`

# 7.1.0

Added:

* `io.jesi.backpack.macros/shorthand-assoc`
* `io.jesi.backpack.macros/shorthand-str`

# 7.0.0

Updated:

* `import-vars` to be non-lazy, giving the ClojureScript compiler a better chance at DCE

Added

* NPM Dependencies:
  * `source-map-support` as version `0.5.19`
  * `ws` as version `7.4.4`

Breaking:

* To prevent the `cljs.pprint` from being included in advanced ClojureScript compilation, the following are Clojure only
  * `pprint-str`
  * `pprint-str-code`
* Removed namespaces:
  * `io.jesi.backpack.env/runtimes`
  * `io.jesi.backpack.macros/fn1`
  * `io.jesi.backpack.macros/fn2`
  * `io.jesi.backpack.macros/fn3`
  * `io.jesi.backpack.macros/when-let`
  * `io.jesi.backpack/clj->json`
  * `io.jesi.backpack/clj->transit`
  * `io.jesi.backpack/json->clj`
  * `io.jesi.backpack/transit->clj`
* Removed dependencies:
  * `cheshire/cheshire`
  * `com.cognitect/transit-clj`
  * `com.cognitect/transit-cljs`
  * `com.lucasbradstreet/cljs-uuid-utils`

# 6.3.1

Updated:

* `bindle` to latest `master`
* `lein-parent` to `0.3.8`
* `io.jesi/parent` to `4.5.0`
* `cheshire/cheshire` to `5.10.0`
* `org.clojure/core.cache` to `1.0.207`
* `com.cognitect/transit-clj` to `1.0.324`
* `com.cognitect/transit-cljs` to `0.8.264`

# 6.3.0

Added:

* `io.jesi.backpack.string/kebab-case->Proper-Kebab-Case`

# 6.2.0

Added:

* `io.jesi.backpack.miscellaneous/xor`
* `io.jesi.backpack.collection/empty->nil`

# 6.1.0

Added:

* `io.jesi.backpack.string/blank?`

# 6.0.0

Added:

* `io.jesi.backpack.string/split-at-first`
* `io.jesi.backpack.fn/any?`
* `io.jesi.backpack.fn/->comparator`

Changed:

* Moved `io.jesi.backpack.miscellaneous/collify` to `io.jesi.backpack.collection/collify`

# 5.3.0

Added:

* `io.jesi.backpack.cache/->Simple-Fn-Cache`

Changed:

* `io.jesi.backpack/->uri` now supports CLJS
* Moved `io.jesi.backpack.miscellaneous/named?` to `io.jesi.backpack.common/named?`
* Moved `io.jesi.backpack.miscellaneous/namespaced?` to `io.jesi.backpack.common/namespaced?`
* Deprecated `io.jesi.backpack/json->clj`
* Deprecated `io.jesi.backpack/clj->json`
* Deprecated `io.jesi.backpack/transit->clj`
* Deprecated `io.jesi.backpack/clj->transit`

Fix:

* `io.jesi.backpack.macros/reify-ifn` to no longer invokes protocol implementation with a seq of args. Throws exception
  if invalid arity during runtime

# 5.2.2

Fix:

* `import-vars` brings in depreciation metadata to the namespace

Misc:

* Added docs strings and markdown doc output formatting

# 5.2.1

Fix:

* StackOverflowError by not using `env/transform`
* `go-call` throwing an error if `f` is nil

Added:

* `io.jesi.backpack.env/symbol`
* Tests in [shadow-cljs](https://github.com/thheller/shadow-cljs)
* `./backpack.sh outdated`

# 5.2.0

Added:

* `io.jesi.backpack.string/->proper-case`
* `io.jesi.backpack.string/kebab->proper-case`

# 5.1.1

Fix:

* `io.jesi.backpack.string/->kebab-case` Correctly convert strings that have letters and digits

# 5.1.0

Added:

* `io.jesi.backpack.collection/conj-some!`
* `io.jesi.backpack.collection/assoc-some!`
* `io.jesi.backpack.collection/update-some!`
* `io.jesi.backpack.collection/remove-nil-vals`

# 5.0.0

Changed:

* Moved `pprint-str` and `pprint-str-code` to `io.jesi.backpack.miscellaneous`
* Moved `io.jesi.backpack.spy` to `io.jesi.backpack.test.spy`
* Moved `io.jesi.backpack.test.*` to [customs](https://github.com/jesims/customs) project

Removed:

* Deprecated `io.jesi.backpack.miscellaneous/assoc-changed!`
* Deprecated `io.jesi.customs.macros/is=`
* Deprecated `io.jesi.customs.macros/testing`

Added:

* [Bindle](https://github.com/jesims/bindle) submodule
* [customs](https://github.com/jesims/customs) dependency

# 4.2.1

Fix:

* cljsbuild compiler errors

# 4.2.0

Added:

* `io.jesi.backpack.collection/index-comparator`
* `io.jesi.backpack.collection/sorted-map-by-index`
* `io.jesi.backpack.collection/create-index`
* `io.jesi.backpack.collection/sorted-map-by-order`
* `io.jesi.backpack.atom/toggle!`

Changed:

* `io.jesi.backpack.macros/import-vars` to exclude vars with `:import/exclude` meta
* `io.jesi.backpack.test.util/wait-for`
  * works in `:cljs`
  * throws exception if timeout is reached

# 4.1.0

Added:

* `io.jesi.backpack.transit`

# 4.0.1

Fixed:

* `->camelCase` not correctly converting strings with a leading and trailing `-`

# 4.0.0

Moved:

* `io.jesi.backpack.test.runner` to `io.jesi.backpack.test.runner.browser`
* `io.jesi.backpack.miscellaneous/cljs-env?` to `io.jesi.backpack.env/cljs?`
* `io.jesi.backpack.clojurescript.cljs` to `io.jesi.backpack.clojurescript.cljc`

Removed:

* `io.jesi.backpack.clojurescript/clj->json` since it's in `json` ns
* `io.jesi.backpack.clojurescript/json->clj` since it's in `json` ns
* `io.jesi.backpack.macros/if-cljs`
* `io.jesi.backpack.miscellaneous/env-specific`

Changed:

* `is-macro=` to compare symbols instead of strings
* `import-vars` to import all public vars by passing in a symbol

Added:

* `io.jesi.backpack.test.strict`
* `io.jesi.backpack.env`
* `io.jesi.backpack.atom`
* `io.jesi.backpack/named?`
* `io.jesi.backpack/not-blank?`

# 3.7.0

Added:

* `io.jesi.backpack.test.macros/testing` macro

# 3.6.0

Added:

* `cache/reset`

# 3.5.0

Added:

* `def-` macro
* `string/subs`

# 3.4.1

Added:

* [humane-test-output](https://github.com/pjstadig/humane-test-output) for better test output

# 3.3.1

Fixed:

* `is=` now takes at least 2 args

# 3.3.0

Added:

* `sorted?`

# 3.2.1

Misc:

* exclude `clojure.core/mod` to suppress warning

# 3.2.0

Added:

* `mod`

# 3.1.0

Added:

* `test.util/wait-for` (clj only)

# 3.0.0

Added:

* `cache/create-lru`
* `cache/create-ttl`
* `cache/create-default`
* `cache/->Simple`
* `macros/reify-ifn`
* `java->clj`
* `diff`
* `map-leaves`
* `reduce-leaves`
* `and-fn`
* `or-fn`
* `random/character`

Removed:

* `cache`
* `keyed-cache`

# 2.0.0

Added:

* `async/go-call`
* `update-some`

Changed:

* Split `http` namespace into `status` and `response`

# 0.0.17

* Initial public release of Backpack
