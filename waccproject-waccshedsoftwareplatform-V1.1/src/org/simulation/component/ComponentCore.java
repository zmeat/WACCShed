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
package org.simulation.component;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.components.interfaces.CommunicationInterface;
import org.components.interfaces.CommunicationInterfaceNotFoundException;
import org.configuration.SimulationMetaData;
import org.metadata.ComponentMetaData;
import org.simulation.LocalLinkAdmin;
import org.simulation.SimulationAdmin;

public abstract class ComponentCore {

	
	private Logger logger;
	protected ComponentMetaData componentConfig;
	protected String compId;
	protected SimulationMetaData simMetaData;
	private LocalLinkAdmin lla;
	protected SimulationAdmin simAdmin;

	private AbstractComponent base;
	private ComponentThread componentThread;
	
	protected ClassLoader classLoader;
	
	private volatile boolean abort = false;
	
	public ComponentCore(ComponentMetaData componentConfig,
			SimulationMetaData simMetaData, LocalLinkAdmin lla, SimulationAdmin simAdmin) {
		logger = Logger.getLogger(ComponentCore.class);
		this.componentConfig = componentConfig;
		this.simMetaData = simMetaData;
		this.lla = lla;
		this.simAdmin = simAdmin;

		// create concrete component
		compId = componentConfig.getId();
		String className = componentConfig.getCompClass();
		try {
			base = (AbstractComponent) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		} catch (ClassNotFoundException e) {
			logger.error(e);
		}
		
		if (base!=null) base.setup(componentConfig, simMetaData, simAdmin.getSimulationParameters());
		else {
			logger.error("Base class could not be instantiated! Aborting...");
			abort();
		}
	}
	
	protected final void startExecution() {
		componentThread = new ComponentThread();
		componentThread.start();
		new AbortThread().start();
	}

	protected AbstractComponent getBase() {
		return base;
	}

	// ensures that we get implementor of type I from component 
	// and not only of type DanubiaInterface
	private <I extends CommunicationInterface> CommunicationInterface getImplementorFromComponent(Class<I> interfaceType) throws CommunicationInterfaceNotFoundException {
		CommunicationInterface implementor = base.getImplementor(interfaceType.getName());
		if (implementor!=null)
			if (interfaceType.isInstance(implementor))
				return implementor;
			else
				throw new CommunicationInterfaceNotFoundException("Interface " + interfaceType.getName() + " is not implemented by " + implementor.getClass().getName());
		else {
			throw new CommunicationInterfaceNotFoundException("Implementor of interface " + interfaceType.getName() + " is null!");
		}
	}
	
	public void init() {
		// get exports from component and register to lla
		Map<String, CommunicationInterface> exports = new HashMap<String, CommunicationInterface>();
		for (String s : componentConfig.getExpInterfaces()) {
			Class<? extends CommunicationInterface> interfaceType = null;
			try {
				interfaceType = (Class<? extends CommunicationInterface>) Class.forName(s);
			} catch (ClassNotFoundException e) {
				logger.error("Interface " + s + " not found in classpath: " + e.toString(), e);
				abort();
				simAdmin.reportError(compId, "Interface " + s + " not found in classpath.", abort);
			}			
			if(!interfaceType.isInterface()) {
				logger.error(s + " is not an interface.");
				abort();
				simAdmin.reportError(compId, s + " is not an interface.", abort);
			}
			if(!CommunicationInterface.class.isAssignableFrom(interfaceType)) {
				logger.error("Interface " + s + " is not a subinterface of DanubiaInterface." );
				abort();
				simAdmin.reportError(compId, "Interface " + s + " is not a subinterface of DanubiaInterface.", abort);
			}
			CommunicationInterface implementor = null;
			try {
				implementor = getImplementorFromComponent(interfaceType);
				exports.put(s, implementor);				
			} catch (CommunicationInterfaceNotFoundException e) {
				logger.error(e);
				abort();
				simAdmin.reportException(compId, e.getMessage(), e, abort);
			}
		}
		try {
			lla.registerExports(componentConfig.getId(), exports);
		} catch (InterruptedException e) {
			logger.error(e);
		}
		
		// get imports from lla and put them into component
		try {
			base.setImports(lla.getImports(componentConfig.getImpInterfaces()));
		} catch (InterruptedException e) {
			logger.error(e);
		} catch (CommunicationInterfaceNotFoundException e) {
			logger.error(e);
		}
		// call plug point of base class
		try {
			base.init();
		}
		catch (RuntimeException e) {
			logger.error("Caught exception in init: " + e.toString(), e);
			simAdmin.reportException(compId, "Caught exception in init: " + e.toString(), e, true);
		}
	}

	protected abstract void execute();

	protected synchronized void abort() {
		abort = true;
	}
	
	protected synchronized boolean isAborted() {
		return abort;
	}

	protected void finish() {
		try {
			base.finish();
		}
		catch (RuntimeException e) {
			logger.error("Caught exception in finish: " + e.toString(), e);
			simAdmin.reportException(compId, "Caught exception in finish: " + e.toString(), e, false);
		}
	}
	
	
	private class ComponentThread extends Thread {

		@Override
		public void run() {
			ComponentCore.this.init();
			try {
				simAdmin.initFinished(compId);
			} catch (InterruptedException e) {
				logger.error("InterruptedException when waiting for initFinished.", e);
			}
			ComponentCore.this.execute();
			try {
				simAdmin.execFinished(compId);
			} catch (InterruptedException e) {
				logger.error("InterruptedException when waiting for execFinished.", e);
			}
			ComponentCore.this.finish();
		}
	}
	
	private class AbortThread extends Thread {
		@Override
		public void run() {
			try {
				simAdmin.waitForAbort();
			} catch (InterruptedException e) {
				logger.debug("InterruptedException when waiting for abort.", e);
			} finally {
				logger.debug("Received abort from SimulationAdmin.");
				ComponentCore.this.abort();
			}
		}
	}
}
