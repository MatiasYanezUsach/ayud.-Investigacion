# Guía Completa del Experimento: Presupuesto Óptimo de CPLEX

## 🎯 Objetivo del Estudio

Determinar **cuánto presupuesto de CPLEX** necesita un algoritmo híbrido (generado automáticamente) para alcanzar soluciones de calidad comparable a CPLEX puro.

**Hipótesis**: Un algoritmo híbrido necesita **menos del 50% del tiempo de CPLEX puro** para obtener soluciones de calidad similar.

---

## ⚡ Inicio Rápido (3 Pasos)

### **1️⃣ Calcular Línea Base (T_base)**
```bash
cl# Windows
run_baseline.bat

# Linux
./run_baseline.sh
```
**Salida**: `out/baseline/cplex_baseline_summary.txt` con el valor de **T_base**

### **2️⃣ Actualizar Parámetros**
1. Abrir `out/baseline/cplex_baseline_summary.txt`
2. Anotar T_base (ejemplo: 45.250 segundos)
3. Calcular cada presupuesto:
   - Grupo 1 (10%): 45.250 × 0.10 = **4.525**
   - Grupo 2 (25%): 45.250 × 0.25 = **11.313**
   - Grupo 3 (50%): 45.250 × 0.50 = **22.625**
   - Grupo 4 (75%): 45.250 × 0.75 = **33.938**
   - Grupo 5 (100%): 45.250 × 1.00 = **45.250**
4. Editar archivos `src/model/params/pdp_groupX_*.params`
5. Buscar línea: `gp.fs.0.func.6.cplex-budget = 0.0`
6. Reemplazar `0.0` con el valor calculado

### **3️⃣ Ejecutar Experimentos**
```bash
# Todos los grupos (180 ejecuciones, ~3-7 días)
./run_experiment.sh all 30

# O grupos individuales
./run_experiment.sh 0 30   # Grupo 0: sin CPLEX
./run_experiment.sh 1 30   # Grupo 1: 10%
# ... etc
```

---

## 📋 Grupos Experimentales

| Grupo | Presupuesto | Descripción | Archivo |
|-------|-------------|-------------|---------|
| 0 | 0% | Sin CPLEX (control) | `pdp_group0_nocplex.params` |
| 1 | 10% | Presupuesto mínimo | `pdp_group1_10pct.params` |
| 2 | 25% | Presupuesto bajo | `pdp_group2_25pct.params` |
| 3 | 50% | Presupuesto moderado | `pdp_group3_50pct.params` |
| 4 | 75% | Presupuesto alto | `pdp_group4_75pct.params` |
| 5 | 100% | Presupuesto completo | `pdp_group5_100pct.params` |

**Total**: 6 grupos × 30 repeticiones = **180 ejecuciones**

---

## ⚙️ Configuración Inicial

### **IMPORTANTE: Ajustar Rutas de CPLEX**

Editar en TODOS los scripts (`run_baseline.*` y `run_experiment.*`):

**Windows** (`.bat`):
```batch
set CPLEX_LIB_PATH=C:\Program Files\IBM\ILOG\CPLEX_Studio201\cplex\bin\x64_win64
```

**Linux** (`.sh`):
```bash
CPLEX_LIB_PATH="/opt/ibm/ILOG/CPLEX_Studio201/cplex/bin/x86-64_linux"
```

### **Prerrequisitos**
- ✅ Java 11+
- ✅ IBM ILOG CPLEX Studio
- ✅ Máquina dedicada (no compartida)

---

## 📊 Resultados Generados

### **FASE 1 (Línea Base)**
```
out/baseline/
├── cplex_baseline_results.csv      # Detalles por instancia
└── cplex_baseline_summary.txt      # T_base calculado
```

### **FASE 2 (Experimentos)**
Por cada ejecución (job):
```
out/results/evolutionX/
├── job.X.CplexUsage.detailed.csv       # ← NUEVO: Log detallado de CPLEX
├── job.X.CplexUsage.summary.csv        # ← NUEVO: Resumen por instancia
├── job.X.CplexUsage.statistics.txt     # ← NUEVO: Estadísticas globales
├── job.X.BestFitness.csv
├── job.X.Statistics.out
└── ... (archivos ECJ estándar)
```

---

## 🔧 Detalles Técnicos

### **Sistema de Control de Presupuesto**

**Cómo funciona:**
1. Cada instancia tiene presupuesto total (ej: 10 segundos)
2. Cada llamada usa máximo 40% del presupuesto restante
3. Se resetea entre instancias
4. Todo se registra en logs detallados

**Ejemplo** (Presupuesto = 10s):
- Llamada 1: Usa máx 40% de 10s = 4s (quedan 6s)
- Llamada 2: Usa máx 40% de 6s = 2.4s (quedan 3.6s)
- Llamada 3: Usa todo el resto = 3.6s

