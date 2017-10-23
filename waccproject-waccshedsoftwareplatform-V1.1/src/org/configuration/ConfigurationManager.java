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
 * Created on 13.01.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metadata.AreaMetaData;
import org.metadata.ComponentMetaData;
import org.metadata.MetaDataAdmin;
import org.metadata.SimulationConfiguration;
import org.metadata.TimeStep;
import org.utilities.time.SystemCalendar;

public class ConfigurationManager {

	private MetaDataAdmin mdAdmin;
	private ConfigurationReaderWriter confrw;
	
	private SimulationMetaData simulationMetaData;

	private TimeConfiguration timeConfiguration;

	private Set<String> componentIds;

	
	
	public ConfigurationManager() throws IOException, Exception {
		mdAdmin = new MetaDataAdmin();
		confrw = new ConfigurationReaderWriter(mdAdmin);
		
		SimulationConfiguration sc = confrw.getSimulationConfiguration();
		SystemCalendar simStart = sc.getSimulationBegin();
		SystemCalendar simEnd = sc.getSimulationEnd();
		this.simulationMetaData = new SimulationMetaData(simStart, simEnd, sc.getSimulationArea()); 

		componentIds = new HashSet<String>();
		Map<String, TimeStep> modelStep = new HashMap<String, TimeStep>();
		for (ComponentMetaData cmd : mdAdmin.getComponentMetaDataList()) {
				modelStep.put(cmd.getId(), cmd.getTimeStep());
				componentIds.add(cmd.getId());
		}
		timeConfiguration = new TimeConfiguration(simulationMetaData.getSimulationStart(), simulationMetaData.getSimulationEnd(), modelStep);
		
	}


	public List<AreaMetaData> getAreaMetaData() {
		return mdAdmin.getAreaMetaDataList();
	}



	public List<ComponentMetaData> getComponentMetaData() {
		return mdAdmin.getComponentMetaDataList();
	}



	
	public void refresh() throws IOException, Exception {
		confrw.refresh();
	}

	public SimulationConfiguration getSimulationConfiguration() throws IOException{
		
		return confrw.getSimulationConfiguration();
	}
	
	public TimeConfiguration getTimeConfiguration() {
		return timeConfiguration;
	}

	
	public SimulationMetaData getSimulationMetaData() {
		return simulationMetaData;
	}
	
	public Set<String> getComponentIds() {
		return componentIds;
	}
	
	
/*	public long getSimulationTimeInMinutes() {
		long minutes = 0;
		TimeStep ts = new TimeStep(1, TimeStepUnit.DAY);
		SystemCalendar x = (SystemCalendar) simulationMetaData.getSimulationStart().clone();
		SystemCalendar end = (SystemCalendar) simulationMetaData.getSimulationEnd().clone();
		while (!x.getNextSystemCalendar(ts).after(end)) {
			minutes += 1440; // 1 day
			x = x.getNextSystemCalendar(ts);
		}
		ts = new TimeStep(1, TimeStepUnit.H);
		while (!x.getNextSystemCalendar(ts).after(end)) {
			minutes += 60; // 1 hour
			x = x.getNextSystemCalendar(ts);
		}
		ts = new TimeStep(1, TimeStepUnit.MIN);
		while (!x.getNextSystemCalendar(ts).after(end)) {
			minutes += 1; // 1 minute
			x = x.getNextSystemCalendar(ts);
		}
		return minutes;
	}*/
	
}
