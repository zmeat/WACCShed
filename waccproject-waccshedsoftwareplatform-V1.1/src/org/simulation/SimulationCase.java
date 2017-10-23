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
 */
/*
 * Created on 15.12.2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 *
 * Changed on 28.02.2016 by Diego Cardoso.
 * - Static methods were relocated to RunSimulation.
 * - Renamed to SimulationCase (from SimulationServerManager)
 * - Extended from Thread, so each instance is a simulation case with
 *    its own parameters 
 */
package org.simulation;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.configuration.ConfigurationManager;
import org.configuration.SimulationMetaData;
import org.configuration.SimulationParameters;
import org.configuration.TimeConfiguration;
import org.metadata.ComponentMetaData;
import org.simulation.coordination.Timecontroller;
import org.simulation.model.ModelManager;
import org.simulation.parallel.SimulationThreadPool;

public class SimulationCase extends Thread {

	public boolean isFinished = false;

	private static Logger logger = Logger.getLogger(SimulationCase.class);
	// when export CM-OFF simulation result, uncomment below:
	//  private static String resultsPath = System.getProperty("user.dir")+"/results/CM_OFF_SimpleRuleBased/";
	// when export CM-ON simulation result comment above, uncomment below:

	private SimulationAdmin simulationAdmin;
	private SimulationParameters simulationParameters;
	private LocalLinkAdmin localLinkAdmin;
	private ConfigurationManager confManager;

	private Timecontroller tc;
	private SimulationMetaData simMetaData;
	private List<ComponentMetaData> components;
	private SimulationThreadPool pool;

	public SimulationCase(SimulationParameters simulationParameters, SimulationThreadPool pool) {
		logger.debug("SimulationServerManager created for simulation run ");
		this.simulationParameters = simulationParameters;
		this.pool = pool;

		try {
			confManager=  new ConfigurationManager();
			confManager.refresh(); 
		} catch (IOException e) {
			System.out.println(e.toString());
		} catch (Exception e) {
			System.out.println( "Configuration problem" + e.toString());
		} 

		this.simMetaData = confManager.getSimulationMetaData();
		
		logger.debug("Called createAndBind.");

		// create Timecontroller
		// collect elements for TimeConfiguration
		logger.debug("Creating Timecontroller.");
		TimeConfiguration tConfig = confManager.getTimeConfiguration();
		tc = new Timecontroller(
				tConfig.getTimeSteps(),
				tConfig.getSimulationStart(),
				tConfig.getSimulationEnd());

		// create LinkAdmin
		localLinkAdmin = new LocalLinkAdmin(confManager.getComponentIds());

		logger.debug("Creating SimulationAdmin.");
		simulationAdmin = new SimulationAdmin(this, confManager, simulationParameters, tc);
	}
	
	
	public boolean isFinished(){
		return simulationAdmin.isFinished();
	}
	
	public void setFinished() {
		if (!this.isFinished) {
			this.isFinished = true;
			try {
				pool.notifyFinish(simulationParameters.getSimulationID());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void abort() {
		if (simulationAdmin!=null) simulationAdmin.abort();
		System.out.println("Simulation is aborted!");

	}

	@Override
	public void run() {
		// create components
		logger.debug("Creating components: "+Arrays.toString(confManager.getComponentIds().toArray()));

		components = confManager.getComponentMetaData();

		for (ComponentMetaData cmd : components) {
			logger.debug("Creating component: " + cmd.getId());
			new ModelManager(cmd, simMetaData, localLinkAdmin, tc, simulationAdmin);
		}
	}
}
