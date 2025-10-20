package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Sistema de logging para registrar el uso detallado de CPLEX
 * durante los experimentos.
 * 
 * Registra:
 * - Cada llamada individual a CPLEX
 * - Tiempo usado por llamada
 * - Presupuesto usado vs asignado
 * - Estadísticas agregadas por individuo, generación y experimento
 * 
 * Los logs permiten analizar:
 * - ¿Cuántas veces se llamó CPLEX por algoritmo?
 * - ¿Cuánto presupuesto se utilizó realmente?
 * - ¿En qué momento del algoritmo se usa CPLEX?
 * - ¿Qué tan eficiente es el uso del presupuesto?
 */
public class CplexUsageLogger {

    private static CplexUsageLogger instance = null;
    private PrintWriter detailedLog = null;
    private PrintWriter summaryLog = null;
    private String outputPath = "";
    private int jobNumber = -1;
    
    // Estadísticas acumulativas del experimento completo
    private int totalIndividualsEvaluated = 0;
    private int totalCplexCalls = 0;
    private double totalCplexTimeUsed = 0.0;
    private double totalBudgetAssigned = 0.0;
    
    // Estadísticas por generación
    private Map<Integer, GenerationStats> generationStats = new HashMap<>();
    
    // Estadísticas de la evaluación actual
    private EvaluationStats currentEvaluation = null;

    private CplexUsageLogger() {}

    /**
     * Obtiene la instancia singleton del logger
     */
    public static synchronized CplexUsageLogger getInstance() {
        if (instance == null) {
            instance = new CplexUsageLogger();
        }
        return instance;
    }
    
    /**
     * Verifica si el logger está inicializado
     */
    public boolean isInitialized() {
        return detailedLog != null && summaryLog != null;
    }

    /**
     * Inicializa el logger para un experimento
     */
    public void initialize(String outputPath, int jobNumber, double budget) {
        try {
            this.outputPath = outputPath;
            this.jobNumber = jobNumber;
            this.totalBudgetAssigned = budget;
            
            // Crear directorio si no existe
            File dir = new File(outputPath);
            dir.mkdirs();
            
            // Abrir archivo de log detallado
            detailedLog = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(outputPath + "job." + jobNumber + ".CplexUsage.detailed.csv"), 
                    StandardCharsets.UTF_8
                )
            );
            
            // Escribir encabezado CSV del log detallado
            detailedLog.println("Timestamp,Generation,Individual,Instance,CallNumber,TimeLimit," +
                              "TimeUsed,BudgetUsed,BudgetRemaining,BudgetUtilization");
            detailedLog.flush();
            
