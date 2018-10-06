#!/usr/bin/env bash
txtbld=$(tput bold 2>/dev/null)             # Bold
grn=$(tput setaf 2 2>/dev/null)             # Green
red=$(tput setaf 1 2>/dev/null)             # Red
bldgrn=${txtbld}$(tput setaf 2 2>/dev/null) # Bold Green
bldred=${txtbld}$(tput setaf 1 2>/dev/null) # Bold Red
txtrst=$(tput sgr0 2>/dev/null)             # Reset

cd $(realpath $(dirname $0))
script_name=$(basename $0)

echo_message () {
	echo "${bldgrn}[$script_name]${txtrst} ${FUNCNAME[1]}: ${grn}$@${txtrst}"
}

echo_red () {
	echo "${bldred}[$script_name]${txtrst} ${FUNCNAME[1]}: ${red}$@${txtrst}"
}

echo_error () {
	echo_red "ERROR: $@"
}

abort_on_error () {
	if [[ $? -ne 0 ]]; then
		echo_error $@
		exit 1
	fi
}

usage () {
	doc=$(grep '^##' "$script_name" | sed -e 's/^##//')
	desc=''
	synopsis=''
	while read -r line; do
		if [[ "$line" == *: ]];then
			fun=${line::-1}
			synopsis+="\n\t$script_name $fun"
			desc+="\n\t$fun"
		elif [[ "$line" == args:* ]];then
			args="$( cut -d ':' -f 2- <<< "$line" )"
			synopsis+="$args"
		elif [[ "$line" == [* ]];then
			desc+="\n\t\t\t$line"
		else
			desc+="\n\t\t$line"
		fi
	done <<< "$doc"
	echo -e "SYNOPSIS$synopsis\n\nDESCRIPTION$desc"
}

# -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

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
else
	if [[ $(grep "^$1\ (" "$script_name") ]];then
		eval $@
	else
		echo_error "Unknown function $1 ($script_name $@)"
	fi
fi
