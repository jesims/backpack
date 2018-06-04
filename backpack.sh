#!/usr/bin/env bash
cd $(realpath $(dirname $0))
source build-scripts/project.sh &&
source build-scripts/s3-wagon-util.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

usage () {
	cat << EOF
${txtbld}SYNOPSIS${txtrst}
	${script_name} clean
	${script_name} release
	${script_name} snapshot [-l]
	${script_name} test [-r]
	${script_name} test-cljs [-r|-n]

${txtbld}DESCRIPTION${txtrst}
	${txtbld}clean${txtrst}
		cleans
	${txtbld}release${txtrst}
		releases the library onto s3 repository
	${txtbld}snapshot${txtrst}
		releases the library onto s3 as a snapshot
			${txtbld}-l${txtrst}  snapshot to local repository
	${txtbld}test${txtrst}
		runs the CLJ unit tests
	${txtbld}test-cljs${txtrst}
		runs the CLJS unit tests
EOF
}

deps () {
	lein deps && npm install
}

clean () {
	stop
	lein clean
}

unit-test () {
	echo_message 'In the animal kingdom, the rule is, eat or be eaten.'
	lein test
	abort_on_error 'Clojure tests failed'
}

unit-test-refresh () {
	clean
	echo_message 'The truth is our natural world is changing. And we are totally dependent on that world.'
	lein auto test $@
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

unit-test-cljs () {
	echo_message 'In the clojure kingdom, the rule is, transform or be transformed.'
	unit-test-karma
}

stop () {
	npx shadow-cljs stop &>/dev/null
	pkill -f 'karma ' &>/dev/null
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

unit-test-browser-refresh () {
	clean
	trap stop EXIT
	npx shadow-cljs watch browser
	abort_on_error
}

parse () {
	case $1 in
		-h|--help)
			usage;;
		clean)
			clean;;
		deps)
			deps;;
		release)
			s3_release;;
		snapshot)
			case $2 in
				-l|-local)
					snapshot_local;;
				*)
					s3_snapshot;;
			esac;;
		stop)
			stop;;
		test)
			case $2 in
				-r)
					unit-test-refresh ${@:3};;
				*)
					unit-test;;
			esac ;;
		test-cljs)
			case $2 in
				-r)
					unit-test-cljs-refresh;;
				-b)
					unit-test-browser-refresh;;
				-n)
					unit-test-node;;
				*)
					unit-test-cljs;;
			esac ;;
		*)
			usage
			exit 1;;
	esac
}

parse $@
