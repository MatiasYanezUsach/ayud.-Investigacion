# Presupuesto de CPLEX en algoritmos híbridos generados por programación genética (VRPSPD)

Código, datos, resultados crudos y reportes del experimento del trabajo de graduación de
Matías Yáñez (Magíster en Ingeniería Informática, USACH; profesor guía: Víctor Parada).

Pregunta: cómo influye el presupuesto de tiempo asignado a un componente exacto (IBM ILOG CPLEX),
usado como terminal de un árbol de programación genética (ECJ), en la calidad y el costo de los
algoritmos generados para el VRPSPD (ruteo con recogida y entrega simultáneas).

Seis condiciones experimentales: `grupo0` = B0 (sin CPLEX), `grupo1` = B10, `grupo2` = B25,
`grupo3` = B50, `grupo4` = B75, `grupo5` = B100 (porcentaje del tiempo base de resolución exacta
por instancia). Cada condición: 5 ejecuciones independientes, 100 generaciones, población 50,
8 instancias de la familia `3C_20` (Rieck y Zimmermann, 2013).

## Mapa del repositorio

```
.
├── src/                    Código Java (ECJ): funciones, terminales, modelo VRPSPD, logger de CPLEX
│   ├── functions/          Nodos de control del árbol (And, Or, IfThenElse, DoWhile, ...)
│   ├── terminals/          Heurísticas y CplexTerminal
│   └── model/              Problema, instancia, MILP (PDPInstance), CplexUsageLogger, params/
├── ecj/                    Biblioteca ECJ (clases compiladas; va en el classpath)
├── bin/                    Clases compiladas de src/ (salida de javac)
├── lib/                    cplex.jar, cplex_2211.jar, commons-math3-3.6.1.jar
├── data/
│   ├── evolution/          36 instancias en orden alfabético; el experimento usa las primeras 8 (experiment.max.instances=8)
│   ├── evaluation/         10 instancias de evaluación
│   ├── results.txt         Óptimos conocidos por instancia (los lee FileIO.readOptimals al cargar cada instancia). NO MOVER.
│   └── legacy/             Instancias y resultados de proyectos anteriores (MISP, Dethloff, Salhi-Nagy, ...). No se usan.
├── out/                    RESULTADOS CRUDOS DEL EXPERIMENTO (no regenerables)
│   ├── baseline/           Tiempo de CPLEX puro por instancia (T_base)
│   ├── results/grupoN/evolutionM/   Salida ECJ de cada ejecución (ver docs/GUIA_EXPERIMENTO_COMPLETA.md)
│   ├── results/mejores_arboles/     Mejor árbol de cada grupo (.dot + .png)
│   ├── prueba_poblacion/   Prueba previa de tamaño de población (10 vs 100)
│   └── reeval_mejores/     Re-evaluación de los 6 mejores algoritmos (poblacion/*.in + salida por condición)
├── reportes/               Excel derivados de out/ y gráficos
│   ├── RESULTADOS_EXPERIMENTO_GRUPO0..5.xlsx, RESULTADOS_EXPERIMENTO_CONSOLIDADO.xlsx
│   ├── TIEMPO_CPLEX_MEJOR_ALGORITMO_POR_GRUPO.xlsx, reporte_prueba_poblacion.xlsx
│   └── graficos/{grupo0..5, comparativos, convergencia}/
├── scripts/
│   ├── windows/            .bat: run_experiment, run_baseline, run_prueba_poblacion, run_reeval_mejores, compile, test_*, convert_dots_to_png, load_env
│   ├── linux/              .sh equivalentes
│   └── analisis/           Python: generate_excel_report, generate_charts, generate_comparative_charts, generate_consolidated_report,
│                           generate_best_algorithm_time_report, generate_reeval_mejores_report, curva_convergencia*
├── docs/                   Guías del experimento y notas históricas
├── tools/graphviz-2.38/    Graphviz portable (render de árboles .dot)
├── .env                    Rutas locales de CPLEX (CPLEX_LIB_PATH, CPLEX_JAR_PATH)
└── requirements.txt        Dependencias Python
```

## Cómo ejecutar

Todos los scripts se invocan desde cualquier directorio: cada uno hace `cd` a la raíz del
repositorio antes de trabajar, porque el código Java y los scripts Python usan rutas relativas
a la raíz (`data/evolution`, `out/results`, `reportes/`).

Requisitos: JDK 11, CPLEX Studio (22.1.x) con `.env` apuntando a su `cplex.jar` y a su carpeta
de binarios nativos, Python 3 con `pip install -r requirements.txt`.

