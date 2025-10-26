# DISEÑO EXPERIMENTAL - Presupuestos Dinámicos de CPLEX por Instancia

## Resumen Ejecutivo

Este documento describe la implementación del diseño experimental para investigar **cuánto presupuesto de CPLEX necesita un algoritmo híbrido generado automáticamente para encontrar soluciones de calidad comparable a CPLEX puro**.

### Pregunta de Investigación

¿Qué porcentaje del tiempo que CPLEX puro necesita para resolver cada instancia requiere un algoritmo híbrido generado automáticamente para alcanzar soluciones óptimas o cercanas al óptimo **EN ESA MISMA INSTANCIA**?

### Hipótesis

Un algoritmo híbrido generado automáticamente requiere significativamente menos presupuesto total de CPLEX (< 50% de T_base[i]) que CPLEX puro para alcanzar soluciones de calidad comparable en cada instancia i.

---

## Métrica Seleccionada: Presupuesto Total de CPLEX POR INSTANCIA

**Definición**: El presupuesto total es el tiempo computacional acumulado que el algoritmo puede usar CPLEX durante toda su ejecución en **UNA instancia específica**.

**Cálculo POR INSTANCIA**:
```
presupuesto[i] = porcentaje_grupo × T_base[i]
```

Donde:
- `T_base[i]`: Tiempo que CPLEX puro necesita para resolver la instancia i
- `porcentaje_grupo`: 0%, 10%, 25%, 50%, 75%, o 100%

**Ejemplo**:
- Instancia A: T_base[A] = 20 segundos
- Instancia B: T_base[B] = 80 segundos
- Grupo experimental al 50%:
  - Presupuesto para A = 0.50 × 20 = 10 segundos
  - Presupuesto para B = 0.50 × 80 = 40 segundos

**Importancia**: Esta métrica respeta la dificultad individual de cada instancia y permite comparación directa INSTANCIA POR INSTANCIA contra CPLEX puro.

---

## Grupos Experimentales

### Grupo 0: Control Negativo (Sin CPLEX)
- **Presupuesto**: 0% de T_base[i]
- **Archivo params**: `pdp_group0_nocplex.params`
- **CPLEX**: NO disponible como terminal
- **Pregunta**: ¿Qué calidad logra el algoritmo puramente heurístico?

### Grupo 1: Presupuesto Mínimo
- **Presupuesto**: 10% de T_base[i]
- **Archivo params**: `pdp_group1_10pct.params`
- **Parámetro**: `gp.fs.0.func.6.cplex-budget-percentage = 0.10`
- **Pregunta**: ¿Con muy poco CPLEX el algoritmo híbrido ya muestra mejora?

### Grupo 2: Presupuesto Bajo
- **Presupuesto**: 25% de T_base[i]
- **Archivo params**: `pdp_group2_25pct.params`
- **Parámetro**: `gp.fs.0.func.6.cplex-budget-percentage = 0.25`
- **Pregunta**: ¿Con un cuarto del tiempo de CPLEX puro POR INSTANCIA, qué tan cerca llegamos del óptimo?

### Grupo 3: Presupuesto Moderado
- **Presupuesto**: 50% de T_base[i]
- **Archivo params**: `pdp_group3_50pct.params`
- **Parámetro**: `gp.fs.0.func.6.cplex-budget-percentage = 0.50`
- **Pregunta**: ¿Con la mitad del tiempo POR INSTANCIA logramos resultados comparables?

### Grupo 4: Presupuesto Alto
- **Presupuesto**: 75% de T_base[i]
- **Archivo params**: `pdp_group4_75pct.params`
- **Parámetro**: `gp.fs.0.func.6.cplex-budget-percentage = 0.75`
- **Pregunta**: ¿Nos acercamos a la calidad de CPLEX puro usando menos tiempo EN CADA INSTANCIA?

### Grupo 5: Presupuesto Completo
- **Presupuesto**: 100% de T_base[i]
- **Archivo params**: `pdp_group5_100pct.params`
- **Parámetro**: `gp.fs.0.func.6.cplex-budget-percentage = 1.00`
- **Pregunta**: ¿Con el mismo tiempo total POR INSTANCIA, el algoritmo híbrido supera a CPLEX puro?

---

## Implementación Técnica

### Fase 1: Línea Base con CPLEX Puro

#### Ejecutar Baseline
```bash
# Compilar
javac -cp ".:ecj/ecj.jar:cplex/cplex.jar" src/model/CplexBaselineRunner.java

# Ejecutar
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" model.CplexBaselineRunner
```

#### Archivos Generados
1. **`out/baseline/cplex_baseline_results.csv`**
   - Formato: `Type,Instance,Optimal,Cost,TimeSeconds,Status,Gap`
   - Resultados detallados de cada ejecución

