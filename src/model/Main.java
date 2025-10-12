package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ilog.concert.*;
import ilog.cplex.*;
import org.apache.commons.math3.util.Precision;

//Funcion ajena a ECJ, Se utiliza para probar que los terminales, métodos de otras funciones, y secuencias de alg. generados por PG, se ejecuten correctamente

public class Main {

	public static void main(String[] args) throws IOException {
		readInstancesTest();

	}

	private static void cplexTest() throws IOException {
		Instance pdpi = readTest("data/R121_15_80.txt");


		try {
			IloCplex cplex = new IloCplex();
			// create model and solve it

			//IloNumVar[] x = cplex.numVarArray(3, 0.0, 100.0);
			IloNumVar x = cplex.numVar(0, Double.MAX_VALUE, "x");
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE, "y");

			IloLinearNumExpr objective = cplex.linearNumExpr();
			objective.addTerm(0.12, x);
			objective.addTerm(0.15, y);

			cplex.addMinimize(objective);

			cplex.addGe(cplex.sum(cplex.prod(60, x), cplex.prod(60, y)), 300);
			cplex.addGe(cplex.sum(cplex.prod(12, x), cplex.prod(6, y)), 36);
			cplex.addGe(cplex.sum(cplex.prod(10, x), cplex.prod(30, y)), 90);

			if (cplex.solve()) {
				System.out.println("obj = " + cplex.getObjValue());
				System.out.println("x = " + cplex.getValue(x));
				System.out.println("y = " + cplex.getValue(y));
			} else {
				System.out.println("Model not solved");
			}


		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}


	private static PDPInstance cplexTestPDP(PDPData pdpData, int time) throws IOException {
		PDPInstance pdpi = pdpData.getPDPInstance();

		int n = pdpi.getCostMatrix().size();
		List<ArrayList<Float>> costMatrix = pdpi.getCostMatrix();
		List<Float> d = pdpi.getDeliveryQuantities();
		List<Float> p = pdpi.getPickupQuantities();
		Float Q = pdpi.getVehicleCapacity();

		try {
			IloCplex cplex = new IloCplex();
			//variables
			IloIntVar[][] x = new IloIntVar[n][];
			IloNumVar[][] y = new IloNumVar[n][];
			IloNumVar[][] z = new IloNumVar[n][];

			for (int i = 0; i < n; i++) {
				x[i] = cplex.boolVarArray(n);
				y[i] = cplex.numVarArray(n, 0, Q);
				z[i] = cplex.numVarArray(n, 0, Q);
			}

			//Create objective function and associate with model
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					objective.addTerm(costMatrix.get(i).get(j), x[i][j]);
				}
			}

			cplex.addMinimize(objective);

			//constraints
			// sum xij = 1 para cada j
			IloLinearNumExpr[] SumXij = new IloLinearNumExpr[n];

