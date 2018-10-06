#!/usr/bin/env bash
cd $(realpath $(dirname $0))
# TODO: Source and load from common repository
source ./project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	echo_message "Installing dependencies"
	lein deps && npm install
}

## clean:
## Cleans up the compiled and generated sources
clean () {
	stop
	lein clean
	rm -rf .shadow-cljs/
}

## stop:
## Stops shadow-cljs and karma
stop () {
	npx shadow-cljs stop &>/dev/null
	pkill -f 'karma ' &>/dev/null
}

_unit-test () {
	refresh=$1
	clean
	echo_message 'In the animal kingdom, the rule is, eat or be eaten.'
	if ${refresh};then
		lein auto test ${@:2}
	else
		lein test
	fi
	abort_on_error 'Clojure tests failed'
}

## unit-test:
## args: [-r]
## Runs the Clojure unit tests
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
unit-test () {
	case $1 in
		-r)
			_unit-test true ${@:2};;
		*)
			_unit-test;;
	esac
}

unit-test-node () {
	npx shadow-cljs compile node \
	&& node target/node/test.js
	abort_on_error 'node tests failed'
}

unit-test-karma () {
	npx shadow-cljs compile karma \
	&& npx karma start --single-run
	abort_on_error 'kamra tests failed'
}

unit-test-browser-refresh () {
	clean
	trap stop EXIT
	open http://localhost:8091/
	npx shadow-cljs watch browser
	abort_on_error
}

unit-test-cljs-refresh () {
	clean
	echo_message 'In a few special places, these clojure changes create some of the greatest transformation spectacles on earth'
	npx shadow-cljs compile karma
	abort_on_error
	trap stop EXIT
	npx karma start --no-single-run &
	npx shadow-cljs watch karma
}

## unit-test-cljs:
## args: [-b|-n|-r]
## Runs the ClojureScript unit tests
## [-b] Watches and compiles tests for execution within a browser
## [-n] Executes the tests targeting Node.js
## [-r] Watches tests and source files for changes, and subsequently re-evaluates
unit-test-cljs () {
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

if [ "$#" -eq 0 ];then
	usage
	exit 1
elif [[ $(grep "^$1\ (" "$script_name") ]];then
	eval $@
else
	echo_error "Unknown function $1 ($script_name $@)"
fi
