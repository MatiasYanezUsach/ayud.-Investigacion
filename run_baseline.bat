@echo off
REM ============================================================================
REM Script para ejecutar la Fase 1: Establecer Línea Base con CPLEX Puro
REM ============================================================================

echo ============================================================================
echo FASE 1: ESTABLECIMIENTO DE LINEA BASE CON CPLEX PURO
echo ============================================================================
echo.

REM Configuración de classpath (AJUSTAR ESTAS RUTAS SEGÚN TU INSTALACIÓN)
set CPLEX_JAR=cplex.jar
set ECJ_JAR=ecj\jar\ecj.28.jar
set COMMONS_JAR=commons-math3-3.6.1.jar

REM Verificar que los JARs existen
if not exist "%CPLEX_JAR%" (
    echo ERROR: No se encuentra %CPLEX_JAR%
    echo Por favor, configure la ruta correcta de CPLEX en este script
    pause
    exit /b 1
)

if not exist "%ECJ_JAR%" (
    echo ERROR: No se encuentra %ECJ_JAR%
    pause
    exit /b 1
)

REM Crear directorio de salida
if not exist "out\baseline" mkdir out\baseline

echo Paso 1: Compilando CplexBaselineRunner...
echo ----------------------------------------
javac -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" src\model\CplexBaselineRunner.java src\model\FileIO.java src\model\PDPData.java src\model\Instance.java src\model\PDPInstance.java

echo.
echo Paso 2: Ejecutando CPLEX puro en todas las instancias...
echo ----------------------------------------
echo NOTA: Esto puede tomar varios minutos
echo.

java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" model.CplexBaselineRunner

echo.
echo ============================================================================
echo LINEA BASE COMPLETADA
echo ============================================================================
echo.
echo Archivos generados en out\baseline\:
echo   - instance_baseline.csv
echo   - cplex_baseline_results.csv
echo   - cplex_baseline_summary.txt
echo.

pause
