@echo off
REM Ejecutar siempre desde la raiz del repositorio (dos niveles arriba de scripts\windows)
cd /d "%~dp0..\.."
REM ================================================================
REM RE-EVALUACION DE LOS 5 MEJORES ALGORITMOS (uno por condicion hibrida)
REM ================================================================
REM
REM Pedido del profesor guia (2026-09-08): alimentar ECJ con los 5 mejores
REM algoritmos generados (B10, B25, B50, B75, B100) y medir, sobre las 8
REM instancias del protocolo, el tiempo de CPLEX de cada uno y el tiempo
REM total de los 5.
REM
REM Diseno de la medicion:
REM   - Modo evaluacion de ECJ: generations=1 (solo se evalua, no se cria),
REM     poblacion cargada desde archivo (pop.file), 1 individuo por corrida.
REM   - Cada algoritmo se evalua con SU presupuesto (gp.fs.0.func.6.cplex-budget),
REM     el mismo con que fue evolucionado.
REM   - evalthreads=1 y breedthreads=1: sin concurrencia, asi el estado
REM     estatico de CplexTerminal y CplexUsageLogger queda bien atribuido y
REM     el tiempo de pared no sufre contencion (limitacion documentada en el
REM     Cap. 5 del trabajo de graduacion).
REM   - Una JVM por algoritmo: los contadores del logger no se acumulan.
REM   - Se usa model.PDPProblemEvo (NO PDPProblemEva): es la unica clase que
REM     lee data/evolution, respeta experiment.max.instances=8, configura el
REM     presupuesto e inicializa el logger de CPLEX.
REM
REM Salida: out\reeval_mejores\<Bxx>\evolution0\
REM   MISPResults.out               tiempo de pared (ms) por instancia, costo, ERP, hits
REM   job.0.CplexUsage.detailed.csv una fila por llamada a CPLEX (TimeUsed = CPU s)
REM   job.0.CplexUsage.summary.csv  una fila por instancia (TotalCalls, TotalTimeUsed)
REM Reporte: reportes\REEVALUACION_MEJORES_ALGORITMOS.xlsx
REM
REM Uso:  scripts\windows\run_reeval_mejores.bat            (los 5 algoritmos)
REM       scripts\windows\run_reeval_mejores.bat B50        (solo uno)
REM ================================================================

call "%~dp0load_env.bat"
if errorlevel 1 exit /b 1

set "OUT=out\reeval_mejores"
set "POP=out/reeval_mejores/poblacion"
set "SEED=20260908"

echo.
echo Compilando codigo Java...
javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src/model/*.java src/terminals/*.java src/functions/*.java
if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    exit /b 1
)

REM MyEvolutionState fuerza esta ruta si no se indica experiment.output.dir; se crea por seguridad.
if not exist "out\results\evaluation" mkdir "out\results\evaluation"

set "ONLY=%~1"

echo.
echo ================================================================
echo RE-EVALUACION DE LOS MEJORES ALGORITMOS POR CONDICION
echo ================================================================

call :run B10  0.10 B10_grupo1_ejec2_gen16
call :run B25  0.25 B25_grupo2_ejec1_gen48
call :run B50  0.50 B50_grupo3_ejec4_gen53
call :run B75  0.75 B75_grupo4_ejec2_gen9
call :run B100 1.00 B100_grupo5_ejec2_gen49

echo.
echo Generando reporte Excel...
python scripts\analisis\generate_reeval_mejores_report.py
if errorlevel 1 (
    echo ADVERTENCIA: No se pudo generar el reporte. Revise los archivos en %OUT%\
) else (
    echo Reporte listo: reportes\REEVALUACION_MEJORES_ALGORITMOS.xlsx
)
echo.
echo ================================================================
echo COMPLETADO
echo ================================================================
exit /b 0

:run
REM %1 = etiqueta, %2 = presupuesto (fraccion del tiempo base), %3 = archivo .in (sin extension)
if defined ONLY if /I not "%ONLY%"=="%~1" exit /b 0
echo.
echo ---------------------------------------------------------------
echo %~1  presupuesto=%~2  individuo=%~3
echo ---------------------------------------------------------------
if exist "%OUT%\%~1" rmdir /s /q "%OUT%\%~1"
java -cp "%CLASSPATH%" -Djava.library.path="%CPLEX_LIB_PATH%" ec.Evolve ^
    -file src\model\params\pdp_group1_10pct.params ^
    -p jobs=1 ^
    -p generations=1 ^
    -p evalthreads=1 ^
    -p breedthreads=1 ^
    -p seed.0=%SEED% ^
    -p pop.subpop.0.size=1 ^
    -p pop.subpop.0.extra-behavior=truncate ^
    -p pop.file=$%POP%/%~3.in ^
    -p breed.elite.0=0 ^
    -p gp.fs.0.func.6.cplex-budget=%~2 ^
    -p experiment.max.instances=8 ^
    -p experiment.output.dir=out/reeval_mejores/%~1
if errorlevel 1 echo ERROR: fallo la evaluacion de %~1
exit /b 0
