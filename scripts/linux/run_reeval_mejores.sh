#!/bin/bash
# Ejecutar siempre desde la raiz del repositorio (dos niveles arriba de scripts/linux)
cd "$(dirname "$0")/../.." || exit 1
# ================================================================
# RE-EVALUACION DE LOS 5 MEJORES ALGORITMOS (uno por condicion hibrida)
# Ver la cabecera de scripts/windows/run_reeval_mejores.bat para el diseno.
#
# CONJUNTOS DE INSTANCIAS (segundo argumento, o la variable REEVAL_CONJUNTO):
#   protocolo   DEFECTO. data/evolution, offset 0, 8 instancias: la familia
#               3C_20 con que se corrio el experimento publicado.
#               Baseline total de CPLEX: 581,7 s.
#               Salida: out/reeval_mejores/[Bxx]/
#   restantes   data/evolution, offset 8, sin tope: las 28 instancias que el
#               experimento NO uso, de 3C_40_66-01 a SCA3-5.
#               Baseline total de CPLEX: 42.349,9 s (11,76 h). Dos instancias
#               concentran el costo: SCA3-5 con 23.971,8 s (6,7 h) y CON3-0
#               con 9.757,1 s (2,7 h); entre las dos, 9,4 de las 11,8 horas.
#               Salida: out/reeval_mejores/restantes/[Bxx]/
#   evaluacion  data/evaluation, offset 0, las 10 instancias reservadas, que
#               nunca entraron en la evolucion.
#               Baseline total de CPLEX: 10.977,6 s (3,05 h).
#               Salida: out/reeval_mejores/evaluacion/[Bxx]/
#   todas       data/evolution, offset 0, sin tope: las 36 instancias de la
#               carpeta de evolucion, o sea protocolo mas restantes juntos.
#               Baseline total de CPLEX: 42.931,5 s (11,93 h). Es casi el
#               mismo que el de restantes porque las 8 del protocolo aportan
#               solo 581,7 s; el costo lo dominan SCA3-5 y CON3-0.
#               Salida: out/reeval_mejores/todas/[Bxx]/
#
# ADVERTENCIA DE COSTO: el conjunto protocolo son unos 10 minutos para los 5
# algoritmos. Con restantes o con todas, el algoritmo B100 gasta el 100 % del
# tiempo base de cada instancia, asi que su peor caso es del orden de esas
# 11,8 o 11,9 horas de CPU en CPLEX, dominadas por SCA3-5 y CON3-0. Si ademas
# se corren los 5 algoritmos de una vez, los presupuestos se suman
# (0,10 + 0,25 + 0,50 + 0,75 + 1,00 = 2,60 veces el tiempo base): sobre todas
# el peor caso llega a unos 111.600 s, cerca de 31 horas. Convienen corridas
# de un solo algoritmo (primer argumento) antes que los 5 de una vez.
#
# El reporte Excel (reportes/REEVALUACION_MEJORES_ALGORITMOS.xlsx) solo cubre
# el conjunto protocolo; para los otros quedan los crudos.
#
# El primer argumento solo admite B10, B25, B50, B75 o B100, o vacio para los
# 5. B0 no es una etiqueta valida: el grupo 0 es la condicion sin CPLEX, no
# tiene arbol re-evaluable ni presupuesto que medir.
#
# Uso: ./scripts/linux/run_reeval_mejores.sh [B10|B25|B50|B75|B100] [conjunto]
#      ./scripts/linux/run_reeval_mejores.sh "" restantes
#      ./scripts/linux/run_reeval_mejores.sh "" todas
# ================================================================

CLASSPATH="bin:ecj:lib/cplex.jar:lib/commons-math3-3.6.1.jar"
CPLEX_LIB_PATH="${CPLEX_LIB_PATH:-/opt/ibm/ILOG/CPLEX_Studio2211/cplex/bin/x86-64_linux}"
POP="out/reeval_mejores/poblacion"
SEED=20260908
ONLY="$1"
CONJUNTO="${2:-${REEVAL_CONJUNTO:-protocolo}}"

# Validacion temprana de la etiqueta de algoritmo. Sin esto, una etiqueta que
# no existe hace que las 5 llamadas a run salgan en silencio por el filtro y el
# script termine sin haber evaluado nada.
case "$ONLY" in
    ""|B10|B25|B50|B75|B100) ;;
    *)
        echo "ERROR: etiqueta de algoritmo desconocida \"$ONLY\"."
        echo "Etiquetas validas: B10, B25, B50, B75, B100."
        echo "B0 no aplica: el grupo 0 es la condicion sin CPLEX, no tiene arbol"
        echo "  re-evaluable ni presupuesto que medir."
        echo "Para correr los 5, deje el primer argumento vacio: \"\" seguido del conjunto."
        exit 1
        ;;
esac

