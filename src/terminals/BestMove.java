package terminals;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;
import model.PDPData;
import model.Terminal;

public class BestMove extends GPNode {

    private static final long serialVersionUID = -2534468795456857335L;

    public String toString() { return "BestMove"; }

    public void checkConstraints(
            final EvolutionState state, final int tree,
            final GPIndividual typicalIndividual, final Parameter individualBase) {
        super.checkConstraints(state, tree, typicalIndividual, individualBase);

        if (children.length != 0) {
            state.output.error("Incorrect number of children for node " + toStringForError() + " at " + individualBase);
        }
    }

    @Override
    public void eval(final EvolutionState state, final int thread,
                     final GPData input, final ADFStack stack,
                     final GPIndividual individual, final Problem problem) {

        PDPData pdpd = (PDPData) input;

        pdpd.setResult(Terminal.best_move(pdpd.getInstance().getPdpInstance()));
        //System.out.println("Add_FewerFNeighb - "+mispd.getIndependenSet().getConjuntoSolucion()+" - "+mispd.getIndependenSet().getCurrentProfit()+ " - "+ mispd.getResult());
    }
}
