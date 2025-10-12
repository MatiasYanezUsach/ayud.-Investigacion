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

public class Repeat extends GPNode {

	private static final long serialVersionUID = 5933556666619805043L;

	public String toString() { return "Repeat"; }

	public void checkConstraints(
			final EvolutionState state, final int tree,
			final GPIndividual typicalIndividual, final Parameter individualBase) {
        
		super.checkConstraints(state, tree, typicalIndividual, individualBase);
        
        if (children.length != 1) {
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
		
		n = pdpd.getInstance().getPdpInstance().getCostMatrix().size();
		x = pdpd.getResult();
		i = 0;
		lastCost = -1;
		y = true;
		List<ArrayList<Integer>> Liste = pdpd.getPDPInstance().getRoutesCopy();// debe ser una copia
		
		while(x && y && i < n) {
			children[0].eval(state, thread, pdpd, stack, individual, problem);
			if(pdpd.getPDPInstance().getTotalCost() == lastCost && pdpd.getPDPInstance().compareRoutes(Liste)) {
				y = false;
			}
			else {
				lastCost = pdpd.getPDPInstance().getTotalCost();
				Liste =  pdpd.getPDPInstance().getRoutesCopy();
				x = pdpd.getResult();
				if(i==(n/2))System.out.println("Ejecucion atrapada: [Repeat]: "+children[0].toString());
				i++;
			}
		}
		pdpd.setResult(i > 0);
	}
}