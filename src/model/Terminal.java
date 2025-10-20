package model;

//import java.util.ArrayList;
//import java.util.ArrayList;
//import java.util.Collections;

public class Terminal {
	
	//TERMINALES==========================================================

	public static boolean add_nearest_neighbor_last_node(PDPInstance pdpi){
		//System.out.println("nearest_last_node");
		return pdpi.add_nearest_neighbor_last_node();
	}

	public static boolean add_nearest_neighbor_first_node(PDPInstance pdpi){
		//System.out.println("nearest_last_node");
		return pdpi.add_nearest_neighbor_first_node();
	}

	public static boolean add_farthest_neighbor_last_node(PDPInstance pdpi){
		//System.out.println("farthest");

		return pdpi.add_farthest_neighbor_last_node();
	}

	public static boolean add_farthest_neighbor_first_node(PDPInstance pdpi){
		//System.out.println("farthest");

		return pdpi.add_farthest_neighbor_first_node();
	}

	public static boolean nearest_insertion(PDPInstance pdpi){
		//System.out.println("nearest_insertion");

		return pdpi.nearest_insertion();
	}

	public static boolean farthest_insertion(PDPInstance pdpi){
		//System.out.println("farthest_insertion");
		return pdpi.farthest_insertion();
	}

	/*public static boolean random_move(PDPInstance pdpi){
		//System.out.println("random_move");

		return pdpi.random_move();
	}*/

	public static boolean best_move(PDPInstance pdpi){
		//System.out.println("best_move");
		return pdpi.best_move();
	}

	public static boolean worst_move(PDPInstance pdpi){
		//System.out.println("worst_move");
		return pdpi.worst_move();
	}

	public static boolean cplex_terminal(PDPInstance pdpi){
		//System.out.println("cplex_terminal");
		return pdpi.cplex_terminal();
		//return false;
	}

	/**
	 * Versión del terminal CPLEX con límite de tiempo configurable
	 * @param pdpi Instancia del problema
	 * @param timeLimitSeconds Límite de tiempo en segundos
	 * @return Tiempo realmente usado por CPLEX en segundos
	 */
	public static double cplex_terminal_with_limit(PDPInstance pdpi, double timeLimitSeconds){
		//System.out.println("cplex_terminal_with_limit: " + timeLimitSeconds + "s");
		return pdpi.cplex_terminal_with_limit(timeLimitSeconds);
	}

