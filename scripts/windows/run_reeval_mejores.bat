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
REM     configura el presupuesto e inicializa el logger de CPLEX.
REM
REM CONJUNTOS DE INSTANCIAS (segundo argumento, o la variable REEVAL_CONJUNTO):
REM   protocolo   DEFECTO. data/evolution, offset 0, 8 instancias: la familia
REM               3C_20 con que se corrio el experimento publicado.
REM               Baseline total de CPLEX: 581,7 s.
REM               Salida: out\reeval_mejores\[Bxx]\
REM   restantes   data/evolution, offset 8, sin tope: las 28 instancias que el
REM               experimento NO uso, de 3C_40_66-01 a SCA3-5.
REM               Baseline total de CPLEX: 42.349,9 s (11,76 h). Dos instancias
REM               concentran el costo: SCA3-5 con 23.971,8 s (6,7 h) y CON3-0
REM               con 9.757,1 s (2,7 h); entre las dos, 9,4 de las 11,8 horas.
REM               Salida: out\reeval_mejores\restantes\[Bxx]\
REM   evaluacion  data/evaluation, offset 0, las 10 instancias reservadas, que
REM               nunca entraron en la evolucion.
REM               Baseline total de CPLEX: 10.977,6 s (3,05 h).
REM               Salida: out\reeval_mejores\evaluacion\[Bxx]\
REM
REM ADVERTENCIA DE COSTO: el conjunto protocolo son unos 10 minutos para los 5
REM algoritmos. Con restantes, el algoritmo B100 gasta el 100 % del tiempo base
REM de cada instancia, asi que su peor caso es del orden de esas 11,8 horas de
REM CPU en CPLEX, dominadas por SCA3-5 y CON3-0. Convienen corridas de un solo
REM algoritmo (primer argumento) antes que los 5 de una vez.
REM
REM Salida cruda (en la carpeta del conjunto):
REM   MISPResults.out               tiempo de pared (ms) por instancia, costo, ERP, hits
REM   job.0.CplexUsage.detailed.csv una fila por llamada a CPLEX (TimeUsed = CPU s)
REM   job.0.CplexUsage.summary.csv  una fila por instancia (TotalCalls, TotalTimeUsed)
REM Reporte: reportes\REEVALUACION_MEJORES_ALGORITMOS.xlsx, solo para protocolo.
REM
REM Uso:  scripts\windows\run_reeval_mejores.bat                  (los 5, protocolo)
REM       scripts\windows\run_reeval_mejores.bat B50              (solo uno, protocolo)
REM       scripts\windows\run_reeval_mejores.bat B100 restantes   (uno, otro conjunto)
REM       scripts\windows\run_reeval_mejores.bat "" evaluacion    (los 5, otro conjunto)
REM ================================================================

call "%~dp0load_env.bat"
if errorlevel 1 exit /b 1

set "POP=out/reeval_mejores/poblacion"
set "SEED=20260908"

set "ONLY=%~1"
set "CONJUNTO=%~2"
if not defined CONJUNTO set "CONJUNTO=%REEVAL_CONJUNTO%"
if not defined CONJUNTO set "CONJUNTO=protocolo"

REM Traduccion del conjunto a parametros de ECJ y a carpeta de salida.
REM El conjunto protocolo conserva la ruta out\reeval_mejores\[Bxx] porque
REM scripts\analisis\generate_reeval_mejores_report.py la lee tal cual.
set "INST_PATH="
if /I "%CONJUNTO%"=="protocolo" (
    set "INST_PATH=data/evolution"
    set "INST_OFFSET=0"
    set "INST_MAX=8"
    set "OUT=out\reeval_mejores"
    set "OUTDIR=out/reeval_mejores"
    set "REPORTE=1"
)
if /I "%CONJUNTO%"=="restantes" (
    set "INST_PATH=data/evolution"
    set "INST_OFFSET=8"
    set "INST_MAX=-1"
    set "OUT=out\reeval_mejores\restantes"
    set "OUTDIR=out/reeval_mejores/restantes"
    set "REPORTE=0"
)
if /I "%CONJUNTO%"=="evaluacion" (
    set "INST_PATH=data/evaluation"
    set "INST_OFFSET=0"
    set "INST_MAX=-1"
    set "OUT=out\reeval_mejores\evaluacion"
    set "OUTDIR=out/reeval_mejores/evaluacion"
    set "REPORTE=0"
)
if not defined INST_PATH (
    echo ERROR: conjunto desconocido "%CONJUNTO%".
    echo Conjuntos validos: protocolo, restantes, evaluacion.
    exit /b 1
)

echo.
echo Compilando codigo Java...
javac -encoding UTF-8 -d bin -cp "%CLASSPATH%" src/model/*.java src/terminals/*.java src/functions/*.java
if errorlevel 1 (
    echo ERROR: Fallo la compilacion
    exit /b 1
)

REM MyEvolutionState fuerza esta ruta si no se indica experiment.output.dir; se crea por seguridad.
if not exist "out\results\evaluation" mkdir "out\results\evaluation"

echo.
echo ================================================================
echo RE-EVALUACION DE LOS MEJORES ALGORITMOS POR CONDICION
echo Conjunto: %CONJUNTO%  (%INST_PATH%, offset %INST_OFFSET%, max %INST_MAX%)
echo Salida:   %OUT%\[Bxx]\evolution0\
echo ================================================================
if /I "%CONJUNTO%"=="restantes" (
    echo ADVERTENCIA: 28 instancias, baseline total 42.349,9 s (11,76 h^).
    echo   Con B100 el peor caso es de ese orden. SCA3-5 y CON3-0 son 9,4 h.
)

call :run B10  0.10 B10_grupo1_ejec2_gen16
call :run B25  0.25 B25_grupo2_ejec1_gen48
call :run B50  0.50 B50_grupo3_ejec4_gen53
call :run B75  0.75 B75_grupo4_ejec2_gen9
call :run B100 1.00 B100_grupo5_ejec2_gen49

if "%REPORTE%"=="1" (
    echo.
    echo Generando reporte Excel...
    python scripts\analisis\generate_reeval_mejores_report.py
    if errorlevel 1 (
        echo ADVERTENCIA: No se pudo generar el reporte. Revise los archivos en %OUT%\
    ) else (
        echo Reporte listo: reportes\REEVALUACION_MEJORES_ALGORITMOS.xlsx
    )
) else (
    echo.
    echo Crudos del conjunto %CONJUNTO% en: %OUT%\[Bxx]\evolution0\
    echo ADVERTENCIA: generate_reeval_mejores_report.py solo cubre el conjunto
    echo   protocolo; el Excel todavia no incluye %CONJUNTO%. Los crudos quedan
    echo   para analizarlos a mano.
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
echo %~1  presupuesto=%~2  individuo=%~3  conjunto=%CONJUNTO%
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
    -p experiment.instances.path=%INST_PATH% ^
    -p experiment.instances.offset=%INST_OFFSET% ^
    -p experiment.max.instances=%INST_MAX% ^
    -p experiment.output.dir=%OUTDIR%/%~1
if errorlevel 1 echo ERROR: fallo la evaluacion de %~1
exit /b 0