```bat
scripts\windows\compile.bat                     :: compila src/ en bin/
scripts\windows\run_baseline.bat                :: FASE 1: T_base por instancia -> out/baseline/
scripts\windows\run_experiment.bat 1 5          :: FASE 2: grupo 1 (B10), 5 ejecuciones -> out/results/grupo1/
scripts\windows\run_experiment.bat all 5
scripts\windows\convert_dots_to_png.bat         :: .dot -> .png en out/results/
```

Linux: los mismos comandos con `scripts/linux/*.sh`.

### Re-evaluación de los 6 mejores algoritmos (pedido de Parada, 2026-09-08)

```bat
scripts\windows\run_reeval_mejores.bat                  :: los 6 algoritmos, conjunto protocolo
scripts\windows\run_reeval_mejores.bat B0               :: solo el control, conjunto protocolo
scripts\windows\run_reeval_mejores.bat B50              :: solo uno, conjunto protocolo
scripts\windows\run_reeval_mejores.bat B100 restantes   :: un algoritmo, otro conjunto
scripts\windows\run_reeval_mejores.bat "" evaluacion    :: los 6, otro conjunto
scripts\windows\run_reeval_mejores.bat "" todas         :: los 6, las 36 de evolution
```

Carga cada mejor individuo (`out/reeval_mejores/poblacion/Bxx_*.in`) como población fija de ECJ
(`pop.file`, `generations=1`, `pop.subpop.0.size=1`), lo evalúa con su propio presupuesto, con
`evalthreads=1` y una JVM por algoritmo (sin el estado compartido que el Cap. 5 declara como
limitación). El reporte `reportes/REEVALUACION_MEJORES_ALGORITMOS.xlsx` lo genera
`scripts/analisis/generate_reeval_mejores_report.py` (tiempo de CPLEX y tiempo de pared por
instancia y por algoritmo, total de los 5 híbridos, comparación con el experimento original).
`out/reeval_mejores/poblacion/mejores_5_algoritmos.in` trae los 5 híbridos en un solo archivo por si
se quiere evaluarlos juntos bajo un mismo presupuesto (`pop.subpop.0.size=5`); no incluye B0.

**B0, la condición de control.** `out/reeval_mejores/poblacion/B0_grupo0_ejec1_gen50.in` es el mejor
árbol del grupo 0, copiado de `out/results/grupo0/evolution0/job.0.BestIndividual.in`. El grupo 0 sí
tiene árbol re-evaluable; lo que no tiene es tiempo de CPLEX que medir, porque su presupuesto es
cero por definición. Su fitness es 0,021640042 con 0 hits, empatado entre `evolution0` y
`evolution4`; el empate se resuelve por criterio determinista quedándose con la primera ejecución,
`evolution0`, o sea `ejec1`. El `gen50` sale de `job.0.EstadisticaProm&Mej.csv`: es la primera
generación en que el mejor fitness llega a ese valor (generación 49 en la numeración de 0 a 99 de
`BestFitness.csv` y de `Statistics.out`), la misma convención que usan los otros cinco nombres. El
árbol no contiene ningún `CplexTerminal`, como corresponde a la condición sin componente exacto.

Corre con `gp.fs.0.func.6.cplex-budget=0.00`. Con presupuesto cero, `PDPProblemEvo` no inicializa el
`CplexUsageLogger`, así que la corrida de B0 **no genera** `CplexUsage.detailed.csv` ni
`CplexUsage.summary.csv`: deja solo `MISPResults.out`. Eso es lo esperado, no una falla. B0 tampoco
consume tiempo de solver, así que agregarlo no cambia las cifras de costo de más abajo.

Sirve como línea base: es el algoritmo sin componente exacto contra el cual se comparan en ERP y en
hits las cinco condiciones híbridas, y permite separar cuánto del desempeño viene de las heurísticas
generadas y cuánto del presupuesto de CPLEX.

**Conjuntos de instancias.** El segundo argumento (o la variable `REEVAL_CONJUNTO`) elige contra
qué instancias se re-evalúa. En Linux es igual: `./scripts/linux/run_reeval_mejores.sh B100 restantes`.

| Conjunto | Carpeta | Offset | Cantidad | Salida cruda | Reporte Excel |
|---|---|---|---|---|---|
| `protocolo` (defecto) | `data/evolution` | 0 | 8 (familia `3C_20`) | `out/reeval_mejores/Bxx/evolution0/` | sí |
| `restantes` | `data/evolution` | 8 | 28 (`3C_40_66-01` … `SCA3-5`) | `out/reeval_mejores/restantes/Bxx/evolution0/` | no |
| `evaluacion` | `data/evaluation` | 0 | 10 | `out/reeval_mejores/evaluacion/Bxx/evolution0/` | no |
| `todas` | `data/evolution` | 0 | 36 (`protocolo` + `restantes`) | `out/reeval_mejores/todas/Bxx/evolution0/` | no |

