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
	${script_name} test
	${script_name} test-cljs

${txtbld}DESCRIPTION${txtrst}
	${txtbld}clean${txtrst}
		cleans
	${txtbld}release${txtrst}
		releases the library onto s3 repository
	${txtbld}snapshot${txtrst}
		releases the library onto as a snapshot
	${txtbld}test${txtrst}
		runs the CLJ unit tests
	${txtbld}test-cljs${txtrst}
		runs the CLJS unit tests
EOF
}

unit-test () {
	echo_message "In the animal kingdom, the rule is, eat or be eaten."
	lein trampoline test
	abort_on_error "Clojure tests failed"
}

unit-test-cljs-refresh () {
	echo_message "The truth is our natural world is changing. And we are totally dependent on that world."
	lein test-refresh
}

unit-test-cljs () {
	echo_message "In the clojure kingdom, the rule is, transform or be transformed."
	lein test-cljs
	abort_on_error "ClojureScript tests failed"
}

unit-test-cljs-refresh () {
	echo_message "In a few special places, these clojure changes create some of the greatest transformation spectacles on earth"
	lein doo
}

parse () {
	case $1 in
		-h|--help)
			usage;;
		clean)
			lein clean;;
		release)
			s3_release;;
		snapshot)
			case $2 in
				-l)
					snapshot_local;;
				*)
					s3_snapshot;;
			esac;;
		test)
			case $2 in
				-r)
					unit-test-refresh;;
				*)
					unit-test;;
			esac ;;
		test-cljs)
			case $2 in
				-r)
					unit-test-cljs-refresh;;
				*)
					unit-test-cljs;;
			esac ;;
		*)
			usage
			exit 1;;
	esac
}

parse $@
