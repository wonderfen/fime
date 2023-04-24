@echo off
chcp 65001
git log --oneline --after="1 week ago" --pretty="format:- %%s" > %~dp0log.txt
