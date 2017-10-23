/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.farmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.components.interfaces.AtmosphereToFarmer;
import org.components.interfaces.CityToFarmer;
import org.components.interfaces.FarmerToCity;
import org.components.interfaces.FarmerToHydrology;
import org.configuration.SimulationParameters;
import org.components.interfaces.EconomicToFarmer;
import org.simulation.model.AbstractModel;
import org.tables.CityDataTable;
import org.tables.EconomicDataTable;
import org.tables.FarmerDataTable;
import org.tables.PrecipitationDataTable;
import org.utilities.time.SystemCalendar;

public class FarmerModel extends AbstractModel<FarmerUnit> implements FarmerToCity, FarmerToHydrology{
	
	// farmerIdentity related variables
	private FarmerAgent[] farmerAgents = null;
	private String[] farmerID = null;
	private double[] farmerTotalArea = null;
	private double[] farmerRetentArea = null;
	private double[] farmerCropArea = null;
	private double[] farmerFallowArea = null;
	private double[] finance_balance = null;
	private String[] locatedCities = null;
	private String[] locatedSubBasins = null;
	private double[] locatedCityCN_weight = null;
	private int numOfFarmer = 0;
	private String FarmersInformationDir = System.getProperty("user.dir")+"/res/data/farmer/farmers_info/";
	
	// communicationInterface
	private CityToFarmer f_cityToFarmer = null;
	private AtmosphereToFarmer f_atmosphereToFarmer = null;
	private EconomicToFarmer f_economicToFarmer = null;
	
	// datatables
	private CityDataTable f_cityDataTable = null;
	private FarmerDataTable f_farmerDataTable = null;
	private PrecipitationDataTable f_precipitationDataTable = null;
	private EconomicDataTable f_economicDataTable = null;
	
	// Action date
	private int[] getThisYearSubsidyRate_date = {3,15};
	
	// other variables
	double[] init_CN = {10,70};  // curve number for water retention area and fallow land
	
	
	// construction method
	public FarmerModel(){

		
	}
	
