@echo off
chcp 65001 >nul
title Music Downloader Web Server
color 0a
echo ============================================================
echo   Starting Music Downloader Web Server on Port 8080...
echo   Open your browser: http://localhost:8080
echo ============================================================
echo.
python "%~dp0web_app.py" --port 8080
pause
