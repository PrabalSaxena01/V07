@echo off
title V07 Standalone Admin Server
cd /d "%~dp0"
echo ======================================================================
echo   Launching V07 Standalone Admin Web Console
echo ======================================================================
echo.
python admin_server.py
pause
