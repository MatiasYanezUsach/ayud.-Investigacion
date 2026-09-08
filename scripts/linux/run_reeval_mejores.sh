#!/bin/bash
# Ejecutar siempre desde la raiz del repositorio (dos niveles arriba de scripts/linux)
cd "$(dirname "$0")/../.." || exit 1
# ================================================================
# RE-EVALUACION DE LOS 5 MEJORES ALGORITMOS (uno por condicion hibrida)
# Ver la cabecera de scripts/windows/run_reeval_mejores.bat para el diseno.
# Uso: ./scripts/linux/run_reeval_mejores.sh [B10|B25|B50|B75|B100]
# ================================================================

CLASSPATH="bin:ecj:lib/cplex.jar:lib/commons-math3-3.6.1.jar"
CPLEX_LIB_PATH="${CPLEX_LIB_PATH:-/opt/ibm/ILOG/CPLEX_Studio2211/cplex/bin/x86-64_linux}"
OUT="out/reeval_mejores"
POP="out/reeval_mejores/poblacion"
SEED=20260908
ONLY="$1"

echo "Compilando codigo fuente..."
javac -encoding UTF-8 -d bin -cp "$CLASSPATH" src/model/*.java src/terminals/*.java src/functions/*.java || { echo "ERROR: fallo la compilacion"; exit 1; }
mkdir -p out/results/evaluation

run() {
    local LABEL=$1 BUDGET=$2 TREE=$3
    if [ -n "$ONLY" ] && [ "$ONLY" != "$LABEL" ]; then return 0; fi
    echo; echo "--- $LABEL  presupuesto=$BUDGET  individuo=$TREE ---"
    rm -rf "$OUT/$LABEL"
    java -cp "$CLASSPATH" -Djava.library.path="$CPLEX_LIB_PATH" ec.Evolve \
        -file src/model/params/pdp_group1_10pct.params \
        -p jobs=1 \
        -p generations=1 \
        -p evalthreads=1 \
        -p breedthreads=1 \
        -p seed.0=$SEED \
        -p pop.subpop.0.size=1 \
        -p pop.subpop.0.extra-behavior=truncate \
        -p pop.file="\$$POP/$TREE.in" \
        -p breed.elite.0=0 \
        -p gp.fs.0.func.6.cplex-budget=$BUDGET \
        -p experiment.max.instances=8 \
        -p experiment.output.dir=$OUT/$LABEL \
        || echo "ERROR: fallo la evaluacion de $LABEL"
}

run B10  0.10 B10_grupo1_ejec2_gen16
run B25  0.25 B25_grupo2_ejec1_gen48
run B50  0.50 B50_grupo3_ejec4_gen53
run B75  0.75 B75_grupo4_ejec2_gen9
run B100 1.00 B100_grupo5_ejec2_gen49

echo; echo "Generando reporte Excel..."
python3 scripts/analisis/generate_reeval_mejores_report.py && echo "Reporte listo: reportes/REEVALUACION_MEJORES_ALGORITMOS.xlsx"
