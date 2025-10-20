@echo off
echo ========================================
echo    PRUEBA RAPIDA DEL EXPERIMENTO
echo ========================================
echo.

REM Configurar rutas (AJUSTAR SEGUN TU INSTALACION)
set CPLEX_LIB_PATH=C:\Program Files\IBM\ILOG\CPLEX_Studio_Community2212\cplex\bin\x64_win64
set CPLEX_JAR_PATH=C:\Program Files\IBM\ILOG\CPLEX_Studio_Community2212\cplex\lib\cplex.jar

echo [1/4] Verificando rutas de CPLEX...
if not exist "%CPLEX_LIB_PATH%" (
    echo ERROR: No se encuentra CPLEX en: %CPLEX_LIB_PATH%
    echo Por favor, ajusta la ruta en este archivo
    pause
    exit /b 1
)

if not exist "%CPLEX_JAR_PATH%" (
    echo ERROR: No se encuentra cplex.jar en: %CPLEX_JAR_PATH%
    echo Por favor, ajusta la ruta en este archivo
    pause
    exit /b 1
)

echo [2/4] Compilando codigo...
javac -cp "commons-math3-3.6.1.jar;%CPLEX_JAR_PATH%;ecj" -d bin src/model/*.java src/terminals/*.java src/functions/*.java

if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la compilacion
    pause
    exit /b 1
)

echo [3/4] Creando directorio de prueba...
if not exist "out\test" mkdir "out\test"

echo [4/4] Ejecutando prueba con 1 instancia...
echo.
echo ========================================
echo    PRUEBA: 1 instancia, presupuesto 5s
echo ========================================

java -cp "bin;commons-math3-3.6.1.jar;%CPLEX_JAR_PATH%;ecj" ^
     -Djava.library.path="%CPLEX_LIB_PATH%" ^
     model.CplexBaselineRunner ^
     -instances "data\test_small.txt" ^
     -output "out\test" ^
     -timeLimit 5.0 ^
     -singleInstance

if %ERRORLEVEL% neq 0 (
    echo ERROR: Fallo en la ejecucion
    pause
    exit /b 1
)

echo.
echo ========================================
echo    PRUEBA COMPLETADA
echo ========================================
echo.
echo Resultados en: out\test\
echo - cplex_baseline_results.csv
echo - cplex_baseline_summary.txt
echo.
echo Si ves estos archivos, el sistema funciona!
echo.
pause
