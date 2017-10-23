/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.farmer;

// 50,000   150,000 minimum consumption

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.utilities.internal.DataBaseManager;
import org.utilities.internal.HSQLDataBaseManager;
import org.utilities.internal.mysqlDataBaseManager;
import org.utilities.time.SystemCalendar;
import org.apache.log4j.Logger;
import org.configuration.SimulationParameters;

/*
 * This is one specific farmer agent type. This class has the productive farmer's related features and decision algorithms.
 * In the platform, we implemented two different decision algorithm for farmer, namely: Two Stage Maximization and Yearly Maximization.
 */

public class ProductiveFarmer extends FarmerAgent{
	
	// logging
	private Logger logger;
	private String farmerConfigFile = System.getProperty("user.dir")+"/res/data/farmer/farmerThetaLQE.txt";

	// Simulation Parameters
	private SimulationParameters simParameters;
	
	// farmerIdentity_related variable
	private String locatedCity = null;
	private String farmerID = null;
	
	// farmerlifestatus
	private boolean isDead = false;
	
	// Action dates
//    private int[] setRetentArea_date = {1,15};
	private int[] decision_date = {3,15};
    private int[] plant_date = { 5, 1};
	private int[] harvest_date = {10, 15};
    private int[] financeDate = {11,1};
	private int[] state_update_date = { 12, 1};


    // historical data
    private List<Double> hist_subsidyRate = new ArrayList<Double>();
    private List<Double> hist_cropProfitPerAcre = new ArrayList<Double>();
    
    // database 
    private DataBaseManager farmerDataDB;
    
