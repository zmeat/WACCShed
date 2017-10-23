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
package org.metadata;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.configuration.ConfigurationHelper;
import org.utilities.internal.PropertyLoader;

public class MetaDataAdmin {

	// private static DanubiaLogger logger =
	// DanubiaLogger.getDanubiaLogger(MetaDataAdmin.class);
	
	private static String defaultLogLevel = System.getProperty("logLevel.default", "DEBUG");

	private SortedMap<String, AreaMetaData> areaMetaDataMap;
	private SortedMap<String, ComponentMetaData> componentMetaDataMap;




	private SortedMap<String, SimulationConfiguration> simulationConfigurationMap;

	private String metadataPath;
	private String metaDataPrefix = System.getProperty("metadata.prefix", "");
	private String metaDataExt = System.getProperty("metadata.ext", ".properties");

	private String propertyFile;
	private Properties metaDataDirs;

	private StringBuilder errorMes = new StringBuilder();
	private boolean errorFlag = false;

	public MetaDataAdmin() throws IOException, Exception {
		metadataPath = System.getProperty("metadata.path", System.getProperty("user.dir")+"/res/metadata");
		propertyFile = System.getProperty("metadata.configfile", "metadata.properties");
		refresh();
	}

	/*
	 * Second constructor that does not refresh automatically
	 */
	public MetaDataAdmin(int i) {
		metadataPath = System.getProperty("metadata.path",  System.getProperty("user.dir")+"/res/metadata");
		propertyFile = System.getProperty("metadata.configfile", "metadata.properties");
	}

	public void refresh() throws IOException, Exception {
		errorFlag = false;
		metaDataDirs = PropertyLoader.loadProperties(metadataPath + File.separator + propertyFile);
		initMetaData();
		/*
		 * loadSiteMetaData(); loadAreaMetaData(); loadComponentMetaData();
		 * loadBaseDataMetaData(); loadNodeMetaData();
		 */
		if (errorFlag)
			throw new Exception(errorMes.toString());
	}

	public List<AreaMetaData> getAreaMetaDataList() {
		return toList(getAreaMetaDataSet());
	}


	public List<ComponentMetaData> getComponentMetaDataList() {
		return toList(getComponentMetaDataSet());
	}

	public SortedSet<AreaMetaData> getAreaMetaDataSet() {
		SortedSet<AreaMetaData> amdSet = new TreeSet<AreaMetaData>(
				new Comparator<AreaMetaData>() {
					@Override
					public int compare(AreaMetaData o1, AreaMetaData o2) {
						String o1Id = o1.getId();
						String o2Id = o2.getId();
						return o1Id.compareTo(o2Id);
					}

				});
		amdSet.addAll(areaMetaDataMap.values());
		return amdSet;
	}


	public SortedSet<ComponentMetaData> getComponentMetaDataSet() {
		SortedSet<ComponentMetaData> cmdSet = new TreeSet<ComponentMetaData>(
				new Comparator<ComponentMetaData>() {
					@Override
					public int compare(ComponentMetaData o1,
							ComponentMetaData o2) {
						return o1.getId().compareTo(o2.getId());
					}
				});
		cmdSet.addAll(componentMetaDataMap.values());
		return cmdSet;
	}

	public AreaMetaData getAreaMetaData(String id) {
		return areaMetaDataMap.get(id);
	}

	public ComponentMetaData getComponentMetaData(String id) {
		List<ComponentMetaData> list = getComponentMetaDataList();
		for (ComponentMetaData cmd : list) {
			if (cmd.getId().equals(id)) return cmd;
		}
		return null;
	}

	public SortedMap<String, AreaMetaData> getAreaMetaDataMap() {
		return areaMetaDataMap;
	}

	public SortedMap<String, ComponentMetaData> getComponentMetaDataMap() {
		return componentMetaDataMap;
	}


