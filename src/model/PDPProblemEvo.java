package model;

import ec.*;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.GPTree;
import ec.simple.SimpleProblemForm;
import ec.util.Code;
import ec.util.Parameter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;

public class PDPProblemEvo extends GPProblem implements SimpleProblemForm {
    public static int indice_lista;
    private static final long serialVersionUID = -8430160211244271537L;
    public float MaxFitness=999;
    public long sizeBestFitness = 9999;
    public static int RESULTS_FILE;
    public static int DOT_FILE;
    public static int JOB_NUMBER;
    public static String RESULTS_namefile = "MISPResults.out";
    public static String DOT_namefile = "BestIndividual.dot";
    public static String IN_namefile = "BestIndividual.in";

    public static long startGenerationTime;
    public static long endGenerationTime;

    public static final double IND_MAX_REL_ERR = 0.001;
    public static final double IND_IDEAL_NODES = 21;
    public static final double POND_ERP = 1.0;
    public static final double POND_ERL = 0.0;//1-POND_ERP;
    public static double bestProfit;

    public static String semillas;
    //public static int cantidad_intancias_coevo = 6;//Co-evolución
    public static String Outputpath = "out/results/evolution";
    public static String Instacespath =  "data/evolution";
    public static String dotexepath = "graphviz-2.38/bin/dot.exe";
    public static String statfile = "Statistics.out";
    public static String statfileCSV = "BestFitness.csv";

    public static int elites;//cantadidad de individuos elites que pasan a la siguiente generacion
    //public static boolean elitismo = true;//variable que indica si hay elitismo
    public static ArrayList<Generacion> Corrida;//Estructura que almacena todos los individuos de todas las generaciones
    ArrayList<PDPData> data;//Estructura que almacena las instancias

