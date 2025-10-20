@echo off
REM ================================================================
REM FASE 1: Establecer Línea Base con CPLEX Puro
REM ================================================================
REM 
REM Este script ejecuta CPLEX puro sobre todas las instancias para
REM calcular el tiempo base (T_base) necesario para cada grupo.
REM 
REM IMPORTANTE: 
REM - Ejecutar en la máquina dedicada del laboratorio
REM - Asegurarse de que CPLEX esté correctamente configurado
REM ================================================================

echo ================================================================
echo FASE 1: ESTABLECIMIENTO DE LINEA BASE CON CPLEX PURO
echo ================================================================
echo.

REM Configurar classpath
set CLASSPATH=bin;ecj;cplex_2211.jar;commons-math3-3.6.1.jar

REM Configurar ruta de bibliotecas nativas de CPLEX
REM AJUSTAR ESTA RUTA SEGÚN LA INSTALACIÓN DE CPLEX
set CPLEX_LIB_PATH=C:\Program Files\IBM\ILOG\CPLEX_Studio2211\cplex\bin\x64_win64

echo Compilando codigo fuente...
javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src/model/*.java src/terminals/*.java src/functions/*.java

if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    pause
    exit /b 1
)

echo.
echo Ejecutando experimento de linea base...
java -cp "%CLASSPATH%" -Djava.library.path="%CPLEX_LIB_PATH%" model.CplexBaselineRunner

echo.
echo ================================================================
echo EXPERIMENTO COMPLETADO
echo ================================================================
echo.
echo Resultados guardados en: out/baseline/
echo.
echo SIGUIENTE PASO:
echo 1. Revisar el archivo out/baseline/cplex_baseline_summary.txt
echo 2. Tomar nota del valor T_base calculado
echo 3. Actualizar los archivos de parametros en src/model/params/
echo    reemplazando TBASE_VALUE con el valor calculado
echo.
pause

