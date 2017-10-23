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
 * Created on 15.09.2009
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.configuration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.metadata.AreaMetaData;
import org.metadata.ComponentMetaData;
import org.metadata.MetaDataAdmin;
import org.metadata.SimulationConfiguration;
import org.utilities.internal.PropertyLoader;
import org.utilities.time.SystemCalendar;

public class ConfigurationReaderWriter {

	// date format for configuration file
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private String configPath = System.getProperty("configuration.path",
			System.getProperty("user.dir")+"/res/configuration/");
	private String configFile = System.getProperty("configuration.file",
			"configuration.properties");
	private String configPrefix = System
			.getProperty("configuration.prefix", "");
	private String configExt = System.getProperty("configuration.ext", ".properties");
	private Properties configDirs;
	private MetaDataAdmin mda;

	//	private SortedMap<String, SimulationConfiguration> configurationMap;
	private SimulationConfiguration sConfiguration;

	public ConfigurationReaderWriter(MetaDataAdmin mda) throws IOException, Exception {
		try {
			configDirs = PropertyLoader.loadProperties(configPath
					+ File.separator + configFile);
		} catch (IOException e) {
			configDirs = new Properties();
			configDirs.setProperty("SimulationConfiguration", "simulation");
		}
		this.mda = mda;
		refresh();

	}

	public SimulationConfiguration getSimulationConfiguration()
			throws IOException {
		synchronized (sConfiguration) {
			try {
				refresh();
			} catch ( Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return sConfiguration;
		}
	}



	public void refresh() throws IOException, Exception {

		mda.refresh();
		loadSimulationConfigurations();

	}

	private String getSimulationConfigDir() {
		String simulationConfigDir = configDirs.getProperty(
				"SimulationConfiguration", "simulation");
		return configPath + File.separator + simulationConfigDir;
	}

	private void loadSimulationConfigurations() throws IOException {
		// logger.debug("Looking for simulation configurations in dir: " +
		// simulationDir);
		String simulationConfigFilenames = loadConfigFilenames(getSimulationConfigDir())[0];
		// logger.debug("Found simulation run configuration files: " +
		// Arrays.asList(simulationConfigFilenames));

		try {
			sConfiguration = createSimulationConfiguration(getSimulationConfigDir() + File.separator
					+ simulationConfigFilenames);
		} catch (Exception e) {
			// logger.error("ConfigurationException when trying to load
			// SimulationRunConfiguration from file: " + s, e);
		}

	}

	private SimulationConfiguration createSimulationConfiguration(
			String configFileName) throws IOException, Exception {
		Properties props = PropertyLoader.loadProperties(configFileName);
		SimulationConfiguration simConfig = new SimulationConfiguration();

		String startDate = props.getProperty("startDate");
		if (startDate == null) {
			System.out.println("No start date given in file: "
					+ configFileName);
		} else {
			try {
				SystemCalendar start = ConfigurationHelper
						.parseDate(startDate);
				simConfig.setSimulationBegin(start);
			} catch (Exception e) {
				System.out.println("Invalid start date specification: "
						+ startDate + " in file: " + configFileName);
			}
		}

		String stopDate = props.getProperty("stopDate");
		if (stopDate == null) {
			System.out.println("No stop date given in file: "
					+ configFileName);
		} else {
			try {
				SystemCalendar stop = ConfigurationHelper
						.parseDate(stopDate);
				simConfig.setSimulationEnd(stop);
			} catch (Exception e) {
				System.out.println("Invalid stop date specification: "
						+ startDate + " in file: " + configFileName);
			}
		}

		String areaId = props.getProperty("area");
		AreaMetaData amd = null;
		if (areaId == null) {
			System.out.println("No area id given in file: "
					+ configFileName);
		} else {
			amd = mda.getAreaMetaData(areaId);
			if (amd == null)
				System.out.println("No area meta data defined for id: "
						+ areaId);
		}
		simConfig.setSimulationArea(amd);




		// read scenario configuration

		// create component configurations

		Set<ComponentMetaData> componentSim = new HashSet<ComponentMetaData>();
		for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
			String s = (String) e.nextElement();
			if (s.startsWith("component.")) {
				String compId = s.substring(10); // 10 =
				// "component.".length()
				ComponentMetaData cmd = mda.getComponentMetaData(compId);
				if (cmd == null) {
					System.out.println(
							"No component meta data defined for component id: "
									+ compId);
				}
				componentSim.add(cmd);
			}
		}

		return simConfig;
	}

	private String[] loadConfigFilenames(String configDir) {
		File f = new File(configDir);
		String[] configFilenames = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File f, String s) {
				return s.startsWith(configPrefix) && s.endsWith(configExt);
			}
		});
		return configFilenames;
	}

	// report configuration errors



}
