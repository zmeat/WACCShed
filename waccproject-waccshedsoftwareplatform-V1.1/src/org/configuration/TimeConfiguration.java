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
 * Created on 03.01.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.configuration;

import java.io.Serializable;
import java.util.Map;

import org.metadata.TimeStep;
import org.utilities.time.SystemCalendar;

public class TimeConfiguration implements Serializable {

	private SystemCalendar simulationStart;
	private SystemCalendar simulationEnd;
	private Map<String, TimeStep> modelStep;
	public TimeConfiguration(SystemCalendar simulationStart, SystemCalendar simulationEnd, Map<String, TimeStep> modelStep) {
		this.simulationStart = simulationStart;
		this.simulationEnd = simulationEnd;
		this.modelStep = modelStep;
	}
	public SystemCalendar getSimulationEnd() {
		return simulationEnd;
	}
	public SystemCalendar getSimulationStart() {
		return simulationStart;
	}
	public Map<String, TimeStep> getTimeSteps() {
		return modelStep;
	}
	public TimeStep getTimeStep(String compId) {
		return modelStep.get(compId);
	}
	
}
