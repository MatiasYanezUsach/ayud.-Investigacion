#!/bin/bash
# ================================================================
# FASE 1: Establecer Línea Base con CPLEX Puro
# ================================================================
# 
# Este script ejecuta CPLEX puro sobre todas las instancias para
# calcular el tiempo base (T_base) necesario para cada grupo.
# 
# IMPORTANTE: 
# - Ejecutar en la máquina dedicada del laboratorio
# - Asegurarse de que CPLEX esté correctamente configurado
# ================================================================

echo "================================================================"
echo "FASE 1: ESTABLECIMIENTO DE LINEA BASE CON CPLEX PURO"
echo "================================================================"
echo ""

# Configurar classpath
CLASSPATH="bin:ecj:cplex.jar:commons-math3-3.6.1.jar"

# Configurar ruta de bibliotecas nativas de CPLEX
# AJUSTAR ESTA RUTA SEGÚN LA INSTALACIÓN DE CPLEX
CPLEX_LIB_PATH="/opt/ibm/ILOG/CPLEX_Studio201/cplex/bin/x86-64_linux"

echo "Compilando codigo fuente..."
javac -encoding UTF-8 -d bin -cp "$CLASSPATH" src/model/*.java src/terminals/*.java src/functions/*.java

if [ $? -ne 0 ]; then
    echo "ERROR: Fallo la compilacion"
    exit 1
fi

echo ""
echo "Ejecutando experimento de linea base..."
java -cp "$CLASSPATH" -Djava.library.path="$CPLEX_LIB_PATH" model.CplexBaselineRunner

echo ""
echo "================================================================"
echo "EXPERIMENTO COMPLETADO"
echo "================================================================"
echo ""
echo "Resultados guardados en: out/baseline/"
echo ""
echo "SIGUIENTE PASO:"
echo "1. Revisar el archivo out/baseline/cplex_baseline_summary.txt"
echo "2. Tomar nota del valor T_base calculado"
echo "3. Actualizar los archivos de parametros en src/model/params/"
echo "   reemplazando TBASE_VALUE con el valor calculado"
echo ""

