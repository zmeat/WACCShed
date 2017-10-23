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
 * Changed on 12.02.2016 by Diego Cardoso.
 * - Changed method .setup(...) and attributes to protected. Reason: some 
 * components need to be aware of simulation-wise parameters for scenario
 * control.
 * - Eliminated unused attributes.
 * 
 * Changed on 23.02.2016 by Diego Cardoso.
 * - Included SimulationParameters
 */
package org.simulation.component;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.components.interfaces.CommunicationInterface;
import org.components.interfaces.CommunicationInterfaceNotFoundException;
import org.configuration.SimulationMetaData;
import org.configuration.SimulationParameters;
import org.metadata.AreaMetaData;
import org.metadata.ComponentMetaData;
import org.utilities.time.SystemCalendar;

public abstract class AbstractComponent {

	protected Logger logger;
	protected Logger componentLogger;
	protected ComponentMetaData componentConfig;
	protected SimulationMetaData simMetaData;
	protected SimulationParameters simParameters;
	protected Map<String, CommunicationInterface> imports;
	
	protected AbstractComponent() {
	}
	
	// will be called by ComponentCore immediately after constructor. 
	// cause: I did not want to have a constructor with these arguments
	protected final void setup(ComponentMetaData componentConfig, SimulationMetaData simMetaData, SimulationParameters simParameters) {
		this.componentConfig = componentConfig;
		this.simMetaData = simMetaData;
		this.simParameters = simParameters;
		logger = Logger.getLogger(AbstractComponent.class);
		componentLogger = Logger.getLogger(this.getClass());
	}
	
	final void setImports(Map<String, CommunicationInterface> imports) {
		this.imports = imports;
	}

	
	//queries
	@SuppressWarnings("unchecked")
	protected final <I extends CommunicationInterface> I getImport(Class<I> interfaceClass) {
		return (I) getImport(interfaceClass.getName());
	}
	
	@SuppressWarnings("unchecked")
	// the cast from DanubiaInterface to I should always be possible because
	// the type is already checked when putting the implementor into the export map
	// of the implementing component
	protected final <I extends CommunicationInterface> I getImport(String interfaceName) {
		CommunicationInterface d = null;
		if (imports.containsKey(interfaceName)){
			d = imports.get(interfaceName);
			if (d==null) {
				throw new RuntimeException("Implementor of interface " + interfaceName + " is null! Please check instantiation process!");
			}
		}
		else throw new RuntimeException("Interface: " + interfaceName + " not in import map. Please check configuration file!");
		return (I) d;
	}
	
	protected final Properties componentProperties() {
		return componentConfig.getComponentProperties();
	}
	
	protected final String componentProperty(String key) {
		return componentConfig.getComponentProperties().getProperty(key, "");
	}
	
	protected final ComponentMetaData componentConfig() {
		return componentConfig;
	}
	
	protected final String componentId() {
		return componentConfig.getId();
	}
	
	protected final SystemCalendar simulationStart() {
		return simMetaData.getSimulationStart();
	}
	
	protected final SystemCalendar simulationEnd() {
		return simMetaData.getSimulationEnd();
	}
	
	protected final AreaMetaData areaMetaData() {
		return simMetaData.getSimulationArea();
	}
	
	protected SimulationParameters getSimParameters() {
		return simParameters;
	}

	//plug points
	protected void init() {
		
	}
		
	protected void finish() {
		
	}
	
	protected Logger logger(){
		return this.componentLogger;
	}
	
	protected <I extends CommunicationInterface> I getImplementor(String interfaceName) throws CommunicationInterfaceNotFoundException {
		logger.debug("Request for interface " + interfaceName);
		I implementor = null;
		try {
			// note that this static cast actually checks against type DanubiaInterface only
			// but type of interfaceName is checked dynamically in ComponentCore
			// TODO possibly change return type of this plug point to DanubiaInterface in later version 
			// (requires new minor release number)
			implementor = (I) this; 
		}
		catch (ClassCastException e) {
			logger.debug("Interface " + interfaceName + " is not implemented by " + getClass().getName());
			throw new CommunicationInterfaceNotFoundException("Interface " + interfaceName + " is not implemented by " + getClass().getName());
		}		
		return implementor;
	}
	
	// other methods
	
	protected final void log(String mes) {
		componentLogger.info(mes);
	}
	
}
