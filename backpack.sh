#!/usr/bin/env bash
GIT_BASE="$( git rev-parse --show-toplevel )"
source ${GIT_BASE}/build-scripts/project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi
source build-scripts/s3-wagon-util.sh
if [[ $? -ne 0 ]];then
    echo_error 'Could not source all files'
fi

usage () {
	cat << EOF
${txtbld}SYNOPSIS${txtrst}
	${script_name} clean
	${script_name} release
	${script_name} snapshot
	${script_name} test
	${script_name} test-cljs

${txtbld}DESCRIPTION${txtrst}
	${txtbld}clean${txtrst}
		cleans
	${txtbld}release${txtrst}
		releases the library onto s3 repository
	${txtbld}snapshot${txtrst}
		releases the library onto s3 as a snapshot
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

unit-test-cljs () {
	echo_message "In the clojure kingdom, the rule is, transform or be transformed."
	lein test-cljs
	abort_on_error "ClojureScript tests failed"
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
			s3_snapshot;;
		test)
			unit-test;;
		test-cljs)
			unit-test-cljs;;
		*)
			usage
			exit 1;;
	esac
}

parse $@
