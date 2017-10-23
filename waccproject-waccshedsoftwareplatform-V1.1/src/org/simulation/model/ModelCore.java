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
import org.configuration.SimulationMetaData;
import org.metadata.ComponentMetaData;
import org.simulation.LocalLinkAdmin;
import org.simulation.SimulationAdmin;
import org.simulation.component.ComponentCore;
import org.simulation.coordination.Timecontroller;
import org.simulation.model.unit.AbstractUnit;
import org.simulation.model.unit.UnitTableImpl;
import org.utilities.time.SystemCalendar;

public class ModelCore extends ComponentCore {

	private Logger logger;

	private AbstractModel base;

	private ModelCycle cycle;

	private UnitTableImpl<? extends AbstractUnit> unitTable;

	private Timecontroller tc;

	public ModelCore(ComponentMetaData componentConfig,
			SimulationMetaData simMetaData, LocalLinkAdmin lla,
			SimulationAdmin simAdmin, Timecontroller tc,
			UnitTableImpl<? extends AbstractUnit> pt) {

		super(componentConfig, simMetaData, lla, simAdmin);
		this.tc = tc;
		if (pt != null)
			this.unitTable = pt;
		else
			abort();
		logger = Logger.getLogger(ModelCore.class);
		try {
			base = (AbstractModel) super.getBase();
		} catch (ClassCastException e) {
			logger.error("ClassCastException when casting "
					+ base.getClass().getName() + "to AbstractModel!");
			abort();
		}
		logger.debug("Creating ModelCycle with parameters: \nId=" + compId
				+ "\n Timestep=" + componentConfig.getTimeStep().toString()
				+ "\nStartDate=" + simMetaData.getSimulationStart().toString()
				+ "\nEndDate=" + simMetaData.getSimulationEnd().toString());
		cycle = new ModelCycle(tc, this, compId, componentConfig
				.getTimeStep(), simMetaData.getSimulationStart(), simMetaData
				.getSimulationEnd(), componentConfig.getSubcycleCount(),
				componentConfig.getSubcycleOffset());
		if (!isAborted())
			startExecution(); // starts component thread in superclass
								// ComponentCore
		else
			simAdmin
					.reportError(
							compId,
							"Error in ModelCore constructor! See log file for details.",
							true);
	}

	@Override
	public void init() {

		base.setUnitTable(unitTable);

		super.init();
		// put this here because model must be initialized before calling getImplementor
	}

	@Override
	protected void execute() {
		cycle.execute();
	}

	@Override
	protected synchronized boolean isAborted() {
		return super.isAborted();
	}

	public void getData(SystemCalendar t) {
		logger.debug("Starting core getData:" + t.toString());

		// call model getData
		try {
			base.getData((SystemCalendar) t.clone());
		} catch (RuntimeException e) {
			logger.error("Caught " + e.toString() + "in getData at date: "
					+ t.toString(), e);
			simAdmin.reportException(compId, "Caught " + e.toString()
					+ " in getData at date: " + t.toString(), e, true);
		}
		logger.debug("Finished core getData: " + t.toString());
	}

	public void compute(SystemCalendar t) {
		logger.debug("Starting core compute: " + t.toString());
		try {
			base.compute((SystemCalendar) t.clone());
		} catch (RuntimeException e) {
			logger.error("Caught " + e.toString() + "in compute at date: "
					+ t.toString(), e);
			simAdmin.reportException(compId, "Caught " + e.toString()
					+ " in compute at date: " + t.toString(), e, true);
		}
		logger.debug("Finished core compute: " + t.toString());
	}

	public void provide(SystemCalendar t) {
		logger.debug("Starting core provide: " + t.toString());

		try {
			base.provide((SystemCalendar) t.clone());
		} catch (RuntimeException e) {
			logger.error("Caught " + e.toString() + "in provide at date: "
					+ t.toString(), e);
			simAdmin.reportException(compId, "Caught " + e.toString()
					+ " in provide at date: " + t.toString(), e, true);
		}

		logger.debug("Finished core provide: " + t.toString());
	}

	public void store(SystemCalendar t) {
		logger.debug("Starting core store: " + t.toString());
		try {
			base.store((SystemCalendar) t.clone());
		} catch (RuntimeException e) {
			logger.error("Caught " + e.toString() + "in store at date: "
					+ t.toString(), e);
			simAdmin.reportException(compId, "Caught " + e.toString()
					+ "in store at date: " + t.toString(), e, false);
		}
	}


	@Override
	protected void finish() {
		super.finish();
	}

	/**
	 * Save recovery state of the model. Template method not be used by model
	 * developper.
	 */


}
