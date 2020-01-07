#!/usr/bin/env bash
cd $(realpath $(dirname $0))
#TODO: Source and load from common repository
source ../project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	echo_message 'Installing dependencies'
	lein -U deps
}

## lint:
lint () {
	lein-dev lint
}

## test:
test () {
	lein test
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

## docs:
## Generate api documentation
docs () {
	-docs
}

script-invoke $@