	/*
	//[T][ADD] 	Agrega un vertice LIBRE Aleatorio a la solucion, FALSE si no se puede agregar
	public static boolean addFreeVertRandom(IndependentSet is){
		if(is.isMaximal()) { //|| is.getCurrentProfit()==0
			//System.out.print("[ADD][addFreeVertRandom]:  ");System.out.println("Maximal");
			return false;}//si es maximal, no hay vertices libres que agregar
		int index_v = (int) Math.floor(Math.random()*(is.gettam_free())+is.gettam_sol());
		if(index_v <is.gettam_sol() || index_v >(is.gettam_sol()+is.gettam_free()-1)){
			System.out.println("ERROR! indice incorrecto!");
			return false;}
		//System.out.print("[ADD][addFreeVertRandom]:  ");System.out.println("["+is.getSol().get(index_v)+"]");
		is.insertVert(index_v);	//Agregarlo en la Soluci�n
		return true;
	}
	//[T][ADD]	Agrega el vertice LIBRE con MENOS vecinos a la soluci�n, FALSE si no se puede agregar
	public static boolean addFreeVertFewerNeighbors(IndependentSet is){
		if(is.isMaximal()) {
			//System.out.print("[ADD][addFreeVertFewerNeighbors]:  ");System.out.println("FALSE:Maximal");
			return false;}//si es maximal, no hay vertices libres que agregar
		int index_v = is.getFreeVertFewerNeighbors();//indice del vertice libre con menos vecinos
		if(index_v ==-1){
			System.out.println("ERROR! indice incorrecto!");
			return false;}
		//System.out.print("[ADD][addFreeVertFewerNeighbors]:  ");System.out.println("["+is.getSol().get(index_v)+"] ");
		is.insertVert(index_v);	//Agregarlo en la Soluci�n
		return true;
	}
	//[T][ADD]	Agrega el vertice LIBRE con MAS vecinos a la soluci�n, FALSE si no se puede agregar
	public static boolean addFreeVertMoreNeighbors(IndependentSet is){
		if(is.isMaximal()) {
			//System.out.print("[ADD][addFreeVertMoreNeighbors]:  ");System.out.println("FALSE:Maximal");
			return false;}//si es maximal, no hay vertices libres que agregar
		int index_v = is.getFreeVertMoreNeighbors();//indice del vertice libre con mas vecinos
		if(index_v ==-1){
			System.out.println("ERROR! indice incorrecto!");
			return false;}
		//System.out.print("[ADD][addFreeVertMoreNeighbors]:  ");System.out.println("["+is.getSol().get(index_v)+"]");
		is.insertVert(index_v);	//Agregarlo en la Soluci�n
		return true;
	}
	//[T][ADD]	Agrega el vertice LIBRE con MENOS vecinos LIBRES a la soluci�n, FALSE si no se puede agregar
	public static boolean addFreeVertFewerFreeNeighbors(IndependentSet is){
		if(is.isMaximal()) {
			//System.out.print("[ADD][addFreeVertFewerFreeNeighbors]:  ");System.out.println("FALSE:Maximal");
			return false;}//si es maximal, no hay vertices libres que agregar
		int index_v = is.getFreeVertFewerFreeNeighbors();//indice del vertice libre con menos vecinos libres
		if(index_v ==-1){
			System.out.println("ERROR! indice incorrecto!");
			return false;}
		//System.out.print("[ADD][addFreeVertFewerFreeNeighbors]:  ");System.out.println("["+is.getSol().get(index_v)+"]");
		is.insertVert(index_v);	//Agregarlo en la Soluci�n
		return true;
	}
	//[T][IS] Pregunta si La solucion es MAXIMAL, TRUE o FALSE
	public static boolean isMaximalSol(IndependentSet is){
		//System.out.println("[IS][isMaximalSol]:"+is.isMaximal());
		return is.isMaximal();
	}
	//[T][IS] Pregunta si la solucion es VACIA, TRUE o FALSE
	public static boolean isVoid_Sol(IndependentSet is){
		//System.out.println("[IS][isVoid_Sol]:"+is.isVoid());
		return is.isVoid();
	}
	//[T][RMV] 	Remueve un vertice solucion aleatorio, FALSE si no se puede remover
	public static boolean removeSolVertRandom(IndependentSet is){
		if(is.isVoid()) {
			//System.out.print("[RMV][removeTheFirstSolVert]:  ");System.out.println("Void");
			return false;}//si es vacio, no hay vertices sol que remover
		int index_v = (int) Math.floor(Math.random()*(is.gettam_sol()));
		if(index_v < 0 || index_v > (is.gettam_sol()-1)){
			System.out.println("ERROR! indice incorrecto!");
			return false;}
		//System.out.print("[RMV][removeTheFirstSolVert]:  ");System.out.println("["+is.getSol().get(index_v)+"]");
		is.removeVert(index_v);	//Remover de la Soluci�n
		return true;
	}
	//[T][RMV]	Remueve el vertice sol con mas vecinos, FALSE si no se puede remover
	public static boolean removeSolVertMoreNeighbors(IndependentSet is){
		if(is.isVoid()) {
			//System.out.print("[RMV][removeSolVertMoreNeighbors]:  ");System.out.println("Void");
			return false;}//si la soluci�n es vacia, no hay vertices sol que remover
		int index_v = is.getSolVertMoreNeighbors();//indice del vertice solucion con mas vecinos
		if(index_v ==-1){
			System.out.println("ERROR! indice incorrecto!");
			return false;}
		//System.out.print("[RMV][removeSolVertMoreNeighbors]:  ");System.out.println("["+is.getSol().get(index_v)+"]");
		is.removeVert(index_v);//Removerlo de la Soluci�n
		return true;
	}
	//[T][RMV]	Remueve el vertice sol con menos vecinos, FALSE si no se puede remover
	public static boolean removeSolVertFewerNeighbors(IndependentSet is){
		
		if(is.isVoid()) {
			//System.out.print("[RMV][removeSolVertFewerNeighbors]:  ");System.out.println("Void");
			return false;}//si la soluci�n es vacia, no hay vertices sol que remover
		int index_v = is.getSolVertFewerNeighbors();//indice del vertice solucion con menos vecinos
		if(index_v ==-1){
			System.out.println("ERROR! indice incorrecto!");
			return false;}
		//System.out.print("[RMV][removeSolVertFewerNeighbors]:  ");System.out.println("["+is.getSol().get(index_v)+"]");
		is.removeVert(index_v);//Removerlo de la Soluci�n
		return true;
	}
	//[T][RMV]	Remueve el vertice sol con mas vecinos 1_Tight, FALSE si no se puede remover
	public static boolean removeSolVertMore1_TightNeighbors(IndependentSet is){
		if(is.isVoid()) {
			//System.out.print("[RMV][removeSolVertMore1_TightNeighbors]:  ");System.out.println("Void");
			return false;}//si la soluci�n es vacia, no hay vertices sol que remover
		int index_v = is.getSolVertMore1_TightNeighbors();//indice del vertice solucion con mas vecinos
		if(index_v ==-1){
			//System.out.println("ERROR! indice incorrecto!");
		return false;}
		//System.out.print("[RMV][removeSolVertMore1_TightNeighbors]:  ");System.out.println("["+is.getSol().get(index_v)+"]");
		is.removeVert(index_v);//Removerlo de la Soluci�n
		return true;
	}
	public static boolean removeALLVert1_TightNeighbors(IndependentSet is){
		if(is.isVoid()) {
			//System.out.print("[RMV][removeSolVertMore1_TightNeighbors]:  ");System.out.println("Void");
			return false;}//si la soluci�n es vacia, no hay vertices sol que remover
		if(!is.isMaximal()) {
			return false;}
		boolean borro = false;
		//int ind=1;
		//System.out.print("oli");is.Print_Cants();
		int index_v = is.getSolVertMore1_TightNeighbors();//indice del vertice solucion con mas vecinos
		if(index_v ==-1)index_v = is.getSolVertMore2_TightNeighbors();
		//System.out.println("antes");
		//is.Print_Cants();
		boolean Hay_ajuste_uno =true;
		if(index_v == -1) Hay_ajuste_uno = false;
		while(is.gettam_free()/10<is.gettam_sol() && Hay_ajuste_uno){
			if(index_v ==-1){
				//System.out.println(".");
				Hay_ajuste_uno=false;}
			else {	
				is.removeVert(index_v);//Removerlo de la Soluci�n
				borro = true;
				index_v = is.getSolVertMore1_TightNeighbors();
				if(index_v == -1) index_v = is.getSolVertMore2_TightNeighbors();
			}
		}
		//System.out.println(is.getSol().subList(0, is.gettam_sol())+" / "+is.getSol().subList(is.gettam_sol(),(is.gettam_sol()+is.gettam_free()))+" - "+is.getSol().subList(is.gettam_sol()+is.gettam_free(), is.getnVert()));
		//System.out.println("despues");
		//is.Print_Cants();
		
		if(borro) {
			//System.out.print("antes");is.Print_Cants();
			//System.out.println(is.getnNeighborsList());
			is.solverCplex();                               //CPLEX CPLEX CPLEX CPLEX CPLEX CPLEX CPLEX CPLEX CPLEX CPLEX CPLEX 
			//System.out.print("despues");is.Print_Cants();
			return true;
		}
		else return false;
	}
	///[T][IMPRV] Busca una Mejora_2, TRUE si la realiza, FALSE si no la encuentra.  
	public static boolean Mejora_2_original(IndependentSet is){
		//System.out.print("[IMPRV][Mejora_2]:  ");
		return is.Mejora_2_original();
	}
	//[T][IMPRV] Busca una Mejora_2 version Directa, TRUE si la realiza, FALSE si no la encuentra.  
	public static boolean Improvements2_Maximal(IndependentSet is){
		if(!is.isMaximal()) {
			//System.out.print("[IMPRV][Improvements2_Maximal]:  ");System.out.println("FALSE:Maximal");
		return false;}
		//System.out.print("[IMPRV][Improvements2_Maximal]:  ");
		return is.Mejora_2_Maximal();
	}  
	//[T][IMPRV] Busca una Mejora_3, TRUE si la realiza, FALSE si no la encuentra.  
	public static boolean Improvements3_Maximal(IndependentSet is){
		if(!is.isMaximal()) {
			//System.out.print("[IMPRV][Improvements3_Maximal]:  ");System.out.println("FALSE:Maximal");
			return false;}
		//System.out.print("[IMPRV][Improvements3_Maximal]:  ");
		return is.Mejora_3_Maximal();
	}
	//[T][IMPRV] Busca una Mejora 2 o 3, TRUE si la realiza, FALSE si no la encuentra.  
	public static boolean OneImprovements2or3_Maximal(IndependentSet is){
		if(!is.isMaximal()) {
			//System.out.print("[IMPRV][OneImprovements2or3_Maximal]:  ");System.out.println("FALSE:Maximal");
			return false;}
		//System.out.print("[IMPRV][OneImprovements2or3_Maximal]:  ");
		return is.M_2o3_Maximal();
	}
	//[T][IMPRV] Busca todas las Mejoras 2 posibles, TRUE si la realiza al menos una, FALSE si no encuentra.  
	public static boolean All_Imprv2_Maximal(IndependentSet is){
		if(!is.isMaximal()) {return false;}
		return is.Ms_2_Maximal();
	}
	//[T][IMPRV] Busca todas las Mejoras 3 posibles, TRUE si la realiza al menos una, FALSE si no encuentra.  
	public static boolean All_Imprv3_Maximal(IndependentSet is){
		if(!is.isMaximal()) {return false;}
		return is.Ms_3_Maximal();
	}
	//[T][IMPRV] Busca todas las Mejoras 2 y 3 posibles, TRUE si realiza al menos una, FALSE si no encuentra.  
	public static boolean All_Imprv2and3_Maximal(IndependentSet is){
		if(!is.isMaximal()) {return false;}//si no es maximal no entra
		return is.Ms_2y3_Maximal();
	}
	//[T][BUILDER] Construye una soluci�n, agregando vertices aleatorios.  
	public static boolean Builder_for_Random(IndependentSet is){
		if(is.isMaximal()) {
			//System.out.print("[BLDR][Builder_for_Random]:  ");System.out.println("FALSE:Maximal");
			return false;}
		//System.out.print("[BLDR][Builder_for_Random]:  ");System.out.println("TRUE");
		is.llenar_por_vertice_libre_aleatorio();
		return true;
	}
	//[T][BUILDER] Construye una soluci�n, agregando vertices segun el criterio del vertice con m�s vecinos.  
	public static boolean Builder_for_MoreNeighbors(IndependentSet is){
		if(is.isMaximal()) {
			//System.out.print("[BLDR][Builder_for_MoreNeighbors]:  ");System.out.println("FALSE:Maximal");
		return false;}
		//System.out.print("[BLDR][Builder_for_MoreNeighbors]:  ");System.out.println("TRUE");	
		is.llenar_por_mayor_num_vecinos();
		return true;
	}
	//[T][BUILDER] Construye una soluci�n, agregando vertices segun el criterio del vertice con menos vecinos.  
	public static boolean Builder_for_FewerNeighbors(IndependentSet is){
		if(is.isMaximal()) {
			//System.out.print("[BLDR][Builder_for_FewerNeighbors]:  ");System.out.println("FALSE:Maximal");
			return false;}
		//System.out.print("[BLDR][Builder_for_FewerNeighbors]:  ");System.out.println("TRUE");
		is.llenar_por_menor_num_vecinos();
		return true;
	}
	//[T][BUILDER] Construye una soluci�n, agregando vertices segun el criterio del vertice con menos vecinos.  
	public static boolean Builder_for_FewerFreeNeighbors(IndependentSet is){
		if(is.isMaximal()) {
			//System.out.print("[BLDR][Builder_for_FewerFreeNeighbors]:  ");System.out.println("FALSE:Maximal");
		return false;}
		//System.out.print("[BLDR][Builder_for_FewerFreeNeighbors]:  ");System.out.println("TRUE");
		is.llenar_por_menor_num_vecinos_libres();
		return true;
	}
	//[T][BUILDER] Construye una soluci�n, por recorrido en anchura.  
	public static boolean Builder_BFS(IndependentSet is){
		if(is.isMaximal()) return false;
		double profit;
		double maxprofit = 0;
		int id = 0;
		for(int i =0; i<is.getnVert(); i++) {
			IndependentSet new_sol = is.clone();
			profit = new_sol.llenar_por_BFS(i);
			//System.out.println(" profit: "+ maxprofit + " i: " + i);	
			if(profit >= maxprofit) {
				maxprofit= profit;
				id = i;
					
			}
		}
		
		is.llenar_por_BFS(id);
		//System.out.println("profit "+is.getCurrentProfit());
		return true;
	}
	public static boolean Builder_BFS2(IndependentSet is, int id){
		if(is.getCurrentProfit()!=0) return false;
		is.llenar_por_BFS(id);
		//System.out.println("profit: "+ is.getCurrentProfit() + " i: " + id);
		return true;
	}
	//[T][METAHEURISTIC] Realiza perturbaciones seguido de mejoras, mantiene la mejor solucion obtenida.
	public static boolean ILS(IndependentSet is){
		if(!is.isMaximal()) {
			//System.out.print("[METAHEURISTIC][ILS]:  ");System.out.println("FALSE:Maximal");
			return false;}
		//System.out.print("[METAHEURISTIC][ILS]:  ");
		is.ILS();
		return true;
	}
		public static boolean SBTS(IndependentSet is, Instance ins){
			if(!is.isMaximal()) {
		//System.out.print("[METAHEURISTIC][SBTS]:  ");System.out.println("FALSE:Maximal");
		return false;}
	//System.out.print("[METAHEURISTIC][SBTS]:  ");
	try {
	int optV = 0;
	boolean opt = ins.isOptimalKnown();
	if(opt)
		optV = ins.getOptimal();
	else
		optV =  ins.getFeasible();
	
	is.callSBTS(ins.getName(), optV);
	} catch (IOException e) {
		    // TODO Auto-generated catch block
	
		    e.printStackTrace();
		}
	return true;
	}
	 */
}
