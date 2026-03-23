"""
Genera curva de convergencia desde los datos de out/results/
Muestra la media ± desviación estándar de todas las repeticiones para un grupo.
"""
import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib
matplotlib.rcParams['font.family'] = 'DejaVu Sans'

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RESULTS_DIR = os.path.join(BASE_DIR, "out", "results")
OUT_DIR = os.path.join(BASE_DIR, "out")


def leer_best_fitness(grupo: str) -> pd.DataFrame:
    """Lee todos los archivos BestFitness.csv de un grupo y retorna DataFrame."""
    grupo_path = os.path.join(RESULTS_DIR, grupo)
    runs = []

    for evo_dir in sorted(os.listdir(grupo_path)):
        evo_path = os.path.join(grupo_path, evo_dir)
        if not os.path.isdir(evo_path):
            continue

        csv_path = os.path.join(evo_path, "job.0.BestFitness.csv")
        if not os.path.exists(csv_path):
            continue

        try:
            df = pd.read_csv(csv_path, sep=";", decimal=",")
            df.columns = [c.strip() for c in df.columns]
            runs.append(df["Standarized"].values)
        except Exception as e:
            print(f"  Advertencia: no se pudo leer {csv_path}: {e}")

    if not runs:
        raise ValueError(f"No se encontraron datos para {grupo}")

    # Igualar longitudes (tomar el mínimo)
    min_len = min(len(r) for r in runs)
    runs = [r[:min_len] for r in runs]

    matrix = np.array(runs)  # shape: (n_runs, n_gen)
    gen = np.arange(min_len)
    media = matrix.mean(axis=0)
    std = matrix.std(axis=0)

    return gen, media, std, len(runs)


def graficar_convergencia(grupo: str = "grupo0"):
    """Genera y guarda la curva de convergencia de un grupo."""
    print(f"Leyendo datos de {grupo}...")
    gen, media, std, n_runs = leer_best_fitness(grupo)

    fig, ax = plt.subplots(figsize=(10, 5))

    ax.plot(gen, media, color="steelblue", linewidth=2, label=f"Media ({n_runs} runs)")
    ax.fill_between(gen, media - std, media + std,
                    alpha=0.25, color="steelblue", label="±1 Desv. Est.")

    # Detectar generación de convergencia (donde el cambio es < 0.001)
    mejoras = np.abs(np.diff(media))
    if np.any(mejoras > 0.001):
        ultima_mejora = np.where(mejoras > 0.001)[0][-1] + 1
        ax.axvline(x=ultima_mejora, color="red", linestyle="--", alpha=0.7,
                   label=f"Convergencia aprox. gen {ultima_mejora}")
        print(f"  Convergencia detectada en generación {ultima_mejora}")

    ax.set_xlabel("Generación", fontsize=12)
    ax.set_ylabel("Fitness Estandarizado (menor = mejor)", fontsize=12)
    ax.set_title(f"Curva de Convergencia - {grupo.capitalize()}", fontsize=14)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)
    ax.invert_yaxis()  # menor fitness = mejor

    outfile = os.path.join(OUT_DIR, f"convergencia_{grupo}.png")
    plt.tight_layout()
    plt.savefig(outfile, dpi=150)
    plt.show()
    print(f"Guardado en: {outfile}")
    return outfile


def graficar_todos_grupos():
    """Genera curvas para todos los grupos en un solo gráfico comparativo."""
    grupos = [f"grupo{i}" for i in range(6)]
    labels = ["Grupo 0 (sin CPLEX)", "Grupo 1 (10%)", "Grupo 2 (25%)",
              "Grupo 3 (50%)", "Grupo 4 (75%)", "Grupo 5 (100%)"]
    colores = ["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b"]

    fig, ax = plt.subplots(figsize=(12, 6))

    for grupo, label, color in zip(grupos, labels, colores):
        grupo_path = os.path.join(RESULTS_DIR, grupo)
        if not os.path.isdir(grupo_path):
            print(f"  {grupo} no encontrado, omitiendo.")
            continue
        try:
            gen, media, std, n_runs = leer_best_fitness(grupo)
            ax.plot(gen, media, color=color, linewidth=1.8, label=f"{label} (n={n_runs})")
            ax.fill_between(gen, media - std, media + std, alpha=0.1, color=color)
        except Exception as e:
            print(f"  Error en {grupo}: {e}")

    ax.set_xlabel("Generación", fontsize=12)
    ax.set_ylabel("Fitness Estandarizado (menor = mejor)", fontsize=12)
    ax.set_title("Curvas de Convergencia - Todos los Grupos", fontsize=14)
    ax.legend(fontsize=9, loc="upper right")
    ax.grid(True, alpha=0.3)
    ax.invert_yaxis()

    outfile = os.path.join(OUT_DIR, "convergencia_todos_grupos.png")
    plt.tight_layout()
    plt.savefig(outfile, dpi=150)
    plt.show()
    print(f"Guardado en: {outfile}")


if __name__ == "__main__":
    import sys

    if len(sys.argv) > 1:
        grupo = sys.argv[1]
        graficar_convergencia(grupo)
    else:
        # Por defecto: mostrar grupo0 individual + comparativo
        graficar_convergencia("grupo0")
        print()
        graficar_todos_grupos()
