package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * FASE 1 DEL EXPERIMENTO: Establecer Línea Base
 * 
 * Esta clase ejecuta CPLEX puro sobre todas las instancias del experimento
 * para determinar el tiempo base (T_base) necesario para resolver cada instancia.
 * 
 * El T_base será usado para calcular los presupuestos de cada grupo experimental:
 * - Grupo 0: 0% de T_base (sin CPLEX)
 * - Grupo 1: 10% de T_base
 * - Grupo 2: 25% de T_base
 * - Grupo 3: 50% de T_base
 * - Grupo 4: 75% de T_base
 * - Grupo 5: 100% de T_base
 * 
 * IMPORTANTE: Ejecutar en la máquina dedicada del laboratorio
 */
public class CplexBaselineRunner {

    // Configuración
    private static final String EVOLUTION_INSTANCES_PATH = "data/evolution";
    private static final String EVALUATION_INSTANCES_PATH = "data/evaluation";
    private static final String OUTPUT_PATH = "out/baseline/";
    private static final String RESULTS_FILE = "cplex_baseline_results.csv";
    private static final String SUMMARY_FILE = "cplex_baseline_summary.txt";
    
    // Límite de tiempo máximo para CPLEX (en segundos)
    // Si una instancia no se resuelve en este tiempo, se registra como timeout
    private static final double MAX_TIME_LIMIT = 300.0; // 5 minutos

    public static void main(String[] args) {
        try {
            // Parsear argumentos de línea de comandos
            String instancesPath = EVOLUTION_INSTANCES_PATH;
            String outputPath = OUTPUT_PATH;
            double timeLimit = MAX_TIME_LIMIT;
            boolean singleInstance = false;
            
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-instances") && i + 1 < args.length) {
                    instancesPath = args[i + 1];
                } else if (args[i].equals("-output") && i + 1 < args.length) {
                    outputPath = args[i + 1];
                } else if (args[i].equals("-timeLimit") && i + 1 < args.length) {
                    timeLimit = Double.parseDouble(args[i + 1]);
                } else if (args[i].equals("-singleInstance")) {
                    singleInstance = true;
                }
            }
            
            System.out.println("================================================================");
            System.out.println("FASE 1: ESTABLECIMIENTO DE LINEA BASE CON CPLEX PURO");
            System.out.println("================================================================");
            System.out.println();
            
            // Crear directorio de salida
            File outputDir = new File(outputPath);
            outputDir.mkdirs();
            
            // Ejecutar experimento
            if (singleInstance) {
                runSingleInstanceTest(instancesPath, outputPath, timeLimit);
            } else {
                runBaselineExperiment();
            }
            