	/*
	 * initializion the model
	 * @see org.simulation.component.AbstractComponent#init()
	 */
	@Override
	protected void init() {
		String[] FarmerAgentsName = new File(FarmersInformationDir).list();
		numOfFarmer = FarmerAgentsName.length;
		farmerAgents = new FarmerAgent[numOfFarmer];
		farmerID = new String[numOfFarmer];
		locatedCities = new String[numOfFarmer];
		locatedSubBasins = new String[numOfFarmer];
		farmerTotalArea = new double[numOfFarmer];
		farmerRetentArea = new double[numOfFarmer];
		farmerCropArea = new double[numOfFarmer];
		farmerFallowArea = new double[numOfFarmer];
		finance_balance = new double[numOfFarmer];
		locatedCityCN_weight = new double[numOfFarmer];
		
		BufferedReader reader;
		File infile;
		
		String farmerTypeTemp = null;

		
		for(int i=0; i<numOfFarmer; i++)
		{
			locatedCityCN_weight[i] = 0.1;
			infile = new File(FarmersInformationDir+FarmerAgentsName[i]);
			try {
				reader = new BufferedReader(new FileReader(infile));
				farmerTypeTemp = reader.readLine();
				
				farmerID[i] = reader.readLine();;
				locatedCities[i] = reader.readLine();
				locatedSubBasins[i] = reader.readLine();;
				farmerTotalArea[i] = Double.parseDouble(reader.readLine());
				farmerCropArea[i] = Double.parseDouble(reader.readLine());
				farmerRetentArea[i] = Double.parseDouble(reader.readLine());
				farmerFallowArea[i] = Double.parseDouble(reader.readLine());
				finance_balance[i] = Double.parseDouble(reader.readLine());
				
				farmerCropArea[i] = farmerTotalArea[i]-farmerRetentArea[i]-farmerFallowArea[i];
				Class<?> clazz = Class.forName("org.components.farmer."+farmerTypeTemp);
				Constructor<?> constructor = clazz.getConstructor(String.class, String.class, SimulationParameters.class);
				farmerAgents[i] = (FarmerAgent)constructor.newInstance(farmerID[i],locatedCities[i], this.getSimParameters());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		f_cityToFarmer = getImport("org.components.interfaces.CityToFarmer");
		f_atmosphereToFarmer = getImport("org.components.interfaces.AtmosphereToFarmer");
		f_economicToFarmer = getImport("org.components.interfaces.EconomicToFarmer");
		
		f_farmerDataTable = new FarmerDataTable(numOfFarmer,farmerID,locatedSubBasins);
		

		double init_subsidyRate = 0;
		
		
		for(int i=0; i<numOfFarmer; i++){
			f_farmerDataTable.setTotalAreaByFarmerIndex(i,this.farmerTotalArea[i]);
			f_farmerDataTable.setRetentAreaByFarmerIndex(i,farmerRetentArea[i]);
			f_farmerDataTable.setRetentPercentByFarmerIndex(i, farmerRetentArea[i]/farmerTotalArea[i]);
			f_farmerDataTable.setCNByFarmerIndex(i, init_CN);
			f_farmerDataTable.setCropAreaByFarmerIndex(i, farmerCropArea[i]);
			f_farmerDataTable.setFallowAreaByFarmerIndex(i, farmerFallowArea[i]);
		    farmerAgents[i].setSubsidyRate(init_subsidyRate);
		    farmerAgents[i].setMoneyBalance(finance_balance[i]);
		    farmerAgents[i].setInitAreaDivision(farmerCropArea[i], farmerRetentArea[i], farmerFallowArea[i]);
		}
		
		
	}
	
	
	/*
	 * Model Cycle methods.
	 * @see org.simulation.model.AbstractModel#provide(org.utilities.time.SystemCalendar)
	 */
	@Override
	protected void provide(SystemCalendar t) {
		for(int i=0; i<numOfFarmer; i++)
		{
			double retentPercentTemp = farmerAgents[i].getRetentPercent();
			double[] cn_weightTemp = new double[2];
			double[] cn_Temp = new double[2]; 
			cn_Temp[0] = init_CN[0];
			cn_Temp[1] = farmerAgents[i].getCurrentCN();
			
			cn_weightTemp[0] = retentPercentTemp*(1-locatedCityCN_weight[i]);
			cn_weightTemp[1] = (1-retentPercentTemp)*(1-locatedCityCN_weight[i]);
			this.f_farmerDataTable.setRetentPercentByFarmerIndex(i, farmerAgents[i].getRetentArea()/this.farmerTotalArea[i]);
			this.f_farmerDataTable.setRetentAreaByFarmerIndex(i, farmerAgents[i].getRetentArea());
			this.f_farmerDataTable.setCropAreaByFarmerIndex(i,farmerAgents[i].getCropArea() );
			this.f_farmerDataTable.setFallowAreaByFarmerIndex(i, farmerAgents[i].getFallowArea());
			this.f_farmerDataTable.setCN_weightByFarmerIndex(i, cn_weightTemp);
			this.f_farmerDataTable.setCNByFarmerIndex(i, cn_Temp);
			this.f_farmerDataTable.setMoneyBalanceByFarmerIndex(i, farmerAgents[i].getMoneyBalance());
		    this.f_farmerDataTable.setIsDeadStatusByFarmerIndex(i, farmerAgents[i].getIsDeadStatus());
		}
	}
	
	@Override
	protected void getData(SystemCalendar t) {
		// get weather condition
		f_precipitationDataTable = f_atmosphereToFarmer.getPrecipitation();
		f_cityDataTable = f_cityToFarmer.getCityDataTable();
		f_economicDataTable = f_economicToFarmer.getEconomicDataTable();
		
		for(int i=0; i<numOfFarmer; i++){
			double[] precipTemp = f_precipitationDataTable.getDailyPrecipValue(locatedSubBasins[i]);
			double[] gageWeightTemp = f_precipitationDataTable.getGageWeightValue(locatedSubBasins[i]);
			double averagePrecipTemp = 0;
			for(int j=0; j<precipTemp.length; j++){
				averagePrecipTemp += precipTemp[j]*gageWeightTemp[j];
			}
			farmerAgents[i].setCurrentPrecip(averagePrecipTemp);
		    try {
				this.locatedCityCN_weight[i] = f_cityDataTable.getCN_weightByCityID(this.locatedCities[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    farmerAgents[i].setPrice_CostData(f_economicDataTable.getCurrentCropPrice(locatedSubBasins[i]));
		}
		
		//get subsidyrate on December
		if(t.getMonth() == getThisYearSubsidyRate_date[0] &&  t.getDay() == getThisYearSubsidyRate_date[1]){
			for(int i=0; i<numOfFarmer; i++){
				try {
					farmerAgents[i].setSubsidyRate(f_cityDataTable.getSubsidyRateForFarmerByCityID(locatedCities[i]));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		// set different scenario to farmer agents before farmer making decisions.
		if(t.getMonth() == 1 && t.getDay() == 2 ){
			for(int i=0; i<numOfFarmer; i++){
				farmerAgents[i].setLookAheadPrecipScenarios( this.f_precipitationDataTable.getMaxDischargeProbValue(this.locatedSubBasins[i]),
						this.f_precipitationDataTable.getGrowPrecipScenarioValue(this.locatedSubBasins[i]));
			    farmerAgents[i].setLookAheadScenarios(f_economicDataTable.getCropPriceScenario(locatedSubBasins[i]), f_economicDataTable.getCropPriceProb(locatedSubBasins[i]), f_economicDataTable.getCurrentSeedCost(locatedSubBasins[i]), f_economicDataTable.getSeedCostScenario(locatedSubBasins[i]), f_economicDataTable.getSeedCostProb(locatedSubBasins[i]));
			}
		}
		
	}
	
	@Override
	protected void compute(SystemCalendar t) {	
		for(int i=0; i<numOfFarmer; i++){
			farmerAgents[i].compute(t);
		}
	}
		
	@Override
	protected void store(SystemCalendar t) {
		
	}

	
	/*
	 *  methods from interfaces(non-Javadoc)
	 * @see org.components.interfaces.FarmerToCity#getFarmerDataTable()
	 */
	@Override
	public FarmerDataTable getFarmerDataTable() {
		// TODO Auto-generated method stub
		return this.f_farmerDataTable;
	}
	
}
