@echo off
setlocal EnableDelayedExpansion
title Hospital Patient Records Deployment
color 0B

echo ==========================================================
echo HOSPITAL PATIENT RECORDS - BUILD AND DEPLOY
echo ==========================================================
echo.

set SOURCE_DIR=%~dp0
if "%SOURCE_DIR:~-1%"=="\" set SOURCE_DIR=%SOURCE_DIR:~0,-1%

if exist "%USERPROFILE%\Downloads\Xampp\tomcat" (
    set TOMCAT_DIR=%USERPROFILE%\Downloads\Xampp\tomcat
) else if exist "C:\xampp\tomcat" (
    set TOMCAT_DIR=C:\xampp\tomcat
) else if exist "D:\xampp\tomcat" (
    set TOMCAT_DIR=D:\xampp\tomcat
) else (
    set TOMCAT_DIR=C:\xampp\tomcat
)

set DEPLOY_DIR=%TOMCAT_DIR%\webapps\hospital_app
set MYSQL_JAR=%SOURCE_DIR%\lib\mysql-connector-j-9.7.0.jar
set SOURCE_LIST=%SOURCE_DIR%\build_sources.txt

echo [1/5] Creating Tomcat deployment folders...
if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"
if not exist "%DEPLOY_DIR%\web" mkdir "%DEPLOY_DIR%\web"
if not exist "%DEPLOY_DIR%\web\html" mkdir "%DEPLOY_DIR%\web\html"
if not exist "%DEPLOY_DIR%\web\css" mkdir "%DEPLOY_DIR%\web\css"
if not exist "%DEPLOY_DIR%\web\js" mkdir "%DEPLOY_DIR%\web\js"
if not exist "%DEPLOY_DIR%\web\images" mkdir "%DEPLOY_DIR%\web\images"
if not exist "%DEPLOY_DIR%\WEB-INF" mkdir "%DEPLOY_DIR%\WEB-INF"
if not exist "%DEPLOY_DIR%\WEB-INF\classes" mkdir "%DEPLOY_DIR%\WEB-INF\classes"
if not exist "%DEPLOY_DIR%\WEB-INF\lib" mkdir "%DEPLOY_DIR%\WEB-INF\lib"
echo [OK] Folders ready.
echo.

echo [2/5] Copying web assets...
copy /Y "%SOURCE_DIR%\web\html\*.html" "%DEPLOY_DIR%\web\html\" >nul
copy /Y "%SOURCE_DIR%\web\css\*.css" "%DEPLOY_DIR%\web\css\" >nul
copy /Y "%SOURCE_DIR%\web\js\*.js" "%DEPLOY_DIR%\web\js\" >nul
copy /Y "%SOURCE_DIR%\web\images\*.*" "%DEPLOY_DIR%\web\images\" >nul
copy /Y "%SOURCE_DIR%\WEB-INF\web.xml" "%DEPLOY_DIR%\WEB-INF\" >nul
echo [OK] Assets copied.
echo.

echo [3/5] Copying MySQL Connector/J...
copy /Y "%MYSQL_JAR%" "%DEPLOY_DIR%\WEB-INF\lib\" >nul
echo [OK] JDBC driver copied.
echo.

echo [4/5] Compiling Java source...
if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"
pushd "%SOURCE_DIR%"
for %%F in (src\config\*.java) do (
    set "SRC=%%F"
    echo "!SRC:\=/!">>"%SOURCE_LIST%"
)
for %%F in (src\models\*.java) do (
    set "SRC=%%F"
    echo "!SRC:\=/!">>"%SOURCE_LIST%"
)
for %%F in (src\dao\*.java) do (
    set "SRC=%%F"
    echo "!SRC:\=/!">>"%SOURCE_LIST%"
)
for %%F in (src\controllers\*.java) do (
    set "SRC=%%F"
    echo "!SRC:\=/!">>"%SOURCE_LIST%"
)

javac -encoding UTF-8 -cp "%TOMCAT_DIR%\lib\servlet-api.jar;%MYSQL_JAR%" -d "%DEPLOY_DIR%\WEB-INF\classes" @"%SOURCE_LIST%"
set COMPILE_RESULT=%errorlevel%
popd

if %COMPILE_RESULT% neq 0 (
    echo.
    echo [ERROR] Compilation failed.
    echo Check that JDK, XAMPP Tomcat, and mysql-connector-j jar are available.
    echo.
    pause
    exit /b %COMPILE_RESULT%
)
if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"
echo [OK] Compilation succeeded.
echo.

echo [5/5] DEPLOYMENT COMPLETE
echo ==========================================================
echo Start MySQL and Tomcat in XAMPP, then open:
echo http://localhost:8080/hospital_app/
echo Login: admin / admin123
echo ==========================================================
echo.
pause
