@echo off
chcp 65001 >nul
set PYTHONIOENCODING=utf-8
title CMD Music Downloader (YouTube & Spotify)
color 0b
echo ============================================================
echo      Starting CMD Music Downloader...
echo ============================================================
echo.
python "%~dp0main.py"
pause
