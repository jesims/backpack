# Backpack
Clojure(Script) cross-project utilities

# Versioning

> Backpack versions should follow the **Semver** convention.

With version number of `MAJOR.MINOR.PATCH`, increment:

* MAJOR version when you make incompatible API changes,
* MINOR version when you add functionality in a backwards-compatible manner, and
* PATCH version when you make backwards-compatible bug fixes.

Active branches should be suffixed with `-SNAPSHOT` to indicate a work-in-progress and likely to rapidly change

## Latest Version

The latest release version of Backpack is hosted on a private AWS S3 bucket and can be pulled by adding [s3-wagon-private](https://github.com/s3-wagon-private/s3-wagon-private) as a plugin and having valid AWS credentials.

# Setup

The following steps are necessary to add *Backpack* as a project dependency:

1. Add `[s3-wagon-private "VERSION"]` to the project plugins.
2. Add the `releases` and `snapshots` configurations to the repositories as shown below
3. Add `[io.jesi/backpack "VERSION"]` to the project dependencies

```clojure
; Sample project.clj
(defproject io.jesi/my-project "0.1.0-SNAPSHOT"
  :description "My JESI project"
  :url "https://jesi.io/"
  :plugins [[s3-wagon-private "1.3.1"]]
  :repositories {"releases"       {:url           "s3p://artifacts.jesi.io/releases/"
                                   :no-auth       true
                                   :sign-releases false}
                 "snapshots"      {:url           "s3p://artifacts.jesi.io/snapshots/"
                                   :no-auth       true
                                   :sign-releases false}}
  :dependencies [[io.jesi/backpack "VERSION"]])
```

# Testing

All utilities should be written to support both Clojure and ClojureScript with unit tests written in **CLJC** where practical.
 Unit tests can then be run by invoking `backpack.sh test` or `backpack.sh test-cljs` respectively.

## Theme: Animal Safari

All string quotes within test functions should be interesting animal facts; with bonus points for David Attenborough quotes.

# Other Projects

* https://github.com/weavejester/medley
* or Search https://crossclj.info/ for function existence