### **Variables Medidas**

**Dependientes** (resultados):
- Error Relativo Promedio (ERP)
- Hits (óptimos encontrados)
- Tiempo de ejecución
- Número de llamadas a CPLEX
- Uso de presupuesto

**Independiente** (controlada):
- Presupuesto total de CPLEX (0%, 10%, 25%, 50%, 75%, 100%)

**Fijas** (constantes):
- Población: 15 individuos
- Generaciones: 100
- Instancias: 36 evolución + 10 evaluación

---

## ⏱️ Tiempo Estimado

- **FASE 1**: 1-3 horas (46 instancias con CPLEX puro)
- **FASE 2**: 3-7 días (180 ejecuciones completas)

---

## 🛠️ Implementación Técnica

### **Archivos Modificados**
1. `src/terminals/CplexTerminal.java` - Control de presupuesto
2. `src/model/Terminal.java` - Interface con límite
3. `src/model/PDPInstance.java` - CPLEX con límite configurable
4. `src/model/PDPProblemEvo.java` - Integración y logging
5. `src/model/PDPProblemEva.java` - Reset de presupuesto

### **Archivos Nuevos**
1. `src/model/CplexBaselineRunner.java` - Calcular T_base
2. `src/model/CplexUsageLogger.java` - Sistema de logging
3. `src/model/params/pdp_group0_nocplex.params` - Grupo 0
4. `src/model/params/pdp_group1_10pct.params` - Grupo 1
5. `src/model/params/pdp_group2_25pct.params` - Grupo 2
6. `src/model/params/pdp_group3_50pct.params` - Grupo 3
7. `src/model/params/pdp_group4_75pct.params` - Grupo 4
8. `src/model/params/pdp_group5_100pct.params` - Grupo 5
9. `run_baseline.bat` / `run_baseline.sh` - Scripts FASE 1
10. `run_experiment.bat` / `run_experiment.sh` - Scripts FASE 2

---

## 🚨 Problemas Corregidos Durante Implementación

### **1. Archivos de Parámetros**
- ❌ Antes: `gp.fs.0.func.6.cplex-budget = TBASE_VALUE * 0.10`
- ✅ Ahora: `gp.fs.0.func.6.cplex-budget = 0.0` (reemplazar con valor numérico)
- **Razón**: ECJ no evalúa expresiones matemáticas

### **2. Inicialización del Presupuesto**
- ❌ Antes: Método `setup()` que nunca se llamaba
- ✅ Ahora: Método `configureBudget()` llamado desde `PDPProblemEvo.setup()`
- **Razón**: Los terminales no tienen setup automático en ECJ

### **3. Compatibilidad de Scripts**
- ❌ Antes: `if [ "$x" == "y" ]` (no POSIX)
- ✅ Ahora: `if [ "$x" = "y" ]` (estándar)
- **Razón**: Mayor portabilidad

---

## ⚠️ Notas Importantes

### **Al Actualizar Parámetros (PASO 2)**
- ⚠️ **CALCULAR MANUALMENTE** cada valor
- ⚠️ **NO** poner expresiones como `45.250 * 0.10`
- ⚠️ **SÍ** poner valores numéricos directos: `4.525`
- ⚠️ Editar los **5 archivos** (grupos 1-5)
- ⚠️ El Grupo 0 **NO** requiere edición (no tiene CPLEX)

### **Durante la Ejecución**
- ✅ Ejecutar en máquina **dedicada**
- ✅ No interrumpir procesos
- ✅ Documentar especificaciones de hardware
- ✅ Respaldar resultados regularmente

---

## 📈 Análisis de Resultados

### **Preguntas que Responde el Experimento**

1. **¿Cuánto presupuesto es necesario?**
   - Comparar ERP de cada grupo
   - Identificar punto de rendimientos decrecientes

2. **¿Existe un punto óptimo?**
   - Analizar curva presupuesto vs calidad
   - Buscar donde más CPLEX no mejora significativamente

3. **¿Cómo usan CPLEX los algoritmos exitosos?**
   - Analizar logs detallados
   - Frecuencia y timing de llamadas

4. **¿Cuánto más eficiente es el híbrido?**
   - Comparar Grupo 5 (100%) vs CPLEX puro
   - Calcular ratio de eficiencia

---

## ✅ Checklist de Ejecución

### **Preparación**
- [ ] CPLEX instalado y configurado
- [ ] Rutas ajustadas en scripts
- [ ] Máquina dedicada disponible
- [ ] Instancias en `data/evolution/` y `data/evaluation/`

### **FASE 1**
- [ ] Ejecutar `run_baseline`
- [ ] Verificar `out/baseline/cplex_baseline_summary.txt`
- [ ] Anotar valor de T_base: _____ segundos

