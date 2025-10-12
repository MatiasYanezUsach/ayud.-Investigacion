package model;

import java.util.ArrayList;

public class Generacion {
    private final ArrayList<Individuo> List_Indv;
    
    public Generacion(){
    	List_Indv = new ArrayList<Individuo>();
    }
    public void AgregarIndv(Individuo indv){
		List_Indv.add(indv);
    }
    public void AgregarIndv(int gen, String id,float adjs,float stand, double erp, double erl, int hits,double time,long nodos,int altura){
    	Individuo indv = new Individuo();
		indv.generation = gen;
		indv.id = id;
		indv.Adjusted = adjs;
		indv.Standardized = stand;
		indv.id2 = List_Indv.size();
		indv.ERL = erl;
		indv.ERP = erp;
		indv.Hits = hits;
		indv.time = time;
		indv.tam = nodos;
		indv.depth = altura;
		if(indv!=null)List_Indv.add(indv);
    }
    public Individuo get(int index){
    	return List_Indv.get(index);
    }
    //=====================================

	public int bestHits(){
		int best=0;
		for(int i=0;i<List_Indv.size();i++){
			if(List_Indv.get(i).Hits>best)
				best = List_Indv.get(i).Hits;
		}
		return best;
	}
	public float AvgHits(){
		float acum=0;
		for(int i=0;i<List_Indv.size();i++){
			acum = acum + List_Indv.get(i).Hits;
		}
		return (acum/List_Indv.size());
	}
    public float BestFitnessEvo(){
    	float best=1;
    	for(int i=0;i<List_Indv.size();i++){
    		if(List_Indv.get(i).Standardized<best)
    			best = List_Indv.get(i).Standardized;
    	}
    	return best;
    }
    public float AvgFitnessEvo(){
    	float acum=0;
    	for(int i=0;i<List_Indv.size();i++){
    		acum = acum + List_Indv.get(i).Standardized;
    	}
    	return (acum/List_Indv.size());
    }
    public float WorstFitnessEvo(){
    	float worst=0;
    	for(int i=0;i<List_Indv.size();i++){
    		if(List_Indv.get(i).Standardized>worst)
    			worst = List_Indv.get(i).Standardized;
    	}
    	return worst;
    }
    public long MinSize(){
    	long min=999;
    	for(int i=0;i<Size();i++){
    		if(List_Indv.get(i).tam<min)
    			min = List_Indv.get(i).tam;
    	}
    	return min;
    }
    public double AvgSize(){
    	long acumSize=0;
    	for(int i=0;i<Size();i++){
    		acumSize = acumSize + List_Indv.get(i).tam;
    		//System.out.println(acumSize);
    	}
    	double  avg = (double) acumSize/Size();
    	return avg;
    }
    public long MaxSize(){
    	long max=0;
    	for(int i=0;i<Size();i++){
    		if(List_Indv.get(i).tam>max)
    			max = List_Indv.get(i).tam;
    	}
    	return max;
    }
    public int MinDepth(){
    	int min=999;
    	for(int i=0;i<Size();i++){
    		if(List_Indv.get(i).depth<min)
    			min = List_Indv.get(i).depth;
    	}
    	return min;
    }
    public double AvgDepth(){
    	int acumDepth=0;
    	for(int i=0;i<Size();i++){
    		acumDepth = acumDepth + List_Indv.get(i).depth;
    	}
    	double  avg = (double) acumDepth/Size();
    	return avg;
    }
    public int MaxDepth(){
    	int max=0;
    	for(int i=0;i<Size();i++){
    		if(List_Indv.get(i).depth>max)
    			max = List_Indv.get(i).depth;
    	}
    	return max;
    }
    public double AvgERL(){
    	double acum=0;
    	for(int i=0;i<Size();i++){
    		acum = acum + List_Indv.get(i).ERL;
    	}
    	return (acum/List_Indv.size());
    }
    public double AvgERP(){
    	double acum=0;
    	for(int i=0;i<Size();i++){
    		acum = acum + List_Indv.get(i).ERP;
    	}
    	return (acum/List_Indv.size());
    }
    public double AvgTime(){
    	double acum=0;
    	for(int i=0;i<Size();i++){
    		acum = acum + List_Indv.get(i).time;
    	}
    	return (acum/List_Indv.size());
    }
    //===================================
    /*public Individuo TheBest(){//desde el principio
    	Individuo best=List_Indv.get(0);
    	for(int i=0;i<Size();i++){
    		if(List_Indv.get(i).Standardized<best.Standardized)
    			best = List_Indv.get(i);
    	}
    	return best;
    }*/
    public Individuo TheBestEvo(){//desde el final, xq al final se coloca el elite
     	boolean nul = false;
     	boolean nul2 = false;
    	int borrar = -1;
    	Individuo best = List_Indv.get(Size()-1);
    	for(int i=List_Indv.size()-1;i>=0;i--){
    		if(List_Indv.get(i)!=null) {
    			if(List_Indv.get(i).Standardized<best.Standardized || (List_Indv.get(i).Standardized<=best.Standardized && List_Indv.get(i).tam<best.tam)) {
    				best = List_Indv.get(i);
    			}
    		}
    		else {borrar = i;
    		nul = true;
    		break;
    		}	
    	}
    	if(nul) {
    	System.out.println(List_Indv+" indice: "+borrar);
    	List_Indv.remove(borrar);
    	best = List_Indv.get(Size()-1);
    	for(int i=List_Indv.size()-1;i>=0;i--){
    		if(List_Indv.get(i)!=null) {
    			if(List_Indv.get(i).Standardized<best.Standardized) {
    				best = List_Indv.get(i);
    			}
    		}
    		else {borrar = i;
    		nul2 = true;
    		break;
    		}	
    	}
    	if(nul2) {
    	System.out.println("indice Null: "+borrar);
    	List_Indv.remove(borrar);
    	
    	}
    	}
    	return best;
    }
    public Individuo TheBestEva(){////desde el principio,una sola generaci�n no hay paso de elites
    	Individuo best=List_Indv.get(0);
    	for(int i=0;i<Size();i++){
    		if(List_Indv.get(i).Standardized<best.Standardized)
    			best = List_Indv.get(i);
    	}
    	return best;
    }
    public int Size(){
    	return List_Indv.size();
    }
}