    @Override
    public PDPProblemEvo clone() {
        PDPProblemEvo pdpp = (PDPProblemEvo) super.clone();
        return pdpp;
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        // very important, remember this
        JOB_NUMBER = ((Integer)(state.job[0])).intValue();
        super.setup(state, base);
        // verify our input is the right class (or subclasses from it)
        if (!(input instanceof PDPData))
            state.output.fatal("GPData class must subclass from " + PDPData.class, base.push(P_DATA), null);

        //Cantidad de individuos elites que pasan a la siguiente generación
        elites=  state.parameters.getInt(new ec.util.Parameter("breed.elite.0"),null);
        //Se guardan las  semillas...¿como?
        semillas=  state.parameters.getString(new ec.util.Parameter("seed.0"),null);

        //Se Lee la instancia desde archivo
        System.out.println("Obteniendo instancias de prueba...");
        try {
            File evofolder = new File(Outputpath+PDPProblemEvo.JOB_NUMBER+"/");//carpeta de archivos de la evolución
            evofolder.mkdir();
            RESULTS_FILE = FileIO.newLog(state.output, Outputpath+PDPProblemEvo.JOB_NUMBER+"/"+RESULTS_namefile);
            DOT_FILE = FileIO.newLog(state.output, Outputpath+PDPProblemEvo.JOB_NUMBER+"/job."+PDPProblemEvo.JOB_NUMBER+"."+DOT_namefile);
            data = new ArrayList<PDPData>();//DATA
            FileIO.readInstances(data, Instacespath);
        } catch (Exception e) {	e.printStackTrace();}
        System.out.println("Lectura de los "+ data.size() +" archivo terminada con éxito!");
        //Se actualiza y setea el resto de las variables
        System.out.println("Actualizando estructuras variables y fijas......");
        Setear_Instancias();
        Inicializa_Corrida(state.numGenerations);//estructura que alamacena los individuos
        
        // Leer y configurar el porcentaje del baseline para CPLEX desde los parámetros
        // El valor debe ser un porcentaje (ej: 0.10 para 10%, 0.25 para 25%, etc.)
        double cplexBudgetPercentage = state.parameters.getDoubleWithDefault(
            new Parameter("gp.fs.0.func.6.cplex-budget"), null, 0.0);
        terminals.CplexTerminal.configureBudgetPercentage(cplexBudgetPercentage);
        
        // Inicializar el logger de uso de CPLEX solo si hay presupuesto (porcentaje > 0)
        // Nota: El logger recibirá el porcentaje, pero el presupuesto real se calcula por instancia
        if (cplexBudgetPercentage > 0) {
            CplexUsageLogger.getInstance().initialize(
                Outputpath + PDPProblemEvo.JOB_NUMBER + "/", 
                PDPProblemEvo.JOB_NUMBER, 
                cplexBudgetPercentage  // Guardamos el porcentaje, no el presupuesto fijo
            );
        }
        
        System.out.println("MISProblem: Evolucionando...");
        startGenerationTime = System.nanoTime();	//inicio cronómetro evolución
    }
    //Evalúa a un individuo
    @Override
    public void evaluate(
            final EvolutionState state,
            final Individual individual,
            final int subpopulation,
            final int threadnum) {


        if (!individual.evaluated) {

            ArrayList<PDPData> auxData = new ArrayList<PDPData>();

            GPIndividual gpind = (GPIndividual) individual;

            // Iniciar registro de evaluación en el logger de CPLEX
            // Nota: Pasamos el porcentaje del baseline, no el presupuesto calculado
            if (CplexUsageLogger.getInstance().isInitialized()) {
                CplexUsageLogger.getInstance().startEvaluation(
                    state.generation, 
                    gpind.toString(), 
                    terminals.CplexTerminal.getBudgetPercentage()
                );
            }

            int hits = 0;
            double relErrAcum = 0.0;
            long timeAcum = 0;
            double instanceRelativeError = 0.0;
            int alturaArbol = gpind.trees[0].child.depth();//altura del arbol
            long tamanoArbol = gpind.size();//tamaño del arbol, numero de nodos
            double currentProfit = 0.0;
            //int avance_de_instancias = (state.generation)/50;// Co-evolución, a las 10 cambia 0, a las 50 cambia 1, a las 100 cambia 2,.... a las 300 cambia 6, a las 350 cambia 7, a las 400 cambia 8...
            //System.out.println("Avance: "+avance_de_instancias);// Co-evolución
            int cantidad_intancias = data.size();
            //cantidad_intancias= cantidad_intancias_coevo; // Co-evolución, subconjunto de instancias
            //int avance_en_rangos_de_cant = avance_de_instancias%(cantidad_intancias+1);// Co-evolucion, nos dice que instancia debe entrar entre [1-6] ===> 3%7=3, 6%7=6, 7%7=0, 8%7=1,13%7=6, 14%7=0,
            //double ERL = Relative_Error_Legibility(tamañoArbol);//Error Relativo de Legibilidad
            double ERL = 0;
            //INICIO Evaluación del algoritmo
            for(int i = 0; i < cantidad_intancias; i++) {
                auxData.add(data.get(i).clone()); //el individuo se enfrenta a una instancia limpia
                //auxData.add(data.get(i+avance_en_rangos_de_cant).clone());	//Co-evolución, se clona la instancia original para evaluar al individuo con una instancia limpia, sin modificaciones

                // Resetear el presupuesto de CPLEX para esta instancia
                String instanceName = auxData.get(i).getInstance().getName();
                terminals.CplexTerminal.resetInstanceBudget(instanceName);
                
                // Registrar inicio de instancia en el logger
                if (CplexUsageLogger.getInstance().isInitialized()) {
                    CplexUsageLogger.getInstance().startInstance(instanceName);
                }

                gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;

                long timeInit, timeEnd;
                timeInit = System.nanoTime();	//inicio cronometro
                //Se evalua al individuo con la instancia i
                gpind.trees[0].child.eval(state, threadnum, auxData.get(i), stack, gpind, this);
                //fase reparatoria
                PDPInstance pdpi = auxData.get(i).getPDPInstance();
//                while(pdpi.getNotVisited().size()>0){
//                    System.out.println("fase reparatoria");
//                    pdpi.nearest_insertion();
//                }
                timeEnd = System.nanoTime();	//fin cronometro
                double duracion = (timeEnd - timeInit) / 1000000;//en milisegundos
                PDPInstance pdp = auxData.get(i).getPDPInstance();

                currentProfit = auxData.get(i).getPDPInstance().getTotalCost() + pdp.getNotVisited().size()*pdp.getMaxCost();//solución obtenida

                //double
                bestProfit = Best_Profit(auxData.get(i).getInstance());//Optimo o Mejor solución conocida
                instanceRelativeError = Relative_Error(auxData.get(i).getInstance());//Error Relativo



                double err = bestProfit - currentProfit;
                //System.out.println(i + " profits " + currentProfit + " " + bestProfit + " " + err);
                //System.out.println(auxData.get(i).getPDPInstance().getRoutes());
                //System.out.println(auxData.get(i).getPDPInstance().getNotVisited());

                //Hits
                if(err == 0){ hits++; //instanceRelativeError < IND_MAX_REL_ERR ||
                    //System.out.println("hit profit: "+ currentProfit + " i: " + i);
                    //break;
                }
                //Time
                //if((duracion/1000)>3) {	instanceRelativeError=1;	}//penalizacion por tiempo excesivo, en casos de prueba

                //[Generation] [Individual ID] [Exec Time] [Profit] [Optimal] [Rel Error Optimus] [Depth] [Tree Size] [Max Nodes] [Rel Error Nodes] [Fitness] [Hits]
                state.output.print(state.generation + " " + gpind + " ", RESULTS_FILE);
                state.output.print(duracion + " ", RESULTS_FILE);
                state.output.print(currentProfit + " " + bestProfit + " " + instanceRelativeError + " ", RESULTS_FILE);
                state.output.print(alturaArbol + " " + tamanoArbol + " " + IND_IDEAL_NODES + " " + ERL + " ", RESULTS_FILE);
                state.output.println(( POND_ERP*instanceRelativeError + POND_ERL*ERL+ " " + hits), RESULTS_FILE);
                // aqui voy a guardar solo los hits

                relErrAcum += instanceRelativeError;//acumulador del error relativo
                timeAcum+=duracion;//acumulador del tiempo de evaluacion
                
                // Finalizar registro de instancia en el logger
                if (CplexUsageLogger.getInstance().isInitialized()) {
                    CplexUsageLogger.getInstance().endInstance();
                }
            }

            //FIN Evaluación del algoritmo con todas las intancias

            //Runtime garbage = Runtime.getRuntime();
            //garbage.gc();

            double ERP = relErrAcum / auxData.size();//Error Relativo Promedio
            ec.gp.koza.KozaFitness f = ((ec.gp.koza.KozaFitness) gpind.fitness);
            double errorhits = ((cantidad_intancias - hits));
            double errorhits2 = errorhits/cantidad_intancias;
            float Fitness = (float) ((POND_ERP*ERP) +(POND_ERL*errorhits2));; //((POND_ERP*ERP) +
            f.setStandardizedFitness(state,Fitness);
            f.hits = hits;
            //gpind.evaluated = true;//Co-evolución, todo se deben volver a evaluar, para enfrentarlos a las nuevas instancias
            gpind.evaluated = true; // siempre y cuando las instancias sea fijas
            float AdjustedFitness = (float) (1.0/(1.0 + Fitness));
            long tiempoPromedio = (timeAcum/auxData.size());
            int gen = state.generation;
            String id = gpind.toString();
            //System.out.println(gen+", "+id+", "+AdjustedFitness+", "+Fitness+", "+ERP+", "+ERL+", "+hits+", "+tiempoPromedio+", "+tamañoArbol+", "+alturaArbol);
            if(Fitness<MaxFitness || (Fitness<=MaxFitness && tamanoArbol<sizeBestFitness)) {
                MaxFitness=Fitness;
                sizeBestFitness = tamanoArbol;
                //System.out.println(gen+" - "+Fitness+" - "+ hits+" - "+AdjustedFitness+" - "+tiempoPromedio+" - "+tamañoArbol+" - "+alturaArbol );
            }
            AgregarIndv(gen,id,AdjustedFitness,Fitness,ERP,ERL, hits,tiempoPromedio,tamanoArbol,alturaArbol);
            
            // Finalizar registro de evaluación en el logger
            if (CplexUsageLogger.getInstance().isInitialized()) {
                CplexUsageLogger.getInstance().endEvaluation();
            }
        }

    }

