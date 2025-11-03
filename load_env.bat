@echo off
REM ================================================================
REM Función para cargar variables del archivo .env
REM ================================================================
REM Uso: call load_env.bat

setlocal enabledelayedexpansion

if not exist ".env" (
    echo ADVERTENCIA: Archivo .env no encontrado. Usando valores por defecto.
    echo.
    echo Creando archivo .env de ejemplo...
    (
        echo # ================================================================
        echo # CONFIGURACION DE CPLEX - Ajusta estas rutas según tu instalación
        echo # ================================================================
        echo.
        echo # Ruta de instalación de CPLEX ^(bibliotecas nativas^)
        echo CPLEX_LIB_PATH=C:\Program Files\IBM\ILOG\CPLEX_Studio_Community2212\cplex\bin\x64_win64
        echo.
        echo # Archivo JAR de CPLEX
        echo CPLEX_JAR_NAME=cplex.jar
    ) > .env
    echo.
    echo Archivo .env creado. Por favor, edítalo con tus rutas de CPLEX
    echo y ejecuta nuevamente.
    echo.
    pause
    exit /b 1
)

REM Leer variables del archivo .env
for /f "usebackq delims=" %%a in (".env") do (
    set "line=%%a"
    
    REM Ignorar comentarios y líneas vacías
    if not "!line:~0,1!"=="#" (
        if not "!line!"=="" (
            REM Separar variable y valor
            for /f "tokens=1,2 delims==" %%b in ("!line!") do (
                set "var=%%b"
                set "val=%%c"
                
                REM Eliminar espacios al inicio y final de la variable
                set "var=!var: =!"
                
                REM Establecer variable de entorno
                if "!var!"=="CPLEX_LIB_PATH" set "CPLEX_LIB_PATH=!val!"
                if "!var!"=="CPLEX_JAR_NAME" set "CPLEX_JAR_NAME=!val!"
                if "!var!"=="CPLEX_JAR_PATH" set "CPLEX_JAR_PATH=!val!"
                if "!var!"=="PROJECT_DIR" set "PROJECT_DIR=!val!"
                if "!var!"=="EXTRA_CLASSPATH" set "EXTRA_CLASSPATH=!val!"
            )
        )
    )
)

REM Verificar y configurar valores por defecto si no están definidos
if not defined CPLEX_JAR_NAME set "CPLEX_JAR_NAME=cplex.jar"
if not defined PROJECT_DIR set "PROJECT_DIR=%~dp0"
if not "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR%\"

REM Construir ruta completa del JAR
if defined CPLEX_JAR_PATH (
    set "CPLEX_JAR=!CPLEX_JAR_PATH!"
) else (
    set "CPLEX_JAR=!PROJECT_DIR!!CPLEX_JAR_NAME!"
)

REM Configurar classpath base
set "CLASSPATH_BASE=bin;ecj;!CPLEX_JAR!;commons-math3-3.6.1.jar"
if defined EXTRA_CLASSPATH (
    set "CLASSPATH=!CLASSPATH_BASE!;!EXTRA_CLASSPATH!"
) else (
    set "CLASSPATH=!CLASSPATH_BASE!"
)

REM Exportar variables al scope padre
endlocal & (
    set "CPLEX_LIB_PATH=%CPLEX_LIB_PATH%"
    set "CPLEX_JAR=%CPLEX_JAR%"
    set "CPLEX_JAR_NAME=%CPLEX_JAR_NAME%"
    set "PROJECT_DIR=%PROJECT_DIR%"
    set "CLASSPATH=%CLASSPATH%"
    if defined EXTRA_CLASSPATH set "EXTRA_CLASSPATH=%EXTRA_CLASSPATH%"
)

exit /b 0

