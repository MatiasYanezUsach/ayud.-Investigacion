#!/bin/bash
# ================================================================
# PRUEBA DE TAMAÑO DE POBLACION
# ================================================================
# Ejecuta 1 corrida por cada combinacion de:
#   - Tamaños de población: 100, 75, 50, 15, 10
#   - Configuracion CPLEX: 0% (grupo0) y 100% (grupo5)
#
# Resultados guardados en: out/prueba_poblacion/popXXX_cplexYY/
# ================================================================

CLASSPATH="bin:ecj:cplex.jar:commons-math3-3.6.1.jar"
CPLEX_LIB_PATH="/opt/ibm/ILOG/CPLEX_Studio2212/cplex/bin/x86-64_linux"

echo "================================================================"
echo "COMPILANDO CODIGO FUENTE..."
echo "================================================================"
javac -encoding UTF-8 -d bin -cp "$CLASSPATH" src/model/*.java src/terminals/*.java src/functions/*.java
if [ $? -ne 0 ]; then
    echo "ERROR: Fallo la compilacion"
    exit 1
fi
echo "Compilacion exitosa."
echo ""

run_combo() {
    local POP=$1
    local CPLEX=$2
    local PARAMS=$3
    local OUTDIR="out/prueba_poblacion/pop${POP}_cplex${CPLEX}"

    echo "----------------------------------------------------------------"
    echo "Ejecutando: población=$POP  CPLEX=${CPLEX}%  params=$PARAMS"
    echo "Salida: $OUTDIR"
    echo "----------------------------------------------------------------"

    java -cp "$CLASSPATH" -Djava.library.path="$CPLEX_LIB_PATH" \
        ec.Evolve \
        -file src/model/params/$PARAMS \
        -p jobs=1 \
        -p generations=100 \
        -p pop.subpop.0.size=$POP \
        -p experiment.output.dir=$OUTDIR

    if [ $? -ne 0 ]; then
        echo "ERROR en población=$POP CPLEX=${CPLEX}%"
    else
        echo "OK: población=$POP CPLEX=${CPLEX}%"
    fi
    echo ""
}

POPS=(100 75 50 15 10)

for POP in "${POPS[@]}"; do
    run_combo $POP 0   pdp_group0_nocplex.params
    run_combo $POP 100 pdp_group5_100pct.params
done

echo "================================================================"
echo "TODAS LAS PRUEBAS COMPLETADAS"
echo "================================================================"
echo "Resultados en: out/prueba_poblacion/"
echo "Ahora ejecuta: python3 curva_convergencia_poblacion.py"
echo "================================================================"
