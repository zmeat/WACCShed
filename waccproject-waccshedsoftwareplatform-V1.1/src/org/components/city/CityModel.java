/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.city;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;

import org.components.interfaces.AtmosphereToCity;
import org.components.interfaces.CityToFarmer;
import org.components.interfaces.CityToHydrology;
import org.components.interfaces.EconomicToCity;
import org.components.interfaces.FarmerToCity;
import org.components.interfaces.HydrologyToCity;
import org.configuration.SimulationParameters;
import org.simulation.model.AbstractModel;
import org.tables.CityDataTable;
import org.tables.EconomicDataTable;
import org.tables.FarmerDataTable;
import org.tables.HydrologyDataTable;
import org.tables.PrecipitationDataTable;
import org.utilities.time.SystemCalendar;

/*
 * City Model: controls the operations of All City Agents within the system.
 */
public class CityModel extends AbstractModel<CityUnit> implements CityToFarmer,CityToHydrology{
	
	// collection of city agents Infos. 
	private CityAgent[] cityAgents = null;
	private String[] cityIDs = null;
	private String[] c_locatedSubBasins = null;
	private int numOfCity = 0;
	private int[] numOffarmerPerCity = null;
	private String[][] farmerIDPerCity = null;
	private double[] budget = null;
	private String CitiesInformationDir = System.getProperty("user.dir")+"/res/data/city/cities_info/";
	
	//communicationInterface and datatables
	private FarmerToCity c_farmerToCity = null;
	private HydrologyToCity c_hydrologyToCity = null;
	private AtmosphereToCity c_atmosphereToCity = null;
	private EconomicToCity c_ecoToCity = null;
	
	private CityDataTable c_cityDataTable = null;
	private HydrologyDataTable c_hydrologyDataTable = null;
	private FarmerDataTable c_farmerDataTable = null;
	private PrecipitationDataTable c_precipDataTable = null;
	private EconomicDataTable c_ecoDataTable = null;
	
	// Action dates
    private int[] setSubsidyRate = {2,2};
	
    /*
     * construction method, init all variables, and read in city Manager info from file. 
     */
	public CityModel(){

	}
	
