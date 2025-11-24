#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para generar gráficos en formato imagen (PNG) a partir de los datos del Excel
"""

import os
import sys
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
matplotlib.use('Agg')  # Backend sin GUI
import numpy as np

def generate_charts(group_num=None):
    """Genera gráficos en formato PNG a partir del Excel"""
    
    # Determinar nombre del archivo Excel
    if group_num is not None:
        excel_file = f"RESULTADOS_EXPERIMENTO_GRUPO{group_num}.xlsx"
        output_dir = f"graficos_grupo{group_num}"
    else:
        excel_file = "RESULTADOS_EXPERIMENTO.xlsx"
        output_dir = "graficos"
    
    if not os.path.exists(excel_file):
        print(f"ERROR: No se encuentra el archivo {excel_file}")
        print("Primero debe generar el Excel ejecutando: python generate_excel_report.py [grupo]")
        return False
    
    # Crear directorio de salida
    os.makedirs(output_dir, exist_ok=True)
    
    print(f"\nGenerando gráficos desde: {excel_file}")
    print(f"Guardando gráficos en: {output_dir}/")
    print("=" * 80)
    
    # ===== GRÁFICO 1: Baseline por Instancia =====
    print("\n1. Creando gráfico: Baseline por Instancia...")
    try:
        df_baseline = pd.read_excel(excel_file, sheet_name="Baseline por Instancia", skiprows=1)
        
        # Ordenar por tiempo y tomar top 20
        df_baseline_sorted = df_baseline.sort_values('Tiempo Baseline (s)').head(20)
        
        plt.figure(figsize=(14, 8))
        plt.barh(range(len(df_baseline_sorted)), df_baseline_sorted['Tiempo Baseline (s)'].values)
        plt.yticks(range(len(df_baseline_sorted)), df_baseline_sorted['Instancia'].values)
        plt.xlabel('Tiempo (segundos)', fontsize=12)
        plt.ylabel('Instancia', fontsize=12)
        plt.title('Tiempo Baseline CPLEX por Instancia (Top 20)', fontsize=14, fontweight='bold')
        plt.grid(axis='x', alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/01_baseline_tiempos.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 01_baseline_tiempos.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    # ===== GRÁFICO 2: Estadísticas por Ejecución - Llamadas CPLEX =====
    print("\n2. Creando gráfico: Llamadas CPLEX por Ejecución...")
    try:
        df_stats = pd.read_excel(excel_file, sheet_name="Estadísticas por Ejecución", skiprows=1)
        
        plt.figure(figsize=(12, 6))
        plt.plot(df_stats['Ejecución'], df_stats['Llamadas CPLEX (Total)'], 
                marker='o', linewidth=2, markersize=6)
        plt.xlabel('Ejecución', fontsize=12)
        plt.ylabel('Llamadas CPLEX', fontsize=12)
        plt.title('Llamadas CPLEX por Ejecución', fontsize=14, fontweight='bold')
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/02_llamadas_cplex.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 02_llamadas_cplex.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    # ===== GRÁFICO 3: Estadísticas por Ejecución - Tiempo Total =====
    print("\n3. Creando gráfico: Tiempo Total CPLEX por Ejecución...")
    try:
        plt.figure(figsize=(12, 6))
        plt.plot(df_stats['Ejecución'], df_stats['Tiempo Total CPLEX (s)'], 
                marker='s', linewidth=2, markersize=6, color='orange')
        plt.xlabel('Ejecución', fontsize=12)
        plt.ylabel('Tiempo (segundos)', fontsize=12)
        plt.title('Tiempo Total CPLEX por Ejecución', fontsize=14, fontweight='bold')
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/03_tiempo_cplex.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 03_tiempo_cplex.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    # ===== GRÁFICO 4: Evolución del Fitness =====
    print("\n4. Creando gráfico: Evolución del Fitness (ERP)...")
    try:
        df_fitness = pd.read_excel(excel_file, sheet_name="Best Fitness por Ejecución", skiprows=1)
        
        # Limpiar datos: eliminar filas con NaN o strings en columnas numéricas
        df_fitness = df_fitness.dropna(subset=['Generación', 'Fitness Estandarizado (ERP)'])
        df_fitness = df_fitness[pd.to_numeric(df_fitness['Fitness Estandarizado (ERP)'], errors='coerce').notna()]
        df_fitness['Fitness Estandarizado (ERP)'] = pd.to_numeric(df_fitness['Fitness Estandarizado (ERP)'])
        
        # Calcular estadísticas por generación
        fitness_by_gen = df_fitness.groupby('Generación')['Fitness Estandarizado (ERP)'].agg([
            ('Promedio', 'mean'),
            ('Mínimo', 'min'),
            ('Máximo', 'max')
        ]).reset_index()
        
        plt.figure(figsize=(14, 8))
        plt.plot(fitness_by_gen['Generación'], fitness_by_gen['Promedio'], 
                label='Promedio', linewidth=2, marker='o', markersize=4)
        plt.plot(fitness_by_gen['Generación'], fitness_by_gen['Mínimo'], 
                label='Mínimo', linewidth=2, linestyle='--', marker='s', markersize=4)
        plt.plot(fitness_by_gen['Generación'], fitness_by_gen['Máximo'], 
                label='Máximo', linewidth=2, linestyle='--', marker='^', markersize=4)
        plt.xlabel('Generación', fontsize=12)
        plt.ylabel('Fitness (ERP)', fontsize=12)
        plt.title('Evolución del Fitness (ERP) - Promedio, Mínimo y Máximo', fontsize=14, fontweight='bold')
        plt.legend(fontsize=11)
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/04_evolucion_fitness.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 04_evolucion_fitness.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    # ===== GRÁFICO 5: Evolución del ERP (AvgERP vs BestERP) =====
    print("\n5. Creando gráfico: Evolución del Error Relativo Promedio (ERP)...")
    try:
        df_stats_pm = pd.read_excel(excel_file, sheet_name="Estadísticas Promedio y Mejor", skiprows=1)
        
        # Limpiar datos
        df_stats_pm = df_stats_pm.dropna(subset=['Gen', 'AvgERP', 'BestERP'])
        df_stats_pm['AvgERP'] = pd.to_numeric(df_stats_pm['AvgERP'], errors='coerce')
        df_stats_pm['BestERP'] = pd.to_numeric(df_stats_pm['BestERP'], errors='coerce')
        df_stats_pm = df_stats_pm[df_stats_pm['AvgERP'].notna() & df_stats_pm['BestERP'].notna()]
        
        # Calcular promedio por generación
        erp_by_gen = df_stats_pm.groupby('Gen').agg({
            'AvgERP': 'mean',
            'BestERP': 'mean'
        }).reset_index()
        
        plt.figure(figsize=(14, 8))
        plt.plot(erp_by_gen['Gen'], erp_by_gen['AvgERP'], 
                label='AvgERP (Promedio de la población)', linewidth=2, marker='o', markersize=4)
        plt.plot(erp_by_gen['Gen'], erp_by_gen['BestERP'], 
                label='BestERP (Mejor individuo)', linewidth=2, marker='s', markersize=4, color='red')
        plt.xlabel('Generación', fontsize=12)
        plt.ylabel('Error Relativo Promedio (ERP)', fontsize=12)
        plt.title('Evolución del Error Relativo Promedio (ERP)', fontsize=14, fontweight='bold')
        plt.legend(fontsize=11)
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/05_evolucion_erp.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 05_evolucion_erp.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    # ===== GRÁFICO 6: Distribución de Individuos Evaluados =====
    print("\n6. Creando gráfico: Distribución de Individuos Evaluados...")
    try:
        plt.figure(figsize=(12, 6))
        plt.bar(df_stats['Ejecución'], df_stats['Individuos Evaluados'], 
               color='steelblue', alpha=0.7)
        plt.xlabel('Ejecución', fontsize=12)
        plt.ylabel('Individuos Evaluados', fontsize=12)
        plt.title('Distribución de Individuos Evaluados por Ejecución', fontsize=14, fontweight='bold')
        plt.grid(axis='y', alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/06_individuos_evaluados.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 06_individuos_evaluados.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    # ===== GRÁFICO 7: Promedio de Llamadas por Individuo =====
    print("\n7. Creando gráfico: Promedio de Llamadas CPLEX por Individuo...")
    try:
        plt.figure(figsize=(12, 6))
        plt.plot(df_stats['Ejecución'], df_stats['Prom. Llamadas/Indiv'], 
                marker='o', linewidth=2, markersize=6, color='green')
        plt.xlabel('Ejecución', fontsize=12)
        plt.ylabel('Promedio Llamadas/Individuo', fontsize=12)
        plt.title('Promedio de Llamadas CPLEX por Individuo', fontsize=14, fontweight='bold')
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/07_promedio_llamadas_indiv.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 07_promedio_llamadas_indiv.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    # ===== GRÁFICO 8: Hits por Generación =====
    print("\n8. Creando gráfico: Hits por Generación...")
    try:
        # Asegurar que Hits sea numérico
        df_fitness['Hits'] = pd.to_numeric(df_fitness['Hits'], errors='coerce')
        df_fitness_hits = df_fitness[df_fitness['Hits'].notna()]
        
        hits_by_gen = df_fitness_hits.groupby('Generación')['Hits'].agg([
            ('Promedio', 'mean'),
            ('Máximo', 'max')
        ]).reset_index()
        
        plt.figure(figsize=(14, 8))
        plt.plot(hits_by_gen['Generación'], hits_by_gen['Promedio'], 
                label='Hits Promedio', linewidth=2, marker='o', markersize=4)
        plt.plot(hits_by_gen['Generación'], hits_by_gen['Máximo'], 
                label='Hits Máximo', linewidth=2, linestyle='--', marker='s', markersize=4, color='red')
        plt.xlabel('Generación', fontsize=12)
        plt.ylabel('Hits (Instancias con solución óptima)', fontsize=12)
        plt.title('Evolución de Hits (Soluciones Óptimas Encontradas)', fontsize=14, fontweight='bold')
        plt.legend(fontsize=11)
        plt.grid(alpha=0.3)
        plt.tight_layout()
        plt.savefig(f"{output_dir}/08_evolucion_hits.png", dpi=300, bbox_inches='tight')
        plt.close()
        print("   [OK] Guardado: 08_evolucion_hits.png")
    except Exception as e:
        print(f"   [ERROR] {e}")
    
    print("\n" + "=" * 80)
    print(f"[OK] Todos los gráficos generados en: {output_dir}/")
    print("=" * 80)
    
    return True

if __name__ == "__main__":
    group_num = None
    if len(sys.argv) > 1:
        try:
            group_num = int(sys.argv[1])
        except ValueError:
            pass
    
    success = generate_charts(group_num)
    if not success:
        exit(1)