    @Override
    public void describe(final EvolutionState state,
                         final Individual individual,
                         final int subpopulation,
                         final int threadnum,
                         final int log) {
        endGenerationTime = System.nanoTime();	//fin cronometro evolución
        long duracion = (endGenerationTime - startGenerationTime) / 1000000;
        state.output.message("Evolution duration: " + duracion + " ms");	//duración evolución en ms
        state.output.print(duracion+ "", RESULTS_FILE);
        System.out.println("Duración: "+(duracion/(1000*60))+" minutos");

        //Print BestIndividual.in
        PrintWriter dataOutput = null;
        Charset charset = StandardCharsets.UTF_8;
        try {
            dataOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Outputpath+PDPProblemEvo.JOB_NUMBER+"/job." + (PDPProblemEvo.JOB_NUMBER) + "."+IN_namefile), charset)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dataOutput.println(Population.NUM_SUBPOPS_PREAMBLE + Code.encode(1));
        dataOutput.println(Population.SUBPOP_INDEX_PREAMBLE + Code.encode(0));
        dataOutput.println(Subpopulation.NUM_INDIVIDUALS_PREAMBLE + Code.encode(1));
        dataOutput.println(Subpopulation.INDIVIDUAL_INDEX_PREAMBLE + Code.encode(0));
        individual.evaluated = false;
        individual.printIndividual(state, dataOutput);
        dataOutput.close();

