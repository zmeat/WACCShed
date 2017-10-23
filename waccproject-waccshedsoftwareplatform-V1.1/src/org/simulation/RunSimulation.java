/*
 * Author: Diego Cardoso
 * Copyright (c) 2016, WACC and individual contributors as listed at
 * https://wacc.las.iastate.edu/
 * All rights reserved. 
 */
package org.simulation;

import org.configuration.SimulationParameters;
import org.simulation.parallel.SimulationThreadMonitor;
import org.simulation.parallel.SimulationThreadPool;
import org.utilities.internal.DataBaseManager;
import org.utilities.internal.HSQLDataBaseManager;
import org.utilities.internal.mysqlDataBaseManager;
import org.utilities.time.SystemCalendar;

/**
 * Entry point for simulation execution.
 * @author Diego Cardoso
 *
 */
public class RunSimulation {
	
	public static void main(String[] args){
		/* Parameters for simulation */
		// Max number of concurrent simulations
		int maxConcurrentSims = 4;
		
		// Parameter space setup for sensitivity analysis
		int numScenario = 31;
		double[] theta = {100, 5000, 20000}; 
		double[] LQE = {51.48, 98.2};
		double[] d = {125.0001, 126};
		boolean[] cmMode = {false, true};
		boolean[] fMode = {false, true};
		/* ************************** */
		
		
		// The simulation parameter space is a tensor product of all possible dimensions. In this case,
		// we have 6 sets of parameters and a specific number of scenarios (read from files) 
		int simTotal = numScenario * theta.length * LQE.length * d.length * cmMode.length * fMode.length;
		
		SimulationThreadPool pool = new SimulationThreadPool(maxConcurrentSims);
		SystemCalendar simulationTimestamp = new SystemCalendar(System.currentTimeMillis());
		
//		 try {
//		     Class.forName("org.hsqldb.jdbc.JDBCDriver" );
//		 } catch (Exception e) {
//		     System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
//		     e.printStackTrace();
//		     return;
//		 }
//		DataBaseManager dbSim = new HSQLDataBaseManager();
		
		// = =======================  20171022
		 try {
		     Class.forName("com.mysql.jdbc.Driver" );
		 } catch (Exception e) {
		     System.err.println("ERROR: failed to load mysql JDBC driver.");
		     e.printStackTrace();
		     return;
		 }
		DataBaseManager dbSim = new mysqlDataBaseManager();

		/* Uncomment the block below to delete all results from previous simulations */ 
//		dbSim.deleteDB("truncate table farmerData_1997_2013 restart identity");
//		dbSim.deleteDB("truncate table cityData_1997_2013 restart identity");
//		dbSim.deleteDB("truncate table simulations restart identity");

		SimulationThreadMonitor monitor = new SimulationThreadMonitor(pool, simulationTimestamp, simTotal);
		monitor.start();
		
		for(int ti=0; ti<theta.length; ti++){
			for(int li = 0; li<LQE.length; li++){
				for(int si=0; si<numScenario; si++){
					// Create and register a new simulation case
					SimulationParameters simParameters = new SimulationParameters(simulationTimestamp,
							si+1, theta[ti], LQE[li], d[0], cmMode[0], fMode[0]);
					
					int simID = registerSimulation(dbSim, simParameters);
					
					if (simID != -1) {
						try {
							pool.notifyStart(simID);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// Identify and execute a simulation case
						simParameters.setSimulationID(simID);
						SimulationCase simServerManager = new SimulationCase(simParameters, pool);
						simServerManager.start();
					}
					else {
						System.out.println("Failed to register simulation "+simParameters.toConsoleString());
						return;
					}
				}
			}
		}
		
		// Wait for all runs to finish and then persists inserted data on db
		while (pool.getFinishedCount() < simTotal) {
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		dbSim.queryDB("SHUTDOWN");
		
		// Reports total time
		String finishMessage = "Simulations finished. Total time elapsed: "
				+ SystemCalendar.GetFormattedInterval(System.currentTimeMillis() - simulationTimestamp.getTimeInMillis());
		System.out.println(finishMessage);
	
	}
	
	/**
	 * Register a specific simulation run (Time stamp and parameters)
	 * A unique key is generated, which is then used by simulation modules to
	 * record results associated to each run 
	 * @param dbSim
	 * @param simParameters Parameters of a specific simulation run
	 * @return unique key associated to simulation run described by the parameters
	 */
	private static Integer registerSimulation(DataBaseManager dbSim, SimulationParameters simParameters) {
		int tempCm = simParameters.isCM_ON() == true ? 1 : 0;
		int tempF = simParameters.isF_ON() == true ? 1 : 0;
		
		String insertCommand = "INSERT INTO simulations(sim_id, sim_time, sim_scenario,sim_theta,"+
				"sim_lqe, sim_d, sim_cm_on, sim_f_on) VALUES("
				+ "NULL,'"
				+ simParameters.getTimestamp().CalendarToTimestamp()+"','"
				+ simParameters.getScenarioID()+"','"
				+ simParameters.getTheta()+"','"
				+ simParameters.getLQE()+"','"
				+ simParameters.getD()+"','"
				+ tempCm+"','"
				+ tempF
				+ "')";
		System.out.println(insertCommand);
		int simID = dbSim.insertDBreturnKey(insertCommand);
		return simID;
	}
}