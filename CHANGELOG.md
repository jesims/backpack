# 5.3.1

Added:

* `io.jesi.backpack.string/split-at-first`
* `io.jesi.backpack.fn/any?`
* `io.jesi.backpack.fn/->comparator`

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

* `io.jesi.backpack.macros/reify-ifn` to no longer invokes protocol implementation with a seq of args. Throws exception if invalid arity during runtime

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
