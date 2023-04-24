#!/usr/bin/env bash
ABS_PATH=$(readlink -f "$0")
ABS_PATH=$(dirname $ABS_PATH)
git log --oneline --after="1 week ago" --pretty="format:- %s" \
--grep="^\(fix\|perf\|feat\|refactor\)\.*" > $ABS_PATH/log.txt
