# Gráficos Comparativos - Análisis Experimental

Este directorio contiene **8 gráficos comparativos superpuestos** que muestran la evolución de diferentes métricas para los **6 grupos experimentales** (presupuestos CPLEX: 0%, 10%, 25%, 50%, 75% y 100%).

## 📊 **Descripción de los Gráficos**

### **1. Baseline Tiempos Comparativo** (`01_baseline_tiempos_comparativo.png`)
- **Eje X:** Instancia de prueba
- **Eje Y:** Tiempo (segundos)
- **Descripción:** Muestra los tiempos de resolución de CPLEX puro (sin programación genética) para cada instancia. Este es el **baseline** contra el cual se compara el rendimiento del algoritmo evolutivo.
- **Colores:** Cada grupo tiene su propia barra (aunque los valores baseline son idénticos para todos).

---

### **2. Llamadas CPLEX Comparativo** (`02_llamadas_cplex_comparativo.png`)
- **Eje X:** Número de Ejecución (0-29)
- **Eje Y:** Total de Llamadas a CPLEX
- **Descripción:** Muestra el **total acumulado de llamadas** al terminal CPLEX a lo largo de cada ejecución completa (100 generaciones). Los grupos con mayor presupuesto hacen más llamadas.
- **Nota:** El Grupo 0 (0% presupuesto) no aparece porque no usa CPLEX.

---

### **3. Tiempo CPLEX Comparativo** (`03_tiempo_cplex_comparativo.png`)
- **Eje X:** Número de Ejecución (0-29)
- **Eje Y:** Tiempo Total CPLEX (segundos)
- **Descripción:** Muestra el **tiempo total** que CPLEX estuvo activo durante cada ejecución. A mayor presupuesto, mayor tiempo invertido en CPLEX.
- **Nota:** El Grupo 0 no aparece.

---

### **4. Evolución Fitness Comparativo** (`04_evolucion_fitness_comparativo.png`)
- **Gráfico Superior:**
  - **Eje X:** Generación (0-100)
  - **Eje Y:** Fitness Promedio
  - **Descripción:** Muestra cómo evoluciona el **fitness promedio** de toda la población a lo largo de las generaciones. Valores más altos = mejor rendimiento.

- **Gráfico Inferior:**
  - **Eje X:** Generación (0-100)
  - **Eje Y:** Mejor Fitness
  - **Descripción:** Muestra el **mejor fitness** encontrado en cada generación.

---

### **5. Evolución ERP Comparativo** (`05_evolucion_erp_comparativo.png`)
- **Gráfico Superior:**
  - **Eje X:** Generación (0-100)
  - **Eje Y:** ERP Promedio (Error Relativo Promedio)
  - **Descripción:** Muestra el **error promedio** de toda la población respecto al óptimo conocido. **Valores más bajos = mejores soluciones**.

- **Gráfico Inferior:**
  - **Eje X:** Generación (0-100)
  - **Eje Y:** Mejor ERP
  - **Descripción:** Muestra el **error del mejor individuo** en cada generación. **Valores más bajos = mejores soluciones**.

---

### **6. Individuos Evaluados Comparativo** (`06_individuos_evaluados_comparativo.png`)
- **Eje X:** Generación (0-100)
- **Eje Y:** Total de Individuos Evaluados Acumulados
- **Descripción:** Muestra el **número acumulado** de individuos evaluados a lo largo de las generaciones. Con 15 individuos por generación, este gráfico es lineal (Gen × 15).

---

### **7. Promedio Llamadas por Individuo Comparativo** (`07_promedio_llamadas_indiv_comparativo.png`)
- **Eje X:** Número de Ejecución (0-29)
- **Eje Y:** Promedio de Llamadas CPLEX por Individuo
- **Descripción:** Muestra cuántas veces, **en promedio**, cada individuo llamó al terminal CPLEX durante su evaluación. Permite comparar la **intensidad de uso de CPLEX** entre grupos.
- **Nota:** El Grupo 0 no aparece.

---

### **8. Evolución Hits Comparativo** (`08_evolucion_hits_comparativo.png`)
- **Eje X:** Generación (0-100)
- **Eje Y:** Hits Promedio
- **Descripción:** Muestra el **número promedio de hits** (soluciones factibles encontradas) por generación. **Valores más altos = más soluciones válidas**.

---

## 🎨 **Leyenda de Colores**

Cada grupo experimental tiene un **color distintivo** que se mantiene consistente en todos los gráficos:

| Grupo | Presupuesto CPLEX | Color |
|-------|-------------------|-------|
| **0** | 0% (Sin CPLEX) | 🔴 Rojo |
| **1** | 10% | 🔵 Azul |
| **2** | 25% | 🟢 Verde |
| **3** | 50% | 🟣 Púrpura |
| **4** | 75% | 🟠 Naranja |
| **5** | 100% | 🩷 Rosa |

---

## 📈 **Interpretación General**

1. **Mayor presupuesto CPLEX** → Más llamadas y tiempo en CPLEX → Potencialmente mejor fitness y menor ERP.
2. **Grupo 0 (Sin CPLEX)** → Representa la **línea base del GP puro** sin ayuda de CPLEX.
3. **Convergencia:** Los gráficos de fitness y ERP muestran cómo cada grupo converge a lo largo de las 100 generaciones.
4. **Trade-off:** Grupos con mayor presupuesto tienen mejor calidad de soluciones pero mayor costo computacional.

---

## 🔧 **Generación de Gráficos**

Estos gráficos fueron generados automáticamente con el script:
```bash
python generate_comparative_charts.py
```

**Datos fuente:** Archivos `RESULTADOS_EXPERIMENTO_GRUPO{0-5}.xlsx`

---

## 📅 **Fecha de Generación**
13 de febrero de 2026