    // valueatrisk related variables
    private double[] var_retentPercent_dD = {0,0.2,0.4,0.6,0.8,1.0}; // of 10% of total land
    private double[] var_cropPercent_dD = {0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
    
    private double var_seedCostPerAcre = 0;
    private double var_max_RetentPercent = 0.25;
    private double var_farmerCornCons = 125; // # of bushels farmer family consumes
    private double var_D = 126;
    private double var_theta = 20000;
    private double farmerOffConstantBeta = 0.225;
    private double farmerOffConstantCropArea = 1600;
    private double var_retentCostPerAcre = 0;  // cost for put one acre of retent land.
    private double var_expectedCornConsCost = var_farmerCornCons*4.535;
    

	// weather and corn price scenarios
    private double[] la_MQ_Prob = null;   // yearly precip probability
    private double[] la_CP_Scenario = null;    // corn price scenario
    private double[] la_CP_Prob = null;   // corn price probability
    private double[] la_SC_Scenario = null;
    private double[] la_SC_Prob = null;
    private double[] la_GP_Scenario = null;
    private int la_MQ_num = 3;    // number of yearly precip scenarios
    private int la_CP_num = 3;    // number of corn price scenarios
    private int la_SC_num = 3;

    
    // Variables
	private double cur_sub_rate = 0;
	private double cropProfit_acre = 0;
    
	private int current_CropCN=0;
	private double current_CN=0;
	public int bare_soil_CN = 86;
	public int mature_crop_CN = 78;
	public int fallow_CN = 70;
	private int day_count=0;
	private boolean cropPlanted = false;
	
	private double total_land = 4000;
	private double retent_area = 0;
	private double crop_area = 0;
	private double fallow_area = 0;
	private double beta = 0;
	private double corn_price = 0;       // price/bushel
	private double crop_revenue=0;
	private double total_profits=0;
	private double money_balance = 4000000;
	private double CornConsumption = 0;
	private double money_consumpt = 0;
	private double utilityOfConsumption = 0;
	private double bushels_per_acre=0;
	private double grow_precip=0;        // grwoing season precip
	private double cur_step_precip=0;
	
	// solution node
	public class retentCropAreaNode{
		double retentPercent = 0;
		double cropArea = 0;
		double UOC = 0;
		retentCropAreaNode(double UOC,double retentPercent,double cropArea){
			this.UOC = UOC;
			this.retentPercent = retentPercent;
			this.cropArea = cropArea;
		}
	}
	
	// random generator
	Random rnd = null;
	
	
	/*
	 *  construction method
	 */
	public ProductiveFarmer(String farmerID, String locatedCity, SimulationParameters simParameters) {
		this.farmerID = farmerID;
		this.locatedCity = locatedCity;
		this.simParameters = simParameters;
		
		this.farmerDataDB = new mysqlDataBaseManager();

		this.logger = Logger.getLogger(ProductiveFarmer.class);
	    this.la_MQ_Prob = new double[la_MQ_num];   // yearly precip probability
	    this.la_CP_Scenario = new double[la_CP_num];    // corn price scenario
	    this.la_CP_Prob = new double[la_CP_num];   // corn price probability
	    this.la_SC_Scenario = new double[la_SC_num];
	    this.la_SC_Prob = new double[la_SC_num];
	    this.la_GP_Scenario = new double[la_MQ_num];
	    
		rnd = new Random();
    	rnd.setSeed(2L);
    	
/*    	BufferedReader reader;
		File infile;
		infile = new File(farmerConfigFile);
		try {
			reader = new BufferedReader(new FileReader(infile));
			var_theta = Double.parseDouble(reader.readLine());
			System.out.println("Farmer, var_f = "+var_theta);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		var_theta = simParameters.getTheta();
	}
	
	/*
	 * farmer's Two Stage decision making process.
	 */
	private void simpleRuleBased_decisionSelect(){
		
//		System.out.println("//////////////in farmer ruleMaking//////////////////////////////");
		
		double expectedCornPrice = 0;
		for(int i=0; i<this.la_CP_Scenario.length; i++){
			expectedCornPrice += la_CP_Scenario[i]*la_CP_Prob[i];
		}
//		System.out.println("expectedCornPrice ="+expectedCornPrice);

		double expectedHarvest = 0;
		for(int i=0; i<this.la_GP_Scenario.length; i++){
			expectedHarvest += harvest_crop(la_GP_Scenario[i])*la_MQ_Prob[i];
		}
		
//		System.out.println("expectedHarvest ="+expectedHarvest);

		
		double expectedCropProfitPerAcre = expectedCornPrice*expectedHarvest-this.var_seedCostPerAcre;
//		System.out.println("expectedCropProfitPerAcre ="+expectedCropProfitPerAcre);

		
		// case 1
		if(expectedCropProfitPerAcre>this.cur_sub_rate && this.cur_sub_rate>0){
			if(this.money_balance/this.var_seedCostPerAcre>=this.total_land){
				this.beta=0;
				this.crop_area = this.total_land;
			}else{
				// (M0+\tau*r*AF)/(IC*AF) + r = 1
				this.beta = Math.min(this.var_max_RetentPercent, (this.var_seedCostPerAcre*this.total_land-this.money_balance)/(this.cur_sub_rate*this.total_land+this.var_seedCostPerAcre*this.total_land));
			    this.crop_area = Math.floor((1-this.beta)*this.total_land);
			}
		}
		// case 2
		if(expectedCropProfitPerAcre>this.cur_sub_rate && this.cur_sub_rate==0){
			double randDouble = rnd.nextDouble();
			int rndIndex = (int)Math.floor(randDouble*2);
			if(rndIndex==0){
				this.crop_area = Math.min(Math.floor(this.money_balance/this.var_seedCostPerAcre),this.total_land);
				this.beta = Math.min(this.var_max_RetentPercent, (this.total_land-this.crop_area)/this.total_land);
			}else{
				this.crop_area = Math.min(Math.floor(this.money_balance/this.var_seedCostPerAcre),this.total_land);
                this.beta = 0;
			}
		}
		
		// case 5
		if(this.cur_sub_rate>=expectedCropProfitPerAcre && expectedCropProfitPerAcre>0){
			this.beta = this.var_max_RetentPercent;
			this.crop_area = Math.min(Math.floor((this.money_balance+this.cur_sub_rate*this.beta*this.total_land)/this.var_seedCostPerAcre), (1-beta)*this.total_land);
		}
		// case 3
		if(this.cur_sub_rate>0 && expectedCropProfitPerAcre<=0){
			this.beta = this.var_max_RetentPercent;
			this.crop_area = 0;
		}
		// case 4
		if(this.cur_sub_rate==0 && expectedCropProfitPerAcre<=0){
			double randDouble = rnd.nextDouble();
			int rndIndex = (int)Math.floor(randDouble*2);
			if(rndIndex==0){
				this.crop_area = 0;
				this.beta = this.var_max_RetentPercent;
			}else{
				this.crop_area = 0;
				this.beta = 0;
			}
		}
	}

	
	/*
	 * farmer's Yearly Maximization decision making process.
	 */
	private void farmr_singleYearMaximization_decisionMake(){
    	double et_futureBalance = 0;
    	ArrayList<retentCropAreaNode> soluSet = new ArrayList<retentCropAreaNode>();
    	
    	// loop over all possible combination of decisions, and calculate expected unility of consumption
		for(int i=0; i<this.var_retentPercent_dD.length; i++){
			for(int k=0; k<this.var_cropPercent_dD.length; k++){
				double et_retentPercent = this.var_retentPercent_dD[i]*this.var_max_RetentPercent;

				if(var_cropPercent_dD[k]+et_retentPercent<=1 ){ // if c+r<=1

				    double retent_rev = (this.cur_sub_rate-this.var_retentCostPerAcre)*et_retentPercent*this.total_land;
                    double cropArea_Affordable = Math.min(Math.floor((this.money_balance+retent_rev)/this.var_seedCostPerAcre),this.var_cropPercent_dD[k]*this.total_land);
              				
					double expected_UOC = 0;
					// consider all possible weather and corn price scenario that will happen in the future of current year. 
					for (int yc = 0; yc < this.la_MQ_num; yc++){   // precipitation
						for (int cc = 0; cc < this.la_CP_num; cc++){    // corn price			
								double bushels_PA = harvest_crop(this.la_GP_Scenario[yc]);
				                //et_cropArea = this.total_land*var_cropPercent_dD[k];
				                double profit = retent_rev + (this.la_CP_Scenario[cc]*bushels_PA-this.var_seedCostPerAcre)*cropArea_Affordable;
				                double ConsF = 0;
				                double subsistConsumption = this.la_CP_Scenario[cc]*var_farmerCornCons;
				                et_futureBalance = var_theta*var_expectedCornConsCost;

				                if(this.money_balance+profit-et_futureBalance >= subsistConsumption)
				                	ConsF = (this.money_balance+profit-et_futureBalance)/this.la_CP_Scenario[cc];
				                else if((this.money_balance+profit>= subsistConsumption) && (subsistConsumption > this.money_balance+profit-et_futureBalance))
				                	ConsF = var_farmerCornCons;
				                else
				                	ConsF =0;
				                
							    double UOC = Math.log(ConsF-this.var_farmerCornCons+this.var_D);
				               
							    expected_UOC = expected_UOC + UOC * this.la_CP_Prob[cc]*this.la_MQ_Prob[yc];
							}
					}
		//			System.out.println("sumProb = "+sumProb);
					soluSet.add(new retentCropAreaNode(expected_UOC,et_retentPercent,cropArea_Affordable));		
			
			     }// if <=1
		     }// for crop area loop
		}// for water retention percent loop
			
			// sort the soluSet from large to small
	        Comparator<retentCropAreaNode> comp = new Comparator<retentCropAreaNode>(){
				@Override
				public int compare(retentCropAreaNode s1, retentCropAreaNode s2){
					if(s2.UOC - s1.UOC ==0)
						return 0;
					else if(s2.UOC - s1.UOC >0)
						return 1;
					else
						return -1;
				}
	        };
	        
	        Collections.sort(soluSet, comp);
	
	        // choose the solu that has the maximum UOC
	        
	        // calculate the number of maximum UOC
	        int count = 1;
	        for(int i=0; i<soluSet.size()-1; i++){
	        	if(soluSet.get(i).UOC==soluSet.get(i+1).UOC){
	        		count++;
	        	}else
	        		break;
	        }
	        double rndNextDouble = rnd.nextDouble();
	        // old selection in difference
	       
	        if(count==1){ // only one maximum UOC
	        	this.beta = soluSet.get(0).retentPercent;
	        	this.crop_area = soluSet.get(0).cropArea;
	        }else{   // more than one maximum UOC, randomize the selection
		        int randIndex =(int)Math.floor(rndNextDouble*count);
//		        System.out.println("farmer RandNum = "+rndNextDouble);
	        	this.beta = soluSet.get(randIndex).retentPercent;
	        	this.crop_area = soluSet.get(randIndex).cropArea;
	        }
	}
	
	@Override
	protected void provide(SystemCalendar t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void getData(SystemCalendar t) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	protected void compute(SystemCalendar t){
		
		String timeIndex = t.CalendarToString();
        if(t.getMonth() ==1 && t.getDay()==1){
        	if(isDead){
        		this.fallow_area = this.total_land;
        		this.crop_area = 0;
        		this.retent_area = 0;
        		this.beta=0;
        		this.utilityOfConsumption = Math.log(this.var_D-this.var_farmerCornCons);
        		this.CornConsumption = 0;
        	}else{
	        	this.fallow_area = this.total_land-this.retent_area;
	        	this.crop_area = 0;
        	}
        }else if(isDead==false && t.getMonth()==decision_date[0] && t.getDay()==decision_date[1]){
        	logger.debug("Productive Farmer is Making decision");
        	calc_land_division();
        	this.current_CropCN = this.fallow_CN;
        	recordEvent(timeIndex);
        }else if(isDead==false && t.getMonth() == plant_date[0] && t.getDay() == plant_date[1]){
			logger.debug("Productive Farmer in planting date......");			
//			logger.debug("Productive Farmer in calculating water retention area..........");
//	       	System.out.println("===================Productive Farmer at ===="+t.CalendarToString());
			compute_Crop_state(t);  // plantCrop
			cropPlanted = true;
		}else if(isDead == false && t.getMonth() == harvest_date[0] && t.getDay() == harvest_date[1]){
			
			logger.debug("Productive Farmer in harvest date......");			
			calc_grow_precip();   // calculate grow precip of the day before harvest_date.
			compute_Crop_state(t);
			this.bushels_per_acre = harvest_crop(this.grow_precip);
			cropPlanted = false;
//			System.out.println("grow_precip = "+this.grow_precip);
			recordEvent(timeIndex);
	
		}else if(isDead==false && t.getMonth()==financeDate[0] && t.getDay()==financeDate[1]){
			calc_crop_revenue();
			calc_total_profit();
			recordEvent(timeIndex);
		}
		else if(t.getMonth() == state_update_date[0] && t.getDay() == state_update_date[1]){
			if(!isDead){
				logger.debug("Productive Farmer in updating info state......");			
				calc_year_consumption();
				calc_eoy_balance();
			
				recordEvent(timeIndex);
				
				// esential parameters set to 0
				this.bushels_per_acre = 0;
				this.cropProfit_acre = 0;
				this.total_profits = 0;
				this.grow_precip = 0;
			}else{
				recordEvent(timeIndex);
			}
		}
		else if(isDead==false && cropPlanted == true){
			calc_grow_precip();
			compute_Crop_state(t);			
		}
		
		// each time step, update land cover curve number
		this.current_CN = this.current_CropCN*this.crop_area/(this.crop_area+this.fallow_area) + this.fallow_CN*(this.fallow_area/(this.crop_area+this.fallow_area));
	}

    /**
     * Record a farmer decision event in the DB. All variables are read
     * from instance parameters, except for the time of the event
     * @param timeIndex time of the event
     */
    private void recordEvent(String timeIndex) {
    	try{
    		String insertCommand = "INSERT INTO farmerData_1997_2013 VALUES(NULL, '"
    				+this.farmerID +"','"+timeIndex +"',"+this.total_land+","
    				+this.retent_area+","+this.crop_area+","+this.fallow_area+","
    				+this.cur_sub_rate+","+this.corn_price+","+this.bushels_per_acre+','
    				+this.grow_precip+','+this.cropProfit_acre+","+this.var_seedCostPerAcre+","
    				+this.total_profits+","+this.money_balance+","+this.current_CropCN+","
    				+this.current_CN+","+this.CornConsumption+","+this.utilityOfConsumption+","
    				+this.simParameters.getSimulationID()+")";
			   farmerDataDB.insertDB(insertCommand);
    	}catch(Exception e){
    		logger.error("Productive Farmer insert event error: ",e);
    	}
    }
	
	@Override
	protected void store(SystemCalendar t) {
	
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.components.farmer.FarmerAgent#getCurrentCN()
	 * The following methods are for interaction between FarmerModel and individual FarmerAgent
	 */
	protected double getCurrentCN(){
		return this.current_CN;
	}
	
	@Override
	protected double getRetentPercent(){
		return this.beta;
	}
	
	@Override
	protected double getRetentArea(){
		return this.retent_area;
	}
	@Override
	protected double getCropArea(){
		return this.crop_area;
	}
	
	@Override
	protected double getFallowArea(){
		return this.fallow_area;
	}
	
	@Override
	protected void setCurrentPrecip(double value){
		cur_step_precip = value;
	}
	
	@Override
	protected void setSubsidyRate(double value){
		this.cur_sub_rate = value;
	}
	
	@Override
	protected void setMoneyBalance(double value){
		this.money_balance = value;
	}
	
	@Override
	protected void setPrice_CostData(double cornPrice){
		this.corn_price = cornPrice;
	}
	
	@Override
	protected void setInitAreaDivision(double cropArea, double retentArea, double fallowArea){
		this.crop_area = cropArea;
		this.retent_area = retentArea;
		this.fallow_area = fallowArea;

		this.current_CropCN = this.fallow_CN;
		this.current_CN = this.fallow_CN;
	}
	
	
	/*
	 * Following are methods called in Compute(), modeling farmer actions and crop reactions.
	 */
	public void compute_Crop_state(SystemCalendar t){
		
		String timeIndex = t.CalendarToString();		
		if(t.getMonth() == plant_date[0] && t.getDay() == plant_date[1]){
			current_CropCN = bare_soil_CN;
			day_count = 0;
		
			recordEvent(timeIndex);						
		}
		
		if(day_count == 10 && this.current_CropCN > this.mature_crop_CN){
			grow();
			day_count = 0;
					
			recordEvent(timeIndex);
		}
		
		if(t.getMonth() == harvest_date[0] && t.getDay() == harvest_date[1]){
			cut_plant();
			day_count = 0;
		}
		
		day_count++;
	}
	
	private void cut_plant(){
		current_CropCN = bare_soil_CN;
	}
	
	private void grow(){
		current_CropCN = current_CropCN - 1; 
	}
	
	private double harvest_crop(double grow_precip){

		double avg_precip = 26.72; // in
		int bushel_max = 168;     // bu/acre   // original=159
		int std = 5;              // standard deviation inches of precip

		return (bushel_max*0.8) + (0.2*bushel_max)*(Math.exp(-Math.pow((grow_precip - avg_precip), 2)/(Math.pow(std, 2))));
	}
	
	private void calc_grow_precip(){
		grow_precip += cur_step_precip;
	}

	private void calc_crop_revenue(){
		cropProfit_acre = bushels_per_acre * corn_price;
		crop_revenue = bushels_per_acre * corn_price * crop_area;
	}
	
	public void calc_land_division(){   // how to decide beta?
		
		
		simpleRuleBased_decisionSelect();
		
		retent_area = beta*total_land;
		this.fallow_area = Math.max(0, total_land - retent_area - crop_area);
		
/*	    var_retentPercent_decisionSelect();
		beta = farmerOffConstantBeta;
		crop_area = farmerOffConstantCropArea;
		
		double retent_rev = (this.cur_sub_rate-this.var_retentCostPerAcre)*beta*this.total_land;
        double cropArea_Affordable = (this.money_balance+retent_rev)/this.var_seedCostPerAcre;

	    this.crop_area = Math.min(cropArea_Affordable,this.farmerOffConstantCropArea);

		
		retent_area = beta*total_land;
		this.fallow_area = Math.max(0, total_land - retent_area - crop_area);*/
	
		/************************/
	}

	
	private void calc_total_profit(){
		total_profits = crop_revenue + (cur_sub_rate-this.var_retentCostPerAcre)*(this.retent_area) - this.var_seedCostPerAcre*this.crop_area;  // *this.crop_area;
	}
	
	private void calc_year_consumption(){
		double subtitute = var_farmerCornCons*this.corn_price;
        double futureBalance = var_theta*var_expectedCornConsCost;

		if(money_balance + total_profits - futureBalance >= subtitute)
	          this.CornConsumption = (money_balance + total_profits -futureBalance)/this.corn_price;
		else if(money_balance + total_profits >= subtitute && (money_balance + total_profits - futureBalance < subtitute))
			this.CornConsumption = var_farmerCornCons;
		else
			this.CornConsumption = 0;//Math.max(0,(money_balance + total_profits)/this.corn_price);
		money_consumpt = CornConsumption*this.corn_price;
		utilityOfConsumption = Math.log(this.CornConsumption-this.var_farmerCornCons+this.var_D);
		if(this.CornConsumption<var_farmerCornCons)
			isDead = true;
	}
	
	private void calc_eoy_balance(){
		if(CornConsumption <var_farmerCornCons)
			this.money_balance = 0;
		else
		    this.money_balance = money_balance + total_profits - money_consumpt;
	}

	@Override
	protected void setLookAheadScenarios(double[] cropPriceScenario,
			double[] cropPriceProb, double seedCostPerAcre, double[] seedCostScenario, double[] seedCostProb) {
		// TODO Auto-generated method stub
		for(int i=0; i<cropPriceScenario.length; i++){
			this.la_CP_Scenario[i] = cropPriceScenario[i];
			this.la_CP_Prob[i] = cropPriceProb[i];
		}
		for(int i=0; i<seedCostScenario.length; i++){
			this.la_SC_Scenario[i] = seedCostScenario[i];
			this.la_SC_Prob[i] = seedCostProb[i];
		}
		this.var_seedCostPerAcre = seedCostPerAcre;
	}

	@Override
	protected void setLookAheadPrecipScenarios(double[] yearlyPrecipProb, 
			double[] growPrecipScenario) {
		// TODO Auto-generated method stub
		for(int i=0; i<yearlyPrecipProb.length; i++){
			this.la_MQ_Prob[i] = yearlyPrecipProb[i];
			this.la_GP_Scenario[i] = growPrecipScenario[i];
		}
	}

	@Override
	protected double getMoneyBalance() {
		// TODO Auto-generated method stub
		return this.money_balance;
	}
	
	@Override
	protected boolean getIsDeadStatus(){
		return this.isDead;
	}

}
