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
