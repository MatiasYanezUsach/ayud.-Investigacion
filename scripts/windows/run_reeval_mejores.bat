@echo off
REM Ejecutar siempre desde la raiz del repositorio (dos niveles arriba de scripts\windows)
cd /d "%~dp0..\.."
REM ================================================================
REM RE-EVALUACION DE LOS 6 MEJORES ALGORITMOS (uno por condicion experimental)
REM ================================================================
REM
REM Pedido del profesor guia (2026-09-08): alimentar ECJ con los mejores
REM algoritmos generados y medir, sobre las 8 instancias del protocolo, el
REM tiempo de CPLEX de cada uno y el tiempo total.
REM
REM Se cubren las seis condiciones del experimento: B0 (grupo 0, control sin
REM CPLEX) mas las cinco hibridas B10, B25, B50, B75 y B100.
REM
REM B0 es la condicion de control: su arbol se re-evalua igual que los otros,
REM pero con presupuesto 0,00, o sea sin componente exacto. Sirve de linea base
REM en ERP y hits contra la cual se comparan las cinco hibridas. Como el
REM presupuesto es cero, PDPProblemEvo no inicializa el CplexUsageLogger, asi
REM que la corrida de B0 NO genera CplexUsage.detailed.csv ni
REM CplexUsage.summary.csv: solo MISPResults.out. Eso es lo esperado, no una
REM falla.
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
REM   todas       data/evolution, offset 0, sin tope: las 36 instancias de la
REM               carpeta de evolucion, o sea protocolo mas restantes juntos.
REM               Baseline total de CPLEX: 42.931,5 s (11,93 h). Es casi el
REM               mismo que el de restantes porque las 8 del protocolo aportan
REM               solo 581,7 s; el costo lo dominan SCA3-5 y CON3-0.
REM               Salida: out\reeval_mejores\todas\[Bxx]\
REM
REM ADVERTENCIA DE COSTO: el conjunto protocolo son unos 10 minutos para los 6
REM algoritmos. Con restantes o con todas, el algoritmo B100 gasta el 100 % del
REM tiempo base de cada instancia, asi que su peor caso es del orden de esas
REM 11,8 o 11,9 horas de CPU en CPLEX, dominadas por SCA3-5 y CON3-0. Si ademas
REM se corren los 6 algoritmos de una vez, los presupuestos se suman
REM (0,00 + 0,10 + 0,25 + 0,50 + 0,75 + 1,00 = 2,60 veces el tiempo base; B0
REM aporta 0 porque no usa CPLEX): sobre todas el peor caso llega a unos
REM 111.600 s, cerca de 31 horas, el mismo total de antes de agregar B0.
REM Convienen corridas de un solo algoritmo (primer argumento) antes que los 6
REM de una vez.
REM
REM Salida cruda (en la carpeta del conjunto):
REM   MISPResults.out               tiempo de pared (ms) por instancia, costo, ERP, hits
REM   job.0.CplexUsage.detailed.csv una fila por llamada a CPLEX (TimeUsed = CPU s)
REM   job.0.CplexUsage.summary.csv  una fila por instancia (TotalCalls, TotalTimeUsed)
REM Los dos CSV de CPLEX solo aparecen con presupuesto mayor que 0: B0 deja solo
REM MISPResults.out.
REM Reporte: reportes\REEVALUACION_MEJORES_ALGORITMOS.xlsx, solo para protocolo.
REM
REM Uso:  scripts\windows\run_reeval_mejores.bat                  (los 6, protocolo)
REM       scripts\windows\run_reeval_mejores.bat B50              (solo uno, protocolo)
REM       scripts\windows\run_reeval_mejores.bat B100 restantes   (uno, otro conjunto)
REM       scripts\windows\run_reeval_mejores.bat "" evaluacion    (los 6, otro conjunto)
REM       scripts\windows\run_reeval_mejores.bat "" todas         (los 6, las 36)
REM
REM El primer argumento solo admite B0, B10, B25, B50, B75 o B100, o vacio para
REM los 6.
REM ================================================================

