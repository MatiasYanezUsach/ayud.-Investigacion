@echo off
REM ================================================================
REM PRUEBA DE TAMAÑO DE POBLACION
REM ================================================================
REM Ejecuta 1 corrida por cada combinacion de:
REM   - Tamaños de población: 100, 75, 50, 15, 10
REM   - Configuracion CPLEX: 0%% (grupo0) y 100%% (grupo5)
REM
REM Resultados guardados en: out/prueba_poblacion/popXXX_cplexYY/
REM ================================================================

REM Cargar configuración desde .env
call load_env.bat
if errorlevel 1 (
    echo ERROR: No se pudo cargar la configuración desde .env
    pause
    exit /b 1
)

echo ================================================================
echo COMPILANDO CODIGO FUENTE...
echo ================================================================
javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src/model/*.java src/terminals/*.java src/functions/*.java
if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)
echo Compilacion exitosa.
echo.

REM Tamaños de población a probar
set POPS=100 75 50 15 10

REM Ejecutar todas las combinaciones
for %%P in (%POPS%) do (
    call :run_combo %%P 0   pdp_group0_nocplex.params
    call :run_combo %%P 100 pdp_group5_100pct.params
)

echo.
echo ================================================================
echo TODAS LAS PRUEBAS COMPLETADAS
echo ================================================================
echo Resultados en: out/prueba_poblacion/
echo Ahora ejecuta: python curva_convergencia_poblacion.py
echo ================================================================
pause
exit /b 0


REM ================================================================
REM Función: run_combo POP_SIZE CPLEX_PCT PARAMS_FILE
REM ================================================================
:run_combo
setlocal
set POP=%1
set CPLEX=%2
set PARAMS=%3
set OUTDIR=out/prueba_poblacion/pop%POP%_cplex%CPLEX%

echo ----------------------------------------------------------------
echo Ejecutando: población=%POP%  CPLEX=%CPLEX%%%  params=%PARAMS%
echo Salida: %OUTDIR%
echo ----------------------------------------------------------------

java -cp "%CLASSPATH%" -Djava.library.path="%CPLEX_LIB_PATH%" ^
    ec.Evolve ^
    -file src/model/params/%PARAMS% ^
    -p jobs=1 ^
    -p generations=100 ^
    -p pop.subpop.0.size=%POP% ^
    -p experiment.output.dir=%OUTDIR%

if errorlevel 1 (
    echo ERROR en población=%POP% CPLEX=%CPLEX%%%
) else (
    echo OK: población=%POP% CPLEX=%CPLEX%%%
)
echo.
endlocal
exit /b 0
