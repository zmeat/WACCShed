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
 * Created on 05.01.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.simulation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.components.interfaces.CommunicationInterface;
import org.components.interfaces.CommunicationInterfaceNotFoundException;


public class LocalLinkAdmin {

	private Logger logger; 
	

	private Set<String> components;

	private Set<String> notRegisteredComponents;

	private Map<String, CommunicationInterface> localExports;


	public LocalLinkAdmin( Set<String> components) {
		logger = Logger.getLogger(LocalLinkAdmin.class);
		logger.debug("LocalLinkAdmin created. Components: " + components.toString());

		this.components = components;

		notRegisteredComponents = new HashSet<String>(components);
		localExports = new HashMap<String, CommunicationInterface>();

	}

	// implement provided interfaces
	// LocalLinkAdminAccess

	public void registerExports(String compId, Map<String, CommunicationInterface> componentExports)
			throws InterruptedException {
		synchronized (notRegisteredComponents) {
			logger.debug("Component " + compId + " registers export interfaces: " + componentExports.keySet().toString());
			localExports.putAll(componentExports);

			notRegisteredComponents.remove(compId);
			notRegisteredComponents.notifyAll();
		}
	}

	public Map<String, CommunicationInterface> getImports(String[] interfaceNames)
	throws InterruptedException, CommunicationInterfaceNotFoundException {
		String iNames = Arrays.asList(interfaceNames).toString();
		synchronized (notRegisteredComponents) {
			logger.debug("Request for import interfaces: " + iNames + " Request is probably blocked.");
			while (!notRegisteredComponents.isEmpty())
				notRegisteredComponents.wait();
		}
		logger.debug("Request for import interfaces " + iNames + " will be served.");
		Map<String, CommunicationInterface> imports = new HashMap<String, CommunicationInterface>(interfaceNames.length);
		for (String s : interfaceNames) {
			if (localExports.containsKey(s)) {
				logger.debug("Interface " + s + " is locally available.");
				imports.put(s, localExports.get(s));
			}else
			{
				logger.debug("Interface " + s + " is not locally available.");
			}
		}
		return imports;
	}
}