call "%~dp0load_env.bat"
if errorlevel 1 exit /b 1

set "POP=out/reeval_mejores/poblacion"
set "SEED=20260908"

set "ONLY=%~1"
set "CONJUNTO=%~2"
if not defined CONJUNTO set "CONJUNTO=%REEVAL_CONJUNTO%"
if not defined CONJUNTO set "CONJUNTO=protocolo"

REM Validacion temprana de la etiqueta de algoritmo. Sin esto, una etiqueta que
REM no existe hace que los 6 call :run salgan en silencio por el filtro y el
REM script anuncie COMPLETADO sin haber evaluado nada.
set "ETIQUETA_OK="
if not defined ONLY set "ETIQUETA_OK=1"
for %%E in (B0 B10 B25 B50 B75 B100) do if /I "%ONLY%"=="%%E" set "ETIQUETA_OK=1"
if not defined ETIQUETA_OK (
    echo ERROR: etiqueta de algoritmo desconocida "%ONLY%".
    echo Etiquetas validas: B0, B10, B25, B50, B75, B100.
    echo Para correr los 6, deje el primer argumento vacio: "" seguido del conjunto.
    exit /b 1
)

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
if /I "%CONJUNTO%"=="todas" (
    set "INST_PATH=data/evolution"
    set "INST_OFFSET=0"
    set "INST_MAX=-1"
    set "OUT=out\reeval_mejores\todas"
    set "OUTDIR=out/reeval_mejores/todas"
    set "REPORTE=0"
)
if not defined INST_PATH (
    echo ERROR: conjunto desconocido "%CONJUNTO%".
    echo Conjuntos validos: protocolo, restantes, evaluacion, todas.
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
REM Advertencia de costo para los conjuntos que incluyen las instancias grandes.
REM Con los 6 algoritmos los presupuestos se suman: 0,10 + 0,25 + 0,50 + 0,75 +
REM 1,00 = 2,60 veces el tiempo base de CPLEX. B0 no entra en esa suma porque
REM corre sin componente exacto, asi que agregarlo no cambia las cifras.
set "AVISO="
set "AVISO5="
if /I "%CONJUNTO%"=="restantes" (
    set "AVISO=28 instancias, baseline total de CPLEX 42.349,9 s, o sea 11,76 h"
    set "AVISO5=110.100 s, unas 30,6 horas"
)
if /I "%CONJUNTO%"=="todas" (
    set "AVISO=36 instancias, baseline total de CPLEX 42.931,5 s, o sea 11,93 h"
    set "AVISO5=111.600 s, unas 31 horas"
)
if defined AVISO (
    echo ADVERTENCIA: %AVISO%.
    echo   Lo dominan SCA3-5 con 23.971,8 s (6,7 h^) y CON3-0 con 9.757,1 s (2,7 h^).
    if defined ONLY (
        echo   Con un solo algoritmo el peor caso es B100, que dispone del 100 %% del
        echo   tiempo base de cada instancia: del orden de ese mismo total.
        echo   B0 es la excepcion: si el elegido es B0, no usa CPLEX y no suma
        echo   tiempo de solver.
    ) else (
        echo   Se van a correr los 6 algoritmos, pero solo 5 tienen presupuesto: B0
        echo   corre sin CPLEX y aporta 0. Los presupuestos se suman
        echo   (0,10 + 0,25 + 0,50 + 0,75 + 1,00 = 2,60 veces el tiempo base^), asi que
        echo   el peor caso es del orden de %AVISO5% de CPU en CPLEX.
        echo   Conviene correr un algoritmo a la vez.
    )
)

REM B0 es el control: presupuesto 0,00, sin CPLEX. Solo deja MISPResults.out.
call :run B0   0.00 B0_grupo0_ejec1_gen50
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