			for (int i = 1; i < n; i++) {
				SumXij[i] = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						SumXij[i].addTerm(1, x[i][j]);
					}

				}
				cplex.addEq(SumXij[i], 1);
			}


			//sum xij -sum xji = 0 para cada j en V
			IloLinearNumExpr[] NegativeSumXji = new IloLinearNumExpr[n];
			for (int i = 0; i < n; i++) {
				NegativeSumXji[i] = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						NegativeSumXji[i].addTerm(-1.0, x[j][i]);
					}
				}
			}

			for (int i = 0; i < n; i++) {
				cplex.addEq(cplex.sum(cplex.sum(x[i]), NegativeSumXji[i]), 0);
			}

			/*//sum x0i < K ESTA RESTRICCION SE NO SE USA
			IloLinearNumExpr SumX0i = cplex.linearNumExpr();
			for (int i = 1; i < n; i++) {
				SumX0i.addTerm(1,x[0][i]);

			}

			//cplex.addLe(SumX0i,K);*/


			//sum yij -sum yji = 0 para cada j en C
			IloLinearNumExpr[] NegativeSumYji = new IloLinearNumExpr[n];
			for (int i = 1; i < n; i++) {
				NegativeSumYji[i] = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						NegativeSumYji[i].addTerm(-1.0, y[j][i]);
					}

				}
			}

			IloLinearNumExpr[] SumYij = new IloLinearNumExpr[n];
			for (int i = 0; i < n; i++) {
				SumYij[i] = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						SumYij[i].addTerm(1.0, y[i][j]);
					}

				}
			}

			for (int i = 1; i < n; i++) {
				cplex.addEq(cplex.sum(SumYij[i], NegativeSumYji[i]), d.get(i - 1));
			}

			//sum zij -sum zji = 0 para cada j en C
			IloLinearNumExpr[] NegativeSumZij = new IloLinearNumExpr[n];
			for (int i = 1; i < n; i++) {
				NegativeSumZij[i] = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						NegativeSumZij[i].addTerm(-1.0, z[i][j]);
					}

				}
			}

			IloLinearNumExpr[] SumZji = new IloLinearNumExpr[n];
			for (int i = 0; i < n; i++) {
				SumZji[i] = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						SumZji[i].addTerm(1.0, z[j][i]);
					}

				}
			}

			for (int i = 1; i < n; i++) {
				cplex.addEq(cplex.sum(SumZji[i], NegativeSumZij[i]), p.get(i - 1));
			}

			//yij + zij < Qxij para cada i,i i distinto de j

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						cplex.addLe(cplex.sum(y[i][j], z[i][j]), cplex.prod(Q, x[i][j]));
					}
				}
			}


			cplex.setParam(IloCplex.Param.Simplex.Display, 0);
			cplex.setParam(IloCplex.Param.MIP.Display, 0);
			cplex.setParam(IloCplex.Param.TimeLimit, time);


			double start = cplex.getCplexTime();

			if (cplex.solve()) {
				System.out.println("obj = " + cplex.getObjValue());

				System.out.println("status = " + cplex.getStatus());

				pdpi.setTotalCost(cplex.getObjValue());
				pdpi.setTime(cplex.getCplexTime() - start);
				pdpi.setOptimal(cplex.getStatus().toString().equals("Optimal"));
				return pdpi;

			} else {
				System.out.println("problem not solved");
			}

			cplex.end();
		} catch (IloException exc) {
			exc.printStackTrace();
		}

		return null;
	}

	public static void calculateOptimals() throws IOException {
		ArrayList<PDPData> pdpDataList = new ArrayList<>();


		FileIO.readInstances(pdpDataList, "data/evolutionAll");
		System.out.println(pdpDataList);


		int[] times = {1, 3, 10, 30, 60};

		for (int time : times) {
			List<String> instanceNames = new ArrayList<>();
			List<Double> objectiveValues = new ArrayList<>();

			List<Boolean> optimals = new ArrayList<>();
			for (PDPData pdpData : pdpDataList) {
				PDPInstance pdpi = cplexTestPDP(pdpData, time);
				instanceNames.add(pdpData.getInstance().getName());
				if (pdpi != null) {
					double objValue = pdpi.getTotalCost();

					objectiveValues.add(objValue);
					optimals.add(pdpi.isOptimal());

				} else {
					objectiveValues.add(-1.0);
					optimals.add(false);
				}


			}

			PrintWriter writer1 = new PrintWriter("data/results" + "Time" + time + ".txt", String.valueOf(StandardCharsets.UTF_8));
			//PrintWriter writer2 = new PrintWriter("data/times" + classNumber +".txt", StandardCharsets.UTF_8);


			for (int i = 0; i < instanceNames.size(); i++) {
				String optimality;
				if (optimals.get(i)) {
					optimality = "Optimal";
				} else {
					optimality = "Feasible";
				}

				String filename = instanceNames.get(i).replace(".txt", "");
				String line1 = filename + " " + objectiveValues.get(i) + " " + optimality;

				System.out.println("Time " + time + " " + line1);
				writer1.println(line1);
				//writer2.println(line2);
			}

			writer1.close();
		}


		//writer2.close();


	}

	private static void cplexTest2() throws IOException {
		int n = 4;
		int m = 3;
		double[] p = {310.0, 380.0, 350.0, 285.0}; //profit
		double[] v = {480.0, 650.0, 580.0, 390.0}; //volume per ton of cargo
		double[] a = {18.0, 15.0, 23.0, 12.0}; //available weight
		double[] c = {10.0, 16.0, 8.0}; //capacity of compartment
		double[] V = {6800.0, 8700.0, 5300.0}; //volume capacity of

		try {
			IloCplex cplex = new IloCplex();
			//variables
			IloNumVar[][] x = new IloNumVar[n][];

			for (int i = 0; i < n; i++) {
				x[i] = cplex.numVarArray(m, 0, Double.MAX_VALUE);

			}
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE);
			//expressions
			IloLinearNumExpr[] usedWeightCapacity = new IloLinearNumExpr[m];
			IloLinearNumExpr[] usedVolumeCapacity = new IloLinearNumExpr[m];

			for (int j = 0; j < m; j++) {
				usedWeightCapacity[j] = cplex.linearNumExpr();
				usedVolumeCapacity[j] = cplex.linearNumExpr();
				for (int i = 0; i < n; i++) {
					usedWeightCapacity[j].addTerm(1.0, x[i][j]);
					usedVolumeCapacity[j].addTerm(v[i], x[i][j]);
				}
			}
			IloLinearNumExpr objective = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < m; j++) {
					objective.addTerm(p[i], x[i][j]);
				}
			}

			//associate objective with model

			cplex.addMaximize(objective);
			//constraints
			for (int i = 0; i < n; i++) {
				cplex.addLe(cplex.sum(x[i]), a[i]);
			}
			for (int j = 0; j < m; j++) {
				cplex.addLe(usedWeightCapacity[j], c[j]);
				cplex.addLe(usedVolumeCapacity[j], V[j]);
				cplex.addEq(cplex.prod(1 / c[j], usedWeightCapacity[j]), y);
			}

			cplex.setParam(IloCplex.Param.Simplex.Display, 0);

			if (cplex.solve()) {
				System.out.println("obj = " + cplex.getObjValue());
			} else {
				System.out.println("problem not solved");
			}

			cplex.end();
		} catch (IloException exc) {
			exc.printStackTrace();
		}

	}

	//METODO QUE COMPRUEBA LA CLASE INSTANICIA Y SUS TERMINALES
	private static void test() {
		PDPInstance pdp = PDPInstance.createRandomPDP(80, 4);

		pdp.printPDPInstance();

		while (pdp.add_nearest_neighbor_last_node()) {

		}

		while (pdp.worst_move()) {
			System.out.println("-------------------------------------------------------");
			pdp.printRoutes();
			pdp.printVisited();
			pdp.printNotVisited();
			pdp.printCurrentVehiclesLoadNodes();
			pdp.printCurrentDeliveryQuantities();
			pdp.printCurrentPickupQuantities();
			pdp.printTotalCost();
		}
		pdp.printRoutes();
		pdp.printTotalCost();
	}

	private static void cloneTest() {
		PDPInstance pdp = PDPInstance.createRandomPDP(4, 4);
		PDPInstance pdpClone = pdp.clone();
		System.out.println("ORIGINAL--------------------");
		pdp.printPDPInstance();
		System.out.println("CLON--------------------");
		pdpClone.printPDPInstance();
	}

	private static Instance readTest(String filename) throws IOException {
		Instance pdpi = new Instance();
		FileIO.readFile(pdpi, filename);
		pdpi.getPdpInstance().printPDPInstance();
		return pdpi;
	}

	private static void readInstancesTest() {
		ArrayList<PDPData> dataList = new ArrayList<>();

		try {
			FileIO.readInstances(dataList, "data/evaluation");
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (PDPData data : dataList) {
			//System.out.println("***Instancia: " + data.getInstance().getName() + "***");

			PDPInstance pdpi = data.getPDPInstance();
			//pdpi.printPDPInstance();
			double start = System.nanoTime();
// CODE HERE

			pdpi.cplex_terminal();
			double finish = System.nanoTime();
			double timeElapsed = ((finish - start) / 1000000) ;//en milisegundos
			double finalTime = timeElapsed < 60000 ? timeElapsed : 60000;
			System.out.println(data.getInstance().getName()+ ", " + finalTime + ", " + pdpi.getTotalCost());



		}
		/*

		dataList = new ArrayList<>();

		try {
			FileIO.readInstances(dataList, "data/evolution");
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (PDPData data : dataList) {
			System.out.println("***Instancia: " + data.getInstance().getName() + "***");
			PDPInstance pdpi = data.getPDPInstance();
			pdpi.printPDPInstance();

			//Test nearest
			while(pdpi.add_farthest_neighbor_last_node())


			while (pdpi.best_move()) {

			}

			System.out.println("Instancia best move: ");
			pdpi.printTotalCost();
			pdpi.printRoutes();
			pdpi.printCurrentVehiclesLoadNodes();


		}*/


	}

	private static void readResultsFile() throws FileNotFoundException {
		File file = new File("data/results.txt");
		Scanner s = new Scanner(file);
		boolean found = false;
		while(s.hasNextLine() && !found) {
			boolean isOptimal = false;
			String instName = s.next();
			double token = s.nextDouble();
			double objective = Precision.round(token,2);
			if(s.next().equals("Optimal")) {
				isOptimal = true;
			}

			System.out.println(instName + " " + objective);

		}
		s.close();
	}


}