@echo off
setlocal
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo ==========================================================
echo Starting Flowra Spring Boot Backend...
echo JAVA_HOME = %JAVA_HOME%
echo ==========================================================

cd /d "%~dp0"
call mvnw.cmd spring-boot:run
pause
