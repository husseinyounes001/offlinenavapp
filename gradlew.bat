@echo off
SETLOCAL
set DIRNAME=%~dp0
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

REM Use the wrapper jar to download and launch Gradle
java -jar "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" %*
