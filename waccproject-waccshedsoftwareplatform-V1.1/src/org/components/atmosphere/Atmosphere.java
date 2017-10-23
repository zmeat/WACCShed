/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.atmosphere;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.components.interfaces.AtmosphereToCity;
import org.components.interfaces.AtmosphereToFarmer;
import org.components.interfaces.AtmosphereToHydrology;
import org.simulation.model.AbstractModel;
import org.tables.PrecipitationDataTable;
import org.utilities.internal.DataBaseManager;
import org.utilities.internal.HSQLDataBaseManager;
import org.utilities.internal.mysqlDataBaseManager;
import org.utilities.time.SystemCalendar;

/*
 * Atmosphere Model: controls the operations of Atmosphere module in different units (subbasins), In this platform
 * it only concerns about hourly precipitation data. It fetches the historical hourly precip data from database, and 
 * output it to datatable to be available for other modules (Hydrology, City, Farmer etc).
 */

public class Atmosphere extends AbstractModel<AtmosphereUnit> implements AtmosphereToHydrology, AtmosphereToFarmer,AtmosphereToCity{


	//data table related variable
	private PrecipitationDataTable precipitationDataTable;

	//private String precipDataBaseTable = "precip1997_2013";
	private String lowPrecipTable = "lowPrecipScenario";
	private String normalPrecipTable = "normalPrecipScenario";
	private String highPrecipTable = "highPrecipScenario";
	private String precipTable = null;

	private int year;
	private int numOfGauge = 1;

	private DataBaseManager db_precip = null; // database to fetch precip data every hour. 

	private String[] pids; // subbasin id


	private double[][] dailyPrecip =null;
	private double[] yearlyPrecip = null;
	private double[] growPrecip = null;

	// scenario informaiton
	private double[][] maxDischargeScenario = null;
	private double[][] maxDischargeProb = null;
	private double[][] growPrecipScenario = null;

	private int numOfScenario = 3;


	// action date
	private int[] plant_date_before = { 4,30,23,0};
	private int[] harvest_date = {10, 15};

	// random number generator
	//		private Random rnd = null;
	BufferedReader scenarioReader;
	File scenariofile;

	// file path
	private String AtmosphereScenarioDir = System.getProperty("user.dir")+"/res/data/atmosphere/atmosphereScenarios/";
	private String scenarioFile = System.getProperty("user.dir")+"/res/data/scenarioID.txt";

	@Override
	public PrecipitationDataTable getPrecipitation() {
		return precipitationDataTable;
	}

	@Override
	protected void compute(SystemCalendar t) {

		logger().debug("Finished computation at date: " + t.toString());
	}

	@Override
	protected void getData(SystemCalendar t) {
		logger().debug("AtmosphereComp Getting data at date: " + t.toString());

	}

