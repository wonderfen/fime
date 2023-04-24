@echo off
chcp 65001
git log --oneline --after="1 week ago" --pretty="format:- %%s" ^
--grep="^\(fix\|perf\|feat\|refactor\)\.*" > %~dp0log.txt
