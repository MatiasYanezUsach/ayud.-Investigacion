@echo off
echo ========================================
echo    PRUEBA EVOLUCION: 1 repeticion
echo ========================================
echo.

REM Configurar rutas (AJUSTAR SEGUN TU INSTALACION)
set CPLEX_LIB_PATH=C:\Program Files\IBM\ILOG\CPLEX_Studio_Community2212\cplex\bin\x64_win64
set CPLEX_JAR_PATH=C:\Program Files\IBM\ILOG\CPLEX_Studio_Community2212\cplex\lib\cplex.jar

echo [1/3] Compilando codigo...
javac -cp "commons-math3-3.6.1.jar;%CPLEX_JAR_PATH%;ecj" -d bin src/model/*.java src/terminals/*.java src/functions/*.java

if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la compilacion
    pause
    exit /b 1
)

echo [2/3] Creando directorio de prueba...
if not exist "out\test_evo" mkdir "out\test_evo"

echo [3/3] Ejecutando evolucion con presupuesto 2 segundos...
echo.
echo ========================================
echo    PRUEBA: Grupo 1 (10%%), 1 repeticion
echo ========================================

java -cp "bin;commons-math3-3.6.1.jar;%CPLEX_JAR_PATH%;ecj" ^
     -Djava.library.path="%CPLEX_LIB_PATH%" ^
     ec.Evolve ^
     -file src\model\params\pdp_group1_10pct.params ^
     -p jobs=1 ^
     -p generations=5 ^
     -p pop.subpop.0.size=5 ^
     -p gp.fs.0.func.6.cplex-budget=2.0 ^
     -p stat.file="out\test_evo\test_evolution.stat" ^
     -p stat.generation=1

if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la ejecucion
    pause
    exit /b 1
)

echo.
echo ========================================
echo    PRUEBA EVOLUCION COMPLETADA
echo ========================================
echo.
echo Resultados en: out\test_evo\
echo - test_evolution.stat
echo - job.0.CplexUsage.detailed.csv
echo - job.0.CplexUsage.summary.csv
echo - job.0.CplexUsage.statistics.txt
echo.
echo Si ves estos archivos, el sistema de evolucion funciona!
echo.
pause
