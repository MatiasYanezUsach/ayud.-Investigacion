#!/bin/bash
# ================================================================
# FASE 2: Ejecutar Experimentos con Diferentes Presupuestos
# ================================================================
# 
# Este script ejecuta todos los grupos experimentales en secuencia.
# Cada grupo se ejecuta 30 veces para obtener resultados estadísticamente
# significativos.
# 
# GRUPOS EXPERIMENTALES:
#   Grupo 0: 0% de T_base (sin CPLEX)
#   Grupo 1: 10% de T_base
#   Grupo 2: 25% de T_base
#   Grupo 3: 50% de T_base
#   Grupo 4: 75% de T_base
#   Grupo 5: 100% de T_base
# 
# IMPORTANTE:
# - Antes de ejecutar, actualizar los archivos de parámetros con T_base
# - Ejecutar en la máquina dedicada del laboratorio
# - El experimento completo puede tardar varios días
# ================================================================

if [ $# -eq 0 ]; then
    echo "ERROR: Debe especificar el grupo experimental"
    echo ""
    echo "Uso: ./run_experiment.sh [grupo] [repeticiones]"
    echo ""
    echo "Grupos disponibles:"
    echo "  0 = Sin CPLEX (0% de T_base)"
    echo "  1 = Presupuesto minimo (10% de T_base)"
    echo "  2 = Presupuesto bajo (25% de T_base)"
    echo "  3 = Presupuesto moderado (50% de T_base)"
    echo "  4 = Presupuesto alto (75% de T_base)"
    echo "  5 = Presupuesto completo (100% de T_base)"
    echo "  all = Ejecutar todos los grupos"
    echo ""
    echo "Ejemplos:"
    echo "  ./run_experiment.sh 0 30    - Ejecutar Grupo 0, 30 repeticiones"
    echo "  ./run_experiment.sh all 30  - Ejecutar todos los grupos, 30 repeticiones cada uno"
    echo ""
    exit 1
fi

GROUP=$1
REPETITIONS=${2:-30}

# Configurar classpath
CLASSPATH="bin:ecj:cplex.jar:commons-math3-3.6.1.jar"

# Configurar ruta de bibliotecas nativas de CPLEX
# AJUSTAR ESTA RUTA SEGÚN LA INSTALACIÓN DE CPLEX
CPLEX_LIB_PATH="/opt/ibm/ILOG/CPLEX_Studio201/cplex/bin/x86-64_linux"

echo "================================================================"
echo "FASE 2: EXPERIMENTOS CON DIFERENTES PRESUPUESTOS DE CPLEX"
echo "================================================================"
echo ""

# Compilar código fuente
echo "Compilando codigo fuente..."
javac -encoding UTF-8 -d bin -cp "$CLASSPATH" src/model/*.java src/terminals/*.java src/functions/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Fallo la compilacion"
    exit 1
fi

# Función para ejecutar un grupo experimental
run_group() {
    local G=$1
    local R=$2
    
    case $G in
        0)
            PARAMS_FILE="pdp_group0_nocplex.params"
            GROUP_NAME="Grupo 0 - Sin CPLEX"
            ;;
        1)
            PARAMS_FILE="pdp_group1_10pct.params"
            GROUP_NAME="Grupo 1 - 10% T_base"
            ;;
        2)
            PARAMS_FILE="pdp_group2_25pct.params"
            GROUP_NAME="Grupo 2 - 25% T_base"
            ;;
        3)
            PARAMS_FILE="pdp_group3_50pct.params"
            GROUP_NAME="Grupo 3 - 50% T_base"
            ;;
        4)
            PARAMS_FILE="pdp_group4_75pct.params"
            GROUP_NAME="Grupo 4 - 75% T_base"
            ;;
        5)
            PARAMS_FILE="pdp_group5_100pct.params"
            GROUP_NAME="Grupo 5 - 100% T_base"
            ;;
        *)
            echo "ERROR: Grupo invalido: $G"
            return 1
            ;;
    esac
    
    echo ""
    echo "================================================================"
    echo "EJECUTANDO: $GROUP_NAME"
    echo "Archivo de parametros: $PARAMS_FILE"
    echo "Repeticiones: $R"
    echo "================================================================"
    echo ""
    
    # Ejecutar ECJ con el archivo de parámetros correspondiente
    java -cp "$CLASSPATH" -Djava.library.path="$CPLEX_LIB_PATH" ec.Evolve \
        -file src/model/params/$PARAMS_FILE \
        -p jobs=$R \
        -p generations=100 \
        -p pop.subpop.0.size=15
    
    if [ $? -ne 0 ]; then
        echo "ERROR: Fallo la ejecucion del grupo $G"
        return 1
    fi
    
    echo ""
    echo "================================================================"
    echo "COMPLETADO: $GROUP_NAME"
    echo "================================================================"
    echo ""
    
    return 0
}

# Ejecutar grupos
if [ "$GROUP" = "all" ]; then
    run_group 0 $REPETITIONS
    run_group 1 $REPETITIONS
    run_group 2 $REPETITIONS
    run_group 3 $REPETITIONS
    run_group 4 $REPETITIONS
    run_group 5 $REPETITIONS
else
    run_group $GROUP $REPETITIONS
fi

echo ""
echo "================================================================"
echo "TODOS LOS EXPERIMENTOS COMPLETADOS"
echo "================================================================"
echo ""

