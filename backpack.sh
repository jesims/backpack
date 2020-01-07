#!/usr/bin/env bash
cd $(realpath $(dirname $0))
#TODO: Source and load from common repository
source project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

shadow-cljs () {
	lein trampoline run -m shadow.cljs.devtools.cli $@
}

## clean:
## Cleans up the compiled and generated sources
clean () {
	stop
	lein clean
}

## lint:
lint () {
	echo_message 'Clojure check'
	lein check
	abort_on_error
	echo_message 'Clojure lint'
	lein-dev lint
	abort_on_error
	if ! is-ci;then
		echo_message 'Checking CircleCI config'
		circleci config validate
		abort_on_error
	fi
	echo_message 'Formatting Markdown'
	npx remark . --output
	abort_on_error
}

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	echo_message 'Installing dependencies'
	lein -U deps
	abort_on_error
	if is-ci;then
		dry ci
	else
		dry install
	fi
	abort_on_error
}

## docs:
## Generate api documentation
docs () {
	-docs
}

## stop:
## Stops shadow-cljs and karma
stop () {
	shadow-cljs stop &>/dev/null
	pkill -f 'karma ' &>/dev/null
}

## test:
## args: [-r]
## Runs the Clojure unit tests
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
test () {
	echo_message 'In the animal kingdom, the rule is, eat or be eaten.'
	case $1 in
		-r)
			lein test-refresh ${@:2};;
		*)
			lein test ${@:2};;
	esac
	abort_on_error 'Clojure tests failed'
}

unit-test-node () {
	shadow-cljs compile node
	abort_on_error 'node tests failed'
}

unit-test-karma () {
	shadow-cljs compile karma \
	&& npx karma start --single-run
	abort_on_error 'kamra tests failed'
}

unit-test-browser-refresh () {
	clean
	trap stop EXIT
	open http://localhost:8091/
	shadow-cljs watch browser
	abort_on_error
}

unit-test-cljs-refresh () {
	clean
	echo_message 'In a few special places, these clojure changes create some of the greatest transformation spectacles on earth'
	shadow-cljs watch node
	abort_on_error
}

## test-cljs:
## args: [-k|-b|-n|-r] [test-ns-regex]
## Runs the ClojureScript unit tests
## [-k] Watches and compiles tests for execution with karma (Default)
## [-b] Watches and compiles tests for execution within a browser
## [-n] Executes the tests targeting Node.js
## [-r] Watches tests and source files for changes, and subsequently re-evaluates with karma
## [test-ns-regex] Watches tests and source files for changes, and subsequently re-evaluates
test-cljs () {
	export TEST_NS_REGEXP=${2:-'-test$'}
	case $1 in
		-r)
			unit-test-cljs-refresh;;
		-b)
			unit-test-browser-refresh;;
		-n)
			unit-test-node;;
		*)
			unit-test-karma;;
	esac
}

## snapshot:
## args: [-l]
## Pushes a snapshot to Clojars
## [-l] local
snapshot () {
	-snapshot $@
}

## release:
## Pushes a release to Clojars
release () {
	-release
}

compare-file-from-master() {
	local branch;
	if [[ -n $CIRCLE_BRANCH ]];then
		branch=$CIRCLE_BRANCH
	else
		branch=$(git rev-parse --abbrev-ref HEAD)
	fi
	echo $(git --no-pager diff --name-only $branch..origin/master -- $1)
}

check-docs () {
	local changelog_changed=$(compare-file-from-master CHANGELOG.md)
	local version_changed=$(compare-file-from-master VERSION)
	echo "$changelog_changed"
	if [[ -n "$version_changed" && -z "$changelog_changed" ]];then
		echo_error "Version has changed without updating CHANGELOG.md"
		exit 1
	fi
}

## test-docs:
## Checks that the committed api documentation is up to date with the latest code
test-docs () {
	check-docs
	docs
	echo_message 'Verifying animal facts...'
	if ! git diff --quiet --exit-code docs;then
		echo_error 'Uncommitted changes to docs'
		exit 1
	fi
}

script-invoke $@
