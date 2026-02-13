"""
Script para renombrar todos los gráficos agregando el número de grupo al nombre
"""
import os
import shutil

grupos = [0, 1, 2, 3, 4, 5]
archivos_base = [
    '01_baseline_tiempos.png',
    '02_llamadas_cplex.png',
    '03_tiempo_cplex.png',
    '04_evolucion_fitness.png',
    '05_evolucion_erp.png',
    '06_individuos_evaluados.png',
    '07_promedio_llamadas_indiv.png',
    '08_evolucion_hits.png'
]

print("=" * 80)
print("RENOMBRANDO GRÁFICOS")
print("=" * 80)

total_renombrados = 0
total_errores = 0

for grupo in grupos:
    carpeta = f"graficos_grupo{grupo}"
    
    if not os.path.exists(carpeta):
        print(f"\n⚠️  Carpeta no encontrada: {carpeta}")
        continue
    
    print(f"\nProcesando {carpeta}...")
    
    for archivo_base in archivos_base:
        archivo_original = os.path.join(carpeta, archivo_base)
        
        # Crear nuevo nombre: 01_baseline_tiempos.png -> 01_baseline_tiempos_grupo0.png
        nombre_base, extension = os.path.splitext(archivo_base)
        nuevo_nombre = f"{nombre_base}_grupo{grupo}{extension}"
        archivo_nuevo = os.path.join(carpeta, nuevo_nombre)
        
        if os.path.exists(archivo_original):
            try:
                # Verificar si ya existe el archivo con el nuevo nombre
                if os.path.exists(archivo_nuevo):
                    print(f"  [INFO] Ya existe: {nuevo_nombre} (omitiendo)")
                else:
                    os.rename(archivo_original, archivo_nuevo)
                    print(f"  [OK] {archivo_base} -> {nuevo_nombre}")
                    total_renombrados += 1
            except Exception as e:
                print(f"  [ERROR] Error al renombrar {archivo_base}: {e}")
                total_errores += 1
        else:
            print(f"  [WARN] No encontrado: {archivo_base}")

print(f"\n{'=' * 80}")
print(f"RESUMEN:")
print(f"  Archivos renombrados: {total_renombrados}")
if total_errores > 0:
    print(f"  Errores: {total_errores}")
print(f"{'=' * 80}")

