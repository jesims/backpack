#@IgnoreInspection BashAddShebang
txtbld=$(tput bold 2>/dev/null)             # Bold
grn=$(tput setaf 2 2>/dev/null)             # Green
red=$(tput setaf 1 2>/dev/null)             # Red
bldgrn=${txtbld}$(tput setaf 2 2>/dev/null) # Bold Green
bldred=${txtbld}$(tput setaf 1 2>/dev/null) # Bold Red
txtrst=$(tput sgr0 2>/dev/null)             # Reset

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
	doc=$(grep '^##' "${script_name}" | sed -e 's/^##//')
	desc=''
	synopsis=''
	while read -r line; do
		if [[ "$line" == *: ]];then
			fun=${line::-1}
			synopsis+="\n\t${script_name} ${txtbld}${fun}${txtrst}"
			desc+="\n\t${txtbld}$fun${txtrst}"
		elif [[ "$line" == args:* ]];then
			args="$( cut -d ':' -f 2- <<< "$line" )"
			synopsis+="$args"
		elif [[ "$line" =~ ^(<[{)* ]];then
			desc+="\n\t\t\t${line}"
		else
			desc+="\n\t\t${line}"
		fi
	done <<< "$doc"
	echo -e "${txtbld}SYNOPSIS${txtrst}${synopsis}\n\n${txtbld}DESCRIPTION${txtrst}${desc}"
}

script-invoke () {
	if [[ "$#" -eq 0 ]] || [[ "$1" =~ ^(help|-h|--help)$ ]];then
		usage
		exit 1
	elif [[ $(grep "^$1\ (" "$script_name") ]];then
		"$@"
	else
		echo_error "Unknown function $1 ($script_name $@)"
		exit 1
	fi
}

is-snapshot () {
	version=$(cat VERSION)
	[[ "$version" == *SNAPSHOT ]]
}

is-ci () {
	[[ -n "$CIRCLECI" ]]
}

deploy () {
	if is-ci;then
		lein-install deploy clojars &>/dev/null
	else
		lein-install deploy clojars
	fi
	abort_on_error
}

-snapshot () {
	if is-snapshot;then
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	else
		local version=$(cat VERSION)
		local snapshot="$version-SNAPSHOT"
		echo ${snapshot} > VERSION
		echo_message "Snapshotting $snapshot"
		case $1 in
			-l)
				lein-install install
				abort_on_error;;
			*)
				deploy;;
		esac
		echo "$version" > VERSION
	fi
}

lein-dev () {
	lein with-profile +dev $@
}

lein-install () {
	lein with-profile +install $@
}

-release () {
	local version=$(cat VERSION)
	if is-snapshot;then
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	else
		echo_message "Releasing $version"
		deploy
	fi
}

-docs () {
	echo_message 'Generating API documentation'
	rm -rf docs
	lein-dev codox
	abort_on_error
}
