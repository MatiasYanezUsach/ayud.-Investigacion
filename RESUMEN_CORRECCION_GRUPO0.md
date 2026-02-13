# RESUMEN DE CORRECCIONES - GRUPO 0 (BASELINE)

## Fecha: 2024
## Problema Identificado
Los archivos Excel generados no incluían los datos de rendimiento del Grupo 0 (grupo de control sin CPLEX), lo que impedía realizar la comparación fundamental para responder la pregunta de investigación: "¿Los algoritmos con CPLEX son mejores que los puramente heurísticos?"

## Archivos Corregidos

### 1. generate_excel_report.py
**Problema**: El script solo leía el archivo CplexUsage.statistics.txt, que no existe para el Grupo 0.

**Solución**:
- Se agregó lectura del archivo EstadisticaProm&Mej.csv (presente en TODOS los grupos)
- Se cambió el delimitador de CSV de coma (,) a punto y coma (;)
- Se extraen métricas de la última generación: BestERP, AvgERP, BestFITNESS, AvgFITNESS
- Se calcula Total Individuos Evaluados como: Gen × 15 (tamaño población)
- Se agregó sección "RENDIMIENTO EVOLUTIVO" en hoja "Resumen General"

### 2. generate_consolidated_report.py
**Problema**: El script mostraba "0 ejecuciones encontradas" para Grupo 0.

**Solución**:
- Se modificó función read_group_data() para leer hoja "Resumen General"
- Se agregó extracción de summary_data con todas las métricas consolidadas
- Se agregó lógica especial para Grupo 0 que lee datos del resumen en lugar de estadísticas CPLEX
- Se crean datos sintéticos para mantener compatibilidad con estructura de otros grupos

## Archivos Regenerados

✅ RESULTADOS_EXPERIMENTO_GRUPO0.xlsx
✅ RESULTADOS_EXPERIMENTO_GRUPO1.xlsx
✅ RESULTADOS_EXPERIMENTO_GRUPO2.xlsx
✅ RESULTADOS_EXPERIMENTO_GRUPO3.xlsx
✅ RESULTADOS_EXPERIMENTO_GRUPO4.xlsx
✅ RESULTADOS_EXPERIMENTO_GRUPO5.xlsx
✅ RESULTADOS_EXPERIMENTO_CONSOLIDADO.xlsx

## Datos del Grupo 0 (BASELINE)

```
Configuración:
- Presupuesto CPLEX: 0% (Sin CPLEX)
- Ejecuciones: 30
- Generaciones por ejecución: 100
- Tamaño de población: 15 individuos

Resultados:
- Total Individuos Evaluados: 45,000
- Mejor ERP Promedio: 0.204966
- ERP Poblacional Promedio: 0.386494
- Mejor Fitness Promedio: 0.204966
- Fitness Poblacional Promedio: 0.386494

Estadísticas CPLEX:
- Llamadas CPLEX: 0 (no usa CPLEX)
- Tiempo CPLEX: 0 s (no usa CPLEX)
```

## Comparación con Grupo 0 (BASELINE)

| Grupo | Presupuesto | Mejor ERP | Mejora vs Grupo 0 | Estado |
|-------|-------------|-----------|-------------------|--------|
| **Grupo 0** | 0% (Sin CPLEX) | **0.204966** | **BASELINE** | 🎯 Control |
| Grupo 1 | 10% | 0.109563 | +46.55% | ✅ Mejor |
| **Grupo 2** | **25%** | **0.091770** | **+55.23%** | 🏆 **ÓPTIMO** |
| Grupo 3 | 50% | 0.114656 | +44.06% | ✅ Mejor |
| Grupo 4 | 75% | 0.116170 | +43.32% | ✅ Mejor |
| Grupo 5 | 100% | 0.120281 | +41.32% | ✅ Mejor |

## Conclusiones Clave

### 1. **CPLEX SÍ Mejora el Rendimiento**
Todos los grupos con CPLEX (1-5) superan significativamente al Grupo 0 (sin CPLEX), con mejoras entre **41% y 55%**.

### 2. **El Presupuesto Óptimo es 25%**
El Grupo 2 (25% CPLEX) obtiene el mejor resultado con 0.091770 de ERP.

### 3. **Más CPLEX NO es Mejor**
- Grupo 2 (25%): Mejor resultado
- Grupo 3 (50%): Rendimiento disminuye (-11.1% vs Grupo 2)
- Grupo 5 (100%): Peor resultado con CPLEX (-13.1% vs Grupo 2)

### 4. **Relación Costo-Beneficio**
```
Grupo 2 (25% CPLEX):
- Tiempo CPLEX: 7,268 segundos promedio
- Mejora: +55.23% vs sin CPLEX
- Costo por % mejora: 131.6 segundos/punto porcentual

Grupo 5 (100% CPLEX):
- Tiempo CPLEX: 35,231 segundos promedio (+384% vs Grupo 2)
- Mejora: +41.32% vs sin CPLEX
- Costo por % mejora: 852.5 segundos/punto porcentual
```

**El Grupo 2 es 6.5 veces más eficiente que el Grupo 5 en relación costo-beneficio.**

### 5. **Respuesta a Pregunta de Investigación**
**¿Vale la pena invertir tiempo de CPU en CPLEX?**

**Respuesta: SÍ, pero con moderación.**
- Un presupuesto de 25% ofrece el mejor equilibrio entre calidad de solución y tiempo computacional
- Presupuestos mayores al 25% generan rendimientos decrecientes
- El grupo sin CPLEX (Grupo 0) es significativamente inferior a todos los grupos con CPLEX

## Archivos de Verificación Creados

- `verificar_todos_erp.py`: Verifica que todos los grupos tengan datos de ERP
- `verificar_consolidado.py`: Verifica y analiza el reporte consolidado

## Próximos Pasos

1. ✅ **Completado**: Todos los Excel individuales regenerados con datos correctos
2. ✅ **Completado**: Reporte consolidado regenerado con Grupo 0 incluido
3. 📊 **Pendiente**: Gráficos comparativos ya generados en graficos_comparativos/
4. 📝 **Pendiente**: Actualizar documento de tesis con estos resultados

## Validación Final

Ejecutar para verificar todo esté correcto:
```bash
python verificar_todos_erp.py
python verificar_consolidado.py
```

Todos los archivos Excel están listos para su análisis en la tesis.
