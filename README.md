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
│   ├── evolution/          36 instancias; el experimento usa las primeras 8 (experiment.max.instances=8)
│   ├── evaluation/         10 instancias de evaluación
│   └── legacy/             Instancias y resultados de proyectos anteriores (MISP, Dethloff, Salhi-Nagy, ...). No se usan.
├── out/                    RESULTADOS CRUDOS DEL EXPERIMENTO (no regenerables)
│   ├── baseline/           Tiempo de CPLEX puro por instancia (T_base)
│   ├── results/grupoN/evolutionM/   Salida ECJ de cada ejecución (ver docs/GUIA_EXPERIMENTO_COMPLETA.md)
│   ├── results/mejores_arboles/     Mejor árbol de cada grupo (.dot + .png)
│   └── prueba_poblacion/   Prueba previa de tamaño de población (10 vs 100)
├── reportes/               Excel derivados de out/ y gráficos
│   ├── RESULTADOS_EXPERIMENTO_GRUPO0..5.xlsx, RESULTADOS_EXPERIMENTO_CONSOLIDADO.xlsx
│   ├── TIEMPO_CPLEX_MEJOR_ALGORITMO_POR_GRUPO.xlsx, reporte_prueba_poblacion.xlsx
│   └── graficos/{grupo0..5, comparativos, convergencia}/
├── scripts/
│   ├── windows/            .bat: run_experiment, run_baseline, run_prueba_poblacion, compile, test_*, convert_dots_to_png, load_env
│   ├── linux/              .sh equivalentes
│   └── analisis/           Python: generate_excel_report, generate_charts, generate_comparative_charts,
│                           generate_consolidated_report, generate_best_algorithm_time_report, curva_convergencia*
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
- La hoja `Configuración` de los Excel por grupo registra la máquina donde se ejecuta el script,
  no la del experimento. Regenerarlos en otro equipo sobrescribe esos datos (la máquina real fue
  Windows 10, AMD Ryzen 7 3700X, 32 GB). Los valores numéricos no cambian.
- Cifras "por corrida" de CPLEX: usar siempre `job.M.CplexUsage.detailed.csv` (filas = llamadas,
  suma de `TimeUsed` = tiempo). `job.M.CplexUsage.statistics.txt` acumula entre jobs de la misma JVM.
- Numeración de generaciones: los Excel y la tesis van de 1 a 100; los archivos crudos de 0 a 99.
- El experimento evaluó con `evalthreads = 6`; `CplexTerminal` y `CplexUsageLogger` usan estado
  estático compartido. Limitación documentada en el Capítulo 5 del trabajo de graduación.

Detalle de archivos de salida, fases y parámetros: `docs/GUIA_EXPERIMENTO_COMPLETA.md`.
