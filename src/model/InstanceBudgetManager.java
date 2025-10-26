package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de presupuestos dinámicos de CPLEX por instancia.
 *
 * Este sistema implementa el diseño experimental que requiere presupuestos
 * POR INSTANCIA basados en el tiempo que CPLEX puro necesita para resolver
 * cada instancia específica (T_base[i]).
 *
 * FUNCIONAMIENTO:
 * 1. Carga el archivo instance_baseline.csv que contiene T_base[i] para cada instancia
 * 2. Almacena el mapping: nombreInstancia -> T_base[i]
 * 3. Calcula presupuestos dinámicos: presupuesto[i] = porcentaje_grupo × T_base[i]
 *
 * EJEMPLO:
 * - Instancia A tiene T_base[A] = 20 segundos
 * - Instancia B tiene T_base[B] = 80 segundos
 * - Grupo experimental al 50%:
 *   * Presupuesto para A = 0.50 × 20 = 10 segundos
 *   * Presupuesto para B = 0.50 × 80 = 40 segundos
 *
 * Esto garantiza que cada instancia recibe un presupuesto proporcional a su dificultad.
 */
public class InstanceBudgetManager {

    private static InstanceBudgetManager instance = null;

    // Mapping: nombreInstancia -> T_base[i] (en segundos)
    private Map<String, Double> instanceBaselines;

    // Porcentaje del presupuesto (0.0 a 1.0)
    // Ejemplos: 0.10 = 10%, 0.50 = 50%, 1.0 = 100%
    private double budgetPercentage;

    // Estadísticas
    private double minTBase;
    private double maxTBase;
    private double meanTBase;
    private int totalInstances;

    // Estado
    private boolean initialized;

    private InstanceBudgetManager() {
        instanceBaselines = new HashMap<>();
        budgetPercentage = 0.0;
        initialized = false;
    }

    /**
     * Obtiene la instancia singleton del manager
     */
    public static synchronized InstanceBudgetManager getInstance() {
        if (instance == null) {
            instance = new InstanceBudgetManager();
        }
        return instance;
    }

    /**
     * Inicializa el manager cargando los datos de línea base y configurando el porcentaje
     *
     * @param baselineFilePath Ruta al archivo instance_baseline.csv
     * @param budgetPercentage Porcentaje de presupuesto (0.0 a 1.0)
     */
    public void initialize(String baselineFilePath, double budgetPercentage) throws IOException {
        this.budgetPercentage = budgetPercentage;
        loadBaselines(baselineFilePath);
        calculateStatistics();
        initialized = true;

        System.out.println("================================================================");
        System.out.println("INSTANCE BUDGET MANAGER INICIALIZADO");
        System.out.println("================================================================");
        System.out.println("Archivo de línea base: " + baselineFilePath);
        System.out.println("Porcentaje de presupuesto: " + String.format("%.0f%%", budgetPercentage * 100));
        System.out.println("Instancias cargadas: " + totalInstances);
        System.out.println("T_base mínimo: " + String.format("%.3f", minTBase) + " segundos");
        System.out.println("T_base máximo: " + String.format("%.3f", maxTBase) + " segundos");
        System.out.println("T_base promedio: " + String.format("%.3f", meanTBase) + " segundos");
        System.out.println("================================================================");
    }

    /**
     * Carga los datos de línea base desde el archivo CSV
     */
    private void loadBaselines(String filePath) throws IOException {
        instanceBaselines.clear();

        BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                new FileInputStream(filePath),
                StandardCharsets.UTF_8
            )
        );

        String line;
        boolean isHeader = true;

        while ((line = reader.readLine()) != null) {
            // Saltar encabezado
            if (isHeader) {
                isHeader = false;
                continue;
            }

            // Parsear línea: InstanceName,T_base_seconds,Optimal_value,Status,Difficulty
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                String instanceName = parts[0].trim();
                double tBase = Double.parseDouble(parts[1].trim());

                instanceBaselines.put(instanceName, tBase);
            }
        }

        reader.close();

        if (instanceBaselines.isEmpty()) {
            throw new IOException("No se cargaron datos de línea base desde: " + filePath);
        }
    }

    /**
     * Calcula estadísticas sobre los T_base cargados
     */
    private void calculateStatistics() {
        if (instanceBaselines.isEmpty()) {
            return;
        }

        totalInstances = instanceBaselines.size();
        minTBase = Double.MAX_VALUE;
        maxTBase = Double.MIN_VALUE;
        double sum = 0.0;

        for (double tBase : instanceBaselines.values()) {
            if (tBase < minTBase) minTBase = tBase;
            if (tBase > maxTBase) maxTBase = tBase;
            sum += tBase;
        }

        meanTBase = sum / totalInstances;
    }

    /**
     * Obtiene el presupuesto de CPLEX para una instancia específica
     *
     * @param instanceName Nombre de la instancia
     * @return Presupuesto en segundos = budgetPercentage × T_base[instanceName]
     */
    public double getBudgetForInstance(String instanceName) {
        if (!initialized) {
            System.err.println("WARNING: InstanceBudgetManager no está inicializado");
            return 0.0;
        }

        Double tBase = instanceBaselines.get(instanceName);

        if (tBase == null) {
            System.err.println("WARNING: No se encontró T_base para instancia: " + instanceName);
            System.err.println("Instancias disponibles: " + instanceBaselines.keySet());
            return 0.0;
        }

        return budgetPercentage * tBase;
    }

    /**
     * Obtiene el T_base original de una instancia (sin aplicar porcentaje)
     *
     * @param instanceName Nombre de la instancia
     * @return T_base en segundos
     */
    public double getTBaseForInstance(String instanceName) {
        if (!initialized) {
            return 0.0;
        }

        Double tBase = instanceBaselines.get(instanceName);
        return (tBase != null) ? tBase : 0.0;
    }

    /**
     * Verifica si el manager está inicializado
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Obtiene el porcentaje de presupuesto configurado
     */
    public double getBudgetPercentage() {
        return budgetPercentage;
    }

    /**
     * Obtiene el número de instancias cargadas
     */
    public int getTotalInstances() {
        return totalInstances;
    }

    /**
     * Obtiene estadísticas del sistema
     */
    public String getStats() {
        return String.format(
            "InstanceBudgetManager - Instances: %d, Budget: %.0f%%, T_base range: [%.3f, %.3f], mean: %.3f",
            totalInstances, budgetPercentage * 100, minTBase, maxTBase, meanTBase
        );
    }

    /**
     * Resetea el manager (útil para testing)
     */
    public void reset() {
        instanceBaselines.clear();
        budgetPercentage = 0.0;
        initialized = false;
    }
}
