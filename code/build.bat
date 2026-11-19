@echo off
setlocal
if exist gradlew.bat (
  call gradlew.bat clean build
) else (
  call gradle clean build
)
exit /b %ERRORLEVEL%
