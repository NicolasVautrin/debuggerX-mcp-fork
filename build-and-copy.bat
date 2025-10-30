@echo off
setlocal enabledelayedexpansion

echo ========================================
echo Building and copying debuggerX proxy
echo ========================================
echo.

set "SOURCE_JAR=debuggerx-bootstrap\target\debuggerX.jar"
set "DEST_JAR=..\mcp-jdwp-java\lib\debuggerX.jar"
set "BACKUP_JAR=..\mcp-jdwp-java\lib\debuggerX.jar.old"

REM Step 1: Backup current JAR if it exists
if exist "%DEST_JAR%" (
    echo [1/4] Backing up current JAR...
    copy /Y "%DEST_JAR%" "%BACKUP_JAR%" >nul
    if !errorlevel! equ 0 (
        echo       SUCCESS: Backup created at %BACKUP_JAR%
    ) else (
        echo       ERROR: Failed to backup JAR
        exit /b 1
    )
) else (
    echo [1/4] No existing JAR to backup
)
echo.

REM Step 2: Clean and build
echo [2/4] Building proxy with Maven...
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

REM Step 3: Verify new JAR exists
if not exist "%SOURCE_JAR%" (
    echo [ERROR] Built JAR not found at %SOURCE_JAR%
    exit /b 1
)

REM Step 4: Copy new JAR
echo [3/4] Copying new JAR to MCP server...
copy /Y "%SOURCE_JAR%" "%DEST_JAR%" >nul
if !errorlevel! equ 0 (
    echo       SUCCESS: JAR copied to %DEST_JAR%
) else (
    echo       ERROR: Failed to copy JAR
    exit /b 1
)
echo.

REM Step 5: Display file info
echo [4/4] Verification:
powershell -Command "Get-Item '%DEST_JAR%' | Select-Object Name, @{Name='Size (MB)';Expression={[math]::Round($_.Length/1MB,2)}}, LastWriteTime | Format-Table -AutoSize"
echo.

echo ========================================
echo DONE!
echo ========================================
echo.
echo Next steps:
echo   1. Kill the running proxy if any:
echo      netstat -ano ^| findstr :55005
echo      taskkill /F /PID ^<PID^>
echo.
echo   2. Reconnect in Claude Code:
echo      jdwp_connect
echo.
echo Backup available at: %BACKUP_JAR%