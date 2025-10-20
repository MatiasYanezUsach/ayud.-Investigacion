#!/bin/bash

echo "========================================"
echo "    PRUEBA EVOLUCION: 1 repeticion"
echo "========================================"
echo

# Configurar rutas (AJUSTAR SEGUN TU INSTALACION)
CPLEX_LIB_PATH="/opt/ibm/ILOG/CPLEX_Studio201/cplex/bin/x86-64_linux"
CPLEX_JAR_PATH="/opt/ibm/ILOG/CPLEX_Studio201/cplex/lib/cplex.jar"

echo "[1/3] Compilando codigo..."
javac -cp "commons-math3-3.6.1.jar:$CPLEX_JAR_PATH:ecj" -d bin src/model/*.java src/terminals/*.java src/functions/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en la compilacion"
    exit 1
fi

echo "[2/3] Creando directorio de prueba..."
mkdir -p "out/test_evo"

echo "[3/3] Ejecutando evolucion con presupuesto 2 segundos..."
echo
echo "========================================"
echo "    PRUEBA: Grupo 1 (10%), 1 repeticion"
echo "========================================"

java -cp "bin:commons-math3-3.6.1.jar:$CPLEX_JAR_PATH:ecj" \
     -Djava.library.path="$CPLEX_LIB_PATH" \
     ec.Evolve \
     -file src/model/params/pdp_group1_10pct.params \
     -p jobs=1 \
     -p generations=5 \
     -p pop.subpop.0.size=5 \
     -p gp.fs.0.func.6.cplex-budget=2.0 \
     -p stat.file="out/test_evo/test_evolution.stat" \
     -p stat.generation=1

if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en la ejecucion"
    exit 1
fi

echo
echo "========================================"
echo "    PRUEBA EVOLUCION COMPLETADA"
echo "========================================"
echo
echo "Resultados en: out/test_evo/"
echo "- test_evolution.stat"
echo "- job.0.CplexUsage.detailed.csv"
echo "- job.0.CplexUsage.summary.csv"
echo "- job.0.CplexUsage.statistics.txt"
echo
echo "Si ves estos archivos, el sistema de evolucion funciona!"
echo
