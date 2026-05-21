@echo off
title Hospital Patient Records Database Migration

echo ===================================================
echo HOSPITAL PATIENT RECORDS - DATABASE MIGRATION
echo ===================================================

set SOURCE_DIR=%~dp0
if "%SOURCE_DIR:~-1%"=="\" set SOURCE_DIR=%SOURCE_DIR:~0,-1%
set MYSQL_JAR=%SOURCE_DIR%\lib\mysql-connector-j-9.7.0.jar

if not exist "%SOURCE_DIR%\bin" mkdir "%SOURCE_DIR%\bin"

echo.
echo [1/2] Compiling migration class...
javac -encoding UTF-8 -cp "%MYSQL_JAR%" -d "%SOURCE_DIR%\bin" "%SOURCE_DIR%\src\config\DbConnection.java" "%SOURCE_DIR%\src\migration\DbMigration.java"

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Migration compiler failed.
    echo Check JDK PATH and mysql-connector-j jar.
    echo.
    pause
    exit /b %errorlevel%
)

echo.
echo [2/2] Running migration against XAMPP MySQL...
pushd "%SOURCE_DIR%"
java -cp "%SOURCE_DIR%\bin;%MYSQL_JAR%;" migration.DbMigration
popd

echo.
echo ===================================================
echo MIGRATION FINISHED
echo ===================================================
echo.
pause
