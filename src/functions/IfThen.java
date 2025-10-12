package functions;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import model.PDPData;

public class IfThen extends GPNode {

	private static final long serialVersionUID = -2407365507599491251L;
	
	public String toString() { return "If_Then"; }
	
	public void checkConstraints(
			final EvolutionState state, final int tree,
			final GPIndividual typicalIndividual, final Parameter individualBase) {
        
		super.checkConstraints(state, tree, typicalIndividual, individualBase);
        
        if (children.length != 2) {
            state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase);
        }
    }
    
	@Override
	public void eval(
			final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {

		//System.out.println("ifthen");
		PDPData pdpd = (PDPData) input;
		//MISProblemEvo mispp = (MISProblemEvo) problem;
		
		children[0].eval(state, thread, pdpd, stack, individual, problem);
		//System.out.println("If_then Izquierdo - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
		if(pdpd.getResult()) {
			children[1].eval(state, thread, pdpd, stack, individual, problem);
			//System.out.println("If_Then derecho - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
			pdpd.setResult(true);
			return;
		}

		pdpd.setResult(false);
    }
}