`protocolo` es el conjunto del experimento publicado y conserva la ruta de salida original porque
`generate_reeval_mejores_report.py` la lee tal cual; los otros tres escriben en subcarpetas propias
para no pisar esos crudos y hoy no entran en el Excel.

El primer argumento solo admite `B0`, `B10`, `B25`, `B50`, `B75` o `B100`, o vacío para correr los
seis; cualquier otra cosa corta con código 1.

**Costo.** El baseline total de CPLEX del conjunto `protocolo` es 581,7 s: los 6 algoritmos toman
unos 10 minutos. El de `restantes` es 42.349,9 s (11,76 h), y dos instancias concentran el gasto:
`SCA3-5` con 23.971,8 s (6,7 h) y `CON3-0` con 9.757,1 s (2,7 h), 9,4 de las 11,8 horas entre las
dos. El de `evaluacion` es 10.977,6 s (3,05 h). El de `todas` es 42.931,5 s (11,93 h), casi el mismo
que el de `restantes` porque las 8 del protocolo aportan solo 581,7 s. Como B100 dispone del 100 %
del tiempo base de cada instancia, su peor caso sobre `restantes` o sobre `todas` es de ese orden de
magnitud en CPU de CPLEX. Con los seis algoritmos los presupuestos se suman
(0,10 + 0,25 + 0,50 + 0,75 + 1,00 = 2,60 veces el tiempo base; B0 aporta 0 porque no usa CPLEX), así
que `todas` con los seis llega a unos 111.600 s, cerca de 31 horas, el mismo total de antes de
agregar B0; conviene correr un algoritmo a la vez. Los scripts avisan por consola antes de empezar y
distinguen el caso de un solo algoritmo del de los seis.

**Parámetros nuevos** (los lee `PDPProblemEvo.setup()`; sirven para cualquier corrida, no solo la
re-evaluación):

| Parámetro | Defecto | Qué hace |
|---|---|---|
| `experiment.instances.path` | `data/evolution` | Carpeta desde la que se leen las instancias |
| `experiment.instances.offset` | `0` | Cuántas instancias se saltan desde el principio |
| `experiment.max.instances` | `-1` (todas) | Cuántas se usan **a partir del offset** |

La selección es `[offset, offset + max)` acotada al total leído; con `max` ausente o `-1` va del
offset hasta el final. Sin ninguno de los tres, el comportamiento es el del experimento publicado.
Un offset fuera de rango o una selección vacía detienen la corrida con `state.output.fatal`, y la
consola lista la carpeta, el offset, el total leído y el nombre de cada instancia seleccionada.
`FileIO.readInstances` ordena las entradas por nombre antes de leerlas: `File.listFiles()` no
garantiza orden alguno (en Windows salía alfabético de facto, que es el orden del experimento
publicado), así que ordenar no cambia qué instancias se usaron y vuelve el offset reproducible.

Reportes (leen `out/`, escriben en `reportes/`):

```bash
python scripts/analisis/generate_excel_report.py 1          # Excel del grupo 1
python scripts/analisis/generate_consolidated_report.py     # consolidado de los 6 grupos
python scripts/analisis/generate_charts.py 1                # 8 gráficos del grupo 1
python scripts/analisis/generate_comparative_charts.py      # 8 gráficos comparativos
python scripts/analisis/generate_best_algorithm_time_report.py   # tiempos del mejor algoritmo por grupo
python scripts/analisis/curva_convergencia_poblacion.py     # prueba de población
```

## Advertencias

- `out/` es la evidencia primaria. No borrar ni regenerar: las semillas fueron `seed = time`, las corridas no se repiten.
- La hoja `Configuración` de los Excel lleva la máquina del experimento fija en el script
  (`EXPERIMENT_MACHINE`: Windows 10, AMD Ryzen 7 3700X, 32 GB). Regenerarlos en otro equipo es seguro.
- Cifras "por corrida" de CPLEX: usar siempre `job.M.CplexUsage.detailed.csv` (filas = llamadas,
  suma de `TimeUsed` = tiempo). `job.M.CplexUsage.statistics.txt` acumula entre jobs de la misma JVM.
  Los Excel de `reportes/` usan `detailed.csv` desde 2026-09-08; versiones anteriores traían valores inflados.
- Numeración de generaciones: los Excel y la tesis van de 1 a 100; los archivos crudos de 0 a 99.
- El experimento evaluó con `evalthreads = 6`; `CplexTerminal` y `CplexUsageLogger` usan estado
  estático compartido. Limitación documentada en el Capítulo 5 del trabajo de graduación.

Detalle de archivos de salida, fases y parámetros: `docs/GUIA_EXPERIMENTO_COMPLETA.md`.
