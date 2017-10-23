/*
 * Author: Yu Jie, David Dziubanski
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.hydrology;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.components.interfaces.AtmosphereToHydrology;
import org.components.interfaces.CityToHydrology;
import org.components.interfaces.FarmerToHydrology;
import org.components.interfaces.HydrologyToCity;
import org.simulation.model.AbstractModel;
import org.tables.CityDataTable;
import org.tables.FarmerDataTable;
import org.tables.HydrologyDataTable;
import org.tables.PrecipitationDataTable;
import org.utilities.internal.DataBaseManager;
import org.utilities.internal.HSQLDataBaseManager;
import org.utilities.internal.mysqlDataBaseManager;
import org.utilities.time.SystemCalendar;

public class Hydrology extends AbstractModel<HydrologyUnit> implements HydrologyToCity{
	
	private static final String HYDROLOGY_DATA_DIR = System.getProperty("user.dir")+File.separator+"res"+File.separator+"data"+File.separator+"hydrology"+File.separator;
	private static final String HYDROLOGY_OUTDATA_DIR = System.getProperty("user.dir")+File.separator+"res"+File.separator+"data"+File.separator+"hydrology"+File.separator+"output"+File.separator;

	// database
    private DataBaseManager dischargeDB;
	
	// communication interface
	private FarmerToHydrology h_farmerToHydrology = null;
	private CityToHydrology h_cityToHydrology = null;
	private AtmosphereToHydrology h_atmosphereToHydrology = null;
	private SystemCalendar m_pSimulationTime;
	private List<Double> discharge;
    
	// dates
	private int[] MaxDischargeDate = {10,10,0};
	
	//export data
	private HydrologyDataTable m_tHydrologyDataTable;
	
	//import data
	private PrecipitationDataTable precipitationDataTable = null;
	private FarmerDataTable h_farmerDataTable = null;
	private CityDataTable h_cityDataTable = null;
	
	//riverreach datatable

	private static final String HYDROLOGYPARAMETER = "hydrologyParameter.txt";

	private static final int NODATA_VALUE = -9999;
	
	// Yearly Total Precip for each subBasin
	private double[] yearPrecip = null;
	
	// variable for record index of discharge of current year
	private int curYear_Qindex = 0;
	
	//implementations of function in interface

	@Override
	public HydrologyDataTable getHydrologyDataTable() {
		// TODO Auto-generated method stub
		return this.m_tHydrologyDataTable;
	}	
	
	
	
	@Override
	protected void compute(SystemCalendar t) {

		 logger().debug("HydrologyComp Finished computation at date: " + t.toString());
		 /// compute discharge in subbasins
		 for(String i: pids())
		 {
			 unitTable().getUnit(i).computeUnit(t);
		 }
	}

	// function for concatenate A and B.
	public double[] concatenate (double[] A, double[] B) {
	    int aLen = A.length;
	    int bLen = B.length;

	    @SuppressWarnings("unchecked")
	    double[] C = new double[aLen+bLen];
	    System.arraycopy(A, 0, C, 0, aLen);
	    System.arraycopy(B, 0, C, aLen, bLen);

	    return C;
	}
	
	@Override
	protected void getData(SystemCalendar t) {
				
		logger().debug("HydrologyComp Getting data at date: " + t.toString());
		h_cityDataTable = h_cityToHydrology.getCityDataTable();
		h_farmerDataTable = h_farmerToHydrology.getFarmerDataTable();
		precipitationDataTable = h_atmosphereToHydrology.getPrecipitation();
		
		if(t.getMonth()==1 && t.getDay()==1 && t.getHour()==0){
			for(int i=0; i<this.yearPrecip.length; i++){
				yearPrecip[i] = 0;
			}
		}
		for (int pid=0; pid < this.areaMetaData().getNumOfsubBasin(); pid++) {
			
			double[] tmpPrecip = precipitationDataTable.getPrecipitationValueByIndex(pid);
			double[] tmpGageWeight = precipitationDataTable.getGageWeightByIndex(pid);
			double tmpSbPrecip = 0;
			for(int i=0; i<tmpPrecip.length; i++){
				tmpSbPrecip = tmpSbPrecip + tmpPrecip[i]*tmpGageWeight[i];
			}
			yearPrecip[pid] = yearPrecip[pid]+tmpSbPrecip;
		
		}
		
		
		for (String i : pids()) {		
			unitTable().getUnit(i).setPrecipitation(precipitationDataTable.getPrecipitationValue(i));
			unitTable().getUnit(i).setGageWeight(precipitationDataTable.getGageWeightValue(i));
			
			double[] cityCN = h_cityDataTable.getCityCNBySubBasinID(i);
			double[] cityCN_weight = h_cityDataTable.getCityCN_weightsBySubBasinID(i);
			double[] farmerCN = h_farmerDataTable.getFarmerCNBySubBasinID(i);
			double[] farmerCN_weight = h_farmerDataTable.getFarmerCN_weightsBySubBasinID(i);
			
			unitTable().getUnit(i).setLanduse(this.concatenate(cityCN, farmerCN), this.concatenate(cityCN_weight, farmerCN_weight));

		}
	}

	@Override
	protected void provide(SystemCalendar t) {
		
		logger().debug("HydrologyComp providing data at date: " + t.toString());
		
		List<Double> tempDischarge = new ArrayList<Double>();
		for (String pid : pids()) {
			m_tHydrologyDataTable.setSubBasinDischarge(pid, unitTable().getUnit(pid).getSubBasinDischarge());
			tempDischarge = unitTable().getUnit(pid).getSubBasinDischarge();
		}
		m_tHydrologyDataTable.setDischarge(tempDischarge);
				 
		 if(t.getMonth() == MaxDischargeDate[0] && t.getDay() == MaxDischargeDate[1] && t.getHour() == MaxDischargeDate[2]){
			 double maxYearlyDischarge = Collections.max(tempDischarge.subList(curYear_Qindex, tempDischarge.size())); 
				 m_tHydrologyDataTable.setMaxYearlyDischarge(maxYearlyDischarge);          
				curYear_Qindex = tempDischarge.size();
		 }
	}

	
	@Override
	protected void store(SystemCalendar t) {
	}

	@Override
	protected void init() {
		this.dischargeDB = new mysqlDataBaseManager();;
		
		yearPrecip = new double[this.areaMetaData().getNumOfsubBasin()];
		
		
		h_farmerToHydrology = getImport("org.components.interfaces.FarmerToHydrology");
		h_cityToHydrology = getImport("org.components.interfaces.CityToHydrology");
		h_atmosphereToHydrology = getImport("org.components.interfaces.AtmosphereToHydrology");
		
		File infile = new File(HYDROLOGY_DATA_DIR+HYDROLOGYPARAMETER);

		//initialize table
		this.m_tHydrologyDataTable = new HydrologyDataTable(this.areaMetaData());
		//read default table
		
	   	try {
			readDataTable(infile);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   	
	   	// set init value for discharges
	   	this.discharge = new ArrayList<Double>();
	   	for(int i=0; i<29; i++)
    	{
	   		discharge.add(Double.valueOf(0));
    	}
	   	this.m_tHydrologyDataTable.setDischarge(discharge);
	   	for(int i=0; i<this.areaMetaData().getNumOfsubBasin(); i++)
	   	{
	   		List<Double> subDischargeInit = new ArrayList<Double>();
	   		for(int j=0; j<29; j++)
	    	{
	   			subDischargeInit.add(Double.valueOf(0));
	    	}
	   		this.m_tHydrologyDataTable.setSubBasinDischargeByIndex(i, subDischargeInit);
	   	}
	   	
	   	for(String pid : pids()){
	   		unitTable().getUnit(pid).setSubBasinDischarge(this.m_tHydrologyDataTable.getSubBasinDischarge(pid));
			unitTable().getUnit(pid).setTlag(m_tHydrologyDataTable.getTlag(pid));
			unitTable().getUnit(pid).setTdelta(m_tHydrologyDataTable.getTdelta(pid));
			unitTable().getUnit(pid).setI(m_tHydrologyDataTable.getI(pid));
			unitTable().getUnit(pid).setP(m_tHydrologyDataTable.getP(pid));	
			unitTable().getUnit(pid).initUnit();
	   	}
	   	
	}
	
	public void readDataTable(File infile) throws IOException  {
		//re-read the file (for listening mode)
		BufferedReader reader;
		int numOfsubBasin;
		double[][] HydroParameterData;

		double NODATA_value;

		try {
			reader = new BufferedReader(new FileReader(infile));

			numOfsubBasin = Integer.valueOf(reader.readLine().split("\\s+")[1]);

			HydroParameterData = new double[numOfsubBasin][4]; 
			
			NODATA_value = Float.valueOf(reader.readLine().split("\\s+")[1]);
		} 
		catch (FileNotFoundException e) {
			throw new IOException("Could not read file: "+e);
		}
		catch (NumberFormatException ne) {
			throw new IOException("Error reading ASCII file (header): "+ne);
		}
		try {
			String[] line;

			for (int r=0; r<numOfsubBasin; r++) {
				line = reader.readLine().split(" ");
				for(int i=0; i<line.length;i++)
					HydroParameterData[r][i] = Double.valueOf(line[i]);
				
			}
			reader.close();
		}
		catch (NumberFormatException ne) {
			throw new IOException("Error reading ASCII file: "+ne);
		}
		catch (IOException ie) {
			throw ie;
		}
		
		for(int i=0; i<numOfsubBasin; i++){
			m_tHydrologyDataTable.setTlagByIndex(i, HydroParameterData[i][0]);
			m_tHydrologyDataTable.setTdelatByIndex(i, HydroParameterData[i][1]);
			m_tHydrologyDataTable.setIByIndex(i, HydroParameterData[i][2]);
			m_tHydrologyDataTable.setPByIndex(i, HydroParameterData[i][3]);			
		}
	}
	
}