	/*
	 * main function for atmosphere class, providing precip data
	 * @see org.simulation.model.AbstractModel#provide(org.utilities.time.SystemCalendar)
	 */
	@Override
	protected void provide(SystemCalendar t) {
		logger().debug("AtmosphereComp providing data at date: " + t.toString());

		if(t.getMonth() == 1 && t.getDay() == 1 && t.getHour()==0){
			int precipScenario;
			try {
				precipScenario = Integer.valueOf(scenarioReader.readLine());
				if(precipScenario==0){
					precipTable = lowPrecipTable;
					year = 1999;
				}else if(precipScenario==1){
					precipTable = normalPrecipTable;
					year = 2007;
				}else{
					precipTable = highPrecipTable;
					year = 2005;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


//			System.out.println("weather = "+year);
		}
		// Construct the timeIndex with different year
		int month = t.getMonth();
		int day = t.getDay();
		int hour = t.getHour();

		// Simple fix for leap days
		if (month == 2 && day == 29) {
			day = 28;
		}
		
		String smonth = String.valueOf(month).length()>1 ? String.valueOf(month):"0"+String.valueOf(month);
		String sday = String.valueOf(day).length()>1 ? String.valueOf(day):"0"+String.valueOf(day);
		String shour = String.valueOf(hour).length()>1 ? String.valueOf(hour):"0"+String.valueOf(hour);
		String timeIndex = String.valueOf(year)+"-"+smonth+"-"+sday+" "+shour+":00:00";


		// fetch data from database
		String queryCommand = "select hourly_precip from " + precipTable + " where time = "+"'"+timeIndex+"'";
		ResultSet rs = db_precip.queryDB(queryCommand);


		double[][] precipTemp = new double[this.pids().length][numOfGauge];
		double[][] precipGageWeightTemp = new double[this.pids().length][numOfGauge];

		int size = 0;
		try { // TODO Diego: This seems inefficient. Check if it can be done without iteration
			while(rs.next()){
				size++;
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		int indexTemp = 0;
		if(size==0){
			precipTemp[0][0] = 0;
			precipGageWeightTemp[0][0]=1;
		}else{
			try {
				rs.beforeFirst();
				while(rs.next()){
					precipTemp[0][indexTemp] = rs.getDouble("hourly_precip"); 
					precipGageWeightTemp[0][indexTemp] = 1;
					indexTemp++;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// set the data to dataTable
		for(int i=0; i<this.pids().length; i++){
			precipitationDataTable.setPrecipitationValueByIndex(i, precipTemp[i]);
			precipitationDataTable.setGageWeightByIndex(i, precipGageWeightTemp[i]);
		}

		// calculate precip over all gages
		for(int i=0; i<this.pids().length; i++){
			double tmpPrecip = 0;
			for(int j=0; j<numOfGauge; j++)
			{
				this.dailyPrecip[i][j] += precipTemp[i][j];
				tmpPrecip += precipTemp[i][j]*precipGageWeightTemp[i][j];
			}

			this.yearlyPrecip[i] += tmpPrecip;
			if(t.after(new SystemCalendar(t.getYear(),plant_date_before[0],plant_date_before[1],plant_date_before[2],plant_date_before[3])) && t.before(new SystemCalendar(t.getYear(),harvest_date[0],harvest_date[1]))){
				this.growPrecip[i] += tmpPrecip;
			}
		}

		// get daily precip.
		if(t.getHour() == 23){

			for(int i=0; i<this.pids().length; i++){
				precipitationDataTable.setDailyPrecipValue(this.pids[i], dailyPrecip[i]);
			}

			// clear the variables at the end of each day.
			for(int i=0; i<this.dailyPrecip.length; i++)
				for(int j=0; j<this.dailyPrecip[i].length; j++)
					this.dailyPrecip[i][j]=0;
		}

		// reinitiate the variables at the end of year. 
		if(t.getMonth() == 12 && t.getDay()==31 && t.getHour()==23){
			for(int i=0; i<this.areaMetaData().getNumOfsubBasin(); i++){
				this.yearlyPrecip[i]=0;
				this.growPrecip[i] = 0;
			}
		}

		// the yearlyPrecip and growPrecip updated hourly
		for(int i=0; i<this.pids().length; i++){
			precipitationDataTable.setYearlyPrecipValue(this.pids[i], yearlyPrecip[i]);
			precipitationDataTable.setGrowPrecipValue(this.pids[i], growPrecip[i]);
		}

	}

	@Override
	protected void store(SystemCalendar t) {
		logger().debug("AtmosphereComp Storing at date: " + t.toString());
	}

	@Override
	protected void init() {

		// make connection to database
		db_precip = new mysqlDataBaseManager();

		// init random number generator
		/*			rnd = new Random();
			rnd.setSeed(3L);*/

		
/*		int scenarioNum = 1;
		try {
			BufferedReader sreader = new BufferedReader(new FileReader(new File(scenarioFile)));
			scenarioNum = Integer.valueOf(sreader.readLine());
		}catch(Exception e){
			e.printStackTrace();
		}
*/
		int scenarioNum = this.getSimParameters().getScenarioID();
		
		scenariofile = new File(AtmosphereScenarioDir+"atmosphereScenario20years_"+scenarioNum+".txt");
		try{
			scenarioReader = new BufferedReader(new FileReader(scenariofile));
		}catch(Exception e){
			e.printStackTrace();
		}


		pids = this.pids();		

		dailyPrecip = new double[pids.length][numOfGauge];
		yearlyPrecip = new double[pids.length];
		growPrecip = new double[pids.length];
		//initialize table
		this.precipitationDataTable = new PrecipitationDataTable(this.areaMetaData());
		//read default table
		maxDischargeScenario = new double[pids.length][numOfScenario];
		maxDischargeProb = new double[pids.length][numOfScenario];
		growPrecipScenario = new double[pids.length][numOfScenario];

		// read init data from file. the maximum discharge scenario and grow precip scenario is precomputed and given. 
		BufferedReader reader;
		File infile;

		infile = new File(AtmosphereScenarioDir+"atmosphere_info");

		try {
			reader = new BufferedReader(new FileReader(infile));
			for (int pix=0; pix<pids.length; pix++) {
				reader.readLine();
				String maxDischargeLine = reader.readLine();
				String[] maxDischargeLineSplit = maxDischargeLine.split(",");
				String maxDischargeProbLine = reader.readLine();
				String[] maxDischargeProbLineSplit = maxDischargeProbLine.split(",");

				String growPrecip = reader.readLine();
				String[] growPrecipLineSplit = growPrecip.split(",");

				for(int i=0; i<this.numOfScenario; i++){
					maxDischargeScenario[pix][i] = Double.parseDouble(maxDischargeLineSplit[i]);
					maxDischargeProb[pix][i] = Double.parseDouble(maxDischargeProbLineSplit[i]);
					growPrecipScenario[pix][i] = Double.parseDouble(growPrecipLineSplit[i]);
				}

				// set gageWeight to 1
				double[] precip = new double[numOfGauge];
				double[] precip_GageWeight = new double[numOfGauge];
				for(int j=0; j<numOfGauge; j++){
					precip[j] = 0;
					precip_GageWeight[j] = 1;
				}

				// set init value for preciptationDataTable, this scenarios of max discharge and Grow Precip are given here.
				this.precipitationDataTable.setPrecipitationValueByIndex(pix, precip);
				this.precipitationDataTable.setGageWeightByIndex(pix, precip_GageWeight);
				this.precipitationDataTable.setDailyPrecipValue(pids[pix], precip);
				this.precipitationDataTable.setMaxDischargeProbValueByIndex(pix, maxDischargeProb[pix]);
				this.precipitationDataTable.setMaxDischargeScenarioValueByIndex(pix, maxDischargeScenario[pix]);
				this.precipitationDataTable.setGrowPrecipScenarioValueByIndex(pix, growPrecipScenario[pix]);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (String pid : pids()) {
			unitTable().getUnit(pid).setPrecipitation( precipitationDataTable.getPrecipitationValue(pid));				
			unitTable().getUnit(pid).setGageWeight(precipitationDataTable.getGageWeightValue(pid));
		}

	}

}