	/*
	 * init data table and cityAgents
	 * @see org.simulation.component.AbstractComponent#init()
	 */
	@Override
	protected void init() {
		String[] CityAgentsName = new File(CitiesInformationDir).list();
		numOfCity = CityAgentsName.length;
		cityIDs = new String[numOfCity];
		cityAgents = new CityAgent[numOfCity];
		c_locatedSubBasins = new String[numOfCity];
		numOffarmerPerCity = new int[numOfCity];
		farmerIDPerCity = new String[numOfCity][];
		budget = new double[numOfCity];
		
		BufferedReader reader;
		File infile;
		
		String cityTypeTemp = null;
		String cityIDTemp = null;
		String locatedSubBasinTemp = null;
		String[] farmerIDTemp = null;
		
		for(int i=0; i<numOfCity; i++)
		{
			infile = new File(CitiesInformationDir+CityAgentsName[i]);
			try {
				reader = new BufferedReader(new FileReader(infile));
				cityTypeTemp = reader.readLine();
				cityIDTemp = reader.readLine();
				locatedSubBasinTemp = reader.readLine();
				numOffarmerPerCity[i] = Integer.parseInt(reader.readLine());
				farmerIDTemp = reader.readLine().split(" ");
				budget[i] = Double.parseDouble(reader.readLine());
				
				Class<?> clazz = Class.forName("org.components.city."+cityTypeTemp);
				Constructor<?> constructor = clazz.getDeclaredConstructor(new Class[]{String.class, String.class, int.class,String[].class, SimulationParameters.class});
				
				double theta = simParameters.getTheta();
				double lqe = simParameters.getLQE();
				
				cityAgents[i] = (CityAgent)constructor.newInstance(cityIDTemp,locatedSubBasinTemp,numOffarmerPerCity[i],farmerIDTemp, simParameters);
				cityIDs[i] = cityIDTemp;
				c_locatedSubBasins[i] = locatedSubBasinTemp;
				farmerIDPerCity[i] = new String[numOffarmerPerCity[i]];
                for(int j=0; j<numOffarmerPerCity[i]; j++){
                	farmerIDPerCity[i][j] = farmerIDTemp[j];
                }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		c_farmerToCity = getImport("org.components.interfaces.FarmerToCity");
		c_hydrologyToCity = getImport("org.components.interfaces.HydrologyToCity");
		c_atmosphereToCity = getImport("org.components.interfaces.AtmosphereToCity");
		c_ecoToCity = getImport("org.components.interfaces.EconomicToCity");
		
		this.c_cityDataTable = new CityDataTable(numOfCity,numOffarmerPerCity,cityIDs,c_locatedSubBasins);
		
		// this init value will be overwritten in the simulation process. 
		double init_subsidy_percent = 0;
		double init_levee_percent = 0.7;
		double init_subsidyRate = 0;
		double init_CN = 90;
		double init_CN_weight = 0.1;
		double init_maxYearlyDischarge = 0;
		
		for(int i=0; i<numOfCity; i++){
		    this.c_cityDataTable.setSubsidyRateForFarmerByCityIndex(i, init_subsidyRate);
		    this.c_cityDataTable.setCityCNByCityIndex(i,init_CN);
		    this.c_cityDataTable.setCityCN_weightByCityIndex(i, init_CN_weight);
		    cityAgents[i].setMaxDischargeOfYear(init_maxYearlyDischarge);
		    cityAgents[i].setInitYearBudget(budget[i]);
		    cityAgents[i].setInitBudgetArrange(init_subsidy_percent, init_levee_percent,init_subsidyRate);
		}
	}
	
	/*
	 * All city Agents will be making their decision in this function
	 * @see org.simulation.model.AbstractModel#compute(org.utilities.time.SystemCalendar)
	 */
	@Override
	protected void compute(SystemCalendar t) {	
		for(int i=0; i<numOfCity; i++)
		{
			cityAgents[i].compute(t);
		}
	}
	
	/*
	 * All city Agents will import external data table in this function 
	 * @see org.simulation.model.AbstractModel#getData(org.utilities.time.SystemCalendar)
	 */
	@Override
	protected void getData(SystemCalendar t) {
		c_farmerDataTable = c_farmerToCity.getFarmerDataTable();
		c_hydrologyDataTable = c_hydrologyToCity.getHydrologyDataTable();
		c_precipDataTable = c_atmosphereToCity.getPrecipitation();
		c_ecoDataTable = c_ecoToCity.getEconomicDataTable();
		
		for(int i=0; i<numOfCity; i++){
			cityAgents[i].setDataTable(c_hydrologyDataTable, c_farmerDataTable, c_precipDataTable,c_ecoDataTable);
			cityAgents[i].getData(t);
		}

	}
	
	/*
	 * update itself's data table to be avaible in the resource pool. 
	 * @see org.simulation.model.AbstractModel#provide(org.utilities.time.SystemCalendar)
	 */
	@Override
	protected void provide(SystemCalendar t) {
    	if(t.getMonth() == setSubsidyRate[0] && t.getDay() == setSubsidyRate[1])
        {
    		for(int i=0; i<numOfCity; i++)
    		{
    			try {
    				this.c_cityDataTable.setSubsidyRateForFarmerByCityIndex(i, cityAgents[i].get_subsidy_rate());
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
        }
	}
	
	@Override
	protected void store(SystemCalendar t) {
		for(int i=0; i<numOfCity; i++)
		{
			cityAgents[i].store(t);
		}
	}

	@Override
	public CityDataTable getCityDataTable() {
		// TODO Auto-generated method stub
		return this.c_cityDataTable;
	}
	
}
