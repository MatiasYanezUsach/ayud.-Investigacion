# Guía de Ejecución en Windows

Esta guía explica cómo ejecutar el diseño experimental en Windows.

---

## 📋 Requisitos Previos

### 1. Software Necesario
- ✅ **Java JDK** (versión 8 o superior)
- ✅ **CPLEX** instalado
- ✅ **Git** (para clonar el repositorio)

### 2. Verificar Instalación de Java

Abre **Command Prompt** (cmd) y ejecuta:

```cmd
java -version
javac -version
```

Deberías ver algo como:
```
java version "1.8.0_XXX"
javac 1.8.0_XXX
```

---

## 🔧 Configuración Inicial

### Paso 1: Ubicar los JARs Necesarios

Necesitas saber dónde están estos archivos:

1. **CPLEX JAR**: Busca `cplex.jar` en tu instalación de CPLEX
   - Ruta típica: `C:\Program Files\IBM\ILOG\CPLEX_StudioXXX\cplex\lib\cplex.jar`

2. **ECJ JAR**: Ya está en el proyecto
   - Ubicación: `ecj\jar\ecj.28.jar`

3. **Commons Math JAR**: Ya está en el proyecto
   - Ubicación: `commons-math3-3.6.1.jar`

### Paso 2: Configurar las Rutas en los Scripts

Edita los archivos `.bat` y ajusta estas líneas según tu instalación:

```batch
set CPLEX_JAR=C:\ruta\a\tu\cplex.jar
set ECJ_JAR=ecj\jar\ecj.28.jar
set COMMONS_JAR=commons-math3-3.6.1.jar
```

**Opción alternativa**: Copia `cplex.jar` al directorio raíz del proyecto y usa:
```batch
set CPLEX_JAR=cplex.jar
```

---

## 🚀 Ejecutar el Experimento

### FASE 1: Generar Línea Base

**IMPORTANTE**: Ejecutar primero antes que cualquier otra cosa.

#### Opción A: Usando el script BAT

1. Abre **Command Prompt**
2. Navega al directorio del proyecto:
   ```cmd
   cd C:\ruta\a\tu\proyecto\ayud.-Investigacion
   ```

3. Ejecuta el script:
   ```cmd
   run_baseline.bat
   ```

#### Opción B: Ejecución Manual (paso a paso)

Si el script no funciona, ejecuta manualmente:

```cmd
REM 1. Crear directorio de salida
mkdir out\baseline

REM 2. Compilar
javac -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" src\model\CplexBaselineRunner.java src\model\FileIO.java src\model\PDPData.java src\model\Instance.java src\model\PDPInstance.java

REM 3. Ejecutar
java -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" model.CplexBaselineRunner
```

**⚠️ NOTA IMPORTANTE**: En Windows se usa `;` (punto y coma) para separar rutas en el classpath, NO `:` (dos puntos) como en Linux.

#### Verificar Resultados

Debe generar estos archivos en `out\baseline\`:
- ✅ `instance_baseline.csv` - **ARCHIVO CLAVE**
- ✅ `cplex_baseline_results.csv`
- ✅ `cplex_baseline_summary.txt`

Verifica el contenido:
```cmd
type out\baseline\instance_baseline.csv
```

Deberías ver algo como:
```
InstanceName,T_base_seconds,Optimal_value,Status,Difficulty
3C_20_50-02.txt,0.260,10054.00,Feasible,Medium
3C_20_50-03.txt,0.440,8387.00,Feasible,Hard
...
```

---

### FASE 2: Ejecutar Grupos Experimentales

Una vez que tengas `instance_baseline.csv`, puedes ejecutar los experimentos.

#### Opción A: Ejecutar TODOS los Grupos (180 ejecuciones)

**ADVERTENCIA**: Esto puede tomar MUCHAS HORAS (posiblemente días).

```cmd
run_experiment.bat
```

#### Opción B: Ejecutar UN Grupo Individual (RECOMENDADO para pruebas)

**Para PROBAR el sistema** (1 ejecución rápida):
```cmd
run_single_group.bat 3 1
```
Esto ejecuta el Grupo 3 (50%) con solo 1 repetición.

**Para ejecutar un grupo completo** (30 repeticiones):
```cmd
run_single_group.bat 0       REM Grupo 0: Sin CPLEX
run_single_group.bat 1       REM Grupo 1: 10%
run_single_group.bat 2       REM Grupo 2: 25%
run_single_group.bat 3       REM Grupo 3: 50%
run_single_group.bat 4       REM Grupo 4: 75%
run_single_group.bat 5       REM Grupo 5: 100%
```

#### Opción C: Ejecución Manual (para un grupo específico)

Ejemplo para Grupo 3 (50%) con 5 repeticiones:

```cmd
REM 1. Compilar todo
javac -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" src\model\*.java src\terminals\*.java src\functions\*.java

REM 2. Ejecutar
java -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" ec.Evolve -file src\model\params\pdp_group3_50pct.params -p jobs=5
```

---

## 📊 Verificar que el Sistema Funciona

### Prueba Rápida (5 minutos)

Ejecuta una prueba corta para verificar que todo funciona:

```cmd
REM Compilar
javac -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" src\model\*.java src\terminals\*.java src\functions\*.java