            // Abrir archivo de log de resumen
            summaryLog = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(outputPath + "job." + jobNumber + ".CplexUsage.summary.csv"), 
                    StandardCharsets.UTF_8
                )
            );
            
            // Escribir encabezado CSV del log de resumen
            summaryLog.println("Generation,Individual,Instance,TotalCalls,TotalTimeUsed," +
                              "BudgetAssigned,BudgetUsed,BudgetUtilization,AvgTimePerCall," +
                              "MaxTimePerCall,MinTimePerCall");
            summaryLog.flush();
            
            System.out.println("CplexUsageLogger inicializado: " + outputPath);
            
        } catch (IOException e) {
            System.err.println("Error al inicializar CplexUsageLogger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicia el registro de una nueva evaluación de individuo
     */
    public synchronized void startEvaluation(int generation, String individualId, double budgetAssigned) {
        currentEvaluation = new EvaluationStats();
        currentEvaluation.generation = generation;
        currentEvaluation.individualId = individualId;
        currentEvaluation.budgetAssigned = budgetAssigned;
        currentEvaluation.startTime = System.currentTimeMillis();
    }

    /**
     * Inicia el registro de una nueva instancia dentro de la evaluación actual
     */
    public synchronized void startInstance(String instanceName) {
        if (currentEvaluation != null) {
            currentEvaluation.currentInstance = instanceName;
            currentEvaluation.instanceCallCount = 0;
            currentEvaluation.instanceTimeUsed = 0.0;
        }
    }

    /**
     * Registra una llamada a CPLEX
     */
    public synchronized void logCplexCall(double timeLimit, double timeUsed, 
                            double budgetUsed, double budgetRemaining) {
        if (currentEvaluation == null || detailedLog == null) {
            return;
        }
        
        currentEvaluation.totalCalls++;
        currentEvaluation.totalTimeUsed += timeUsed;
        currentEvaluation.instanceCallCount++;
        currentEvaluation.instanceTimeUsed += timeUsed;
        
        if (timeUsed > currentEvaluation.maxTimePerCall) {
            currentEvaluation.maxTimePerCall = timeUsed;
        }
        if (timeUsed < currentEvaluation.minTimePerCall) {
            currentEvaluation.minTimePerCall = timeUsed;
        }
        
        // Calcular utilización del presupuesto
        double utilization = (currentEvaluation.budgetAssigned > 0) ? 
            (budgetUsed / currentEvaluation.budgetAssigned) * 100.0 : 0.0;
        
        // Escribir en log detallado
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        detailedLog.println(String.format("%s,%d,%s,%s,%d,%.3f,%.3f,%.3f,%.3f,%.2f",
            timestamp,
            currentEvaluation.generation,
            currentEvaluation.individualId,
            currentEvaluation.currentInstance,
            currentEvaluation.instanceCallCount,
            timeLimit,
            timeUsed,
            budgetUsed,
            budgetRemaining,
            utilization
        ));
        detailedLog.flush();
    }

    /**
     * Finaliza el registro de la instancia actual
     */
    public synchronized void endInstance() {
        if (currentEvaluation != null && summaryLog != null) {
            // Calcular estadísticas de la instancia
            double avgTime = (currentEvaluation.instanceCallCount > 0) ? 
                currentEvaluation.instanceTimeUsed / currentEvaluation.instanceCallCount : 0.0;
            
            double budgetUsed = currentEvaluation.totalTimeUsed;
            double utilization = (currentEvaluation.budgetAssigned > 0) ? 
                (budgetUsed / currentEvaluation.budgetAssigned) * 100.0 : 0.0;
            
            // Escribir en log de resumen
            summaryLog.println(String.format("%d,%s,%s,%d,%.3f,%.3f,%.3f,%.2f,%.3f,%.3f,%.3f",
                currentEvaluation.generation,
                currentEvaluation.individualId,
                currentEvaluation.currentInstance,
                currentEvaluation.instanceCallCount,
                currentEvaluation.instanceTimeUsed,
                currentEvaluation.budgetAssigned,
                budgetUsed,
                utilization,
                avgTime,
                currentEvaluation.maxTimePerCall,
                (currentEvaluation.minTimePerCall == Double.MAX_VALUE ? 0.0 : currentEvaluation.minTimePerCall)
            ));
            summaryLog.flush();
        }
    }

    /**
     * Finaliza el registro de la evaluación actual
     */
    public synchronized void endEvaluation() {
        if (currentEvaluation == null) {
            return;
        }
        
        // Actualizar estadísticas globales
        totalIndividualsEvaluated++;
        totalCplexCalls += currentEvaluation.totalCalls;
        totalCplexTimeUsed += currentEvaluation.totalTimeUsed;
        
        // Actualizar estadísticas por generación
        int gen = currentEvaluation.generation;
        if (!generationStats.containsKey(gen)) {
            generationStats.put(gen, new GenerationStats());
        }
        GenerationStats genStats = generationStats.get(gen);
        genStats.individualsEvaluated++;
        genStats.totalCalls += currentEvaluation.totalCalls;
        genStats.totalTimeUsed += currentEvaluation.totalTimeUsed;
        
        currentEvaluation = null;
    }

    /**
     * Cierra los archivos de log y escribe estadísticas finales
     */
    public void close() {
        try {
            if (detailedLog != null) {
                detailedLog.close();
            }
            if (summaryLog != null) {
                summaryLog.close();
            }
            
            // Escribir estadísticas finales
            writeFinalStatistics();
            
        } catch (Exception e) {
            System.err.println("Error al cerrar CplexUsageLogger: " + e.getMessage());
        }
    }

    /**
     * Escribe las estadísticas finales del experimento
     */
    private void writeFinalStatistics() {
        try {
            PrintWriter statsWriter = new PrintWriter(
                new OutputStreamWriter(
                    new FileOutputStream(outputPath + "job." + jobNumber + ".CplexUsage.statistics.txt"), 
                    StandardCharsets.UTF_8
                )
            );
            
            statsWriter.println("================================================================");
            statsWriter.println("ESTADISTICAS DE USO DE CPLEX");
            statsWriter.println("Job: " + jobNumber);
            statsWriter.println("================================================================");
            statsWriter.println();
            
            statsWriter.println("ESTADISTICAS GLOBALES:");
            statsWriter.println("  Individuos evaluados:     " + totalIndividualsEvaluated);
            statsWriter.println("  Llamadas totales a CPLEX: " + totalCplexCalls);
            statsWriter.println("  Tiempo total usado:       " + String.format("%.3f", totalCplexTimeUsed) + " segundos");
            statsWriter.println("  Presupuesto asignado:     " + String.format("%.3f", totalBudgetAssigned) + " segundos/instancia");
            
            if (totalIndividualsEvaluated > 0) {
                double avgCallsPerInd = (double) totalCplexCalls / totalIndividualsEvaluated;
                double avgTimePerInd = totalCplexTimeUsed / totalIndividualsEvaluated;
                statsWriter.println("  Promedio llamadas/indiv:  " + String.format("%.2f", avgCallsPerInd));
                statsWriter.println("  Promedio tiempo/indiv:    " + String.format("%.3f", avgTimePerInd) + " segundos");
            }
            
            if (totalCplexCalls > 0) {
                double avgTimePerCall = totalCplexTimeUsed / totalCplexCalls;
                statsWriter.println("  Promedio tiempo/llamada:  " + String.format("%.3f", avgTimePerCall) + " segundos");
            }
            
            statsWriter.println();
            statsWriter.println("ESTADISTICAS POR GENERACION:");
            statsWriter.println(String.format("%-10s %-15s %-15s %-20s %-20s",
                "Gen", "Individuos", "Llamadas", "Tiempo Total (s)", "Prom. Tiempo/Indiv"));
            statsWriter.println("--------------------------------------------------------------------------------");
            
            List<Integer> sortedGens = new ArrayList<>(generationStats.keySet());
            Collections.sort(sortedGens);
            
            for (int gen : sortedGens) {
                GenerationStats stats = generationStats.get(gen);
                double avgTime = stats.individualsEvaluated > 0 ? 
                    stats.totalTimeUsed / stats.individualsEvaluated : 0.0;
                
                statsWriter.println(String.format("%-10d %-15d %-15d %-20.3f %-20.3f",
                    gen, stats.individualsEvaluated, stats.totalCalls, stats.totalTimeUsed, avgTime));
            }
            
            statsWriter.println();
            statsWriter.println("================================================================");
            
            statsWriter.close();
            
            System.out.println("Estadísticas de uso de CPLEX guardadas en: " + 
                             outputPath + "job." + jobNumber + ".CplexUsage.statistics.txt");
            
        } catch (IOException e) {
            System.err.println("Error al escribir estadísticas finales: " + e.getMessage());
        }
    }

    /**
     * Clase para almacenar estadísticas de una evaluación
     */
    private static class EvaluationStats {
        int generation;
        String individualId;
        String currentInstance;
        double budgetAssigned;
        long startTime;
        
        int totalCalls = 0;
        double totalTimeUsed = 0.0;
        
        int instanceCallCount = 0;
        double instanceTimeUsed = 0.0;
        
        double maxTimePerCall = 0.0;
        double minTimePerCall = Double.MAX_VALUE;
    }

    /**
     * Clase para almacenar estadísticas por generación
     */
    private static class GenerationStats {
        int individualsEvaluated = 0;
        int totalCalls = 0;
        double totalTimeUsed = 0.0;
    }
}

