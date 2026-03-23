"""
Curvas de convergencia para prueba de tamaño de población.
Lee resultados de out/prueba_poblacion/ y genera gráficos comparativos.

Directorios esperados:
  out/prueba_poblacion/popXXX_cplexYY/evolution0/job.0.BestFitness.csv
"""
import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import matplotlib
matplotlib.rcParams['font.family'] = 'DejaVu Sans'

BASE_DIR   = os.path.dirname(os.path.abspath(__file__))
PRUEBA_DIR = os.path.join(BASE_DIR, "out", "prueba_poblacion")
OUT_DIR    = os.path.join(BASE_DIR, "out")

POBLACIONES = [100, 75, 50, 15, 10]
COLORES_POP = {
    100: "#e41a1c",
    75:  "#ff7f00",
    50:  "#4daf4a",
    15:  "#377eb8",
    10:  "#984ea3",
}


def leer_fitness(outdir: str):
    """Lee job.0.BestFitness.csv desde el directorio de resultados."""
    csv_path = os.path.join(outdir, "evolution0", "job.0.BestFitness.csv")
    if not os.path.exists(csv_path):
        print(f"  No encontrado: {csv_path}")
        return None
    try:
        df = pd.read_csv(csv_path, sep=";", decimal=",")
        df.columns = [c.strip() for c in df.columns]
        return df["Standarized"].values
    except Exception as e:
        print(f"  Error leyendo {csv_path}: {e}")
        return None


def graficar(cplex_pct: int):
    """Genera gráfico comparativo de poblaciones para un % de CPLEX dado."""
    fig, ax = plt.subplots(figsize=(11, 5))
    encontrados = 0

    for pop in POBLACIONES:
        outdir = os.path.join(PRUEBA_DIR, f"pop{pop}_cplex{cplex_pct}")
        fitness = leer_fitness(outdir)
        if fitness is None:
            continue

        gen = np.arange(len(fitness))
        ax.plot(gen, fitness, color=COLORES_POP[pop], linewidth=2,
                label=f"Población {pop}")

        # Marcar generación de convergencia
        mejoras = np.abs(np.diff(fitness))
        if np.any(mejoras > 0.001):
            conv_gen = np.where(mejoras > 0.001)[0][-1] + 1
            ax.axvline(x=conv_gen, color=COLORES_POP[pop],
                       linestyle="--", alpha=0.4)
        encontrados += 1

    if encontrados == 0:
        print(f"  Sin datos para CPLEX={cplex_pct}%. Ejecuta run_prueba_poblacion.bat primero.")
        plt.close()
        return None

    titulo = f"Convergencia por Tamaño de Población — CPLEX {cplex_pct}%"
    ax.set_title(titulo, fontsize=13)
    ax.set_xlabel("Generación", fontsize=11)
    ax.set_ylabel("Fitness Estandarizado (menor = mejor)", fontsize=11)
    ax.legend(fontsize=10)
    ax.grid(True, alpha=0.3)
    ax.invert_yaxis()

    outfile = os.path.join(OUT_DIR, f"convergencia_poblacion_cplex{cplex_pct}.png")
    plt.tight_layout()
    plt.savefig(outfile, dpi=150)
    plt.show()
    print(f"  Guardado: {outfile}")
    return outfile


def graficar_comparativo_4():
    """Genera figura con 2 subplots: CPLEX 0% y CPLEX 100% lado a lado."""
    fig, axes = plt.subplots(1, 2, figsize=(16, 5), sharey=True)

    for ax, cplex_pct in zip(axes, [0, 100]):
        encontrados = 0
        for pop in POBLACIONES:
            outdir = os.path.join(PRUEBA_DIR, f"pop{pop}_cplex{cplex_pct}")
            fitness = leer_fitness(outdir)
            if fitness is None:
                continue
            gen = np.arange(len(fitness))
            ax.plot(gen, fitness, color=COLORES_POP[pop], linewidth=2,
                    label=f"Población {pop}")
            encontrados += 1

        if encontrados == 0:
            ax.text(0.5, 0.5, "Sin datos", ha="center", va="center",
                    transform=ax.transAxes, fontsize=12)
        ax.set_title(f"CPLEX {cplex_pct}%", fontsize=12)
        ax.set_xlabel("Generación", fontsize=10)
        ax.grid(True, alpha=0.3)
        ax.invert_yaxis()
        ax.legend(fontsize=9)

    axes[0].set_ylabel("Fitness Estandarizado (menor = mejor)", fontsize=10)
    fig.suptitle("Convergencia por Tamaño de Población", fontsize=14, fontweight="bold")

    outfile = os.path.join(OUT_DIR, "convergencia_poblacion_comparativo.png")
    plt.tight_layout()
    plt.savefig(outfile, dpi=150)
    plt.show()
    print(f"Guardado: {outfile}")


if __name__ == "__main__":
    print("=== Curvas de convergencia — Prueba de población ===\n")

    print("Generando gráfico CPLEX 0%...")
    graficar(0)

    print("\nGenerando gráfico CPLEX 100%...")
    graficar(100)

    print("\nGenerando gráfico comparativo (0% vs 100%)...")
    graficar_comparativo_4()

    print("\nListo.")
