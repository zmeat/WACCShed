/*
* Copyright (c) 2011, GLOWA-Danube and individual contributors as listed at
* http://www.glowa-danube.de/de/opendanubia/framework_core.php
* All rights reserved. 
*
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions 
* are met: 
* * Redistributions of source code must retain the above copyright notice, 
* this list of conditions and the following disclaimer. 
* * Redistributions in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation 
* and/or other materials provided with the distribution. 
* * Neither the name of GLOWA-Danube nor the names of its contributors 
* may be used to endorse or promote products derived from this software without
* specific prior written permission. 
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
* POSSIBILITY OF SUCH DAMAGE.
* 
*  Changed on 23.02.2016 by Diego Cardoso.
* - Included SimulationParameters
* 
*  Changed on 27.02.2016 by Diego Cardoso.
* - Included console messages
* - Included notifications to SimulationCase for Thread management purposes
*/
/**
 * SimulationAdmin.java
 * 
 * Administrates a simulation run, in particular synchronizes the simulation components
 * on initializing and finishing, receives (error) messages from the components and
 * broadcasts messages to the components
 * 
 * @author Matthias Ludwig
 * @
 */
package org.simulation;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.configuration.ConfigurationManager;
import org.configuration.SimulationMetaData;
import org.configuration.SimulationParameters;
import org.simulation.coordination.Timecontroller;


public class SimulationAdmin{

	private static Logger logger = Logger.getLogger(SimulationAdmin.class);
	
	private Set<String> components;
	private Set<String> notInitializedComponents;
	private Set<String> notFinishedComponents;
	
	private long tstart;
	private long tend;
	private SimulationParameters simulationParameters;

	private boolean abortFlag = false;
	private Object abortSyncObject = new Object();
	private Timecontroller tc;
	
	private SimulationCase serverManager;


	private int recCount;
	
	public SimulationAdmin(SimulationCase serverManager, ConfigurationManager confManager,
				SimulationParameters simulationParameters, Timecontroller tc) {

		tstart = System.currentTimeMillis();
		System.out.println("Simulation start - "+simulationParameters.toConsoleString());
		this.simulationParameters = simulationParameters;
		this.serverManager = serverManager;
		this.components = confManager.getComponentIds();
		this.tc = tc;

		notInitializedComponents = new HashSet<String>(components);

		notFinishedComponents = new HashSet<String>(components);

		recCount = components.size();
		logger.debug("Number of components: " + recCount);
		logger.debug("SimulationAdmin initialized with components: " + components.toString());
	}
	
	public SimulationParameters getSimulationParameters() {
		return simulationParameters;
	}

	public boolean isFinished(){
		if(notFinishedComponents.isEmpty())
			return true;
		else
			return false;
	}
	// implement provided interfaces
	// SimulationAdminAccess
	
	/**
	 * With this method a simulation component signals that it has finished initialization.
	 * The calling thread is blocked until all controllers have finished initialization. This ensures
	 * starting the simulation models with correct import data (note that controllers are not synchronized
	 * by the Timecontroller!)
	 * 
	 * @param compId - The component id of the calling component
	 */
	public void initFinished(String compId) throws InterruptedException {
		synchronized (notInitializedComponents) {
			logger.debug("Component " + compId + " finished initialization.");
			notInitializedComponents.remove(compId);

			logger.debug("Waiting for components to finish initialization: " + notInitializedComponents.toString());
			notInitializedComponents.notifyAll();
			if (notInitializedComponents.isEmpty()) {
				tc.setSimulationStart();
				logger.debug("Initialization finished. Simulation starts.");
			}
	
		}
	}
	
	/**
	 * With this method a simulation component signals that its execution cycle is finished.
	 * A controller calls this method immediately after initialization. The calling thread is blocked
	 * until all components have finished execution. This ensures in particular that controllers are
	 * available until the last simulation model has finished.
	 * 
	 *  @param compId - The component id of the calling component
	 */
	public void execFinished(String compId) throws InterruptedException {
		synchronized (notFinishedComponents) {
			logger.debug("Component " + compId + " finished execution.");
			notFinishedComponents.remove(compId);
			logger.debug("Waiting for components to finish execution: " + notFinishedComponents.toString());
			notFinishedComponents.notifyAll();
			if (notFinishedComponents.isEmpty()) {
				if (!isAborted()) { 
					logger.debug("Simulation finished.");
					tend = System.currentTimeMillis();
					tc.setSimulationEnd();
					this.abort();
					System.out.println("Simulation finished - "+simulationParameters.toConsoleString()+" | Time="+(tend-tstart)/1000+" s");
					this.serverManager.setFinished();
				}
			}
			while (!notFinishedComponents.isEmpty()) notFinishedComponents.wait();
		}
		
//		System.out.println("Simulation Finished!!!");
	}



	public void waitForAbort() throws InterruptedException {
		synchronized (abortSyncObject) {
			while (!isAborted()) abortSyncObject.wait();
		}
	}

	public void reportException(String compId, String mes, Exception e, boolean abort) {
		logger.error("Received exception report from component " + compId + " with message: " + mes, e);
		if (abort) {
			logger.error("Aborting simulation due to " + e.toString() + " in component " + compId);
			abort();
		}
		else logger.error("Aborting not necessary.");
		e.printStackTrace();
	}
		
	public void reportError(String compId, String mes, boolean abort) {
		logger.error("Received error report from component " + compId + " with message: " + mes);
		if (abort) {
			logger.error("Aborting simulation due to error in component " + compId);

			abort();
		}
		else logger.error("Aborting not necessary.");
		System.out.println("Received error report from component " + compId + " with message: " + mes);
	}


	private boolean isAborted() {
		return abortFlag;
	}
	
	void abort() {
		synchronized (abortSyncObject) {
			abortFlag = true;
			abortSyncObject.notifyAll();
		}
		tc.abort();
	}
}
