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

    // ========== SISTEMA DE CONTROL DE PRESUPUESTO POR INSTANCIA ==========
    // NUEVO: Ahora el presupuesto es DINÁMICO por instancia, calculado como:
    // totalBudget[i] = budgetPercentage × T_base[i]
    // donde T_base[i] es el tiempo que CPLEX puro necesita para resolver la instancia i

    private static double totalBudget = 0.0;        // Presupuesto total para la instancia ACTUAL
    private static double usedBudget = 0.0;         // Acumulador del presupuesto usado en la instancia actual
    private static int callCount = 0;               // Contador de llamadas a CPLEX en la instancia actual
    private static String currentInstanceName = ""; // Nombre de la instancia actual para detectar cambios

    // Variables para estadísticas y logging
    private static double totalCplexTimeUsed = 0.0;     // Tiempo total de CPLEX usado en todas las instancias
    private static int totalCplexCalls = 0;              // Total de llamadas a CPLEX en todas las instancias
    private static double maxTimePerCall = 0.0;          // Tiempo máximo usado en una sola llamada
    private static double minTimePerCall = Double.MAX_VALUE; // Tiempo mínimo usado en una sola llamada

    // NUEVO: Modo de presupuesto
    // false = Modo legacy (presupuesto fijo global)
    // true = Modo dinámico (presupuesto por instancia usando InstanceBudgetManager)
    private static boolean useDynamicBudget = false;

    /**
     * Configura el presupuesto total de CPLEX desde los parámetros (MODO LEGACY)
     * Este método es llamado desde PDPProblemEvo.setup()
     * DEPRECATED: Usar enableDynamicBudget() para el diseño experimental correcto
     */
    public static void configureBudget(double budget) {
        totalBudget = budget;
        useDynamicBudget = false;
        System.out.println("CplexTerminal: Presupuesto fijo configurado = " + totalBudget + " segundos [LEGACY MODE]");

        // Resetear contadores globales
        resetGlobalStats();
    }

    /**
     * NUEVO: Habilita el modo de presupuesto dinámico por instancia
     * En este modo, el presupuesto se calcula automáticamente para cada instancia
     * usando el InstanceBudgetManager: presupuesto[i] = porcentaje × T_base[i]
     */
    public static void enableDynamicBudget() {
        useDynamicBudget = true;
        System.out.println("CplexTerminal: Modo de presupuesto DINÁMICO habilitado");
        System.out.println("  -> Presupuesto se calculará automáticamente por instancia");

        // Resetear contadores globales
        resetGlobalStats();
    }

    /**
     * Resetea el presupuesto para una nueva instancia
     * Debe llamarse al inicio de cada evaluación de instancia
     *
     * NUEVO: Si useDynamicBudget está habilitado, calcula el presupuesto
     * específico para esta instancia usando InstanceBudgetManager
     */
    public static void resetInstanceBudget(String instanceName) {
        usedBudget = 0.0;
        callCount = 0;
        currentInstanceName = instanceName;

        // NUEVO: Calcular presupuesto dinámico por instancia
        if (useDynamicBudget) {
            model.InstanceBudgetManager manager = model.InstanceBudgetManager.getInstance();
            if (manager.isInitialized()) {
                totalBudget = manager.getBudgetForInstance(instanceName);
                //System.out.println("CplexTerminal: Presupuesto para " + instanceName +
                //                 " = " + String.format("%.3f", totalBudget) + "s " +
                //                 "(T_base=" + String.format("%.3f", manager.getTBaseForInstance(instanceName)) + "s)");
            } else {
                totalBudget = 0.0;
                System.err.println("WARNING: InstanceBudgetManager no está inicializado, presupuesto = 0");
            }
        }

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