	/**
	 * 
	 * @param selectedModels
	 *            A set of ComponentMetaData objects representing the simulation
	 *            models selected for a simulation configuration
	 * @return A map from component identifier to an error message
	 */
	public Map<ComponentMetaData, String> checkConfiguration(
			Set<ComponentMetaData> selectedModels) {

		// map to store the errors
		SortedMap<String, StringBuilder> errorMap = new TreeMap<String, StringBuilder>();

		// map to store the ComponentMetaData objects by id
		SortedMap<String, ComponentMetaData> metaDataMap = new TreeMap<String, ComponentMetaData>();
		for (ComponentMetaData cmd : selectedModels) {
			metaDataMap.put(cmd.getId(), cmd);
		}
		
		// build a map exportInterface -> set of implementing components
		// if size of this set > 1 then we have a duplicate export interface!
		Map<String, Set<String>> exportMap = new HashMap<String, Set<String>>();
		for (ComponentMetaData cmd : selectedModels) {
			for (String exportInterface : cmd.getExpInterfaces()) {
				if (exportMap.containsKey(exportInterface)) {
					// interface already in map
					exportMap.get(exportInterface).add(cmd.getId());
				} else {
					Set<String> compIds = new HashSet<String>(1);
					compIds.add(cmd.getId());
					exportMap.put(exportInterface, compIds);
				}
			}
		}

		// iterate over components if there is an unresolved import interface

		for (ComponentMetaData cmd : selectedModels) {
			SortedSet<String> unresolvedImportInterfaces = new TreeSet<String>();
			for (String importInterface : cmd.getImpInterfaces()) {
				if (!exportMap.containsKey(importInterface)) {
					unresolvedImportInterfaces.add(importInterface);
				}
			}
			if (unresolvedImportInterfaces.size() > 0) {
				StringBuilder sb = new StringBuilder(
						"unresolved import interfaces: ");
				sb.append(unresolvedImportInterfaces.toString());
				errorMap.put(cmd.getId(), sb);
			}
		}

		for (String exportInterface : exportMap.keySet()) {
			Set<String> implementors = exportMap.get(exportInterface);
			if (implementors.size() > 1) {
				StringBuilder sb = new StringBuilder("export interface ");
				sb.append(exportInterface);
				sb.append(" multiply defined by ");
				sb.append(implementors);
				for (String compId : implementors) {
					if (errorMap.containsKey(compId)) {
						errorMap.get(compId).append("\n").append(sb);
					} else
						errorMap.put(compId, sb);
				}
			}
		}

		Map<ComponentMetaData, String> resultErrorMap = new HashMap<ComponentMetaData, String>();
		for (String compId : errorMap.keySet()) {
			resultErrorMap.put(metaDataMap.get(compId), errorMap.get(
					compId).toString());
		}
		return resultErrorMap;

	}

	private void initMetaData() throws IOException {
		areaMetaDataMap = loadMetaData(AreaMetaData.class);
		componentMetaDataMap = loadComponentMetaData();
	}

	private SortedMap<String, ComponentMetaData> loadComponentMetaData()
			throws IOException {
		SortedMap<String, ComponentMetaData> metaDataMap = new TreeMap<String, ComponentMetaData>();
		String metaDataDir = metaDataDirs.getProperty(ComponentMetaData.class.getSimpleName());
		if (metaDataDir != null) {
			String[] metaDataFilenames = loadMetaDataFilenames(metadataPath
					+ File.separator + metaDataDir);
			for (String s : metaDataFilenames) {
				ComponentMetaData md = createMetaData(ComponentMetaData.class, metadataPath + File.separator
						+ metaDataDir + File.separator + s);
			
				metaDataMap.put(md.getId(), md);
			}
		} else
			error("No metadata directory given for " + ComponentMetaData.class.getSimpleName());
		return metaDataMap;

	}

