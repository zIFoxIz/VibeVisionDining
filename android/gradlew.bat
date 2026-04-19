@ECHO OFF
SETLOCAL

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

set JAVA_EXE=
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set JAVA_EXE=%JAVA_HOME%\bin\java.exe
if "%JAVA_EXE%"=="" if exist "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" set JAVA_EXE=C:\Program Files\Android\Android Studio\jbr\bin\java.exe
if "%JAVA_EXE%"=="" if exist "C:\Program Files\Android\Android Studio\jre\bin\java.exe" set JAVA_EXE=C:\Program Files\Android\Android Studio\jre\bin\java.exe

where java >NUL 2>&1
if %ERRORLEVEL% NEQ 0 if "%JAVA_EXE%"=="" (
  echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
  exit /b 1
)

if "%JAVA_EXE%"=="" (
  java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
) else (
  "%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
)
ENDLOCAL
