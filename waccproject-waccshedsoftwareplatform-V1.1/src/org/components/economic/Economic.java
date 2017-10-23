/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.economic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.components.interfaces.AtmosphereToFarmer;
import org.components.interfaces.AtmosphereToHydrology;
import org.components.interfaces.EconomicToCity;
import org.components.interfaces.EconomicToFarmer;
import org.simulation.model.AbstractModel;
import org.tables.EconomicDataTable;
import org.tables.PrecipitationDataTable;
import org.utilities.time.SystemCalendar;

/*
 * Economic Model: controls the operations of Economic module in different units (subbasins), In this platform
 * it only concerns about crop price and input cost data. It instantiate the data according to given distribution, and 
 * output it to datatable to be available for other modules (City, Farmer etc).
 */
public class Economic extends AbstractModel<EconomicUnit> implements EconomicToFarmer, EconomicToCity{
	  private EconomicDataTable economicDataTable;
	  
	  private int numOfSubBasins = 0;
		
		private double[][] cropPriceScenario = null;
		private double[][] cropPriceProb = null;
		private double[][] inputCostScenario = null;
		private double[][] inputCostProb = null;

		private int numOfScenario = 3;
		// input cost realization date
		private int[] inputCostDate = {1,1};
				
		// file path
		private String EconomicScenarioDir = System.getProperty("user.dir")+"/res/data/economic/economicScenarios/";
        private String scenarioFile = System.getProperty("user.dir")+"/res/data/scenarioID.txt";

		BufferedReader cornPriceScenarioReader;
		File cornPriceScenariofile;
		BufferedReader inputCostScenarioReader;
		File inputCostScenariofile;
		@Override
		public EconomicDataTable getEconomicDataTable() {
			return economicDataTable;
		}
	  
		@Override
		protected void compute(SystemCalendar t) {

			 logger().debug("Finished computation at date: " + t.toString());
		}

		@Override
		protected void getData(SystemCalendar t) {
			logger().debug("EconomicComp Getting data at date: " + t.toString());

		}

		@Override
		protected void provide(SystemCalendar t) {
			logger().debug("EconomicComp providing data at date: " + t.toString());
                    
           if(t.getMonth()==inputCostDate[0] && t.getDay()==inputCostDate[1]){  // production cost is know at the beginning of year
				
			// set the data to dataTable
				for(int i=0; i<this.areaMetaData().getNumOfsubBasin(); i++){				
					int cornPriceScenarioValue;
					int inputCostScenarioValue;
					try {
						cornPriceScenarioValue = Integer.valueOf(cornPriceScenarioReader.readLine());
						inputCostScenarioValue = Integer.valueOf(inputCostScenarioReader.readLine());
						if(cornPriceScenarioValue==0){
							this.economicDataTable.setCurrentCropPriceByIndex(i, cropPriceScenario[i][0]);
						}else if(cornPriceScenarioValue==1){
							this.economicDataTable.setCurrentCropPriceByIndex(i, cropPriceScenario[i][1]);
						}else{
							this.economicDataTable.setCurrentCropPriceByIndex(i, cropPriceScenario[i][2]);
						}	
						
						if(inputCostScenarioValue==0){
							this.economicDataTable.setCurrentSeedCostByIndex(i, inputCostScenario[i][0]);
						}else if(inputCostScenarioValue==1){
							this.economicDataTable.setCurrentSeedCostByIndex(i, inputCostScenario[i][1]);
						}else{
							this.economicDataTable.setCurrentSeedCostByIndex(i, inputCostScenario[i][2]);
						}	
						
				    }catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	             }
           }
		}

		@Override
		protected void store(SystemCalendar t) {
			logger().debug("AtmosphereComp Storing at date: " + t.toString());
		}

		@Override
		protected void init() {

			
			numOfSubBasins = this.areaMetaData().getNumOfsubBasin();
			String[] pids= this.pids();  // subbasin ids. 
			
			//initialize table
			this.economicDataTable = new EconomicDataTable(this.areaMetaData());
            cropPriceScenario = new double[numOfSubBasins][numOfScenario];
			cropPriceProb = new double[numOfSubBasins][numOfScenario];
            inputCostScenario = new double[numOfSubBasins][numOfScenario];
            inputCostProb = new double[numOfSubBasins][numOfScenario];
			
/*            int scenarioNum = 1;
			try {
				BufferedReader sreader = new BufferedReader(new FileReader(new File(scenarioFile)));
				scenarioNum = Integer.valueOf(sreader.readLine());
			}catch(Exception e){
				e.printStackTrace();
			}*/
            int scenarioNum = this.getSimParameters().getScenarioID();			
			
			cornPriceScenariofile = new File(EconomicScenarioDir+"cornPriceScenarios/"+"cornPriceScenario20years_"+scenarioNum+".txt");
			inputCostScenariofile = new File(EconomicScenarioDir+"inputCostScenarios/"+"inputCostScenario20years_"+scenarioNum+".txt");
			try{
			   cornPriceScenarioReader = new BufferedReader(new FileReader(cornPriceScenariofile));
			   inputCostScenarioReader = new BufferedReader(new FileReader(inputCostScenariofile));
			}catch(Exception e){
				e.printStackTrace();
			}

			// initiate scenario information for each subbasin. 
			for (int pix=0; pix<numOfSubBasins; pix++) {
				cropPriceProb[pix][0]=0.25;
				cropPriceProb[pix][1]=0.5;
				cropPriceProb[pix][2]=0.25;
				cropPriceScenario[pix][0]=3.66;
				cropPriceScenario[pix][1]=4.4;
				cropPriceScenario[pix][2]=5.68;

				this.inputCostScenario[pix][0] = 604.2;
				this.inputCostScenario[pix][1] = 698;
				this.inputCostScenario[pix][2] = 815.5;
				this.inputCostProb[pix][0]=0.25;
				this.inputCostProb[pix][1]=0.5;
				this.inputCostProb[pix][2]=0.25;
				

				this.economicDataTable.setCropPriceScenarioByIndex(pix, cropPriceScenario[pix]);
				this.economicDataTable.setCropPriceProbByIndex(pix, cropPriceProb[pix]);
				this.economicDataTable.setSeedCostScenarioByIndex(pix,inputCostScenario[pix]);
				this.economicDataTable.setSeedCostProbByIndex(pix,inputCostProb[pix]);
			}
		}
		
/*       private void randPriceCostGeneration(int pix){
//			double randomno = rnd.nextDouble();
			
			// generate crop price and plant cost according to the build scenario
			double sumProb = 0;
			for(int j=0; j<numOfScenario; j++){
			    sumProb += this.cropPriceProb[pix][j];
				if(sumProb >= randomno){
					cur_cropPrice = cropPriceScenario[pix][j];
					break;
				}
			}
			
			randomno = rnd.nextDouble();
			
			sumProb = 0;
			for(int j=0; j<numOfScenario; j++){
			    sumProb += this.seedCostProb[pix][j];
				if(sumProb >= randomno){
					cur_seedCost = seedCostScenario[pix][j];
					break;
				}
			}
			
       }
       
       private void randCornPrice(int pix){
    	   double randomno = rnd.nextDouble();
			
			// generate crop price and plant cost according to the build scenario
			double sumProb = 0;
			for(int j=0; j<numOfScenario; j++){
			    sumProb += this.cropPriceProb[pix][j];
				if(sumProb >= randomno){
					cur_cropPrice = cropPriceScenario[pix][j];
					break;
				}
			}
       }*/
		
}