            System.out.println();
            System.out.println("================================================================");
            System.out.println("EXPERIMENTO COMPLETADO");
            System.out.println("================================================================");
            System.out.println("Resultados guardados en: " + OUTPUT_PATH);
            
        } catch (Exception e) {
            System.err.println("Error durante la ejecución del experimento:");
            e.printStackTrace();
        }
    }
    
    /**
     * Ejecuta una prueba con una sola instancia para verificar que el sistema funciona
     */
    private static void runSingleInstanceTest(String instancePath, String outputPath, double timeLimit) {
        try {
            System.out.println("PRUEBA RAPIDA: Procesando 1 instancia");
            System.out.println("Instancia: " + instancePath);
            System.out.println("Límite de tiempo: " + timeLimit + " segundos");
            System.out.println();
            
            // Crear instancia PDP
            Instance instance = new Instance();
            FileIO.readFile(instance, instancePath);
            PDPInstance pdpi = instance.getPdpInstance();
            
            // Configurar flags para que CPLEX se ejecute
            pdpi.setLastChange(true);
            pdpi.setCplexLastChange(true);
            
            // Verificar configuración
            System.out.println("Configuración de la instancia:");
            System.out.println("  - Nodos: " + pdpi.getCostMatrix().size());
            System.out.println("  - Capacidad vehículo: " + pdpi.getVehicleCapacity());
            System.out.println("  - lastChange: " + pdpi.isLastChange());
            System.out.println("  - cplexLastChange: " + pdpi.isCplexLastChange());
            System.out.println("  - optimal: " + pdpi.getOptimal());
            
            // Ejecutar CPLEX con límite de tiempo
            System.out.println("Ejecutando CPLEX...");
            long startTime = System.currentTimeMillis();
            double timeUsed = pdpi.cplex_terminal_with_limit(timeLimit);
            long endTime = System.currentTimeMillis();
            
            double actualTime = (endTime - startTime) / 1000.0;
            
            // Mostrar resultados
            System.out.println("================================================================");
            System.out.println("RESULTADOS DE LA PRUEBA");
            System.out.println("================================================================");
            System.out.println("Instancia: " + instancePath);
            System.out.println("Tiempo CPLEX: " + String.format("%.3f", timeUsed) + " segundos");
            System.out.println("Tiempo total: " + String.format("%.3f", actualTime) + " segundos");
            System.out.println("Costo: " + String.format("%.2f", pdpi.getTotalCost()));
            System.out.println("Óptimo: " + (pdpi.getOptimal() ? "Sí" : "No"));
            System.out.println("Estado: " + (timeUsed > 0 ? "Exitoso" : "Falló"));
            System.out.println();
            
            // Guardar resultados
            String resultsFile = outputPath + "/cplex_baseline_results.csv";
            String summaryFile = outputPath + "/cplex_baseline_summary.txt";
            
            // Crear archivo de resultados
            try (PrintWriter writer = new PrintWriter(new FileWriter(resultsFile, StandardCharsets.UTF_8))) {
                writer.println("Instancia,Tiempo_CPLEX,Costo,Optimo,Estado");
                writer.println(instancePath + "," + String.format("%.3f", timeUsed) + "," + 
                             String.format("%.2f", pdpi.getTotalCost()) + "," + 
                             (pdpi.getOptimal() ? "Optimal" : "Feasible") + "," + 
                             (timeUsed > 0 ? "Success" : "Failed"));
            }
            
            // Crear archivo de resumen
            try (PrintWriter writer = new PrintWriter(new FileWriter(summaryFile, StandardCharsets.UTF_8))) {
                writer.println("================================================================");
                writer.println("RESUMEN ESTADISTICO - PRUEBA RAPIDA");
                writer.println("================================================================");
                writer.println();
                writer.println("INSTANCIAS PROCESADAS:");
                writer.println("  Total: 1");
                writer.println("  Exitosas: " + (timeUsed > 0 ? "1" : "0"));
                writer.println("  Fallidas: " + (timeUsed > 0 ? "0" : "1"));
                writer.println();
                writer.println("ESTADISTICAS:");
                writer.println("  Tiempo promedio: " + String.format("%.3f", timeUsed) + " segundos");
                writer.println("  T_base calculado: " + String.format("%.3f", timeUsed) + " segundos");
                writer.println();
                writer.println("RESULTADO: " + (timeUsed > 0 ? "SISTEMA FUNCIONA CORRECTAMENTE" : "SISTEMA FALLA"));
            }
            
            System.out.println("Resultados guardados en:");
            System.out.println("- " + resultsFile);
            System.out.println("- " + summaryFile);
            
        } catch (Exception e) {
            System.err.println("ERROR en prueba rápida: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runBaselineExperiment() throws IOException {
        // Leer instancias de evolución
        System.out.println("Leyendo instancias de evolución desde: " + EVOLUTION_INSTANCES_PATH);
        ArrayList<PDPData> evolutionData = new ArrayList<>();
        FileIO.readInstances(evolutionData, EVOLUTION_INSTANCES_PATH);
        System.out.println("  Instancias de evolución leídas: " + evolutionData.size());
        
        // Leer instancias de evaluación
        System.out.println("Leyendo instancias de evaluación desde: " + EVALUATION_INSTANCES_PATH);
        ArrayList<PDPData> evaluationData = new ArrayList<>();
        FileIO.readInstances(evaluationData, EVALUATION_INSTANCES_PATH);
        System.out.println("  Instancias de evaluación leídas: " + evaluationData.size());
        
        int totalInstances = evolutionData.size() + evaluationData.size();
        System.out.println("Total de instancias a resolver: " + totalInstances);
        System.out.println();
        
        // Crear archivo CSV de resultados
        PrintWriter csvWriter = new PrintWriter(
            new OutputStreamWriter(
                new FileOutputStream(OUTPUT_PATH + RESULTS_FILE), 
                StandardCharsets.UTF_8
            )
        );
        
        // Encabezado CSV
        csvWriter.println("Type,Instance,Optimal,Cost,TimeSeconds,Status,Gap");
        
        // Listas para estadísticas
        List<Double> evolutionTimes = new ArrayList<>();
        List<Double> evaluationTimes = new ArrayList<>();
        List<Double> allTimes = new ArrayList<>();
        
        // Procesar instancias de evolución
        System.out.println("================================================================");
        System.out.println("PROCESANDO INSTANCIAS DE EVOLUCIÓN");
        System.out.println("================================================================");
        for (int i = 0; i < evolutionData.size(); i++) {
            PDPData data = evolutionData.get(i);
            String instanceName = data.getInstance().getName();
            
            System.out.println("  [" + (i + 1) + "/" + evolutionData.size() + "] " + instanceName);
            
            BaselineResult result = solvePDPWithCplex(data, MAX_TIME_LIMIT);
            
            // Escribir resultado en CSV
            csvWriter.println(String.format("Evolution,%s,%.2f,%.2f,%.3f,%s,%.4f",
                instanceName,
                result.optimalValue,
                result.solutionCost,
                result.timeSeconds,
                result.status,
                result.gap
            ));
            csvWriter.flush();
            
            // Agregar a estadísticas
            if (result.status.equals("Optimal") || result.status.equals("Feasible")) {
                evolutionTimes.add(result.timeSeconds);
                allTimes.add(result.timeSeconds);
            }
            
            System.out.println("    Tiempo: " + String.format("%.3f", result.timeSeconds) + "s, " +
                             "Costo: " + String.format("%.2f", result.solutionCost) + ", " +
                             "Estado: " + result.status);
        }
        
        // Procesar instancias de evaluación
        System.out.println();
        System.out.println("================================================================");
        System.out.println("PROCESANDO INSTANCIAS DE EVALUACIÓN");
        System.out.println("================================================================");
        for (int i = 0; i < evaluationData.size(); i++) {
            PDPData data = evaluationData.get(i);
            String instanceName = data.getInstance().getName();
            
            System.out.println("  [" + (i + 1) + "/" + evaluationData.size() + "] " + instanceName);
            
            BaselineResult result = solvePDPWithCplex(data, MAX_TIME_LIMIT);
            
            // Escribir resultado en CSV
            csvWriter.println(String.format("Evaluation,%s,%.2f,%.2f,%.3f,%s,%.4f",
                instanceName,
                result.optimalValue,
                result.solutionCost,
                result.timeSeconds,
                result.status,
                result.gap
            ));
            csvWriter.flush();
            
            // Agregar a estadísticas
            if (result.status.equals("Optimal") || result.status.equals("Feasible")) {
                evaluationTimes.add(result.timeSeconds);
                allTimes.add(result.timeSeconds);
            }
            
            System.out.println("    Tiempo: " + String.format("%.3f", result.timeSeconds) + "s, " +
                             "Costo: " + String.format("%.2f", result.solutionCost) + ", " +
                             "Estado: " + result.status);
        }
        
        csvWriter.close();
        
        // Calcular y escribir estadísticas
        writeStatistics(evolutionTimes, evaluationTimes, allTimes, totalInstances);
    }

    /**
     * Resuelve una instancia PDP usando CPLEX puro
     */
    private static BaselineResult solvePDPWithCplex(PDPData data, double timeLimit) {
        BaselineResult result = new BaselineResult();
        
        PDPInstance pdpi = data.getInstance().getPdpInstance();
        Instance instance = data.getInstance();
        
        // Obtener valor óptimo conocido
        result.optimalValue = instance.isOptimalKnown() ? 
            instance.getOptimal() : instance.getFeasible();
        
        try {
            // Configurar flags para que CPLEX se ejecute
            pdpi.setLastChange(true);
            pdpi.setCplexLastChange(true);
            
            // Medir tiempo de inicio
            double startTime = System.nanoTime();
            
            // Ejecutar CPLEX con el límite de tiempo
            double timeUsed = pdpi.cplex_terminal_with_limit(timeLimit);
            
            // Medir tiempo total (incluyendo overhead)
            double totalTime = (System.nanoTime() - startTime) / 1_000_000_000.0; // nanosegundos a segundos
            
            result.timeSeconds = totalTime;
            result.solutionCost = pdpi.getTotalCost();
            
            // Determinar estado
            if (pdpi.getOptimal()) {
                result.status = "Optimal";
                result.gap = 0.0;
            } else if (totalTime >= timeLimit * 0.95) { // Si usó el 95% o más del tiempo límite
                result.status = "Timeout";
                result.gap = Math.abs(result.optimalValue - result.solutionCost) / result.optimalValue;
            } else if (result.solutionCost > 0) {
                result.status = "Feasible";
                result.gap = Math.abs(result.optimalValue - result.solutionCost) / result.optimalValue;
            } else {
                result.status = "Failed";
                result.gap = 1.0;
            }
            
        } catch (Exception e) {
            System.err.println("    ERROR al resolver instancia: " + e.getMessage());
            result.status = "Error";
            result.timeSeconds = 0.0;
            result.solutionCost = 0.0;
            result.gap = 1.0;
        }
        
        return result;
    }

    /**
     * Calcula y escribe estadísticas del experimento
     */
    private static void writeStatistics(List<Double> evolutionTimes, 
                                       List<Double> evaluationTimes, 
                                       List<Double> allTimes,
                                       int totalInstances) throws IOException {
        
        PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(
                new FileOutputStream(OUTPUT_PATH + SUMMARY_FILE), 
                StandardCharsets.UTF_8
            )
        );
        
        writer.println("================================================================");
        writer.println("RESUMEN ESTADISTICO - LINEA BASE CPLEX PURO");
        writer.println("================================================================");
        writer.println();
        
        writer.println("INSTANCIAS PROCESADAS:");
        writer.println("  Total: " + totalInstances);
        writer.println("  Evolución: " + evolutionTimes.size() + " resueltas");
        writer.println("  Evaluación: " + evaluationTimes.size() + " resueltas");
        writer.println();
        
        // Estadísticas de evolución
        if (!evolutionTimes.isEmpty()) {
            writer.println("ESTADISTICAS - INSTANCIAS DE EVOLUCION:");
            writeTimeStats(writer, evolutionTimes);
            writer.println();
        }
        
        // Estadísticas de evaluación
        if (!evaluationTimes.isEmpty()) {
            writer.println("ESTADISTICAS - INSTANCIAS DE EVALUACION:");
            writeTimeStats(writer, evaluationTimes);
            writer.println();
        }
        
        // Estadísticas globales
        if (!allTimes.isEmpty()) {
            writer.println("ESTADISTICAS GLOBALES:");
            writeTimeStats(writer, allTimes);
            writer.println();
        }
        
        // Calcular T_base (tiempo total global)
        double tBase = calculateSum(allTimes);
        writer.println("================================================================");
        writer.println("T_base (Tiempo Total Global): " + String.format("%.3f", tBase) + " segundos");
        writer.println("================================================================");
        writer.println();
        writer.println("PRESUPUESTOS PARA GRUPOS EXPERIMENTALES:");
        writer.println("  Grupo 0 (0%):    0.000 segundos (sin CPLEX)");
        writer.println("  Grupo 1 (10%):   " + String.format("%.3f", tBase * 0.10) + " segundos");
        writer.println("  Grupo 2 (25%):   " + String.format("%.3f", tBase * 0.25) + " segundos");
        writer.println("  Grupo 3 (50%):   " + String.format("%.3f", tBase * 0.50) + " segundos");
        writer.println("  Grupo 4 (75%):   " + String.format("%.3f", tBase * 0.75) + " segundos");
        writer.println("  Grupo 5 (100%):  " + String.format("%.3f", tBase * 1.00) + " segundos");
        writer.println();
        writer.println("================================================================");
        writer.println("SIGUIENTE PASO:");
        writer.println("Actualizar los archivos de parámetros en src/model/params/");
        writer.println("Reemplazar TBASE_VALUE con: " + String.format("%.3f", tBase));
        writer.println("================================================================");
        
        writer.close();
        
        // También imprimir en consola
        System.out.println();
        System.out.println("================================================================");
        System.out.println("T_base calculado: " + String.format("%.3f", tBase) + " segundos");
        System.out.println("================================================================");
        System.out.println("Ver resumen completo en: " + OUTPUT_PATH + SUMMARY_FILE);
    }

    private static void writeTimeStats(PrintWriter writer, List<Double> times) {
        double mean = calculateMean(times);
        double min = calculateMin(times);
        double max = calculateMax(times);
        double stdDev = calculateStdDev(times, mean);
        double total = calculateSum(times);
        
        writer.println("  Promedio: " + String.format("%.3f", mean) + " segundos");
        writer.println("  Mínimo:   " + String.format("%.3f", min) + " segundos");
        writer.println("  Máximo:   " + String.format("%.3f", max) + " segundos");
        writer.println("  Desv.Est: " + String.format("%.3f", stdDev) + " segundos");
        writer.println("  Total:    " + String.format("%.3f", total) + " segundos");
    }

    // Métodos auxiliares para cálculos estadísticos
    private static double calculateMean(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static double calculateMin(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    private static double calculateMax(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private static double calculateSum(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        return values.stream().mapToDouble(Double::doubleValue).sum();
    }

    private static double calculateStdDev(List<Double> values, double mean) {
        if (values.size() <= 1) return 0.0;
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .sum() / (values.size() - 1);
        return Math.sqrt(variance);
    }

    /**
     * Clase para almacenar resultados de una ejecución
     */
    private static class BaselineResult {
        double optimalValue;
        double solutionCost;
        double timeSeconds;
        String status; // "Optimal", "Feasible", "Timeout", "Failed", "Error"
        double gap;
    }
}

