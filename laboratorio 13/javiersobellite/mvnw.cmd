@echo off
setlocal
set MAVEN_VERSION=3.9.11
set MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip
set CACHE_ROOT=%USERPROFILE%\.m2\wrapper\dists
set MAVEN_HOME=%CACHE_ROOT%\apache-maven-%MAVEN_VERSION%

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

if exist "%MAVEN_HOME%\bin\mvn.cmd" goto runmaven
if not exist "%CACHE_ROOT%" mkdir "%CACHE_ROOT%"
set TEMP_DIR=%TEMP%\techlab-maven-%RANDOM%
mkdir "%TEMP_DIR%"
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%TEMP_DIR%\maven.zip'; Expand-Archive -Path '%TEMP_DIR%\maven.zip' -DestinationPath '%TEMP_DIR%' -Force"
if errorlevel 1 exit /b %ERRORLEVEL%
move "%TEMP_DIR%\apache-maven-%MAVEN_VERSION%" "%MAVEN_HOME%" >nul
rmdir /s /q "%TEMP_DIR%"

:runmaven
call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %ERRORLEVEL%
