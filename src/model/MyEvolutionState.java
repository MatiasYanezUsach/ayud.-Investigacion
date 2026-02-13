package model;

import ec.simple.SimpleEvolutionState;

public class MyEvolutionState  extends SimpleEvolutionState {

	private static final long serialVersionUID = 2626239927902040327L;

	public void startFresh() {
		// setup() hasn't been called yet, so very few instance variables are valid at this point.
		// Here's what you can access: parameters, random, output, evalthreads, breedthreads,
		// randomSeedOffset, job, runtimeArguments, checkpointPrefix,
		// checkpointDirectory
		// Let's modify the 'generations' parameter based on the job number
		
		int jobNum = ((Integer)(job[0])).intValue();
		int genNum =  parameters.getInt(new ec.util.Parameter("generations"),null);
		
		// Leer el número de grupo del experimento (default: -1 para backward compatibility)
		int groupNum = parameters.getIntWithDefault(new ec.util.Parameter("experiment.group"), null, -1);
		
		if(genNum!=1){
			if(groupNum >= 0) {
				// Nuevo formato: out/results/grupo{G}/evolution{jobNum}
				parameters.set(new ec.util.Parameter("stat.file"), "$out/results/grupo" + groupNum + "/evolution" + jobNum + "/Statistics.out");
			} else {
				// Formato antiguo (backward compatibility)
				parameters.set(new ec.util.Parameter("stat.file"), "$out/results/evolution" + jobNum + "/Statistics.out");
			}
		}else{
			parameters.set(new ec.util.Parameter("stat.file"), "$out/results/evaluation/Statistics.out");
		}
		// call super.startFresh() here at the end. It'll call setup() from the parameters
		super.startFresh();
	}
}