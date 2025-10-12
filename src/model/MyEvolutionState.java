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
		if(genNum!=1){
			parameters.set(new ec.util.Parameter("stat.file"), "$out/results/evolution" + jobNum + "/Statistics.out");
		}else{
			parameters.set(new ec.util.Parameter("stat.file"), "$out/results/evaluation/Statistics.out");
		}
		// call super.startFresh() here at the end. It'll call setup() from the parameters
		super.startFresh();
	}
}