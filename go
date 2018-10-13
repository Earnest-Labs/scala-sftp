#!/bin/bash
################################################################################
#
# Execution wrapper for sbt + docker-compose
#
set -e

NAME=scala-sftp


# Executables
D=docker
DC=docker-compose

## Docker compose name
DC_LIBRARY_DEV_IMAGE=library-dev

R="\x1B[1;31m"
G="\x1B[1;32m"
B="\x1B[1;34m"
W="\x1B[0m"

function info {
  echo -e "${G}${1}${W}"
}

function error {
  echo -e "${R}${1}${W}"
}

function helptext {
    echo "Usage: ./go <command> [sub-command]"
    echo ""
    echo "Available commands are:"
    echo "    sbt [cmd]       SBT commands (http://www.scala-sbt.org/)"
    echo "    test            Run tests"
}

function test_ {
  info "Running tests"
  sbt_ test
}

function sbt_ {
  info "Running sbt"

  [[ "$1" == "sbt" ]] && shift
  create_sftp_folder
  ${DC} run ${DC_LIBRARY_DEV_IMAGE} sbt $@
}

function create_sftp_folder {
  mkdir -p sftp-data/data
}


[[ $@ ]] || { helptext; exit 1; }

case "$1" in
    help) helptext
    ;;
    sbt) sbt_ $@
    ;;
    test) test_
    ;;
    *)
      helptext
      error $"Usage: $0 {help|sbt|test}"
      exit 1
esac
