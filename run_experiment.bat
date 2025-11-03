@echo off
REM ================================================================
REM FASE 2: Ejecutar Experimentos con Diferentes Presupuestos
REM ================================================================
REM 
REM Este script ejecuta todos los grupos experimentales en secuencia.
REM Cada grupo se ejecuta 30 veces para obtener resultados estadísticamente
REM significativos.
REM 
REM GRUPOS EXPERIMENTALES:
REM   Grupo 0: 0% de T_base (sin CPLEX)
REM   Grupo 1: 10% de T_base
REM   Grupo 2: 25% de T_base
REM   Grupo 3: 50% de T_base
REM   Grupo 4: 75% de T_base
REM   Grupo 5: 100% de T_base
REM 
REM IMPORTANTE:
REM - Antes de ejecutar, actualizar los archivos de parámetros con T_base
REM - Ejecutar en la máquina dedicada del laboratorio
REM - El experimento completo puede tardar varios días
REM ================================================================

if "%1"=="" (
    echo ERROR: Debe especificar el grupo experimental
    echo.
    echo Uso: run_experiment.bat [grupo] [repeticiones]
    echo.
    echo Grupos disponibles:
    echo   0 = Sin CPLEX (0%% de T_base^)
    echo   1 = Presupuesto minimo (10%% de T_base^)
    echo   2 = Presupuesto bajo (25%% de T_base^)
    echo   3 = Presupuesto moderado (50%% de T_base^)
    echo   4 = Presupuesto alto (75%% de T_base^)
    echo   5 = Presupuesto completo (100%% de T_base^)
    echo   all = Ejecutar todos los grupos
    echo.
    echo Ejemplos:
    echo   run_experiment.bat 0 30    - Ejecutar Grupo 0, 30 repeticiones
    echo   run_experiment.bat all 30  - Ejecutar todos los grupos, 30 repeticiones cada uno
    echo.
    pause
    exit /b 1
)

set GROUP=%1
set REPETITIONS=%2

if "%REPETITIONS%"=="" set REPETITIONS=30

REM Cargar configuración desde .env
call load_env.bat
if errorlevel 1 (
    echo ERROR: No se pudo cargar la configuración desde .env
    pause
    exit /b 1
)

echo ================================================================
echo FASE 2: EXPERIMENTOS CON DIFERENTES PRESUPUESTOS DE CPLEX
echo ================================================================
echo.

echo Configuración cargada:
echo   CPLEX_LIB_PATH: %CPLEX_LIB_PATH%
echo   CPLEX_JAR: %CPLEX_JAR%
echo.

REM Compilar código fuente
echo Compilando codigo fuente...
javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src/model/*.java src/terminals/*.java src/functions/*.java

if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

if "%GROUP%"=="all" (
    call :run_group 0 %REPETITIONS%
    call :run_group 1 %REPETITIONS%
    call :run_group 2 %REPETITIONS%
    call :run_group 3 %REPETITIONS%
    call :run_group 4 %REPETITIONS%
    call :run_group 5 %REPETITIONS%
) else (
    call :run_group %GROUP% %REPETITIONS%
)

echo.
echo ================================================================
echo TODOS LOS EXPERIMENTOS COMPLETADOS
echo ================================================================
echo.
pause
exit /b 0

REM ================================================================
REM Función para ejecutar un grupo experimental
REM ================================================================
:run_group
setlocal
set G=%1
set R=%2

if "%G%"=="0" (
    set PARAMS_FILE=pdp_group0_nocplex.params
    set GROUP_NAME=Grupo 0 - Sin CPLEX
) else if "%G%"=="1" (
    set PARAMS_FILE=pdp_group1_10pct.params
    set GROUP_NAME=Grupo 1 - 10%% T_base
) else if "%G%"=="2" (
    set PARAMS_FILE=pdp_group2_25pct.params
    set GROUP_NAME=Grupo 2 - 25%% T_base
) else if "%G%"=="3" (
    set PARAMS_FILE=pdp_group3_50pct.params
    set GROUP_NAME=Grupo 3 - 50%% T_base
) else if "%G%"=="4" (
    set PARAMS_FILE=pdp_group4_75pct.params
    set GROUP_NAME=Grupo 4 - 75%% T_base
) else if "%G%"=="5" (
    set PARAMS_FILE=pdp_group5_100pct.params
    set GROUP_NAME=Grupo 5 - 100%% T_base
) else (
    echo ERROR: Grupo invalido: %G%
    exit /b 1
)

echo.
echo ================================================================
echo EJECUTANDO: %GROUP_NAME%
echo Archivo de parametros: %PARAMS_FILE%
echo Repeticiones: %R%
echo ================================================================
echo.

REM Ejecutar ECJ con el archivo de parámetros correspondiente
java -cp "%CLASSPATH%" -Djava.library.path="%CPLEX_LIB_PATH%" ec.Evolve -file src/model/params/%PARAMS_FILE% -p jobs=%R% -p generations=100 -p pop.subpop.0.size=15

if errorlevel 1 (
    echo ERROR: Fallo la ejecucion del grupo %G%
    exit /b 1
)

echo.
echo ================================================================
echo COMPLETADO: %GROUP_NAME%
echo ================================================================
echo.

endlocal
exit /b 0