        //Print BestIndividual.dot
        GPIndividual gpind = (GPIndividual) individual;
        gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;
        String indid = gpind.toString().substring(19);
        state.output.println("label=\"Individual=" + indid + " Fitness=" + ((ec.gp.koza.KozaFitness) gpind.fitness).standardizedFitness() + " Hits=" + ((ec.gp.koza.KozaFitness) gpind.fitness).hits + " Size=" + gpind.size() + " Depth=" + gpind.trees[0].child.depth() + "\";", DOT_FILE);
        gpind.printIndividualForHumans(state, DOT_FILE);

        String path = Outputpath+PDPProblemEvo.JOB_NUMBER+"/job."+PDPProblemEvo.JOB_NUMBER+".";
        try {
            elite();

            FileIO.Stat_a_csv(path,statfile,statfileCSV);
            FileIO.Estadistica_Promedio_y_Mejores(path,Corrida,"EstadisticaProm&Mej.csv");
            FileIO.Estadistica_Todos(path,Corrida,"Estadistica_Todos.csv");//agregar tiempo
            var threadSeeds = Evolve.V_SEED_TIME;
            System.out.println("Seeds: "+threadSeeds+" "+Evolve.P_SEED);
            FileIO.GuardarSemillas(path,semillas,state.evalthreads,state.breedthreads,"Semillas.csv");//incompleta
            
            // Reparar el formato del archivo .dot (mover label dentro del digraph)
            FileIO.repairDot(path,DOT_namefile);
            
            System.out.println("");
            System.out.println("==============================================");
            System.out.println("NOTA: Para generar las imagenes PNG, ejecuta:");
            System.out.println("  convert_dots_to_png.bat");
            System.out.println("==============================================");
            
            // Cerrar el logger de uso de CPLEX
            if (CplexUsageLogger.getInstance().isInitialized()) {
                CplexUsageLogger.getInstance().close();
            }
            
        } catch (Exception e) {e.printStackTrace();}
    }
    public static double Relative_Error_Legibility(long tam){//El error depende de que tan alejado esta del tamaño ideal, sea mayor o menor
        double error_leg = Math.abs(tam - IND_IDEAL_NODES) / IND_IDEAL_NODES;
        return error_leg;
    }

    public static double Relative_Error(Instance inst){//Retorna Error Relativo, de un individuo
        double error_rel = 0;
        boolean opt = inst.isOptimalKnown();
        double currentProfit = inst.getPdpInstance().getTotalCost();
        if(opt){
            double optimo = inst.getOptimal();
            if(optimo == 0.0) {
                System.out.println(" Optimo = 0 " + inst.getName());
                error_rel = currentProfit;
                //System.out.println("optimo: "+optimo + " - currentProfit" + currentProfit);
            }
            else {
                error_rel = Math.abs((optimo - currentProfit)/(optimo));//error normalizado, porcentaje de error //abs

            }
        }else{
            System.out.println("FEASIBLE INSTANCE, DELETE");
            double feasible = inst.getFeasible();
            if(feasible == 0.0) {
                error_rel = currentProfit;
                //System.out.println("feasible: "+feasible + " / currentProfit" + currentProfit);
            }
            else {
                error_rel = Math.abs((feasible - currentProfit)/(feasible));//error normalizado, porcentaje de error
            }
        }
        return error_rel;
    }
    public static double Best_Profit(Instance inst){//Retorna el Optimo o Mejor solución concida
        boolean opt = inst.isOptimalKnown();
        if(opt)
            return inst.getOptimal();
        else
            return inst.getFeasible();
    }


    private void Setear_Instancias(){
        PrintWriter dataOutput = null;
        Charset charset = StandardCharsets.UTF_8;
        try {
            dataOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Outputpath+PDPProblemEvo.JOB_NUMBER+"/job." + (PDPProblemEvo.JOB_NUMBER) + "."+"listas.txt"), charset)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for(int i=0; i<data.size();i++){
            dataOutput.println(data.get(i).getInstance().getName() + Code.encode(1));
            dataOutput.println(data.get(i).getPDPInstance().getTotalCost() + " / " +data.get(i).getPDPInstance().getRoutes());
            //dataOutput.println(data.get(i).getIndependenSet().getSol().subList(0, data.get(i).getIndependenSet().gettam_sol())+" / "+data.get(i).getIndependenSet().getSol().subList(data.get(i).getIndependenSet().gettam_sol(),(data.get(i).getIndependenSet().gettam_sol()+data.get(i).getIndependenSet().gettam_free()))+" - "+data.get(i).getIndependenSet().getSol().subList(data.get(i).getIndependenSet().gettam_sol()+data.get(i).getIndependenSet().gettam_free(), data.get(i).getIndependenSet().getnVert()));
        }
        dataOutput.close();
    }
    private void Inicializa_Corrida(int numgen){
        Corrida = new ArrayList<Generacion>();
        for(int i=0;i<numgen;i++){
            Corrida.add(new Generacion());
        }
    }
    private void AgregarIndv(int gen, String id,float adjs,float stand, double erp, double erl, int hits,long time,long nodos,int altura){
        // Verificar que el índice esté dentro de los límites
        if (gen >= 0 && gen < Corrida.size()) {
            Corrida.get(gen).AgregarIndv(gen,id,adjs,stand,erp,erl, hits,time,nodos,altura);
        } else {
            System.err.println("Warning: Generation index " + gen + " is out of bounds (Corrida size: " + Corrida.size() + ")");
        }
    }
    private void elite(){//agrega el mejor de cada generación a la siguiente generación, solo uno
        if(elites>0){
            //if(elitismo){
            int c = Corrida.size()-1;
            Individuo best;
            for(int i=0;i<c;i++){
                //System.out.print(i + " - ");
                best = Corrida.get(i).TheBestEvo();
                Corrida.get(i+1).AgregarIndv(best);
            }
        }
    }
}


