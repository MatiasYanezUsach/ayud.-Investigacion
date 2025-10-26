#!/bin/bash

# ============================================================================
# Script para ejecutar un solo grupo experimental
# ============================================================================
# Uso:
#   ./run_single_group.sh <grupo> [repeticiones]
#
# Ejemplos:
#   ./run_single_group.sh 0       # Ejecuta grupo 0 con 30 repeticiones
#   ./run_single_group.sh 3 10    # Ejecuta grupo 3 con 10 repeticiones
#   ./run_single_group.sh 5 1     # Ejecuta grupo 5 con 1 repetición (prueba)
#
# Grupos disponibles:
#   0: Sin CPLEX
#   1: 10% de T_base[i]
#   2: 25% de T_base[i]
#   3: 50% de T_base[i]
#   4: 75% de T_base[i]
#   5: 100% de T_base[i]
# ============================================================================

set -e  # Exit on error

# Verificar argumentos
if [ $# -lt 1 ]; then
    echo "ERROR: Debe especificar el número de grupo"
    echo ""
    echo "Uso: $0 <grupo> [repeticiones]"
    echo ""
    echo "Grupos disponibles:"
    echo "  0: Sin CPLEX (0%)"
    echo "  1: Presupuesto Mínimo (10%)"
    echo "  2: Presupuesto Bajo (25%)"
    echo "  3: Presupuesto Moderado (50%)"
    echo "  4: Presupuesto Alto (75%)"
    echo "  5: Presupuesto Completo (100%)"
    echo ""
    echo "Ejemplos:"
    echo "  $0 0       # Ejecuta grupo 0 con 30 repeticiones"
    echo "  $0 3 10    # Ejecuta grupo 3 con 10 repeticiones"
    echo "  $0 5 1     # Ejecuta grupo 5 con 1 repetición (prueba)"
    exit 1
fi

GROUP=$1
JOBS=${2:-30}  # Default: 30 repeticiones

# Verificar que el grupo es válido
if [ "$GROUP" -lt 0 ] || [ "$GROUP" -gt 5 ]; then
    echo "ERROR: Grupo inválido. Debe ser un número entre 0 y 5"
    exit 1
fi

# Verificar que estamos en el directorio correcto
if [ ! -d "src/model" ]; then
    echo "ERROR: Debe ejecutar este script desde el directorio raíz del proyecto"
    exit 1
fi

# Verificar que existe la línea base (excepto para grupo 0)
if [ "$GROUP" != "0" ] && [ ! -f "out/baseline/instance_baseline.csv" ]; then
    echo "ERROR: No se encuentra out/baseline/instance_baseline.csv"
    echo "Debe ejecutar primero run_baseline.sh para generar la línea base"
    exit 1
fi

# Configuración
CPLEX_JAR="cplex/cplex.jar"
ECJ_JAR="ecj/ecj.jar"

# Verificar que los JARs existen
if [ ! -f "$CPLEX_JAR" ]; then
    echo "ERROR: No se encuentra $CPLEX_JAR"
    exit 1
fi

if [ ! -f "$ECJ_JAR" ]; then
    echo "ERROR: No se encuentra $ECJ_JAR"
    exit 1
fi

# Determinar archivo de parámetros y nombre del grupo
case $GROUP in
    0)
        PARAMS_FILE="src/model/params/pdp_group0_nocplex.params"
        GROUP_NAME="Grupo 0: Control Negativo (Sin CPLEX)"
        ;;
    1)
        PARAMS_FILE="src/model/params/pdp_group1_10pct.params"
        GROUP_NAME="Grupo 1: Presupuesto Mínimo (10%)"
        ;;
    2)
        PARAMS_FILE="src/model/params/pdp_group2_25pct.params"
        GROUP_NAME="Grupo 2: Presupuesto Bajo (25%)"
        ;;
    3)
        PARAMS_FILE="src/model/params/pdp_group3_50pct.params"
        GROUP_NAME="Grupo 3: Presupuesto Moderado (50%)"
        ;;
    4)
        PARAMS_FILE="src/model/params/pdp_group4_75pct.params"
        GROUP_NAME="Grupo 4: Presupuesto Alto (75%)"
        ;;
    5)
        PARAMS_FILE="src/model/params/pdp_group5_100pct.params"
        GROUP_NAME="Grupo 5: Presupuesto Completo (100%)"
        ;;
esac

# Verificar que el archivo de parámetros existe
if [ ! -f "$PARAMS_FILE" ]; then
    echo "ERROR: No se encuentra $PARAMS_FILE"
    exit 1
fi

# Compilar el proyecto
echo "============================================================================"
echo "Compilando el proyecto..."
echo "============================================================================"
echo ""
javac -cp ".:$ECJ_JAR:$CPLEX_JAR" \
    src/model/*.java \
    src/terminals/*.java \
    src/functions/*.java

# Ejecutar el grupo
echo ""
echo "============================================================================"
echo "EJECUTANDO: $GROUP_NAME"
echo "============================================================================"
echo "Archivo de parámetros: $PARAMS_FILE"
echo "Repeticiones: $JOBS"
echo "Inicio: $(date)"
echo ""

java -cp ".:$ECJ_JAR:$CPLEX_JAR" ec.Evolve \
    -file "$PARAMS_FILE" \
    -p jobs=$JOBS

echo ""
echo "============================================================================"
echo "COMPLETADO: $GROUP_NAME"
echo "============================================================================"
echo "Fin: $(date)"
echo "Repeticiones ejecutadas: $JOBS"
echo ""
echo "Resultados guardados en: out/results/"
echo ""
