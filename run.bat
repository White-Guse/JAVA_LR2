@echo off
setlocal
cd /d "%~dp0"
if not exist "target\shape-prototype-1.0-SNAPSHOT.jar" (
    call "%~dp0build.bat"
    if errorlevel 1 exit /b 1
)
java -jar "target\shape-prototype-1.0-SNAPSHOT.jar"
if errorlevel 1 pause
