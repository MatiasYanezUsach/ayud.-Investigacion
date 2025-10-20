package terminals;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import model.PDPData;
import model.Terminal;

public class CplexTerminal extends GPNode {

    private static final long serialVersionUID = -2534468795456857335L;

    // ========== SISTEMA DE CONTROL DE PRESUPUESTO ==========
    // Variables estáticas para control de presupuesto de CPLEX por instancia
    private static double totalBudget = 0.0;        // Presupuesto total de CPLEX en segundos (configurable)
    private static double usedBudget = 0.0;         // Acumulador del presupuesto usado en la instancia actual
    private static int callCount = 0;               // Contador de llamadas a CPLEX en la instancia actual
    private static String currentInstanceName = ""; // Nombre de la instancia actual para detectar cambios
    
    // Variables para estadísticas y logging
    private static double totalCplexTimeUsed = 0.0;     // Tiempo total de CPLEX usado en todas las instancias
    private static int totalCplexCalls = 0;              // Total de llamadas a CPLEX en todas las instancias
    private static double maxTimePerCall = 0.0;          // Tiempo máximo usado en una sola llamada
    private static double minTimePerCall = Double.MAX_VALUE; // Tiempo mínimo usado en una sola llamada

    /**
     * Configura el presupuesto total de CPLEX desde los parámetros
     * Este método es llamado desde PDPProblemEvo.setup()
     */
    public static void configureBudget(double budget) {
        totalBudget = budget;
        System.out.println("CplexTerminal: Presupuesto total configurado = " + totalBudget + " segundos");
        
        // Resetear contadores globales
        resetGlobalStats();
    }

    /**
     * Resetea el presupuesto para una nueva instancia
     * Debe llamarse al inicio de cada evaluación de instancia
     */
    public static void resetInstanceBudget(String instanceName) {
        usedBudget = 0.0;
        callCount = 0;
        currentInstanceName = instanceName;
        //System.out.println("CplexTerminal: Presupuesto reseteado para instancia: " + instanceName);
    }

    /**
     * Resetea las estadísticas globales
     */
    public static void resetGlobalStats() {
        totalCplexTimeUsed = 0.0;
        totalCplexCalls = 0;
        maxTimePerCall = 0.0;
        minTimePerCall = Double.MAX_VALUE;
    }

    /**
     * Obtiene el presupuesto restante para la instancia actual
     */
    public static double getRemainingBudget() {
        return Math.max(0.0, totalBudget - usedBudget);
    }

    /**
     * Calcula el límite de tiempo para la próxima llamada
     * Estrategia: Distribuir el presupuesto restante entre las llamadas restantes
     */
    private static double calculateTimeLimit() {
        double remainingBudget = getRemainingBudget();
        
        if (remainingBudget <= 0.0) {
            return 0.0; // Sin presupuesto disponible
        }
        
        // Estrategia de distribución:
        // Limitar cada llamada a un máximo del 40% del presupuesto restante
        // Esto permite múltiples llamadas estratégicas
        double limit = Math.min(remainingBudget, remainingBudget * 0.4);
        
        return limit;
    }

    /**
     * Registra el tiempo usado en una llamada a CPLEX
     */
    public static void registerTimeUsed(double timeUsedInSeconds) {
        usedBudget += timeUsedInSeconds;
        callCount++;
        totalCplexCalls++;
        totalCplexTimeUsed += timeUsedInSeconds;
        
        // Actualizar estadísticas
        if (timeUsedInSeconds > maxTimePerCall) {
            maxTimePerCall = timeUsedInSeconds;
        }
        if (timeUsedInSeconds < minTimePerCall) {
            minTimePerCall = timeUsedInSeconds;
        }
    }

    /**
     * Obtiene estadísticas de uso de CPLEX para logging
     */
    public static String getUsageStats() {
        return String.format(
            "CPLEX Stats - Total Budget: %.2fs, Used: %.2fs (%.1f%%), Calls: %d, Avg/call: %.2fs, Max/call: %.2fs, Min/call: %.2fs",
            totalBudget, usedBudget, (totalBudget > 0 ? (usedBudget / totalBudget) * 100 : 0),
            callCount, (callCount > 0 ? usedBudget / callCount : 0),
            maxTimePerCall, (minTimePerCall == Double.MAX_VALUE ? 0 : minTimePerCall)
        );
    }

    /**
     * Obtiene estadísticas globales de uso de CPLEX
     */
    public static String getGlobalUsageStats() {
        return String.format(
            "CPLEX Global Stats - Total Time Used: %.2fs, Total Calls: %d, Avg/call: %.2fs",
            totalCplexTimeUsed, totalCplexCalls, 
            (totalCplexCalls > 0 ? totalCplexTimeUsed / totalCplexCalls : 0)
        );
    }

    // Getters para estadísticas (útiles para análisis posterior)
    public static double getTotalBudget() { return totalBudget; }
    public static double getUsedBudget() { return usedBudget; }
    public static int getCallCount() { return callCount; }
    public static double getTotalCplexTimeUsed() { return totalCplexTimeUsed; }
    public static int getTotalCplexCalls() { return totalCplexCalls; }

    // ========== FIN SISTEMA DE CONTROL DE PRESUPUESTO ==========

    public String toString() { return "CplexTerminal"; }

    public void checkConstraints(
            final EvolutionState state, final int tree,
            final GPIndividual typicalIndividual, final Parameter individualBase) {
        super.checkConstraints(state, tree, typicalIndividual, individualBase);

        if (children.length != 0) {
            state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase);
        }
    }

    @Override
    public void eval(final EvolutionState state, final int thread,
                     final GPData input, final ADFStack stack,
                     final GPIndividual individual, final Problem problem) {

        PDPData pdpd = (PDPData) input;
        
        // Calcular el límite de tiempo para esta llamada
        double timeLimit = calculateTimeLimit();
        
        if (timeLimit <= 0.0) {
            // Sin presupuesto disponible, no ejecutar CPLEX
            pdpd.setResult(false);
            return;
        }
        
        // Ejecutar CPLEX con el límite calculado
        double timeUsed = Terminal.cplex_terminal_with_limit(
            pdpd.getInstance().getPdpInstance(), timeLimit);
        
        // Registrar el tiempo usado
        registerTimeUsed(timeUsed);
        
        // Registrar en el logger solo si está inicializado
        if (model.CplexUsageLogger.getInstance().isInitialized()) {
            model.CplexUsageLogger.getInstance().logCplexCall(
                timeLimit, 
                timeUsed, 
                usedBudget, 
                getRemainingBudget()
            );
        }
        
        // El resultado ya fue establecido por cplex_terminal_with_limit
        pdpd.setResult(timeUsed > 0);
    }
}
