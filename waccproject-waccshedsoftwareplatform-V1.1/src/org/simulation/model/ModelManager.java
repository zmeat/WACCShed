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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.log4j.Logger;
import org.configuration.SimulationMetaData;
import org.metadata.ComponentMetaData;
import org.simulation.LocalLinkAdmin;
import org.simulation.SimulationAdmin;
import org.simulation.component.ComponentManager;
import org.simulation.coordination.Timecontroller;
import org.simulation.model.unit.AbstractUnit;
import org.simulation.model.unit.UnitTableImpl;

public class ModelManager extends ComponentManager {

	private ModelCore core;

	private Logger logger;
	
	public ModelManager(ComponentMetaData componentConfig,
			SimulationMetaData simMetaData, LocalLinkAdmin lla,
			Timecontroller timecontroller,
			SimulationAdmin simAdmin
		    ) {
		
		
		super(lla, simAdmin);
		logger = Logger.getLogger(ModelManager.class);
		
		logger.debug("in the ModelManager");
		
		/*
		 * initialize UnitTableImpl
		 */
		
		UnitTableImpl<? extends AbstractUnit> pt = null;
		try {
			Class compClass = Class.forName(componentConfig.getCompClass());
			
			/* Gets the Unit type of the component (the parameter of AbstractModel, superclass of component) */
			ParameterizedType superc = null;
			try {
				superc = (ParameterizedType) compClass.getGenericSuperclass();
			}
			catch (ClassCastException e) {
				logger.error("Creation of UnitTable failed: No binding to Unit class given in model class: " + componentConfig.getCompClass());
			}
			
			if (superc!=null) {
				/* Get specific Unit type (used to create UnitTable */
				Type[] types = superc.getActualTypeArguments();
				if (types.length>0) {
					try {
						Class<? extends AbstractUnit> unitType = (Class<? extends AbstractUnit>) types[0];
						logger.debug("Instantiating unit table with unitType: " + unitType.getName());

						pt = UnitTableImpl.create(componentConfig.getId(),/*landuseQuery,*/ simMetaData.getSimulationArea(),
								unitType);
					}
					catch (ClassCastException e) {
						logger.error("Parameter binding in model class " + componentConfig.getCompClass() + " is not a subclass of AbstractUnit!");
					}
				}
				else {
					logger.error("No unit class given as parameter of AbstractModel.");
				}
			}
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException when trying to create UnitTable.", e);
		}
		/*
		 * If pt==null due to exceptions the simulation will be aborted in
		 * constructor of class ModelCore
		 *  
		 */
		core = new ModelCore(componentConfig, simMetaData, lla,
				simAdmin, timecontroller, pt);

	}
	
}
