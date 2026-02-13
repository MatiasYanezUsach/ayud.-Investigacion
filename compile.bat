@echo off
REM ================================================================
REM Script de compilación rápida
REM ================================================================
call load_env.bat
echo.
echo Compilando codigo Java...
javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src/model/*.java src/terminals/*.java src/functions/*.java
if errorlevel 1 (
    echo.
    echo ERROR: Fallo la compilacion
    echo.
    pause
    exit /b 1
)
echo.
echo ================================================================
echo COMPILACION EXITOSA
echo ================================================================
echo.