2. **`out/baseline/instance_baseline.csv`** (NUEVO)
   - Formato: `InstanceName,T_base_seconds,Optimal_value,Status,Difficulty`
   - Mapeo de cada instancia a su T_base[i] individual
   - Clasificación de dificultad: Easy/Medium/Hard

3. **`out/baseline/cplex_baseline_summary.txt`**
   - Estadísticas globales
   - Distribución de tiempos
   - Recomendaciones de presupuestos

#### Estadísticas de la Línea Base Actual
```
Total de instancias: 46 (36 evolución + 10 evaluación)
T_base mínimo: 0.061 segundos
T_base máximo: 2.092 segundos
T_base promedio: 0.485 segundos
Desviación estándar: 0.411 segundos
```

**IMPORTANTE**: La gran variación (min=0.061s, max=2.092s) justifica el uso de presupuestos POR INSTANCIA en lugar de un presupuesto fijo basado en el promedio.

---

### Fase 2: Sistema de Presupuestos Dinámicos

#### Componentes Implementados

1. **`InstanceBudgetManager.java`** (NUEVO)
   - Carga `instance_baseline.csv`
   - Almacena mapping: `instanceName -> T_base[i]`
   - Calcula presupuestos dinámicos: `presupuesto[i] = porcentaje × T_base[i]`
   - Proporciona estadísticas del sistema

2. **`CplexTerminal.java`** (MODIFICADO)
   - Soporta dos modos:
     - **Legacy**: Presupuesto fijo global (compatible con código anterior)
     - **Dinámico**: Presupuesto por instancia usando `InstanceBudgetManager`
   - Método `enableDynamicBudget()` para activar modo dinámico
   - Método `resetInstanceBudget(instanceName)` calcula automáticamente presupuesto específico

3. **`PDPProblemEvo.java`** (MODIFICADO)
   - Lee parámetro `gp.fs.0.func.6.cplex-budget-percentage`
   - Inicializa `InstanceBudgetManager` con porcentaje y archivo baseline
   - Habilita modo dinámico en `CplexTerminal`
   - Mantiene compatibilidad con modo legacy

#### Flujo de Ejecución

```
1. PDPProblemEvo.setup()
   ├─ Lee cplex-budget-percentage del archivo params
   ├─ Inicializa InstanceBudgetManager(instance_baseline.csv, percentage)
   └─ Habilita modo dinámico en CplexTerminal

2. Para cada individuo evaluado:
   └─ Para cada instancia i:
       ├─ CplexTerminal.resetInstanceBudget(instanceName)
       │   ├─ Obtiene T_base[i] de InstanceBudgetManager
       │   └─ Calcula presupuesto[i] = percentage × T_base[i]
       │
       ├─ Ejecuta algoritmo en instancia i
       │   └─ Llamadas a CPLEX limitadas por presupuesto[i]
       │
       └─ Registra resultados (ERP[i], tiempo usado, etc.)
```

---

### Fase 3: Mecanismo de Control de Presupuesto

#### Control Acumulativo por Instancia

Para cada instancia i, el sistema mantiene:

```java
totalBudget[i] = porcentaje × T_base[i]  // Presupuesto asignado
usedBudget[i] = 0.0                       // Inicialmente cero
callCount[i] = 0                          // Contador de llamadas
```

#### Distribución de Presupuesto Entre Llamadas

Estrategia implementada en `CplexTerminal.calculateTimeLimit()`:
```java
remainingBudget = totalBudget[i] - usedBudget[i]
timeLimit = min(remainingBudget, remainingBudget × 0.4)
```

Esta estrategia:
- Limita cada llamada al 40% del presupuesto restante
- Permite múltiples llamadas estratégicas
- Evita agotamiento prematuro del presupuesto

#### Reset Entre Instancias

**CRÍTICO**: Al cambiar de instancia:
```java
CplexTerminal.resetInstanceBudget(newInstanceName)
```

Esto automáticamente:
1. Calcula nuevo `totalBudget = porcentaje × T_base[newInstance]`
2. Resetea `usedBudget = 0`
3. Resetea `callCount = 0`

Garantiza que **cada instancia es independiente** en cuanto a presupuesto.

---

## Variables del Experimento

### Variable Independiente (lo que VARIAMOS)
- **Porcentaje del tiempo base T_base[i] usado como presupuesto de CPLEX**
- Valores: 0%, 10%, 25%, 50%, 75%, 100%
- Configuración: `gp.fs.0.func.6.cplex-budget-percentage`

### Variables Dependientes (lo que MEDIMOS)

