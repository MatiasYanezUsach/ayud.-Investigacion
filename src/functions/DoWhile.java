package functions;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import model.PDPData;

import java.util.ArrayList;
import java.util.List;

public class DoWhile extends GPNode {

	private static final long serialVersionUID = 5933556719619805043L;

	public String toString() { return "While"; }

	public void checkConstraints(
			final EvolutionState state, final int tree,
			final GPIndividual typicalIndividual, final Parameter individualBase) {

		super.checkConstraints(state, tree, typicalIndividual, individualBase);

		if (children.length != 2) {
			state.output.error("Incorrect number of children for node " +  toStringForError() + " at " + individualBase);
		}
	}

	@Override
	public void eval(
			final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {
		
		boolean x, y;
		int i, n;
		double lastCost;
		
		PDPData pdpd = (PDPData) input;
		//MISProblemEvo mispp = (MISProblemEvo) problem;

		children[0].eval(state, thread, pdpd, stack, individual, problem);
		n = pdpd.getInstance().getPdpInstance().getCostMatrix().size();//por el numero de vertices de la instancia
		x = pdpd.getResult();//resultado de la condicion
		i = 0;//numero de veces que se ha entrado al ciclo
		lastCost = pdpd.getPDPInstance().getTotalCost();//= -1;
		List<ArrayList<Integer>> Liste = pdpd.getPDPInstance().getRoutesCopy();// debe ser una copia
		y = true;
		//System.out.println("While_Izquierdo - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
		//Revisar
		while(x && y && i < n) {//MIENTRAS X, Y e I menor a N, sean verdadero
			//System.out.println("While: " + i + x + y);
			children[1].eval(state, thread, pdpd, stack, individual, problem);

			//System.out.println("While_Derecho - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
			if(pdpd.getPDPInstance().getTotalCost() == lastCost && pdpd.getPDPInstance().compareRoutes(Liste)) {

				y = false;

			}	
			else {
				lastCost = pdpd.getPDPInstance().getTotalCost();
				Liste =  pdpd.getPDPInstance().getRoutesCopy();
				children[0].eval(state, thread, pdpd, stack, individual, problem);
				//System.out.println("While_Izquierdo2 - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
				x = pdpd.getResult();
				i++;
			}
		}
		//Si el while iter� al menos una vez se considera resultado verdaderos, caso contrario es falso
		pdpd.setResult(i > 0);
	}
}
