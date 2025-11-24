# Guía para Ejecutar Más Experimentos

## Estado Actual
✅ **Grupo 1 (10%)**: COMPLETADO - 30 ejecuciones (evolution0 a evolution29)

## Grupos Pendientes
- Grupo 0: 0% (sin CPLEX) - Control
- Grupo 2: 25% de presupuesto
- Grupo 3: 50% de presupuesto
- Grupo 4: 75% de presupuesto
- Grupo 5: 100% de presupuesto

## Cómo Ejecutar

### Opción 1: Ejecutar un grupo específico
```batch
run_experiment.bat 2 30
```
Esto ejecutará el Grupo 2 (25%) con 30 repeticiones.

### Opción 2: Ejecutar todos los grupos restantes
```batch
run_experiment.bat all 30
```
⚠️ **ADVERTENCIA**: Esto ejecutará TODOS los grupos (0-5), incluyendo el Grupo 1 que ya está completo.
Esto puede tardar varios días.

### Opción 3: Ejecutar grupos específicos uno por uno
```batch
run_experiment.bat 0 30   # Grupo 0: Sin CPLEX
run_experiment.bat 2 30   # Grupo 2: 25%
run_experiment.bat 3 30   # Grupo 3: 50%
run_experiment.bat 4 30   # Grupo 4: 75%
run_experiment.bat 5 30   # Grupo 5: 100%
```

## Notas Importantes

1. **Tiempo estimado**: Cada grupo con 30 repeticiones puede tardar varias horas o días dependiendo de la máquina.

2. **Reportes automáticos**: Después de cada grupo, se generará automáticamente:
   - `RESULTADOS_EXPERIMENTO_GRUPOX.xlsx`
   - `graficos_grupoX/` (8 gráficos PNG)

3. **Archivos de parámetros**: Ya están configurados correctamente:
   - `pdp_group0_nocplex.params` → 0% (sin CPLEX)
   - `pdp_group1_10pct.params` → 10% ✅ (ya ejecutado)
   - `pdp_group2_25pct.params` → 25%
   - `pdp_group3_50pct.params` → 50%
   - `pdp_group4_75pct.params` → 75%
   - `pdp_group5_100pct.params` → 100%

4. **Baseline**: Ya está calculado y se usa automáticamente por instancia.

## Recomendación

Ejecutar los grupos uno por uno para poder monitorear el progreso:
```batch
run_experiment.bat 2 30
```
Esperar a que termine, revisar resultados, y luego continuar con el siguiente.

