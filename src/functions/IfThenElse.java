package functions;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import model.PDPData;

public class IfThenElse extends GPNode {

	private static final long serialVersionUID = 1589755750993162501L;
	
	public String toString() { return "If_Then_Else"; }
	
	public void checkConstraints(
			final EvolutionState state, final int tree,
			final GPIndividual typicalIndividual, final Parameter individualBase) {
        
		super.checkConstraints(state, tree, typicalIndividual, individualBase);
        
        if (children.length != 3) {
            state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase);
        }
    }
	
	@Override
	public void eval(
			final EvolutionState state, final int thread,
			final GPData input, final ADFStack stack,
			final GPIndividual individual, final Problem problem) {
		//System.out.println("ifthenelse");
    	//Validations
		PDPData pdpd = (PDPData) input;
		//MISProblemEvo mispp = (MISProblemEvo) problem;
		
		//Evalua al hijo izquierdo
		children[0].eval(state, thread, pdpd, stack, individual, problem);
		//System.out.println("If_Izquierdo - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
		//Si es verdadero evaluar al hijo derecho 1
		if(pdpd.getResult()) {
			children[1].eval(state, thread, pdpd, stack, individual, problem);
			//System.out.println("If_Centro - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
			pdpd.setResult(true);
			return;
		}
		//Si es falso evaluar al hijo derecho 2
		else {
			children[2].eval(state, thread, pdpd, stack, individual, problem);
			//System.out.println("If_Derecho - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
			pdpd.setResult(true);
			return;
		}
    }
}