# Traduccion del conjunto a parametros de ECJ y a carpeta de salida.
# El conjunto protocolo conserva la ruta out/reeval_mejores/[Bxx] porque
# scripts/analisis/generate_reeval_mejores_report.py la lee tal cual.
case "$CONJUNTO" in
    protocolo)
        INST_PATH="data/evolution"; INST_OFFSET=0; INST_MAX=8
        OUT="out/reeval_mejores"; REPORTE=1
        ;;
    restantes)
        INST_PATH="data/evolution"; INST_OFFSET=8; INST_MAX=-1
        OUT="out/reeval_mejores/restantes"; REPORTE=0
        ;;
    evaluacion)
        INST_PATH="data/evaluation"; INST_OFFSET=0; INST_MAX=-1
        OUT="out/reeval_mejores/evaluacion"; REPORTE=0
        ;;
    todas)
        INST_PATH="data/evolution"; INST_OFFSET=0; INST_MAX=-1
        OUT="out/reeval_mejores/todas"; REPORTE=0
        ;;
    *)
        echo "ERROR: conjunto desconocido \"$CONJUNTO\"."
        echo "Conjuntos validos: protocolo, restantes, evaluacion, todas."
        exit 1
        ;;
esac

echo "Compilando codigo fuente..."
javac -encoding UTF-8 -d bin -cp "$CLASSPATH" src/model/*.java src/terminals/*.java src/functions/*.java || { echo "ERROR: fallo la compilacion"; exit 1; }
mkdir -p out/results/evaluation

echo
echo "Conjunto: $CONJUNTO ($INST_PATH, offset $INST_OFFSET, max $INST_MAX)"
echo "Salida:   $OUT/[Bxx]/evolution0/"
# Advertencia de costo para los conjuntos que incluyen las instancias grandes.
# Con los 5 algoritmos los presupuestos se suman: 0,10 + 0,25 + 0,50 + 0,75 +
# 1,00 = 2,60 veces el tiempo base de CPLEX.
AVISO=""
AVISO5=""
case "$CONJUNTO" in
    restantes)
        AVISO="28 instancias, baseline total de CPLEX 42.349,9 s, o sea 11,76 h"
        AVISO5="110.100 s, unas 30,6 horas"
        ;;
    todas)
        AVISO="36 instancias, baseline total de CPLEX 42.931,5 s, o sea 11,93 h"
        AVISO5="111.600 s, unas 31 horas"
        ;;
esac
if [ -n "$AVISO" ]; then
    echo "ADVERTENCIA: $AVISO."
    echo "  Lo dominan SCA3-5 con 23.971,8 s (6,7 h) y CON3-0 con 9.757,1 s (2,7 h)."
    if [ -n "$ONLY" ]; then
        echo "  Con un solo algoritmo el peor caso es B100, que dispone del 100 % del"
        echo "  tiempo base de cada instancia: del orden de ese mismo total."
    else
        echo "  Se van a correr los 5 algoritmos y los presupuestos se suman"
        echo "  (0,10 + 0,25 + 0,50 + 0,75 + 1,00 = 2,60 veces el tiempo base), asi que"
        echo "  el peor caso es del orden de $AVISO5 de CPU en CPLEX."
        echo "  Conviene correr un algoritmo a la vez."
    fi
fi

run() {
    local LABEL=$1 BUDGET=$2 TREE=$3
    if [ -n "$ONLY" ] && [ "$ONLY" != "$LABEL" ]; then return 0; fi
    echo; echo "--- $LABEL  presupuesto=$BUDGET  individuo=$TREE  conjunto=$CONJUNTO ---"
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
        -p experiment.instances.path=$INST_PATH \
        -p experiment.instances.offset=$INST_OFFSET \
        -p experiment.max.instances=$INST_MAX \
        -p experiment.output.dir=$OUT/$LABEL \
        || echo "ERROR: fallo la evaluacion de $LABEL"
}

run B10  0.10 B10_grupo1_ejec2_gen16
run B25  0.25 B25_grupo2_ejec1_gen48
run B50  0.50 B50_grupo3_ejec4_gen53
run B75  0.75 B75_grupo4_ejec2_gen9
run B100 1.00 B100_grupo5_ejec2_gen49

if [ "$REPORTE" = "1" ]; then
    echo; echo "Generando reporte Excel..."
    python3 scripts/analisis/generate_reeval_mejores_report.py && echo "Reporte listo: reportes/REEVALUACION_MEJORES_ALGORITMOS.xlsx"
else
    echo
    echo "Crudos del conjunto $CONJUNTO en: $OUT/[Bxx]/evolution0/"
    echo "ADVERTENCIA: generate_reeval_mejores_report.py solo cubre el conjunto"
    echo "  protocolo; el Excel todavia no incluye $CONJUNTO. Los crudos quedan"
    echo "  para analizarlos a mano."
fi
