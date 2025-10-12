package model;

import ec.EvolutionState;
import ec.util.Output;
import org.apache.commons.math3.util.Precision;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class FileIO {

	public static int newLog(Output output, String filename) throws IOException {
		FileWriter fw = new FileWriter(filename, false);//abre
		fw.write("");//ingresa NADA!
		fw.close();//cierra
		File file = new File(filename);
		return output.addLog(file, true);//agrega el File recien creado(vacio) al output
	}

	public static void readInstances(ArrayList<PDPData> data, String path_folder) throws IOException {//Metodo que lee todas las instancias de una carpeta
		final File folder = new File(path_folder);
		for (final File fileEntry : folder.listFiles()) {//recorre los files de la carpeta folder
			if (fileEntry.isDirectory()) {
				readInstances(data, fileEntry.getPath());
			} else {
				System.out.print("-");
				//System.out.println("Leyendo: " + fileEntry.getName());
				PDPData pdpd = new PDPData();//Declara una nueva variable PDPData, con atributos vacios
				readFile(pdpd.getInstance(), fileEntry.getPath());//metodo readFile, recibe un parametro instancia vacio y  el nombre del file correspondiente a una instancia a leer
				//mispd.instance.getIndependenSet().Print_ListAdya();//Imprime por consola la lista enlazada
				data.add(pdpd);
			}
		}
	}



	private static void readOptimals(Instance inst) throws IOException {//Lee los resultados y setea las instancia segun sea optima o no
		System.out.println("Reading optimals");
		File file = new File("data/results.txt");
		Scanner s = new Scanner(file);
		boolean found = false;
		while(s.hasNextLine() && !found) {
			boolean isOptimal = false;
			String instName = s.next();
			double token = s.nextDouble();
			double objective = Precision.round(token,2);
			if(s.next().equals("Optimal")) {
				isOptimal = true;
			}
			if(inst.getName().equals(instName + ".txt")) {
				found = true;
				System.out.println(inst.getName()+ " " + objective);
				if(isOptimal) {
					inst.setOptimal(objective);
					inst.setOptimalKnown(true);
				}
				else {
					inst.setFeasible(objective);
					inst.setOptimalKnown(false);
				}
			}
		}
		s.close();
	}

	public static void readFile(Instance inst, String filename) throws IOException {//Lee UNA instancia
		System.out.println("reading "+ filename);
		File file = new File(filename);
		Scanner s = new Scanner(file);
		inst.setName(file.getName());	//Nombre de instancia

		String token;
		s.nextLine(); //SE OMITE LA PRIMERA LINEA QUE DICE COST MATRIX

		//LEER MATRIZ DE COSTOS
		List<ArrayList<Float>> costMatrix = new ArrayList<>();
		while(s.hasNextLine()){
			token = s.nextLine();
			if (token.equals("~")){
				break;
			}
			ArrayList<Float> costList = new ArrayList<>(Arrays.stream(token.split("\\s+"))
					.map(Float::parseFloat)
					.collect(Collectors.toList())) ;
			costMatrix.add(costList);
		}

		s.nextLine(); //SE OMITE LA LINEA QUE DICE DELIVERY QUANTITIES

		token = s.nextLine();
		List<Float> deliveryQuantities = new ArrayList<>(Arrays.stream(token.split("\\s+"))
				.map(Float::parseFloat)
				.collect(Collectors.toList())) ;

		s.nextLine(); //SE OMITE ~
		s.nextLine(); //SE OMITE LA LINEA QUE DICE PICKUP QUANTITIES

		token = s.nextLine();
		List<Float> pickupQuantities = new ArrayList<>(Arrays.stream(token.split("\\s+"))
				.map(Float::parseFloat)
				.collect(Collectors.toList())) ;

		s.nextLine(); //SE OMITE ~
		s.nextLine(); //SE OMITE LA LINEA QUE DICE VEHICLE CAPACITIES

		token = s.nextLine();
		List<Float> vehicleCapacitites = new ArrayList<>(Arrays.stream(token.split("\\s+"))
				.map(Float::parseFloat)
				.collect(Collectors.toList())) ;

		float vehicleCapacity = vehicleCapacitites.get(0);


		inst.getPdpInstance().updatePDPInstanceFromRead(costMatrix,deliveryQuantities,pickupQuantities,vehicleCapacity);


		s.close();
		readOptimals(inst);
	}

	public static void writeFile(String line, String filename) throws IOException {//Escribe una linea de texto en un archivo
		File file = new File(filename);

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(line);
		bw.close();
	}
	public static void repairDot(String path,String namefile) throws IOException {
		System.out.println(path+namefile);
		File file = new File(path+ namefile);
		Scanner s = new Scanner(file);
		StringBuilder buffer = new StringBuilder();
		int i = 1;
		String label = "";
		while(s.hasNextLine()) {
			if(i == 1)
				label = s.nextLine();
			else if(i > 4) {
				buffer.append(s.nextLine() + "\n");
				if(i == 5)
					buffer.append(label + "\n");
			}
			else
				s.nextLine();
			i++;
		}

		writeFile(buffer.toString(), path+namefile);
		s.close();
	}
	public static void dot_a_png(String path,String dotPath,String file) {
		try {
			System.out.println("[dot_a_png]");
			String fileInputPath =	path+file;
			String[] s = file.split("\\.");//sin extensión
			String fileOutputPath =	path+s[0]+".png";
			Runtime rt = Runtime.getRuntime();
			rt.exec(dotPath+" -Tpng "+fileInputPath+" -o "+fileOutputPath);

		} catch (IOException ioe) {
			System.out.println (ioe);
		} finally {
		}

	}
	public static void convert_cql_to_mis( String path_folder) throws IOException  {//Metodo que lee todas las instancias de una carpeta
		final File folder = new File(path_folder);
		for (final File fileEntry : folder.listFiles()) {//recorre los files de la carpeta folder
			//if (fileEntry.isDirectory()) {
			//	convert_cql_to_mis(fileEntry.getPath());
			//} else {
				System.out.println("Leyendo: " + fileEntry.getPath());
				ArrayList<ArrayList<Integer>> instancia = new ArrayList<ArrayList<Integer>>();//Declara una nueva variable MISPData, con atributos vacios
				ArrayList<Integer> info = new ArrayList<Integer>(); 
				readCLQFile(instancia, fileEntry.getPath(), info);

				System.out.println("Escribiendo: " + "data/all/mis/" +fileEntry.getName());
				writeMISFile(instancia, ("data/all/mis/"+fileEntry.getName()), info);
			//}
		}		
	}

	
	private static void readCLQFile(ArrayList<ArrayList<Integer>> inst, String filename,ArrayList<Integer> info) throws IOException{//Lee UNA instancia
		File file = new File(filename);
		Scanner s = new Scanner(file);
		String token;
		//Omitir comentarios de la entrada, comentarios empiezan con la letra 'c', lineas utiles empiezan con la letra p y e
		while(s.hasNextLine()){
			if(s.hasNext("p")){break;}
			token = s.nextLine();
		}
		//Siguiente linea comienza con la letra p
		s.next();s.next();//p edge
		//Informacion básica
		System.out.println("  Leyendo: numVert,numArist...");
		info.add(s.nextInt());//numVert
		info.add(s.nextInt());//numArist
		System.out.println("  INFO: "+info.get(0)+", "+info.get(1));
		//Setear la matriz de insidencia con ceros
		for(int i=0;i<(info.get(0));i++){
			inst.add(new ArrayList<Integer>());
			for(int j=0; j<(info.get(0));j++){
				inst.get(i).add(0);
			}
		}
		//quitar comentarios
		while(s.hasNextLine()){
			if(s.hasNext("e")||s.hasNext("a")){break;}
			token = s.nextLine();
		}		
		//Siguientes lineas comienzan con la leltra c o a 
		//Pares de vertices adyacentes igual al numero de aristas
		System.out.println("  Leyendo: Adyacencias...");
		for(int i = 0; i < (info.get(1)); i++) {
			s.next();	//primer elemento un string corresponde a una letra
			int v1 = s.nextInt();	//segundo elemento un entero corresponde a un vertice
			int v2 = s.nextInt();	//tercer elemento un entero corresponde  a un vertice
			inst.get(v1-1).set(v2-1, 1);
			inst.get(v2-1).set(v1-1, 1);
		}
		s.close();
	}
	private static void writeMISFile(ArrayList<ArrayList<Integer>> inst, String filename,ArrayList<Integer> info) throws IOException {
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		int numVert = info.get(0);
		int num_arist = (numVert*(numVert-1)/2)-info.get(1);
		System.out.println("  INFO: "+numVert+", "+num_arist);
		bw.write("p edge "+numVert+" "+num_arist);
		for(int i=0;i<info.get(0);i++){
			for(int j=i+1;j<info.get(0);j++){
				if(inst.get(i).get(j)==0){
					bw.newLine();
					bw.write("e "+(i+1)+" "+(j+1)+"");
				}

			}
		}
		bw.close();
	}
	public static void join_thebest(String infolder,String outfolder,String str_infile,String str_outfile ) throws IOException{	
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+str_outfile);

		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Number of Subpopulations: i1|");bw.newLine();
		bw.write("Subpopulation Number: i0|");bw.newLine();
		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			String path = infolder+cont+"/job."+cont+"."+str_infile;
			//System.out.println(path);
			File in_file = new File(path);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		//Se escriben en un solo archivo
		bw.write("Number of Individuals: i"+cont+"|");bw.newLine();

		for(int i=0; i<cont;i++){
			File in_file = new File(infolder+i+"/job."+i+"."+str_infile);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();s.nextLine();s.nextLine();s.nextLine();
			bw.write("Individual Number: i"+i+"|");
			bw.newLine();
			while(s.hasNextLine()){
				bw.write(s.nextLine());
				bw.newLine();
			}
			bw.newLine();
			s.close();
		}

		bw.close();
		System.out.println(" Mejores Individuos unidos: OK!");
	}
	public static void Stat_a_csv(String path, String infile,String outfile) throws IOException {
		String fileInputPath =	path+infile;
		File file = new File(fileInputPath);
		//Lee el archivo
		Scanner s = new Scanner(file); String temp;
		ArrayList<Individuo> listEstadisticas = new ArrayList<Individuo>();
		Individuo estadisticaTemp = null;
		do {
			temp = s.nextLine();
			if (temp.indexOf("Generation") != -1) {
				estadisticaTemp = new Individuo();
				//estadisticaTemp.name = fileInputPath;
				estadisticaTemp.generation = Integer.parseInt(temp.split(":")[1].replace(" ", ""));
			}
			if (temp.indexOf("Fitness") != -1) {
				estadisticaTemp.Standardized = Float.parseFloat(temp.split("=")[1].split(" ")[0]);
				estadisticaTemp.Adjusted = Float.parseFloat(temp.split("=")[2].split(" ")[0]);
				estadisticaTemp.Hits = Integer.parseInt(temp.split("=")[3].split(" ")[0]);
				listEstadisticas.add(estadisticaTemp);
			}
		} while (s.hasNextLine());	s.close();
		//Escribe el txt
		listEstadisticas.remove(listEstadisticas.size()-1);//el ultimo se repite
		System.out.print("["+outfile+"]"+listEstadisticas.size()+" Escribe...");
		DecimalFormat f = new DecimalFormat("###.#######");//7 decimales con separador de coma
		FileWriter fichero = null;	PrintWriter pw = null;
		try {
			fichero = new FileWriter(path+outfile);
			pw = new PrintWriter(fichero);
			pw.println(
					/*estadisticas.name + ";" +*/
					"Gen;" +
					//estadisticas.Standardized + ";" +
					//estadisticas.Adjusted + ";" +
					"Standarized;" +
					"Ajusted;" +
					"Hits"
					);
			for (Individuo estadisticas : listEstadisticas) {
				pw.println(
						/*estadisticas.name + ";" +*/
						estadisticas.generation + ";" +
						//estadisticas.Standardized + ";" +
						//estadisticas.Adjusted + ";" +
						f.format(estadisticas.Standardized) + ";" +
						f.format(estadisticas.Adjusted) + ";" +
						estadisticas.Hits
						);
			}
		} catch (Exception e) { e.printStackTrace();
		} finally {
			try {
				// Nuevamente aprovechamos el finally para 
				// asegurarnos que se cierra el fichero.
				if (null != fichero) {
					fichero.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		System.out.println(listEstadisticas.size()+" OK");

	}
	public static void join_StatsCSV(String infolder,String outfolder, String file) throws IOException{
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+file);
		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(infolder+cont+"/job."+cont+"."+file);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		ArrayList<ArrayList<String>> job_List = new ArrayList<ArrayList<String>>();
		//Se leen los archivos y se guardan en una estructura
		for(int i=0;i<cont;i++){
			ArrayList<String> job = new ArrayList<String>();
			File in_file = new File(infolder+i+"/job."+i+"."+file);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();//primera linea con encabezados
			while(s.hasNextLine()){
				job.add(s.nextLine().split(";")[1]);
			}
			s.close();
			job_List.add(job);
		}
		//Se escriben en un solo archivo
		bw.write("GENERACIÓN");
		for(int i=0;i<cont;i++){
			bw.write(";EVOLUCIÓN "+i);
		}bw.newLine();
		int num_gen = job_List.get(0).size();
		for(int i=0; i<num_gen;i++){
			bw.write(""+i+"");
			for(int j=0;j<cont;j++){
				bw.write(";"+job_List.get(j).get(i));
			}
			bw.newLine();
		}
		bw.close();
		System.out.println(" Stats unidas: OK!");
	}
	public static void logEvo(final EvolutionState state, int log) throws IOException{
		state.output.println("PARAMETROS",log);
		int tam = state.parameters.getInt(new ec.util.Parameter("gp.fs.0.size"),null);
		state.output.println("FyT = "+tam,log);
		for(int i=0;i<tam;i++){
			state.output.println("GPNodo("+i+") = "+state.parameters.getString(new ec.util.Parameter("gp.fs.0.func."+i+""),null),log);	
		}
		state.output.println("CrossoverPipeline = "+state.parameters.getString(new ec.util.Parameter("pop.subpop.0.species.pipe.source.0.prob"),null),log);
		state.output.println("ReproductionPipeline = "+state.parameters.getString(new ec.util.Parameter("pop.subpop.0.species.pipe.source.1.prob"),null),log);
		state.output.println("MutationPipeline = "+state.parameters.getString(new ec.util.Parameter("pop.subpop.0.species.pipe.source.2.prob"),null),log);
		state.output.println("MutateOneNodePipeline = "+state.parameters.getString(new ec.util.Parameter("pop.subpop.0.species.pipe.source.3.prob"),null),log);
		state.output.print(readEvolution(),log);
	}
	public static String readEvolution() throws IOException{
		String path = "out/results/evolution";
		String file_name = "MISPResults.out";
		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(path+cont+"/job."+cont+"."+file_name);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		//System.out.println(cont);
		//Se leen los archivos y se guardan en una estructura
		ArrayList<String> job_List = new ArrayList<String>();
		String aux="";
		for(int i=0;i<cont;i++){
			File in_file = new File(path+i+"/job."+i+"."+file_name);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			String time="";
			while(s.hasNextLine()){
				aux = time;
				time = s.nextLine();
			}
			s.close();
			job_List.add(time);
		}
		int gen = Integer.parseInt(aux.split(" ")[0])+1;
		String EvoResume = "Jobs = "+cont+"\n";
		EvoResume += "Gen = "+(gen)+"\n";
		EvoResume += "=================================\n";
		for(int i=0;i<cont;i++){
			int time = Integer.parseInt(job_List.get(i));
			EvoResume +="EVOLUCIÓN:["+i+"]\n";
			EvoResume +="  Tiempo total Evolución = "+time+"ms = "+(time/1000)+"seg = "+(time/60000)+"min\n";
			EvoResume +="------------------------------\n";
		}
		System.out.println("Leyendo log evolución...OK!");
		return EvoResume;   	
	}
	public static void GuardarSemillas(String path,String semillas,int evalT,int breedT,String outfile) throws IOException {//Metodo que intenta guardar la semilla del experimento, sin exito D:
		System.out.print("[Semillas_a_csv] Escribe...");
		//DecimalFormat f = new DecimalFormat("###.#######");//7 decimales con separador de coma
		FileWriter fichero = null;	PrintWriter pw = null;
		try {
			fichero = new FileWriter(path+outfile);
			pw = new PrintWriter(fichero);
			pw.println(
					"Semilla;" +
					"evalThreads;" +
					"breedThreads;"
					);
			pw.println(
					semillas+";"+
					evalT+";"+
					breedT
						);
		} catch (Exception e) { e.printStackTrace();
		} finally {
			try {
				// Nuevamente aprovechamos el finally para 
				// asegurarnos que se cierra el fichero.
				if (null != fichero) {
					fichero.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		System.out.println(" OK");
	}
	public static void Estadistica_Mejores(String path, ArrayList<Generacion> Indvs, String outfile) throws IOException {
		System.out.print("["+outfile+"]"+Indvs.size()+" Escribe...");
		DecimalFormat f = new DecimalFormat("###.#######");//7 decimales con separador de coma
		FileWriter fichero = null;	PrintWriter pw = null;
		try {
			fichero = new FileWriter(path+outfile);
			pw = new PrintWriter(fichero);
			pw.println(
					/*estadisticas.name + ";" +*/
					"Gen;" +
					"Size;" +
					"ERL;" +
					"ERP;" +
					"Fitness"
					);
			for(int i=0;i<Indvs.size();i++){
				Individuo indv = Indvs.get(i).TheBestEvo();
				pw.println(
						/*estadisticas.name + ";" +*/
						(i+1) + ";" +
						indv.tam + ";" +
						f.format(indv.ERL) + ";" +
						f.format(indv.ERP) + ";" +
						f.format(indv.Standardized)
						);
			}
		} catch (Exception e) { e.printStackTrace();
		} finally {
			try {
				// Nuevamente aprovechamos el finally para 
				// asegurarnos que se cierra el fichero.
				if (null != fichero) {
					fichero.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		System.out.println(Indvs.size()+" OK");
	}
	public static void Estadistica_Promedio_y_Mejores(String path, ArrayList<Generacion> Indvs,String outfile) throws IOException {
		System.out.print("[EstadisticaProm&Mej_a_csv]"+Indvs.size()+" Escribe...");
		DecimalFormat f = new DecimalFormat("###.#######");//7 decimales con separador de coma
		FileWriter fichero = null;	PrintWriter pw = null;
		try {
			fichero = new FileWriter(path+outfile);
			pw = new PrintWriter(fichero);
			pw.println(
					/*estadisticas.name + ";" +*/
					"Gen;" +
					"AvgSize;" +
					"AvgERL;" +
					"AvgERP;" +
					"AvgFITNESS;" +
					"BestSize;" +
					"BestERL;" +
					"BestERP;" +
					"BestFITNESS;" +
					"AvgHits;" +
					"BestHits"
					);
			for(int i=0;i<Indvs.size();i++){
				Generacion gen = Indvs.get(i);
				Individuo indv = gen.TheBestEvo();
				pw.println(
						/*estadisticas.name + ";" +*/
						(i+1) + ";" +
						f.format(gen.AvgSize()) + ";" +
						f.format(gen.AvgERL()) + ";" +
						f.format(gen.AvgERP()) + ";" +
						f.format(gen.AvgFitnessEvo()) + ";"+
						indv.tam + ";" +
						f.format(indv.ERL) + ";" +
						f.format(indv.ERP) + ";" +
						f.format(indv.Standardized) + ";" +
						f.format(gen.AvgHits()) + ";" +
						f.format(indv.Hits)
						);
			}
		} catch (Exception e) { e.printStackTrace();
		} finally {
			try {
				// Nuevamente aprovechamos el finally para 
				// asegurarnos que se cierra el fichero.
				if (null != fichero) {
					fichero.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		System.out.println(Indvs.size()+" OK");
	}
	public static void Estadistica_Todos(String path, ArrayList<Generacion> Indvs,String outfile) throws IOException {
		System.out.print("["+outfile+"]"+Indvs.size()+" Escribe...");
		DecimalFormat f = new DecimalFormat("###.#######");//7 decimales con separador de coma
		FileWriter fichero = null;	PrintWriter pw = null;
		try {
			fichero = new FileWriter(path+outfile);
			pw = new PrintWriter(fichero);
			pw.println(
					"Gen;" +
					"BestFitness;" +
					"AvgFitness;" +
					"WorstFitness;" +
					"MinSize;" +
					"AvgSize;" +
					"MaxSize;" +
					"MinDepth;" +
					"AvgDepth;" +
					"MaxDepth;"
					);
			for(int i=0;i<Indvs.size();i++){
				pw.println(
						(i+1) + ";" +
						f.format(Indvs.get(i).BestFitnessEvo()) + ";" +
						f.format(Indvs.get(i).AvgFitnessEvo()) + ";" +
						f.format(Indvs.get(i).WorstFitnessEvo()) + ";" +
						Indvs.get(i).MinSize() + ";" +
						f.format(Indvs.get(i).AvgSize()) + ";" +
						Indvs.get(i).MaxSize() + ";" +
						Indvs.get(i).MinDepth() + ";" +
						f.format(Indvs.get(i).AvgDepth()) + ";" +
						Indvs.get(i).MaxDepth()
						);
			}
		} catch (Exception e) { e.printStackTrace();
		} finally {
			try {
				// Nuevamente aprovechamos el finally para 
				// asegurarnos que se cierra el fichero.
				if (null != fichero) {
					fichero.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		System.out.println(Indvs.size()+" OK");
	}
	public static void join_Estadistica_Promedio_y_Mejores(String infolder, String outfolder, String file) throws IOException{//Une las estadisticas de los mejores individuos de cada generación de todas las corridas	
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+file);
		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(infolder+cont+"/job."+cont+"."+file);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		ArrayList<ArrayList<String>> jobAvg_List = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> jobBest_List = new ArrayList<ArrayList<String>>();
		//Se leen los archivos y se guardan en una estructura
		for(int i=0;i<cont;i++){
			ArrayList<String> jobAvg = new ArrayList<String>();
			ArrayList<String> jobBest = new ArrayList<String>();
			File in_file = new File(infolder+i+"/job."+i+"."+file);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();//primera linea con encabezados, no se guarda
			while(s.hasNextLine()){
				String[] words = s.nextLine().split(";");
				//numNods_x	ERL_x	ERP_x	FITNESS_x
				jobAvg.add(words[1]+";"+words[2]+";"+words[3]+";"+words[4]);
				jobBest.add(words[5]+";"+words[6]+";"+words[7]+";"+words[8]);
			}
			s.close();
			jobAvg_List.add(jobAvg);
			jobBest_List.add(jobBest);
		}
		//Se escriben en un solo archivo
		bw.write("GENERACIÓN");
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";AvgNods_"+i+";AvgERL_"+i+";AvgERP_"+i+";AvgFITNESS_"+i);
		}
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";BestNods_"+i+";BestERL_"+i+";BestERP_"+i+";BestFITNESS_"+i);
		}
		bw.newLine();

		int num_gen = jobAvg_List.get(0).size();
		for(int i=0; i<num_gen;i++){//Resto de lineas, igual al numero de generaciones
			bw.write(""+i+"");
			for(int j=0;j<cont;j++){
				bw.write(";"+jobAvg_List.get(j).get(i));
			}
			for(int j=0;j<cont;j++){
				bw.write(";"+jobBest_List.get(j).get(i));
			}
			bw.newLine();
		}
		bw.close();
		System.out.println(" EstadisticaProm&Mej unidas: OK!");
	}
	public static void join_Fitness_BestAvg(String infolder, String outfolder, String infile,String outfile) throws IOException{//Une las estadisticas de los mejores individuos de cada generación de todas las corridas	
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+outfile);
		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(infolder+cont+"/job."+cont+"."+infile);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		ArrayList<ArrayList<String>> jobAvg_List = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> jobBest_List = new ArrayList<ArrayList<String>>();
		//Se leen los archivos y se guardan en una estructura
		for(int i=0;i<cont;i++){
			ArrayList<String> jobAvg = new ArrayList<String>();
			ArrayList<String> jobBest = new ArrayList<String>();
			File in_file = new File(infolder+i+"/job."+i+"."+infile);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();//primera linea con encabezados, no se guarda
			while(s.hasNextLine()){
				String[] words = s.nextLine().split(";");
				//Avg: numNods_x[1]	ERL_x[2]	ERP_x[3]	FITNESS_x[4]
				jobAvg.add(words[4]);
				//Best: numNods_x[5]	ERL_x[6]	ERP_x[7]	FITNESS_x[8]
				jobBest.add(words[8]);
			}
			s.close();
			jobAvg_List.add(jobAvg);
			jobBest_List.add(jobBest);
		}
		//Se escriben en un solo archivo
		bw.write("GENERACIÓN");
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";AvgFITNESS_"+i);
		}
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";BestFITNESS_"+i);
		}
		bw.newLine();

		int num_gen = jobAvg_List.get(0).size();
		for(int i=0; i<num_gen;i++){//Resto de lineas, igual al numero de generaciones
			bw.write(""+i+"");
			for(int j=0;j<cont;j++){
				bw.write(";"+jobAvg_List.get(j).get(i));
			}
			for(int j=0;j<cont;j++){
				bw.write(";"+jobBest_List.get(j).get(i));
			}
			bw.newLine();
		}
		bw.close();
		System.out.println(" Fitness_Prom&Mej unidas: OK!");
	}
	public static void join_Tamano_BestAvg(String infolder, String outfolder, String infile, String outfile) throws IOException{//Une las estadisticas de los mejores individuos de cada generación de todas las corridas
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+outfile);
		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(infolder+cont+"/job."+cont+"."+infile);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		ArrayList<ArrayList<String>> jobAvg_List = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> jobBest_List = new ArrayList<ArrayList<String>>();
		//Se leen los archivos y se guardan en una estructura
		for(int i=0;i<cont;i++){
			ArrayList<String> jobAvg = new ArrayList<String>();
			ArrayList<String> jobBest = new ArrayList<String>();
			File in_file = new File(infolder+i+"/job."+i+"."+infile);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();//primera linea con encabezados, no se guarda
			while(s.hasNextLine()){
				String[] words = s.nextLine().split(";");
				//Avg: numNods_x[1]	ERL_x[2]	ERP_x[3]	FITNESS_x[4]
				jobAvg.add(words[1]);
				//Best: numNods_x[5]	ERL_x[6]	ERP_x[7]	FITNESS_x[8]
				jobBest.add(words[5]);
			}
			s.close();
			jobAvg_List.add(jobAvg);
			jobBest_List.add(jobBest);
		}
		//Se escriben en un solo archivo
		bw.write("GENERACIÓN");
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";AvgTam_"+i);
		}
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";BestTam_"+i);
		}
		bw.newLine();

		int num_gen = jobAvg_List.get(0).size();
		for(int i=0; i<num_gen;i++){//Resto de lineas, igual al numero de generaciones
			bw.write(""+i+"");
			for(int j=0;j<cont;j++){
				bw.write(";"+jobAvg_List.get(j).get(i));
			}
			for(int j=0;j<cont;j++){
				bw.write(";"+jobBest_List.get(j).get(i));
			}
			bw.newLine();
		}
		bw.close();
		System.out.println(" Tamaño_Prom&Mej unidas: OK!");
	}
	public static void join_ERP_BestAvg(String infolder, String outfolder, String infile,String outfile) throws IOException{//Une las estadisticas de los mejores individuos de cada generación de todas las corridas	
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+outfile);
		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(infolder+cont+"/job."+cont+"."+infile);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		ArrayList<ArrayList<String>> jobAvg_List = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> jobBest_List = new ArrayList<ArrayList<String>>();
		//Se leen los archivos y se guardan en una estructura
		for(int i=0;i<cont;i++){
			ArrayList<String> jobAvg = new ArrayList<String>();
			ArrayList<String> jobBest = new ArrayList<String>();
			File in_file = new File(infolder+i+"/job."+i+"."+infile);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();//primera linea con encabezados, no se guarda
			while(s.hasNextLine()){
				String[] words = s.nextLine().split(";");
				//Avg: numNods_x[1]	ERL_x[2]	ERP_x[3]	FITNESS_x[4]
				jobAvg.add(words[3]);
				//Best: numNods_x[5]	ERL_x[6]	ERP_x[7]	FITNESS_x[8]
				jobBest.add(words[7]);
			}
			s.close();
			jobAvg_List.add(jobAvg);
			jobBest_List.add(jobBest);
		}
		//Se escriben en un solo archivo
		bw.write("GENERACIÓN");
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";AvgERP_"+i);
		}
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";BestERP_"+i);
		}
		bw.newLine();

		int num_gen = jobAvg_List.get(0).size();
		for(int i=0; i<num_gen;i++){//Resto de lineas, igual al numero de generaciones
			bw.write(""+i+"");
			for(int j=0;j<cont;j++){
				bw.write(";"+jobAvg_List.get(j).get(i));
			}
			for(int j=0;j<cont;j++){
				bw.write(";"+jobBest_List.get(j).get(i));
			}
			bw.newLine();
		}
		bw.close();
		System.out.println(" ERP_Prom&Mej unidas: OK!");
	}
	public static void join_ERL_BestAvg(String infolder, String outfolder, String infile,String outfile) throws IOException{//Une las estadisticas de los mejores individuos de cada generación de todas las corridas	
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+outfile);
		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(infolder+cont+"/job."+cont+"."+infile);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		ArrayList<ArrayList<String>> jobAvg_List = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> jobBest_List = new ArrayList<ArrayList<String>>();
		//Se leen los archivos y se guardan en una estructura
		for(int i=0;i<cont;i++){
			ArrayList<String> jobAvg = new ArrayList<String>();
			ArrayList<String> jobBest = new ArrayList<String>();
			File in_file = new File(infolder+i+"/job."+i+"."+infile);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();//primera linea con encabezados, no se guarda
			while(s.hasNextLine()){
				String[] words = s.nextLine().split(";");
				//Avg: numNods_x[1]	ERL_x[2]	ERP_x[3]	FITNESS_x[4]
				jobAvg.add(words[2]);
				//Best: numNods_x[5]	ERL_x[6]	ERP_x[7]	FITNESS_x[8]
				jobBest.add(words[6]);
			}
			s.close();
			jobAvg_List.add(jobAvg);
			jobBest_List.add(jobBest);
		}
		//Se escriben en un solo archivo
		bw.write("GENERACIÓN");
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";AvgERL_"+i);
		}
		for(int i=0;i<cont;i++){//Primera linea, por cada corrida
			bw.write(";BestERL_"+i);
		}
		bw.newLine();

		int num_gen = jobAvg_List.get(0).size();
		for(int i=0; i<num_gen;i++){//Resto de lineas, igual al numero de generaciones
			bw.write(""+i+"");
			for(int j=0;j<cont;j++){
				bw.write(";"+jobAvg_List.get(j).get(i));
			}
			for(int j=0;j<cont;j++){
				bw.write(";"+jobBest_List.get(j).get(i));
			}
			bw.newLine();
		}
		bw.close();
		System.out.println(" ERL_Prom&Mej unidas: OK!");
	}
	public static void join_Estadistica_Todos(String infolder,String outfolder, String file) throws IOException{//Une las estadisticas de los mejores individuos de cada generación de todas las corridas	
		File out_folder = new File(outfolder);
		out_folder.mkdir();
		File out_file = new File(outfolder+file);
		if (!out_file.exists()) {out_file.createNewFile();}

		FileWriter fw = new FileWriter(out_file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);

		//se cuentan los archivos
		int cont=0;
		for(boolean existe = true;existe;){
			File in_file = new File(infolder+cont+"/job."+cont+"."+file);
			if(!in_file.exists()){existe=false;}
			else cont++;
		}
		System.out.print(cont);
		ArrayList<String> job_List = new ArrayList<String>();
		//Se leen los archivos y se guardan en una estructura
		for(int i=0;i<cont;i++){
			String job="";
			File in_file = new File(infolder+i+"/job."+i+"."+file);
			if (!in_file.exists()) {break;}
			Scanner s = new Scanner(in_file);
			s.nextLine();//primera linea con encabezados, no se guarda
			while(s.hasNextLine()){
				String[] words = s.nextLine().split(";");
				if(!s.hasNextLine()){
					//BestFitness	AvgFitness	WorstFitness	MinSize	AvgSize	MaxSize	MinDepth	AvgDepth MaxDepth
					job = words[1]+";"+words[2]+";"+words[3]+";"+words[4]+";"+words[5]+";"+words[6]+";"+words[7]+";"+words[8]+";"+words[9];
				}
			}
			s.close();
			job_List.add(job);
		}
		//Se escriben en un solo archivo
		//Primera linea
		bw.write("CORRIDA;BestFitness;AvgFitness;WorstFitness;MinSize;AvgSize;MaxSize;MinDepth;AvgDepth;MaxDepth");
		bw.newLine();

		int num_corridas = job_List.size();
		for(int i=0; i<num_corridas;i++){//Resto de lineas, igual al numero de corridas
			bw.write(""+i+"");
				bw.write(";"+job_List.get(i));
			bw.newLine();
		}
		bw.close();
		System.out.println(" Estadistica_Todos unidas: OK!");
	}
	public static void Estadistica_ResumenEva(String path, Generacion Indvs,String outfile) throws IOException {
		System.out.print("["+outfile+"]"+Indvs.Size()+" Escribe...");
		DecimalFormat f = new DecimalFormat("###.#######");//7 decimales con separador de coma
		FileWriter fichero = null;	PrintWriter pw = null;
		try {
			fichero = new FileWriter(path+outfile);
			pw = new PrintWriter(fichero);
			pw.println(
					"Id;" +
					"ERL;" +
					"ERP;" +
					"Fitness_Evolución;" +
					"Fitness_Evaluación;" +
					"Tam.;" +
					"Alt.;" +
					"Hits;" +
					"Ejec.;" +
					"Tmp(ms)"
					);
			for(int i=0;i<Indvs.Size();i++){
				Individuo ind = Indvs.get(i); 
				pw.println(
						"MISP"+i + ";" +
						f.format(ind.ERL) + ";" +
						f.format(ind.ERP) + ";" +
						f.format(ind.Standardized) + ";" +
						f.format(ind.ERP) + ";" +
						ind.tam + ";" +
						ind.depth + ";" +
						ind.Hits + ";" +
						i + ";" +
						f.format(ind.time)
						);
			}
		} catch (Exception e) { e.printStackTrace();
		} finally {
			try {
				// Nuevamente aprovechamos el finally para 
				// asegurarnos que se cierra el fichero.
				if (null != fichero) {
					fichero.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		System.out.println(Indvs.Size()+" OK");
	}

	public static void WriteCostEvaluation(String fileName, List<Double> instanceCosts) throws IOException {
		List<String> roundedInstanceCosts = instanceCosts.stream().map(cost -> Precision.round(cost,2) +"").collect(Collectors.toList());

		String line = String.join(",",roundedInstanceCosts);
		System.out.println(line);
		FileWriter fw = new FileWriter(fileName, true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(line);
		bw.newLine();
		bw.close();

		fw.close();
	}
}
