@echo off
setlocal enabledelayedexpansion

REM Change to the directory where this script is located
cd /d "%~dp0"

echo ========================================
echo Building and copying debuggerX proxy
echo ========================================
echo.
echo Working directory: %CD%
echo.

set "SOURCE_JAR=debuggerx-bootstrap\target\debuggerx-bootstrap-1.0-SNAPSHOT.jar"
set "DEST_JAR=..\mcp-jdwp-java\lib\debuggerX.jar"
set "BACKUP_JAR=..\mcp-jdwp-java\lib\debuggerX.jar.old"

REM Step 1: Backup current JAR if it exists
if exist "%DEST_JAR%" (
    echo [1/5] Backing up current JAR...
    copy /Y "%DEST_JAR%" "%BACKUP_JAR%" >nul
    if !errorlevel! equ 0 (
        echo       SUCCESS: Backup created at %BACKUP_JAR%
    ) else (
        echo       ERROR: Failed to backup JAR
        exit /b 1
    )
) else (
    echo [1/5] No existing JAR to backup
)
echo.

REM Step 2: Delete old build artifacts to ensure fresh build
echo [2/5] Cleaning old build artifacts...
if exist "%SOURCE_JAR%" (
    del /Q "%SOURCE_JAR%" >nul
    echo       SUCCESS: Old JAR deleted
) else (
    echo       No old JAR to delete
)
echo.

REM Step 3: Clean and build
echo [3/5] Building proxy with Maven...
echo       This may take 30-60 seconds...
echo.
call "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2.3\plugins\maven\lib\maven3\bin\mvn.cmd" clean package -DskipTests
set BUILD_RESULT=!errorlevel!
echo.

if !BUILD_RESULT! neq 0 (
    echo [ERROR] Build failed!
    echo.
    echo You can restore the old JAR with:
    echo   copy "%BACKUP_JAR%" "%DEST_JAR%"
    exit /b 1
)

echo [SUCCESS] Build completed successfully!
echo.

REM Step 4: Verify new JAR exists
if not exist "%SOURCE_JAR%" (
    echo [ERROR] Built JAR not found at %SOURCE_JAR%
    exit /b 1
)

REM Step 5: Copy new JAR
echo [4/5] Copying new JAR to MCP server...
copy /Y "%SOURCE_JAR%" "%DEST_JAR%" >nul
if !errorlevel! equ 0 (
    echo       SUCCESS: JAR copied to %DEST_JAR%
) else (
    echo       ERROR: Failed to copy JAR
    exit /b 1
)
echo.

REM Step 6: Display file info
echo [5/5] Verification:
powershell -Command "Get-Item '%DEST_JAR%' | Select-Object Name, @{Name='Size (MB)';Expression={[math]::Round($_.Length/1MB,2)}}, LastWriteTime | Format-Table -AutoSize"
echo.

echo ========================================
echo DONE!
echo ========================================
echo.
echo Next steps:
echo   1. Shutdown the running proxy gracefully:
echo      curl -X POST http://localhost:55006/shutdown
echo.
echo   2. Reconnect in Claude Code:
echo      jdwp_connect
echo.
echo Backup available at: %BACKUP_JAR%