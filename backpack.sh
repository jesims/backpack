#!/usr/bin/env bash
#shellcheck disable=2215
cd "$(realpath "$(dirname "$0")")" &&
source bindle/project.sh
if [ $? -ne 0 ];then
	exit 1
fi

shadow-cljs(){
	lein-dev trampoline run -m shadow.cljs.devtools.cli "$@"
}

## clean:
## Cleans up the compiled and generated sources
clean(){
	stop
	lein-clean
}

## lint:
lint(){
	-lint
}

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps(){
	-deps "$@"
}

## docs:
## Generate api documentation
docs(){
	lein-docs
}

## stop:
## Stops shadow-cljs and karma
stop(){
	echo-message 'Stopping'
	shadow-cljs stop &>/dev/null
	pkill -f 'karma ' &>/dev/null
}

## test:
## args: [-r]
## Runs the Clojure unit tests
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
test(){
	echo-message 'In the animal kingdom, the rule is, eat or be eaten.'
	-test-clj "$@"
}

unit-test-node(){
	echo-message 'Running shadow-cljs tests on nodejs'
	shadow-cljs compile node-test
	abort-on-error 'node tests failed'
}

unit-test-node-refresh(){
	echo-message 'Running shadow-cljs tests on nodejs'
	shadow-cljs watch node-test
	abort-on-error 'node tests failed'
}

unit-test-karma(){
	echo-message 'Running shadow-cljs tests on karma'
	shadow-cljs compile karma \
	&& npx karma start --single-run
	abort-on-error 'kamra tests failed'
}

unit-test-browser-refresh(){
	echo-message 'Running shadow-cljs tests in browser'
	stop
	trap stop EXIT
	open http://localhost:8091/
	shadow-cljs watch browser-test
	abort-on-error
}

## test-cljs:
## args: [-k|-b|-n|-r]
## Runs the ClojureScript unit tests using Kaocha
## [-n] Executes the tests targeting Node.js (Default)
## [-b] Watches and compiles tests for execution within a browser
## [-r] Watches tests and source files for changes, and subsequently re-evaluates with karma
test-cljs(){
	-test-cljs "$@"
}

## test-shadow:
## args: [-k|-b|-n|-r]
## Runs the ClojureScript unit tests using shadow-cljs
## [-n] Executes the tests targeting Node.js (Default)
## [-b] Watches and compiles tests for execution within a browser
## [-r] Watches tests and source files for changes, and subsequently re-evaluates with node
test-shadow(){
	case $1 in
		-k)
			unit-test-karma;;
		-b)
			unit-test-browser-refresh;;
		-r)
			unit-test-node-refresh;;
		*)
			unit-test-node;;
	esac
}

## snapshot:
## args: [-l]
## Pushes a snapshot to Clojars
## [-l] local
snapshot(){
	-snapshot "$@"
}

## release:
## Pushes a release to Clojars
release(){
	-release
}

compare-file-from-master() {
	local branch;
	if [[ -n $CIRCLE_BRANCH ]];then
		branch=$CIRCLE_BRANCH
	else
		branch=$(git rev-parse --abbrev-ref HEAD)
	fi
	git --no-pager diff --name-only "$branch..origin/master" -- "$1"
}

check-docs(){
	local changelog_changed
	changelog_changed=$(compare-file-from-master CHANGELOG.md)
	abort-on-error
	local version_changed
	version_changed=$(compare-file-from-master VERSION)
	abort-on-error
	echo "$changelog_changed"
	if [[ -n "$version_changed" && -z "$changelog_changed" ]];then
		echo-error "Version has changed without updating CHANGELOG.md"
		exit 1
	fi
}

## test-docs:
## Checks that the committed api documentation is up to date with the latest code
test-docs(){
	check-docs
	docs
	echo-message 'Verifying animal facts...'
	require-committed docs
}

deploy(){
	deploy-clojars
}

deploy-snapshot(){
	deploy-clojars
}

## outdated:
outdated(){
	-outdated
}

script-invoke "$@"
