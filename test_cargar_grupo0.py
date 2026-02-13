import sys
sys.path.insert(0, '.')
from generate_comparative_charts import cargar_datos_grupo

# Cargar solo Grupo 0
datos_g0 = cargar_datos_grupo(0)

print("\n=== VERIFICACIÓN GRUPO 0 ===")
print(f"Grupo: {datos_g0['grupo']}")
print(f"Presupuesto: {datos_g0['presupuesto']}")
print(f"\nBaseline instancias: {len(datos_g0['baseline'])}")
print(f"Estadísticas generaciones: {len(datos_g0['estadisticas_prom_mej'])}")
print(f"Best fitness registros: {len(datos_g0['best_fitness'])}")
print(f"Estadísticas CPLEX: {len(datos_g0['estadisticas_cplex'])}")

if datos_g0['estadisticas_prom_mej']:
    print(f"\n=== PRIMERAS 3 GENERACIONES ===")
    for i in range(min(3, len(datos_g0['estadisticas_prom_mej']))):
        gen = datos_g0['estadisticas_prom_mej'][i]
        print(f"Gen {gen['generacion']}: AvgERP={gen['avg_erp']:.4f}, BestERP={gen['best_erp']:.4f}, AvgFitness={gen['avg_fitness']:.4f}")
    
    print(f"\n=== ÚLTIMAS 3 GENERACIONES ===")
    for i in range(max(0, len(datos_g0['estadisticas_prom_mej']) - 3), len(datos_g0['estadisticas_prom_mej'])):
        gen = datos_g0['estadisticas_prom_mej'][i]
        print(f"Gen {gen['generacion']}: AvgERP={gen['avg_erp']:.4f}, BestERP={gen['best_erp']:.4f}, AvgFitness={gen['avg_fitness']:.4f}")
else:
    print("\n❌ ERROR: No se cargaron estadísticas para Grupo 0")

print(f"\n=== RESUMEN ===")
print(f"¿Tiene datos de rendimiento? {'✅ SÍ' if datos_g0['estadisticas_prom_mej'] else '❌ NO'}")
print(f"¿Tiene datos de CPLEX? {'✅ SÍ' if datos_g0['estadisticas_cplex'] else '❌ NO (esperado para Grupo 0)'}")
