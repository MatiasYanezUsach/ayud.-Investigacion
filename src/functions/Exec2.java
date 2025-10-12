package functions;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import model.PDPData;

public class Exec2 extends GPNode{
	private static final long serialVersionUID = 5933556719619805043L;

	public String toString() { return "Exec2"; }

	public void checkConstraints(
			final EvolutionState state, final int tree,
			final GPIndividual typicalIndividual, final Parameter individualBase) {
        
		super.checkConstraints(state, tree, typicalIndividual, individualBase);
        
        if (children.length != 2) {
            state.output.error("Incorrect number of children for node " +  toStringForError() + " at " + individualBase);
        }
    }

	public void eval(
			final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {
		PDPData pdpd = (PDPData) input;
		//MISProblemEvo mispp = (MISProblemEvo) problem;
		
		children[0].eval(state, thread, pdpd, stack, individual, problem);
		children[1].eval(state, thread, pdpd, stack, individual, problem);
		
		pdpd.setResult(true);
	}

}
