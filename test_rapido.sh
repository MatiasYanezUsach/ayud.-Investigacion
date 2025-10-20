#!/bin/bash

echo "========================================"
echo "    PRUEBA RAPIDA DEL EXPERIMENTO"
echo "========================================"
echo

# Configurar rutas (AJUSTAR SEGUN TU INSTALACION)
CPLEX_LIB_PATH="/opt/ibm/ILOG/CPLEX_Studio201/cplex/bin/x86-64_linux"
CPLEX_JAR_PATH="/opt/ibm/ILOG/CPLEX_Studio201/cplex/lib/cplex.jar"

echo "[1/4] Verificando rutas de CPLEX..."
if [ ! -d "$CPLEX_LIB_PATH" ]; then
    echo "ERROR: No se encuentra CPLEX en: $CPLEX_LIB_PATH"
    echo "Por favor, ajusta la ruta en este archivo"
    exit 1
fi

if [ ! -f "$CPLEX_JAR_PATH" ]; then
    echo "ERROR: No se encuentra cplex.jar en: $CPLEX_JAR_PATH"
    echo "Por favor, ajusta la ruta en este archivo"
    exit 1
fi

echo "[2/4] Compilando codigo..."
javac -cp "commons-math3-3.6.1.jar:$CPLEX_JAR_PATH:ecj" -d bin src/model/*.java src/terminals/*.java src/functions/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en la compilacion"
    exit 1
fi

echo "[3/4] Creando directorio de prueba..."
mkdir -p "out/test"

echo "[4/4] Ejecutando prueba con 1 instancia..."
echo
echo "========================================"
echo "    PRUEBA: 1 instancia, presupuesto 5s"
echo "========================================"

java -cp "bin:commons-math3-3.6.1.jar:$CPLEX_JAR_PATH:ecj" \
     -Djava.library.path="$CPLEX_LIB_PATH" \
     model.CplexBaselineRunner \
     -instances "data/class1/C101_20_02.txt" \
     -output "out/test" \
     -timeLimit 5.0

if [ $? -ne 0 ]; then
    echo "ERROR: Fallo en la ejecucion"
    exit 1
fi

echo
echo "========================================"
echo "    PRUEBA COMPLETADA"
echo "========================================"
echo
echo "Resultados en: out/test/"
echo "- cplex_baseline_results.csv"
echo "- cplex_baseline_summary.txt"
echo
echo "Si ves estos archivos, el sistema funciona!"
echo