	private <T> SortedMap<String, T> loadMetaData(Class<T> type)
			throws IOException {
		SortedMap<String, T> metaDataMap = new TreeMap<String, T>();
		String metaDataDir = metaDataDirs.getProperty(type.getSimpleName());
		if (metaDataDir != null) {
			String[] metaDataFilenames = loadMetaDataFilenames(metadataPath
					+ File.separator + metaDataDir);
			for (String s : metaDataFilenames) {
				T md = createMetaData(type, metadataPath + File.separator
						+ metaDataDir + File.separator + s);
				// nodeMetaDataSet.add(nmd);
				String id = "";
				try {
					id = (String) md.getClass().getMethod("getId",
							new Class<?>[] {}).invoke(md, (Object[]) null);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (id != null && !id.equals(""))
					if (!metaDataMap.containsKey(id))
						metaDataMap.put(id, md);
					else
						error("Duplicate " + type.getSimpleName() + " id: "
								+ id + " in file: " + s);
				else
					error("Id could not be extracted in file: " + s);
			}
		} else
			error("No metadata directory given for " + type.getSimpleName());
		return metaDataMap;
	}

	private String[] loadMetaDataFilenames(String metaDataDir) {
		File f = new File(metaDataDir);
		String[] metaDataFilenames = f.list(new FilenameFilter() {
			@Override
			public boolean accept(File f, String s) {
				return s.startsWith(metaDataPrefix) && s.endsWith(metaDataExt);
			}
		});
		if (metaDataFilenames != null)
			return metaDataFilenames;
		else {
			error("I/O error or directory does not exist: " + metaDataDir);
			return new String[] {};
		}
	}

	private <T> T createMetaData(Class<T> type, String filename)
			throws IOException {
		String typeName = type.getSimpleName();
		if (typeName.equals("AreaMetaData"))
			return (T) createAreaMetaData(filename);
		if (typeName.equals("ComponentMetaData"))
			return (T) createComponentMetaData(filename);
		return null;
	}

	private AreaMetaData createAreaMetaData(String filename) throws IOException {
		Properties props = PropertyLoader.loadProperties(filename);

		String id = props.getProperty("id");
		if (id == null)
			error(filename + ": No id for AreaMetaData given!");
		String description = props.getProperty("description");
		int numOfsubBasin = 0;
		try {
			numOfsubBasin = ConfigurationHelper.parseInt(props.getProperty("numOfsubBasin"));
		} catch (Exception e) {
			error(filename + ": numOfsubBasin: " + e.getMessage());
		}
		
		String[] subBasins = null;
		
		subBasins = ConfigurationHelper.parseList(props.getProperty("subBasins"));
		
        double[] basinAreas = null;
		
        basinAreas = ConfigurationHelper.parseDoubleList(props.getProperty("basinAreas"));
		
		return new AreaMetaData( id,
				description, numOfsubBasin, subBasins,basinAreas);
	}

	private ComponentMetaData createComponentMetaData(String filename)
			throws IOException {
		// logger.debug("Loading ComponentMetaData from file: " + filename);
		Properties props = PropertyLoader.loadProperties(filename);
		String includeFile = props.getProperty("includefile");
		if (includeFile != null) {
			String absoluteIncludeFile = new File(filename).getParent().concat(
					File.separator).concat(includeFile);
			Properties includeProps = PropertyLoader
					.loadProperties(absoluteIncludeFile);
			props.putAll(includeProps);
		}
		String id = props.getProperty("id");
		if (id == null)
			error(filename + ": No component id given!");
		String description = props.getProperty("description");
		String version = props.getProperty("version");
		String author = props.getProperty("author");

		String compClass = props.getProperty("compClass");
		if (compClass == null)
			error(filename + ": No compClass given!");
		String unitClass = props.getProperty("unitClass");

		String[] impInterfaces = ConfigurationHelper.parseList(props
				.getProperty("impInterfaces"));
		String[] expInterfaces = ConfigurationHelper.parseList(props
				.getProperty("expInterfaces"));

		TimeStep timeStep = null;
		
			try {
				timeStep = ConfigurationHelper.parseTimeStep(props
						.getProperty("timeStep"), props
						.getProperty("timeStepUnit"));
			} catch (Exception e) {
				error(filename + ": " + e.getMessage());
			}
		
		int subcycleCount = 1;
		try {
			subcycleCount = ConfigurationHelper.parseInt(props.getProperty(
					"subcycleCount", "1"));
		} catch (Exception e) {
			error(filename + ": subcycleCount: " + e.getMessage());
		}
		int subcycleOffset = 1;
		try {
			subcycleOffset = ConfigurationHelper.parseInt(props.getProperty(
					"subcycleOffset", "1"));
		} catch (Exception e) {
			error(filename + ": subcycleOffset: " + e.getMessage());
		}
		if (subcycleOffset > subcycleCount)
			error(filename + ": subcycleOffset (" + subcycleOffset
					+ ") greater than subcycleCount(" + subcycleCount + ")!");

		// TODO read scenario properties

		return new ComponentMetaData(id, description, version, author,
				compClass, unitClass, 
				impInterfaces, expInterfaces, subcycleCount,
				subcycleOffset, timeStep, props
			     );
	}

	private void error(String mes) {
		errorFlag = true;
		errorMes.append(mes + "\n");
	}

	private <E> List<E> toList(Set<E> set) {
		List<E> list = new ArrayList<E>();
		list.addAll(set);
		return list;
	}

}
