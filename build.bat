@echo off
setlocal
cd /d "%~dp0"
set "MAVEN_CMD=%LOCALAPPDATA%\Programs\ApacheMaven\apache-maven-3.9.9\bin\mvn.cmd"
if exist "%MAVEN_CMD%" (
    call "%MAVEN_CMD%" package
) else (
    call mvn package
)
if errorlevel 1 (
    pause
    exit /b 1
)