### **FASE 2 - Preparación**
- [ ] Calcular presupuestos manualmente
  - [ ] Grupo 1 (10%): _____
  - [ ] Grupo 2 (25%): _____
  - [ ] Grupo 3 (50%): _____
  - [ ] Grupo 4 (75%): _____
  - [ ] Grupo 5 (100%): _____
- [ ] Actualizar 5 archivos de parámetros
- [ ] Verificar valores guardados correctamente

### **FASE 2 - Ejecución**
- [ ] Grupo 0 (30 repeticiones)
- [ ] Grupo 1 (30 repeticiones)
- [ ] Grupo 2 (30 repeticiones)
- [ ] Grupo 3 (30 repeticiones)
- [ ] Grupo 4 (30 repeticiones)
- [ ] Grupo 5 (30 repeticiones)

### **Post-Experimento**
- [ ] Verificar 180 ejecuciones completadas
- [ ] Recolectar archivos de resultados
- [ ] Respaldar datos
- [ ] Iniciar análisis

---

## 💡 Tips y Recomendaciones

### **Para Depuración**
- Revisar logs en: `out/results/evolutionX/job.X.Statistics.out`
- Logs de CPLEX en: `out/results/evolutionX/job.X.CplexUsage.*`
- Verificar presupuesto configurado: mensaje al inicio de ejecución

### **Para Ejecución Eficiente**
- Ejecutar grupos en paralelo en diferentes máquinas (si disponible)
- Empezar con Grupo 0 (más rápido, sin CPLEX)
- Monitorear primera ejecución de cada grupo para verificar

### **Si Algo Falla**
1. Verificar rutas de CPLEX en scripts
2. Verificar parámetros actualizados correctamente
3. Revisar que archivos .params tengan valores numéricos (no expresiones)
4. Verificar que `out/` tenga permisos de escritura

---

## 📞 Estructura del Proyecto

```
ayud.-Investigacion/
├── src/
│   ├── model/
│   │   ├── CplexBaselineRunner.java      ← NUEVO
│   │   ├── CplexUsageLogger.java         ← NUEVO
│   │   ├── PDPProblemEvo.java            ← MODIFICADO
│   │   ├── PDPProblemEva.java            ← MODIFICADO
│   │   ├── PDPInstance.java              ← MODIFICADO
│   │   ├── Terminal.java                 ← MODIFICADO
│   │   └── params/
│   │       ├── pdp_group0_nocplex.params ← NUEVO
│   │       ├── pdp_group1_10pct.params   ← NUEVO
│   │       ├── pdp_group2_25pct.params   ← NUEVO
│   │       ├── pdp_group3_50pct.params   ← NUEVO
│   │       ├── pdp_group4_75pct.params   ← NUEVO
│   │       └── pdp_group5_100pct.params  ← NUEVO
│   └── terminals/
│       └── CplexTerminal.java            ← MODIFICADO
├── data/
│   ├── evolution/        (36 archivos; el experimento usa las primeras 8: experiment.max.instances=8)
│   └── evaluation/       (10 instancias)
├── out/
│   ├── baseline/         ← FASE 1 resultados
│   └── results/          ← FASE 2 resultados
├── run_baseline.bat      ← NUEVO
├── run_baseline.sh       ← NUEVO
├── run_experiment.bat    ← NUEVO
├── run_experiment.sh     ← NUEVO
└── GUIA_EXPERIMENTO_COMPLETA.md  ← Este archivo
```

---

## 🎯 Resumen Ejecutivo

**Estado**: ✅ **Sistema completo, probado y listo para ejecutar**

**Implementado**:
- ✅ Sistema de control de presupuesto CPLEX
- ✅ Logging detallado de uso
- ✅ Cálculo de línea base
- ✅ 6 grupos experimentales configurados
- ✅ Scripts de ejecución automatizados
- ✅ Todos los problemas corregidos

**Para ejecutar**:
1. Ajustar rutas de CPLEX
2. Ejecutar FASE 1 (calcular T_base)
3. Actualizar parámetros con valores numéricos
4. Ejecutar FASE 2 (6 grupos × 30 repeticiones)

**Tiempo total estimado**: ~1 semana

---

**Fecha de implementación**: Octubre 20, 2025  
**Versión**: 1.0 - Revisado y Corregido  
**Estado**: ✅ Producción

---

## 🚀 Comando Rápido de Ejecución Completa

```bash
# 1. Calcular línea base
./run_baseline.sh

# 2. [MANUAL] Abrir out/baseline/cplex_baseline_summary.txt
#            Calcular presupuestos
#            Actualizar archivos pdp_groupX_*.params

# 3. Ejecutar todos los experimentos
./run_experiment.sh all 30

# ¡Listo! Esperar ~3-7 días
```

**¡El sistema está listo para comenzar! 🎉**

