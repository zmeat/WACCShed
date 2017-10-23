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
package org.simulation.model;

import org.apache.log4j.Logger;
import org.metadata.TimeStep;
import org.simulation.coordination.Timecontroller;
import org.utilities.time.SystemCalendar;

public class ModelCycle {

	private Logger logger;
	private Timecontroller tc;
	private ModelCore m;
	private String id;
	private TimeStep step;
	private SystemCalendar simStart;
	private SystemCalendar simEnd;

	String resultingTimeStep;

	private ModelStoreThread modelStore;

	public ModelCycle(Timecontroller tc, ModelCore m, String id,
			TimeStep step, SystemCalendar simStart, SystemCalendar simEnd,
			int subcycleCount, int subcycleOffset) {
		logger = Logger.getLogger(ModelCycle.class);
		this.tc = tc;
		this.m = m;
		this.id = id;
		this.step = step;
		
		this.simStart = simStart;
		this.simEnd = simEnd;

		this.resultingTimeStep = "" + step.getValue() + step.getUnit();
		modelStore = new ModelStoreThread(m, simStart);
	}


	public void execute() {
		logger.debug("Starting ModelStoreThread");
		modelStore.start();


		SystemCalendar t = simStart;
	
			logger.debug("Starting model cycle at date: " + simStart.toString());
		
		try {
			tc.enterProv(id, t);
		} catch (InterruptedException e) {
			logger.debug("InterruptedException when "
					+ "trying to call enterProv: " + id + "time: "
					+ t.toString(), e);
		}

		logger.debug("enterProv, and model provide data begin");
		
		m.provide(t);
		
		tc.exitProv(id, t);
		
		modelStore.execute(t);

		// execution loop
		while (!simEnd.before(t.getNextSystemCalendar(step)) && !m.isAborted()) {
			try {
				tc.enterGet(id, t);
			} catch (InterruptedException e) {
				logger.debug("InterruptedException when "
						+ "trying to call enterGet: " + id + "time: "
						+ t.toString(), e);
			}

			if (!m.isAborted()) {
				m.getData(t);
			} else
				logger.debug("Skipping getData due to subcycling at date: "
						+ t.toString());

			tc.exitGet(id, t);
			
			if (!m.isAborted()) {
				m.compute(t);
			} else
				logger.debug("Skipping compute due to subcycling/abort at date: "
						+ t.toString());

			t = t.getNextSystemCalendar(step);
			try {
				tc.enterProv(id, t);
			} catch (InterruptedException e) {
				logger.debug("InterruptedException when "
						+ "trying to call enterProv: " + id + "time: "
						+ t.toString(), e);
			}

			if (!m.isAborted()) {
				logger.debug("Waiting for ModelStoreThread to finish.");
				modelStore.waitForFinish();
				logger.debug("ModelStoreThread finished");
				m.provide(t);
			} else
				logger
				.debug("Skipping provide due to subcycling/abort at date: "
						+ t.toString());

			tc.exitProv(id, t);
			if ( !m.isAborted()) {
				logger.debug("Waking up ModelStoreThread at date: "
						+ t.toString());
				modelStore.execute(t);
			} else
				logger.debug("Skipping store due to subcycling/abort at date: "
						+ t.toString());

		} // end execution loop
		modelStore.waitForFinish();
		modelStore.abort();
	}
}
