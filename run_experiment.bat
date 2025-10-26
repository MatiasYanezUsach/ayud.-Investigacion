@echo off
REM ============================================================================
REM Script para ejecutar la Fase 2: Experimento con 6 Grupos
REM ============================================================================

echo ============================================================================
echo FASE 2: EJECUCION DE GRUPOS EXPERIMENTALES
echo ============================================================================
echo.

REM Configuración
set CPLEX_JAR=cplex.jar
set ECJ_JAR=ecj\jar\ecj.28.jar
set COMMONS_JAR=commons-math3-3.6.1.jar
set JOBS=30

REM Compilar el proyecto
echo Compilando el proyecto completo...
javac -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" src\model\*.java src\terminals\*.java src\functions\*.java

echo.
echo Ejecutando grupos experimentales...
echo.

REM GRUPO 0
echo ============================================================================
echo GRUPO 0: Sin CPLEX (0%%)
echo ============================================================================
java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" ec.Evolve -file src\model\params\pdp_group0_nocplex.params -p jobs=%JOBS%

REM GRUPO 1
echo ============================================================================
echo GRUPO 1: Presupuesto Minimo (10%%)
echo ============================================================================
java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" ec.Evolve -file src\model\params\pdp_group1_10pct.params -p jobs=%JOBS%

REM GRUPO 2
echo ============================================================================
echo GRUPO 2: Presupuesto Bajo (25%%)
echo ============================================================================
java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" ec.Evolve -file src\model\params\pdp_group2_25pct.params -p jobs=%JOBS%

REM GRUPO 3
echo ============================================================================
echo GRUPO 3: Presupuesto Moderado (50%%)
echo ============================================================================
java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" ec.Evolve -file src\model\params\pdp_group3_50pct.params -p jobs=%JOBS%

REM GRUPO 4
echo ============================================================================
echo GRUPO 4: Presupuesto Alto (75%%)
echo ============================================================================
java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" ec.Evolve -file src\model\params\pdp_group4_75pct.params -p jobs=%JOBS%

REM GRUPO 5
echo ============================================================================
echo GRUPO 5: Presupuesto Completo (100%%)
echo ============================================================================
java -cp ".;%ECJ_JAR%;%CPLEX_JAR%;%COMMONS_JAR%" ec.Evolve -file src\model\params\pdp_group5_100pct.params -p jobs=%JOBS%

echo.
echo ============================================================================
echo EXPERIMENTO COMPLETADO
echo ============================================================================
echo.

pause
