@echo off
REM ============================================================================
REM Script para ejecutar un solo grupo experimental
REM ============================================================================
REM Uso:
REM   run_single_group.bat <grupo> [repeticiones]
REM
REM Ejemplos:
REM   run_single_group.bat 0       REM Ejecuta grupo 0 con 30 repeticiones
REM   run_single_group.bat 3 10    REM Ejecuta grupo 3 con 10 repeticiones
REM   run_single_group.bat 5 1     REM Ejecuta grupo 5 con 1 repetición (prueba)
REM
REM Grupos disponibles:
REM   0: Sin CPLEX
REM   1: 10%% de T_base[i]
REM   2: 25%% de T_base[i]
REM   3: 50%% de T_base[i]
REM   4: 75%% de T_base[i]
REM   5: 100%% de T_base[i]
REM ============================================================================

setlocal enabledelayedexpansion

REM Verificar argumentos
if "%1"=="" (
    echo ERROR: Debe especificar el numero de grupo
    echo.
    echo Uso: %~nx0 ^<grupo^> [repeticiones]
    echo.
    echo Grupos disponibles:
    echo   0: Sin CPLEX (0%%)
    echo   1: Presupuesto Minimo (10%%)
    echo   2: Presupuesto Bajo (25%%)
    echo   3: Presupuesto Moderado (50%%)
    echo   4: Presupuesto Alto (75%%)
    echo   5: Presupuesto Completo (100%%)
    echo.
    echo Ejemplos:
    echo   %~nx0 0       REM Ejecuta grupo 0 con 30 repeticiones
    echo   %~nx0 3 10    REM Ejecuta grupo 3 con 10 repeticiones
    echo   %~nx0 5 1     REM Ejecuta grupo 5 con 1 repeticion (prueba)
    pause
    exit /b 1
)

set GROUP=%1
if "%2"=="" (
    set JOBS=30
) else (
    set JOBS=%2
)

REM Verificar que el grupo es válido
if %GROUP% LSS 0 (
    echo ERROR: Grupo invalido. Debe ser un numero entre 0 y 5
    pause
    exit /b 1
)
if %GROUP% GTR 5 (
    echo ERROR: Grupo invalido. Debe ser un numero entre 0 y 5
    pause
    exit /b 1
)

REM Verificar que estamos en el directorio correcto
if not exist "src\model" (
    echo ERROR: Debe ejecutar este script desde el directorio raiz del proyecto
    pause
    exit /b 1
)

REM Verificar que existe la línea base (excepto para grupo 0)
if NOT "%GROUP%"=="0" (
    if not exist "out\baseline\instance_baseline.csv" (
        echo ERROR: No se encuentra out\baseline\instance_baseline.csv
        echo Debe ejecutar primero run_baseline.bat para generar la linea base
        pause
        exit /b 1
    )
)

REM Configuración (AJUSTAR ESTAS RUTAS SEGÚN TU INSTALACIÓN)
set CPLEX_JAR=cplex.jar
set ECJ_JAR=ecj\jar\ecj.28.jar
set COMMONS_JAR=commons-math3-3.6.1.jar

REM Verificar que los JARs existen
if not exist "%CPLEX_JAR%" (
    echo ERROR: No se encuentra %CPLEX_JAR%
    pause
    exit /b 1
)

if not exist "%ECJ_JAR%" (
    echo ERROR: No se encuentra %ECJ_JAR%
    pause
    exit /b 1
)

REM Determinar archivo de parámetros y nombre del grupo
if "%GROUP%"=="0" (
    set PARAMS_FILE=src\model\params\pdp_group0_nocplex.params
    set GROUP_NAME=Grupo 0: Control Negativo (Sin CPLEX)
)
if "%GROUP%"=="1" (
    set PARAMS_FILE=src\model\params\pdp_group1_10pct.params
    set GROUP_NAME=Grupo 1: Presupuesto Minimo (10%%)
)
if "%GROUP%"=="2" (
    set PARAMS_FILE=src\model\params\pdp_group2_25pct.params
    set GROUP_NAME=Grupo 2: Presupuesto Bajo (25%%)
)
if "%GROUP%"=="3" (
    set PARAMS_FILE=src\model\params\pdp_group3_50pct.params
    set GROUP_NAME=Grupo 3: Presupuesto Moderado (50%%)
)
if "%GROUP%"=="4" (
    set PARAMS_FILE=src\model\params\pdp_group4_75pct.params
    set GROUP_NAME=Grupo 4: Presupuesto Alto (75%%)
)
if "%GROUP%"=="5" (
    set PARAMS_FILE=src\model\params\pdp_group5_100pct.params
    set GROUP_NAME=Grupo 5: Presupuesto Completo (100%%)
)

REM Verificar que el archivo de parámetros existe
if not exist "%PARAMS_FILE%" (
    echo ERROR: No se encuentra %PARAMS_FILE%
    pause
    exit /b 1
)

REM Compilar el proyecto
echo ============================================================================
echo Compilando el proyecto...
echo ============================================================================
echo.
javac -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" src\model\*.java src\terminals\*.java src\functions\*.java

if %ERRORLEVEL% NEQ 0 (
    echo ERROR en compilacion
    pause
    exit /b 1
)

REM Ejecutar el grupo
echo.
echo ============================================================================
echo EJECUTANDO: !GROUP_NAME!
echo ============================================================================
echo Archivo de parametros: %PARAMS_FILE%
echo Repeticiones: %JOBS%
echo Inicio: %date% %time%
echo.

java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" ec.Evolve -file %PARAMS_FILE% -p jobs=%JOBS%

echo.
echo ============================================================================
echo COMPLETADO: !GROUP_NAME!
echo ============================================================================
echo Fin: %date% %time%
echo Repeticiones ejecutadas: %JOBS%
echo.
echo Resultados guardados en: out\results\
echo.

pause
