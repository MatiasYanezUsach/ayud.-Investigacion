package model;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.GPTree;
import ec.gp.koza.KozaFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Code;
import ec.util.Parameter;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.util.Precision;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PDPProblemEva extends GPProblem implements SimpleProblemForm {

	private static final long serialVersionUID = -8430160211244271537L;

	public static int RESULTS_FILE;
	public static int DOT_FILE;
	public static int JOB_NUMBER;
	public static int LOG_FILE;
	public static long startGenerationTime;
	public static long endGenerationTime;
	public static final double IND_MAX_REL_ERR = 0.001;

	public static String Outputpath = "out/results/evaluation/";
	public static String Instacespath =  "data/evaluation";
	public static String resumenfile =  "Resumen_BestIndvividual.csv";
	public static String BestIndividualList =  "List"+PDPProblemEvo.IN_namefile;// Archivo en el cual se reunen los mejores de cada ejecucinn
	
	
	
	ArrayList<GPIndividual> Alg_List= new ArrayList<GPIndividual>();
	public static Generacion Corrida_Eva = new Generacion();
	ArrayList<PDPData> data;
	
	
	
	@Override
	public PDPProblemEva clone() {
		PDPProblemEva mispp = (PDPProblemEva) super.clone();
		return mispp;
	}

	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		// very important, remember this
		JOB_NUMBER = ((Integer)(state.job[0])).intValue();
		super.setup(state, base);
		// verify our input is the right class (or subclasses from it)
		if (!(input instanceof PDPData))
			state.output.fatal("GPData class must subclass from " + PDPData.class, base.push(P_DATA), null);
		
		try {
			LOG_FILE = FileIO.newLog(state.output, "out/results/MISPlog.out");
			File evafolder = new File(Outputpath);
			evafolder.mkdir();
			//Leemos los mejores de cada corrida y los unimos en un unico archivo de poblacinn
			System.out.println("Obteniendo estadisticas y algoritmos de cada job...");
			String inpath = PDPProblemEvo.Outputpath;
			String outpath = Outputpath+"evaluatedjobs/";
			FileIO.join_thebest(inpath,outpath,PDPProblemEvo.IN_namefile,BestIndividualList);//une los mejores arboles en un archivo .in de cada corrida
			FileIO.join_StatsCSV(inpath,outpath,PDPProblemEvo.statfileCSV);//une los archivos csv de los mejores de cada generacinn de cada corrida
			FileIO.join_Fitness_BestAvg(inpath, outpath, "EstadisticaProm&Mej.csv","Prom&Mej_Fitness.csv");
			FileIO.join_Tamano_BestAvg(inpath, outpath, "EstadisticaProm&Mej.csv","Prom&Mej_Tamano.csv");
			FileIO.join_ERP_BestAvg(inpath, outpath, "EstadisticaProm&Mej.csv","Prom&Mej_ERP.csv");
			FileIO.join_ERL_BestAvg(inpath, outpath, "EstadisticaProm&Mej.csv","Prom&Mej_ERL.csv");
			FileIO.join_Estadistica_Todos(inpath,outpath,"Estadistica_Todos.csv");
			//FileIO.join_ResumenFitness(inpath,outpath,"EstadisticaResumen.csv");
			//FileIO.join_Estadistica_Mejores(inpath,outpath,"Estadistica_Mejores.csv");//une los archivos de las estadisticas de los mejores de cada generacinn de cada corrida
			//FileIO.join_Estadistica_Promedio_y_Mejores(inpath,outpath,"EstadisticaProm&Mej.csv");
			FileIO.logEvo(state,LOG_FILE);			

			RESULTS_FILE = FileIO.newLog(state.output, Outputpath+PDPProblemEvo.RESULTS_namefile);
			DOT_FILE = FileIO.newLog(state.output, Outputpath+PDPProblemEvo.DOT_namefile);

			//Leemos la instancia desde archivo
			System.out.println("Obteniendo instancias de prueba...");
			data = new ArrayList<PDPData>();//DATA
			FileIO.readInstances(data, Instacespath);
			System.out.println("Lectura desde archivo terminada con nxito!");
		} catch (Exception e) {	e.printStackTrace();}
		
		//Actualizamos y setiamos el resto de las variables
		System.out.println("Actualizando estructuras variable y fijas......");
		/*for(int i=0; i<data.size();i++){
			data.get(i).getPDPInstance().UpdateIS_2();//Actualiza y setea las estructuras fijas y variables
		}*/
		System.out.println("MISProblem: Evolucionando...");
		startGenerationTime = System.nanoTime();	//inicio cronnmetro evolucinn
		state.output.println("========================================", LOG_FILE);
		state.output.println("EVALUACInN",LOG_FILE);
		state.output.println("Instancias de Evaluacinn = " + data.size() , LOG_FILE);
		state.output.println("------------------------------", LOG_FILE);
	}

	//Evalna a un individuo
	@Override
	public void evaluate(
			final EvolutionState state,
			final Individual individual,
			final int subpopulation,
			final int threadnum) {

		System.out.println("EVA evaluate");

		if (!individual.evaluated) {
			
			ArrayList<PDPData> auxData = new ArrayList<>();
			GPIndividual gpind = (GPIndividual) individual;
			Alg_List.add(gpind);//IMPORTANTE: para ubicarlo mas tarde
			
			//state.output.println("Generacinn:" + state.generation, LOG_FILE);
			state.output.println("["+(Alg_List.size()-1)+"]Individuo:[" + gpind + "]", LOG_FILE);
			//state.output.println("---- Iniciando evaluacinn ---", LOG_FILE);
			int hits = 0;
			double relErrAcum = 0.0;
			double timeAcum = 0;
			double currentProfit = 0.0; /////////////
			double instanceRelativeError = 0.0;
			int alturaArbol = gpind.trees[0].child.depth();//altura del arbol
			long tamanoArbol = gpind.size();
			double ERL = PDPProblemEvo.Relative_Error_Legibility(tamanoArbol);//Error Relativo de Legibilidad
			List<ArrayList<Integer>> NodosSolucion = new ArrayList<>();
			ArrayList<String> ListaHits = new ArrayList<String>();
			//INICIO Evaluacinn del algoritmo
			List<Double> times = new ArrayList<>();
			for(int i = 0; i < data.size(); i++) {
				NodosSolucion.clear();
				
				auxData.add(data.get(i).clone());	//nuevo data (vaciar mis)
				
				// Resetear el presupuesto de CPLEX para esta instancia
				terminals.CplexTerminal.resetInstanceBudget(auxData.get(i).getInstance().getName());
				
				gpind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;	//escribir individuos en formato dot

				double timeInit, timeEnd;
				timeInit = System.nanoTime();	//inicio cronometro
				//Se evalua al individuo con la instancia i
				gpind.trees[0].child.eval(state, threadnum, auxData.get(i), stack, gpind, this);	//evaluar el individuo gpind para la instancia i
				timeEnd = System.nanoTime();	//fin cronometro
				PDPInstance pdp = auxData.get(i).getPDPInstance();

				currentProfit = auxData.get(i).getPDPInstance().getTotalCost() + pdp.getNotVisited().size()*pdp.getMaxCost();//solucinn obtenida
				double bestProfit = PDPProblemEvo.Best_Profit(auxData.get(i).getInstance());//Obtiene el nptimo o la mejor solucinn conocida
				instanceRelativeError = PDPProblemEvo.Relative_Error(auxData.get(i).getInstance());//Error Relativo
				double err = bestProfit - currentProfit;
				List<ArrayList<Integer>> currentSol = auxData.get(i).getPDPInstance().getSol(); //solucinn obtenida
				/*for(int u=0; u<currentProfit; u++) {
					NodosSolucion.add(currentSol.get(u)+1);
				}*/
				NodosSolucion = currentSol;
				//System.out.print(auxData.get(i).instance.getName()+": ");
				//System.out.println(currentProfit);
				//Hits
				//if(instanceRelativeError==Infinity) {instanceRelativeError = MISProblemEvo.Relative_Error(auxData.get(i).getInstance());}
				if(err == 0) {hits++; //instanceRelativeError < IND_MAX_REL_ERR ||
					ListaHits.add(auxData.get(i).getInstance().getName());
				}
				//Time
				double duracion = (timeEnd - timeInit) / 1000000;//en milisegundos
				//[Generation] [Individual ID] [Exec Time] [Profit] [Optimal] [Rel Error Optimus] [Depth] [Tree Size] [Max Nodes] [Rel Error Nodes] [Fitness] [Hits]
				state.output.print("generation= " + state.generation + " individuo= " + gpind + " ", RESULTS_FILE);
				state.output.print(" duracion= " + duracion + " ", RESULTS_FILE);
				state.output.print(" currentProfit= "+currentProfit + " bestProfit= " + bestProfit + " " + " instanceRelativeError= "+instanceRelativeError + " ", RESULTS_FILE);
				state.output.print(" alturaArbol= "+ alturaArbol + " tamanoArbol= " + tamanoArbol + " IDEAL_NODES= " + PDPProblemEvo.IND_IDEAL_NODES + " ERL= " + ERL + " ", RESULTS_FILE);
				state.output.println((" hits= " + hits), RESULTS_FILE);
				state.output.println((" NodosSolucion.size= "+NodosSolucion.size()+" NodosSolucion= " +NodosSolucion), RESULTS_FILE);
				//state.output.println(auxData.get(i).getIndependenSet().getSol().subList(0, auxData.get(i).getIndependenSet().gettam_sol())+" / "+auxData.get(i).getIndependenSet().getSol().subList(auxData.get(i).getIndependenSet().gettam_sol(),(auxData.get(i).getIndependenSet().gettam_sol()+auxData.get(i).getIndependenSet().gettam_free()))+" - "+auxData.get(i).getIndependenSet().getSol().subList(auxData.get(i).getIndependenSet().gettam_sol()+auxData.get(i).getIndependenSet().gettam_free(), auxData.get(i).getIndependenSet().getnVert()), RESULTS_FILE);
				relErrAcum += instanceRelativeError;
				timeAcum += duracion;
				times.add(duracion);
				//System.out.println(auxData.get(i).getIndependenSet().getSol().subList(0, auxData.get(i).getIndependenSet().gettam_sol())+" / "+auxData.get(i).getIndependenSet().getSol().subList(auxData.get(i).getIndependenSet().gettam_sol(),(auxData.get(i).getIndependenSet().gettam_sol()+auxData.get(i).getIndependenSet().gettam_free()))+" - "+auxData.get(i).getIndependenSet().getSol().subList(auxData.get(i).getIndependenSet().gettam_sol()+auxData.get(i).getIndependenSet().gettam_free(), auxData.get(i).getIndependenSet().getnVert()));
				System.out.println(auxData.get(i).getInstance().getName()+ ", currentProfit= "+currentProfit + ", ideal= " +bestProfit +", Fitness= " + instanceRelativeError+", Time= " + duracion );
				PDPInstance pdpI = auxData.get(i).getInstance().getPdpInstance();
			//auxData.get(i).getIndependenSet().Print_ListAdya();
			}
			//FIN Evaluacinn del algoritmo con todas las intancias

			//Runtime garbage = Runtime.getRuntime();
			//garbage.gc();
			double ERP = relErrAcum / auxData.size();//Error Relativo Promedio
			KozaFitness f = ((KozaFitness) gpind.fitness);
			float Eva_fitness = (float)(ERP);
			float Evo_fitness =(float)((PDPProblemEvo.POND_ERP*ERP) + (PDPProblemEvo.POND_ERL*ERL));
			f.setStandardizedFitness(state,Eva_fitness);
			f.hits = hits;
			gpind.evaluated = true;
			float AdjustedFitness = (float) (1.0/(1.0 + Evo_fitness));
			double tiempoPromedio = timeAcum / auxData.size();
			int gen = state.generation;
			String id = gpind.toString();
			Corrida_Eva.AgregarIndv(gen,id,AdjustedFitness,Evo_fitness,ERP,ERL, hits,tiempoPromedio,tamanoArbol,alturaArbol);
			//...log
			state.output.println("  Num GPNodos = " + gpind.size(), LOG_FILE);
			state.output.println("  Hits = " + hits, LOG_FILE);
			state.output.println("  ListaHits = " + ListaHits, LOG_FILE);
			
			state.output.println("  Tiempo promedio = " +((float)(tiempoPromedio))+"ms", LOG_FILE);
			//state.output.println("  Error Relativo acumulado = " + (float)instanceRelativeError, LOG_FILE); //relErrAcum, LOG_FILE);
			state.output.println("  auxData.size = " + (float)auxData.size(), LOG_FILE);
			state.output.println("  Error Relativo Promedio = " + (float)ERP, LOG_FILE);
			state.output.println("  Error Relativo de Legibilidad = " +(float)ERL, LOG_FILE);
			state.output.println("  FITNESS EVO("+PDPProblemEvo.POND_ERP+"*ERP + "+PDPProblemEvo.POND_ERL+"*ERL) = "+Evo_fitness+"", LOG_FILE);
			state.output.println("  FITNESS EVA(ERP) = "+Eva_fitness, LOG_FILE);
			state.output.println("  NodosEnSolucion = " + currentProfit , LOG_FILE);
			state.output.println("------------------------------", LOG_FILE);
			double meanTime = Precision.round(new Mean().evaluate(times.stream().mapToDouble(Double::doubleValue).toArray()),3);
			System.out.println("  Tiempo promedio = " +((float)(meanTime))+"ms");
		}		
	}

	@Override
	public void describe(final EvolutionState state,
			final Individual individual,
			final int subpopulation,
			final int threadnum,
			final int log) {
		endGenerationTime = System.nanoTime();	//fin cronometro evolucinn
		long duracion = (endGenerationTime - startGenerationTime) / 1000000;
		state.output.println("========================================", LOG_FILE);
		state.output.println("Tiempo total Evaluacinn: " + duracion + " ms="+(duracion/1000)+"seg", LOG_FILE);
		state.output.println("========================================", LOG_FILE);
		state.output.message("Evaluation duration: " + duracion + " ms");	//duracinn evolucinn en ms
		state.output.print(duracion + "", RESULTS_FILE);
		
		//Print BestIndividual.in
		PrintWriter dataOutput = null;
		Charset charset = StandardCharsets.UTF_8;
		try {
			dataOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Outputpath+PDPProblemEvo.IN_namefile), charset)));
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
		state.output.println("label=\"Individual=" + indid + " Fitness=" + ((KozaFitness) gpind.fitness).standardizedFitness() + " Hits=" + ((KozaFitness) gpind.fitness).hits + " Size=" + gpind.size() + " Depth=" + gpind.trees[0].child.depth() + "\";", DOT_FILE);
		gpind.printIndividualForHumans(state, DOT_FILE);
		try {
			FileIO.repairDot(Outputpath,PDPProblemEvo.DOT_namefile);
			FileIO.dot_a_png(Outputpath,PDPProblemEvo.dotexepath,PDPProblemEvo.DOT_namefile);
			FileIO.Estadistica_ResumenEva(Outputpath,Corrida_Eva,"Estadistica_ResumenEva.csv");//agregar tiempo
			} catch (Exception e) {e.printStackTrace();}
		
		
		//Print all BestIndividuals.in
		//...
		//Print all BestIndividuals.dot & .png
		String path_ind;
		GPIndividual ind;
		int dot_file=0;
		String folder_path = Outputpath+"evaluatedjobs/";
		File folder = new File(folder_path);
		folder.mkdir();
		for(int i=0; i<Alg_List.size();i++){			
			ind = Alg_List.get(i);
			path_ind = folder_path+"job."+i+".";
			try { dot_file = FileIO.newLog(state.output, path_ind+PDPProblemEvo.DOT_namefile);} catch (IOException e) {e.printStackTrace();}
			ind.trees[0].printStyle = GPTree.PRINT_STYLE_DOT;
			String ind_id = ind.toString().substring(19);
			state.output.println("label=\"Individual=" + ind_id + " Fitness=" + ((KozaFitness) ind.fitness).standardizedFitness() + " Hits=" + ((KozaFitness) ind.fitness).hits + " Size=" + ind.size() + " Depth=" + ind.trees[0].child.depth() + "\";", dot_file);
			ind.printIndividualForHumans(state, dot_file);
			try {
				FileIO.repairDot(path_ind,PDPProblemEvo.DOT_namefile);
				FileIO.dot_a_png(path_ind,PDPProblemEvo.dotexepath,PDPProblemEvo.DOT_namefile);
			} catch (Exception e) {e.printStackTrace();}
			
		}
	}



}

