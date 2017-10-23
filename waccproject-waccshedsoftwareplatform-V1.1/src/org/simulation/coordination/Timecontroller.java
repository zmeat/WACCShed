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
 */
package org.simulation.coordination;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.metadata.TimeStep;
import org.simulation.coordination.ModelState.ModelStates;
import org.utilities.time.SystemCalendar;

public class Timecontroller {

	private static Logger logger = Logger.getLogger(Timecontroller.class);

	private Map<String, TimeStep> modelStep;

	private SystemCalendar simStart;

	private SystemCalendar simEnd;

	private Map<String, SystemCalendar> nextGet;

	private Map<String, SystemCalendar> nextProv;

	private SortedMap<String, ModelState> modelStates;

	private volatile boolean abort = false;

	private long simulationTime;
	private long startTime;
	private GregorianCalendar realTimeSimulationStart = null;
	private GregorianCalendar realTimeSimulationEnd = null;
	private GregorianCalendar estimatedRealTimeSimulationEnd = null;
	private long realTimeStart;
	//private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private String realTimeStartString = "";
	private String realTimeEndString = "";

	public Timecontroller( Map<String, TimeStep> modelStep,
			SystemCalendar simStart, SystemCalendar simEnd) {
		this.modelStep = modelStep;
		this.simStart = simStart;
		startTime = simStart.getTimeInMillis();
		this.simEnd = simEnd;
		simulationTime = simEnd.getTimeInMillis() - simStart.getTimeInMillis();
		if (simulationTime == 0)
			simulationTime = 1; // prevent division by zero
		
		// initialize nextGet and nextProv with value of simStart
		nextGet = new HashMap<String, SystemCalendar>();
		nextProv = new HashMap<String, SystemCalendar>();
		modelStates = new TreeMap<String, ModelState>();
		for (String s : modelStep.keySet()) {
			nextGet.put(s, this.simStart);
			nextProv.put(s, this.simStart);
			modelStates.put(s, new ModelState(this.simStart));
		}

	}

	private void log(String compId, ModelStates state, SystemCalendar t) {
		// logger.debug("Model " + compId + " entered state " + state.toString()
		// + " for time " + t.toString());
		logger.debug("[" + compId + "]." + state.toString() + "("
				+ t.toString() + ")");
	}

	private void updateModelState(String compId, ModelStates state,
			SystemCalendar time) {
		modelStates.get(compId).set(state, time);
		// update DanubiaMonitor
	}

	
	public synchronized void abort() {
		logger.debug("Received abort... releasing all waiting threads.");
		abort = true;
	}


	public void setSimulationEnd() {
		realTimeSimulationEnd = new GregorianCalendar();
		realTimeEndString = dateFormat.format(realTimeSimulationEnd.getTime());
		logger.debug("Simulation ends at "
				+ dateFormat.format(realTimeSimulationEnd.getTime()));
	}

	public void setSimulationStart() {
		realTimeSimulationStart = new GregorianCalendar();
		realTimeStart = realTimeSimulationStart.getTimeInMillis() - 1L; // -1 to
																		// avoid
																		// division
																		// by
																		// zero
		realTimeStartString = dateFormat.format(realTimeSimulationStart
				.getTime());
		logger.debug("Simulation starts at " + realTimeStartString);
	}


	public synchronized void enterGet(String compId, SystemCalendar t)
			throws InterruptedException {
		updateModelState(compId, ModelStates.waitForGet, t);
		while (!checkProv(t) && !abort)
			wait();
		updateModelState(compId, ModelStates.get, t);
	}

	public synchronized void exitGet(String compId, SystemCalendar t) {
		// get t increased by time step
		SystemCalendar dc = t.getNextSystemCalendar(modelStep.get(compId));
		// if (dc.getNextDanubiaCalendar(modelStep.get(compId)).after(simEnd))
		// nextGet.put(compId, simEnd);
		// else
		nextGet.put(compId, dc);
		notifyAll();
		updateModelState(compId, ModelStates.compute, t);
	}

	public synchronized void enterProv(String compId, SystemCalendar t)
			throws InterruptedException {
		updateModelState(compId, ModelStates.waitForProv, t);
		
		while (!checkGet(t) && !abort)
		{
			wait();
		}
		updateModelState(compId, ModelStates.prov, t);

	}

	public synchronized void exitProv(String compId, SystemCalendar t) {
		// get t increased by time step
		SystemCalendar dc = t.getNextSystemCalendar(modelStep.get(compId));
		if (dc.after(simEnd)) {
			nextProv.put(compId, simEnd);
			// need this if time step greater than simulation time,
			// because get is never called in this case
			nextGet.put(compId, simEnd);
			updateModelState(compId, ModelStates.idle, t);
		} else
			nextProv.put(compId, dc);
		notifyAll();
	}

	/**
	 * Verifies if all components finished Provide operation. Used to synchronize
	 * state transition for models.
	 * @param t Current simulation time
	 * @return True, if the next Provide cycle for all components are set to a time AFTER t
	 * (i.e. next time step) 
	 */
	private boolean checkProv(SystemCalendar t) {
		for (String s : nextProv.keySet()) {
			if (!t.before(nextProv.get(s)))
				return false;
		}
		return true;
	}

	/**
	 * Verifies if all components finished Get operation. Used to synchronize
	 * state transition for models.
	 * @param t Current simulation time
	 * @return True, if the next Get cycle for all components are set to time t 
	 */
	private boolean checkGet(SystemCalendar t) {
		for (String s : nextGet.keySet()) {
			if (t.after(nextGet.get(s)))
				return false;
		}
		return true;
	}
	
}