#### 1. Calidad de Soluciones POR INSTANCIA
```
ERP[i] = ((Valor_encontrado[i] - OPT[i]) / OPT[i]) × 100
Hit[i] = 1 si encuentra OPT[i], 0 en caso contrario
```

Métricas agregadas:
- ERP_promedio = promedio de todos los ERP[i]
- Total_hits = suma de todos los Hit[i]
- Desviación estándar del ERP

#### 2. Eficiencia Computacional POR INSTANCIA
```
Presupuesto_asignado[i] = porcentaje × T_base[i]
Presupuesto_usado[i] = tiempo real de CPLEX usado en instancia i
Eficiencia[i] = Presupuesto_usado[i] / Presupuesto_asignado[i]
Eficiencia_relativa[i] = Presupuesto_usado[i] / T_base[i]
```

#### 3. Distribución del Uso de CPLEX
- Número de llamadas por instancia
- Tiempo por llamada (primera vs última)
- Distribución de uso del presupuesto

#### 4. Análisis por Dificultad de Instancia
Clasificación según T_base[i]:
- **Fáciles**: T_base[i] < umbral_bajo
- **Medianas**: umbral_bajo ≤ T_base[i] < umbral_alto
- **Difíciles**: T_base[i] ≥ umbral_alto

Pregunta: ¿Las instancias difíciles necesitan más presupuesto?

### Variables Fijas (CONSTANTES)

Estos parámetros NO cambian entre grupos:
- Tamaño de población: 15 individuos
- Número de generaciones: 100
- Tasa de cruce: 85%
- Tasa de reproducción: 10%
- Tasa de mutación: 5%
- Altura máxima del árbol: 5 niveles
- Número máximo de nodos: 45
- Conjunto de instancias: 46 instancias fijas
- Hardware y software: misma máquina, misma versión de CPLEX

---

## Formato de Salida de Datos

### Tabla de Línea Base (Fase 1)
```csv
InstanceName,T_base_seconds,Optimal_value,Status,Difficulty
3C_20_50-02.txt,0.260,10054.00,Feasible,Medium
3C_20_50-03.txt,0.440,8387.00,Feasible,Hard
...
```

### Resultados por Instancia y Grupo (Fase 2)
```csv
Grupo,% Presup,Instancia,Presup_asign[i],Rep,Valor,ERP[i],Hit[i],Presup_usado[i],N_llamadas
G2,10%,inst_001,1.53s,1,1270,1.6%,0,1.48s,3
G2,10%,inst_001,1.53s,2,1250,0.0%,1,1.51s,2
...
```

### Resultados Agregados por Grupo (Fase 3)
```csv
Grupo,% Presup,ERP_prom,σ_ERP,Hits_tot,Presup_usado_prom,Eficiencia_prom
G1,0%,14.2%,8.5%,2,0.0s,N/A
G2,10%,9.8%,6.2%,8,4.2s,85%
...
```

---

## Ejecución del Experimento

### Paso 1: Generar Línea Base
```bash
# IMPORTANTE: Ejecutar en máquina dedicada del laboratorio

# Compilar
cd /ruta/al/proyecto
javac -cp ".:ecj/ecj.jar:cplex/cplex.jar" src/model/*.java src/terminals/*.java src/functions/*.java

# Ejecutar baseline
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" model.CplexBaselineRunner

# Verificar archivos generados
ls -lh out/baseline/
# Debe mostrar:
# - cplex_baseline_results.csv
# - instance_baseline.csv (NUEVO)
# - cplex_baseline_summary.txt
```

### Paso 2: Ejecutar Grupos Experimentales

#### Grupo 0 (Sin CPLEX)
```bash
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" ec.Evolve \
  -file src/model/params/pdp_group0_nocplex.params \
  -p jobs=30
```

#### Grupo 1 (10% de T_base[i])
```bash
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" ec.Evolve \
  -file src/model/params/pdp_group1_10pct.params \
  -p jobs=30
```

#### Grupo 2 (25% de T_base[i])
```bash
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" ec.Evolve \
  -file src/model/params/pdp_group2_25pct.params \
  -p jobs=30
```

#### Grupo 3 (50% de T_base[i])
```bash
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" ec.Evolve \
  -file src/model/params/pdp_group3_50pct.params \
  -p jobs=30
```

#### Grupo 4 (75% de T_base[i])
```bash
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" ec.Evolve \
  -file src/model/params/pdp_group4_75pct.params \
  -p jobs=30
```

#### Grupo 5 (100% de T_base[i])
```bash
java -cp ".:ecj/ecj.jar:cplex/cplex.jar" ec.Evolve \
  -file src/model/params/pdp_group5_100pct.params \
  -p jobs=30
```

---

## Análisis de Resultados

### Paso 1: Analizar Logs de CPLEX

