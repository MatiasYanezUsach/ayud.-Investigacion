package model;

//import java.util.ArrayList;

public class Instance {
	
	private String name;			//Nombre de la instancia
	private double optimal;			//El valor �ptimo conocido para la instancias. Evaluado en 0 si no se conoce
	private double feasible;			//El mejor valor conocido para la instancia. Evaluado en 0 si ya se conoce el �pimo
	private boolean optimalKnown;	//Verdadero si el �ptimo de la instancia es conocido.
	
	//private IndependentSet IndependentSet;
	private PDPInstance pdpInstance;

	//CONSTRUCTOR
	public Instance() {
		this.optimal = 0;
		this.feasible = 0;
		this.optimalKnown = true;
		this.pdpInstance = new PDPInstance();

		//IndependentSet = new IndependentSet();
	}

	//GETTERS
	public String getName() {		return name;	}
	public double getOptimal() {		return optimal;	}
	public double getFeasible() {		return feasible;	}
	public boolean isOptimalKnown() {		return optimalKnown;	}
	//public IndependentSet getIndependentSet() {		return IndependentSet;	}
	public PDPInstance getPdpInstance() {		return pdpInstance;	}

	//SETTERS
	public void setName(String name) {		this.name = name;	}
	public void setOptimal(double optimal) {	this.optimal = optimal;	}
	public void setFeasible(double feasible) {	this.feasible = feasible;	}
	public void setOptimalKnown(boolean optimalKnown) {	this.optimalKnown = optimalKnown;	}
	//public void setIndependentSet(IndependentSet independentset) {	IndependentSet = independentset;	}
	public void setPdpInstance(PDPInstance pdpInstance) {	this.pdpInstance = pdpInstance;	}

	public Instance clone() {
		Instance i = new Instance();
		i.optimal = this.optimal;
		i.feasible = this.feasible;
		i.optimalKnown = this.optimalKnown;
		i.name = this.name;
		i.pdpInstance = this.pdpInstance.clone();
		return i;
	}

	//TO STRING
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("Instancia: " + name).append("\n");
		buffer.append("Cantidad Vertices: " + pdpInstance.getnNodes()).append("\n");
		
		if(isOptimalKnown())// pregunta si el optimo es conocido
			buffer.append("�ptimo: " + optimal).append("\n");//true
		else
			buffer.append("Mejor: " + feasible);//false

		return buffer.toString();
	}

}