REM Ejecutar 1 repetición del Grupo 3
java -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" ec.Evolve -file src\model\params\pdp_group3_50pct.params -p jobs=1
```

**Busca en la salida**:
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

Si ves esto, **¡el sistema está funcionando correctamente!** ✅

---

## 🔍 Solución de Problemas

### Error: "No se encuentra cplex.jar"

**Solución**:
1. Localiza tu instalación de CPLEX
2. Busca el archivo `cplex.jar`
3. Actualiza la ruta en los scripts `.bat`:
   ```batch
   set CPLEX_JAR=C:\ruta\completa\a\cplex.jar
   ```

### Error: "package ec does not exist"

**Causa**: Classpath incorrecto

**Solución**: Verifica que estás usando `;` (punto y coma) en Windows:
```cmd
javac -cp ".;ecj\jar\ecj.28.jar;cplex.jar" ...
```

NO uses `:` (dos puntos) - eso es para Linux/Mac.

### Error: "instance_baseline.csv not found"

**Causa**: No ejecutaste la Fase 1 primero

**Solución**: Ejecuta `run_baseline.bat` antes de ejecutar los experimentos.

### Error: Compilación falla con "cannot find symbol"

**Causa**: Faltan dependencias en el classpath

**Solución**: Asegúrate de incluir TODOS los JARs:
```cmd
javac -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" src\model\*.java src\terminals\*.java src\functions\*.java
```

### Ejecución muy lenta

**Normal**: Cada grupo con 30 repeticiones puede tomar HORAS.

**Solución para pruebas**: Reduce el número de repeticiones:
```cmd
run_single_group.bat 3 1    REM Solo 1 repetición
run_single_group.bat 3 5    REM 5 repeticiones
```

---

## 📁 Estructura de Resultados

Después de ejecutar, encontrarás:

```
out/
├── baseline/
│   ├── instance_baseline.csv         ← T_base[i] por instancia
│   ├── cplex_baseline_results.csv
│   └── cplex_baseline_summary.txt
│
└── results/
    ├── evolution0/                    ← Job 0
    │   ├── job.0.CplexUsage.detailed.csv
    │   ├── job.0.CplexUsage.summary.csv
    │   ├── job.0.CplexUsage.statistics.txt
    │   └── ...
    ├── evolution1/                    ← Job 1
    └── ...
```

---

## 📝 Comandos de Referencia Rápida

### Compilar Todo
```cmd
javac -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" src\model\*.java src\terminals\*.java src\functions\*.java
```

### Ejecutar Baseline
```cmd
java -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" model.CplexBaselineRunner
```

### Ejecutar Grupo Específico (manual)
```cmd
java -cp ".;ecj\jar\ecj.28.jar;cplex.jar;commons-math3-3.6.1.jar" ec.Evolve -file src\model\params\pdp_group3_50pct.params -p jobs=1
```

### Ver Resultados de Baseline
```cmd
type out\baseline\instance_baseline.csv
type out\baseline\cplex_baseline_summary.txt
```

### Listar Archivos de Resultados
```cmd
dir /s /b out\results\*.csv
```

---

## ⏱️ Estimación de Tiempos

Basado en los datos de baseline (T_base promedio = 0.485s por instancia):

| Tarea | Tiempo Estimado |
|-------|-----------------|
| **Baseline** (46 instancias) | ~30 segundos |
| **1 job** (100 generaciones × 15 ind × 46 inst) | ~1-2 horas |
| **Grupo completo** (30 jobs) | ~30-60 horas |
| **Experimento completo** (6 grupos × 30 jobs) | ~180-360 horas (7-15 días) |

**RECOMENDACIÓN**:
1. Empezar con pruebas de 1 job
2. Luego ejecutar 5-10 jobs por grupo
3. Finalmente ejecutar los 30 jobs completos

---

## 🎯 Checklist de Ejecución

### Antes de Empezar:
- [ ] Java instalado y funcionando
- [ ] CPLEX instalado
- [ ] Rutas de JARs configuradas en scripts .bat
- [ ] Directorio `data/evolution` con instancias
- [ ] Directorio `data/evaluation` con instancias

### Fase 1 - Baseline:
- [ ] Ejecutar `run_baseline.bat`
- [ ] Verificar que se creó `out\baseline\instance_baseline.csv`
- [ ] Verificar que tiene 46 instancias (47 líneas con header)

### Fase 2 - Experimentos:
- [ ] Ejecutar prueba con `run_single_group.bat 3 1`
- [ ] Verificar salida: "MODO DE PRESUPUESTO DINÁMICO POR INSTANCIA"
- [ ] Verificar que se crean archivos en `out\results\`
- [ ] Ejecutar grupos completos con 30 repeticiones

---

## 💡 Consejos

1. **Empezar Pequeño**: Usa `jobs=1` para probar
2. **Monitorear**: Revisa los logs en tiempo real
3. **Backup**: Guarda `instance_baseline.csv` - es crucial
4. **Paciencia**: Los experimentos completos toman días
5. **Paralelo**: Si tienes varias máquinas, ejecuta diferentes grupos en paralelo

---

## 📞 ¿Necesitas Ayuda?

Si algo no funciona:
1. Revisa la sección "Solución de Problemas"
2. Verifica que los JARs están en las rutas correctas
3. Ejecuta los comandos manualmente paso a paso
4. Revisa los mensajes de error completos

---

**Última actualización**: 2025-10-26
**Estado**: ✅ Scripts de Windows creados y listos para usar
