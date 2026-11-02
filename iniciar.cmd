@echo off
rem Doble clic para compilar y arrancar todo el sistema del Corte 2.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0iniciar.ps1" %*
pause
