@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0verify-core.ps1"
if errorlevel 1 pause