Para cada ejecución job.X, revisar:
```
out/results/evolutionX/job.X.CplexUsage.detailed.csv
out/results/evolutionX/job.X.CplexUsage.summary.csv
out/results/evolutionX/job.X.CplexUsage.statistics.txt
```

### Paso 2: Comparación Entre Grupos

1. **Por instancia individual**:
   - Para cada instancia i, comparar ERP[i] entre grupos
   - Identificar punto de rendimiento decreciente para esa instancia

2. **Por categoría de dificultad**:
   - Comparar desempeño en instancias fáciles vs difíciles
   - ¿El presupuesto óptimo depende de la dificultad?

3. **Agregado global**:
   - ERP promedio por grupo
   - Total de hits por grupo
   - Eficiencia en uso del presupuesto

### Paso 3: Visualizaciones Recomendadas

1. **Curva de rendimiento por presupuesto**:
   - Eje X: Porcentaje de presupuesto (0% a 100%)
   - Eje Y: ERP promedio
   - Identificar punto de rendimiento decreciente

2. **Box plots por dificultad**:
   - Comparar distribución de ERP entre grupos
   - Separar por dificultad de instancia

3. **Scatter plot instancia por instancia**:
   - Cada punto = una instancia
   - Eje X: T_base[i]
   - Eje Y: ERP[i]
   - Colores = grupos experimentales

---

## Preguntas de Investigación Específicas

1. **¿Cuánto presupuesto es suficiente?**
   - ¿En qué porcentaje el ERP se estabiliza?
   - ¿Existe un punto de rendimiento decreciente claro?

2. **¿Depende de la dificultad?**
   - ¿Instancias fáciles necesitan menos presupuesto?
   - ¿Instancias difíciles se benefician más de presupuestos altos?

3. **¿Cómo se usa el presupuesto?**
   - ¿Cuántas llamadas a CPLEX hace cada algoritmo?
   - ¿Se concentra al inicio, al final, o distribuido?

4. **¿Supera al CPLEX puro?**
   - En Grupo 5 (100% T_base[i]), ¿el híbrido encuentra mejores soluciones?
   - ¿Encuentra el óptimo más rápido?

---

## Notas Importantes

### Compatibilidad con Código Anterior

El sistema mantiene compatibilidad con experimentos anteriores:
- Si `cplex-budget-percentage` NO está definido → Modo Legacy
- Si `cplex-budget` está definido → Usa presupuesto fijo
- Los resultados anteriores siguen siendo válidos

### Verificación de la Implementación

Para verificar que el sistema funciona correctamente:

1. **Revisar logs de inicio**:
```
================================================================
MODO DE PRESUPUESTO DINÁMICO POR INSTANCIA
================================================================
Porcentaje de presupuesto: 50%
Archivo de línea base: out/baseline/instance_baseline.csv

INSTANCE BUDGET MANAGER INICIALIZADO
Instancias cargadas: 46
T_base mínimo: 0.061 segundos
T_base máximo: 2.092 segundos
```

2. **Verificar cálculos por instancia** (descomentar líneas de debug):
```
CplexTerminal: Presupuesto para 3C_20_50-02.txt = 0.130s (T_base=0.260s)
CplexTerminal: Presupuesto para C101_20_08.txt = 0.084s (T_base=0.168s)
```

3. **Revisar estadísticas finales**:
```
CPLEX Stats - Total Budget: 0.130s, Used: 0.125s (96.2%), Calls: 3
```

---

## Conclusiones Esperadas

### Hipótesis Confirmada
Si los algoritmos híbridos con 50% de presupuesto logran ERP ≤ 5%, entonces:
- **Conclusión**: Los algoritmos híbridos son significativamente más eficientes
- **Implicación**: Con la mitad del tiempo, logran resultados comparables

### Hipótesis Rechazada
Si se necesita 100% de presupuesto para alcanzar ERP ≤ 5%, entonces:
- **Conclusión**: Los algoritmos híbridos no son más eficientes que CPLEX puro
- **Implicación**: El beneficio principal es diversidad de soluciones, no eficiencia

### Hallazgos Adicionales Posibles
- Punto óptimo de presupuesto (ej: 60% es suficiente)
- Diferencias por tipo de instancia
- Patrones de uso de CPLEX (inicio vs refinamiento final)

---

## Referencias

- Archivo de propuesta original: Ver email/documento de especificación
- Código fuente: `src/model/InstanceBudgetManager.java`
- Resultados baseline: `out/baseline/`
- Archivos de parámetros: `src/model/params/pdp_group*.params`

---

**Última actualización**: 2025-10-26
**Implementado por**: Claude Code
**Estado**: ✅ Implementación completa - Listo para ejecutar experimentos
