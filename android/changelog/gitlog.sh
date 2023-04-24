#!/usr/bin/env bash
#ABS_PATH=$(readlink -f "$0")
#ABS_PATH=$(dirname $ABS_PATH)
git log --oneline --after="1 week ago" --pretty="format:- %s" > $GITHUB_WORKSPACE/android/changelog/log.txt
#--grep="^\(fix\|perf\|feat\|refactor\)\.*" > log.txt
