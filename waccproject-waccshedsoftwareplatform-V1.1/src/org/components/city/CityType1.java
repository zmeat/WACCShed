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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.tables.EconomicDataTable;
import org.tables.FarmerDataTable;
import org.tables.HydrologyDataTable;
import org.tables.PrecipitationDataTable;
import org.utilities.internal.DataBaseManager;
import org.utilities.internal.HSQLDataBaseManager;
import org.utilities.internal.mysqlDataBaseManager;
import org.utilities.time.SystemCalendar;
import org.apache.log4j.Logger;
import org.components.farmer.ProductiveFarmer.retentCropAreaNode;
import org.configuration.SimulationParameters;

/*
 * This is one specific city agent type. This class has the city agent type 1's specific features and decision algorithms. 
 */

public class CityType1 extends CityAgent{
	
	// logging
	private Logger logger;
	private String cityResFolder = System.getProperty("user.dir")+"/res/data/city/";
	private String cityConfigFile = System.getProperty("user.dir")+"/res/data/city/cityThetaLQE.txt";

	// file to CM off parameters
	private BufferedReader offDecisionReader;
	private File offDecisionFile;
	
	// Simulation Parameters
	private SimulationParameters simParameters;
	
	//city identity data
	protected String cityID = null;
	protected String locatedSubBasin = null;
	protected String[] farmerID = null;
	
	protected double[] retentAreaOfFarmers = null;
	protected double[] totalAreaOfFarmers = null;
	protected int numOfFarmer = 0;
	protected boolean[] isDead = null;

	//database 
    private DataBaseManager cityDataDB;
    
    //datatable
	private HydrologyDataTable c1_hydrologyDataTable = null;
	private FarmerDataTable c1_farmerDataTable = null;
	private PrecipitationDataTable c1_precipDataTable = null;
	private EconomicDataTable c1_ecoDataTable = null;
	
	
	// related variables of farmer
	private double phi = 1;
	
    private double var_maxRetentPercent = 0.25;
    private double var_farmerCornCons=125;  // farmer yearly live cost
    private double var_seedCostPerAcre = 0;   
    private double var_D = 126;
    private double var_f = 20000;  // parameter control leftover money of farmer.
    private double LQE = 51.48;
    private int var_offDecisionIndex = 0;

    private double var_retentCostPerAcre = 0;  // cost for put one acre of retent land.
    private double var_expectedCornCons = var_farmerCornCons*4.535;

    // cityManager decision Domain
    private double[] la_Sub_dD = {0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1}; // subsidy percent decision domain
    private double[] la_lev_dD = {0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1}; // levee percent decision domain
    
	// weather and corn price scenarios
    private double[] la_MQ_Prob = null;   // maximum discharge probability 
    private double[] la_CP_Scenario = null;    // corn price scenario
    private double[] la_CP_Prob = null;   // corn price probability
    private double[] la_SC_Scenario = null; // seedCost scenario
    private double[] la_SC_Prob = null; // seedCost probability
    private double[] la_GP_Scenario = null; // grow precip scenario
    private int la_MQ_num = 3;    // number of maximum discharge scenarios
    private int la_CP_num = 3;    // number of corn price scenarios
	private int la_SC_num  =3;    // number of seed cost scenarios
	
	// other city related Variables
    private double social_benefit = 0; //$ 
	private double subsidy_percent = 0;
	private double levInv_percent = 0;
	private double SocialServiceInvest = 0;
	private double max_Q = 0;
	private double lev_qual = 3.00;
	private double lev_invest=0;
	private final double g = 1E-5;
	private final double lev_depr_rate = 0.02;
	private double FDam=0;
	private double budget=0;
	private double cur_sub_rate=0;            // subsidy/acre
	private int FD_max = 100*1000000;           //  Maximum flood damage ($)

	
	// Action dates
	private int[] makeDecision_date = {2,1};
	private int[] budgetArrange_date = {3,16};
	private int[] floodDM_date = {10, 10};
	
	// random number generator
	Random rnd1 = null;
//	Random rnd2 = null;
	
	// cityManager decision node
	class sdlevInvestNode{
		double socialW = 0;
		double subsidyPercent = 0;
		double leveeInvPercent = 0;
		sdlevInvestNode(double socialW, double subsidyPercent, double leveeInvPercent){
			this.socialW = socialW;
			this.subsidyPercent = subsidyPercent;
			this.leveeInvPercent = leveeInvPercent;
		}
	}
	
	
	// farmer solution node
	
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

	/*
	 * Construction Method
	 */
	public CityType1(String cityName, String subBasinName, int numOfFarmer, String[] farmerID, SimulationParameters simParameters) {
		
		logger = Logger.getLogger(CityType1.class);
		
		this.cityID = cityName;
		this.locatedSubBasin = subBasinName;
		this.numOfFarmer = numOfFarmer;
		if(this.farmerID == null){
			this.farmerID = new String[numOfFarmer];
		}
		if(this.totalAreaOfFarmers == null){
			this.totalAreaOfFarmers = new double[numOfFarmer];
		}
		if(this.isDead == null){
			this.isDead = new boolean[numOfFarmer];
		}
		if(this.retentAreaOfFarmers==null){
			this.retentAreaOfFarmers = new double[numOfFarmer];
		}
		
		for(int i=0; i<numOfFarmer; i++){
			this.farmerID[i] = farmerID[i];
		}
		
		this.simParameters = simParameters;
		
		this.cityDataDB = new mysqlDataBaseManager();
	
		// init random generator
		rnd1 = new Random();
		rnd1.setSeed(1L);
        
		// new scenario arrays
	    la_MQ_Prob = new double[la_MQ_num];   // yearly precip probability 
	    la_CP_Scenario = new double[la_CP_num];    // corn price scenario
	    la_CP_Prob = new double[la_CP_num];   // corn price probability
	    la_GP_Scenario = new double[la_MQ_num];
	    la_SC_Scenario = new double[la_SC_num];
	    la_SC_Prob = new double[la_SC_num];

	    // when CM is on, uncomment below block, when CM is OFF, comment below block
/*	    BufferedReader reader;
		File infile;
		infile = new File(cityConfigFile);
		try {
			reader = new BufferedReader(new FileReader(infile));
			var_f = Double.parseDouble(reader.readLine());
			LQE = Double.parseDouble(reader.readLine());
			reader.readLine(); // read the third line which only will be useful when CM-OFF
			System.out.println("City Manager: var_f = "+var_f);
			System.out.println("City Manager: LQE = "+LQE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    var_f = simParameters.getTheta();
		LQE = simParameters.getLQE();
	    
		
	// when CM is off, uncomment below block, when CM is ON, comment below block
/*	    BufferedReader reader;
		File infile;
		infile = new File(cityConfigFile);
		try {
			reader = new BufferedReader(new FileReader(infile));
			var_f = Double.parseDouble(reader.readLine());
			LQE = Double.parseDouble(reader.readLine());
			var_offDecisionIndex = Integer.parseInt(reader.readLine());
			System.out.println("City Manager: var_f = "+var_f);
			System.out.println("City Manager: LQE = "+LQE);
			System.out.println("City Manager: offDecisionIndex = "+var_offDecisionIndex);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// open the file to the off decisions
		// when farmer is ON (yearly maximization), uncomment below
		//offDecisionFile = new File(cityResFolder+"farmerON/"+"city1OffDecisions"+var_offDecisionIndex+".txt");
		// when farmer is OFF (two stage maximization), uncomment below
		offDecisionFile = new File(cityResFolder+"farmerOFF/"+"city1OffDecisions"+var_offDecisionIndex+".txt");
		
		try {
			offDecisionReader = new BufferedReader(new FileReader(offDecisionFile));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	
	/*
	 * CityManager's decisions will be made in this method
	 * @see org.components.city.CityAgent#compute(org.utilities.time.SystemCalendar)
	 */
    @Override
	protected void compute(SystemCalendar t) {	
    	
    	String timeIndex = t.CalendarToString();
    	
        if(t.getMonth() == makeDecision_date[0] && t.getDay() == makeDecision_date[1])
        {
        	logger.debug("CityType1 is making budget distribution decision.");
        	
            set_ThisYear_budget();
            set_subsidy_Rate_Levee_Percent();
            recordEvent(timeIndex);
        }
        else if(t.getMonth() == budgetArrange_date[0] && t.getDay() == budgetArrange_date[1]) {
        	logger.debug("CityType1 is calculating levee quality.");
        	
        	budgetArrangement();
        	this.lev_qual = maintain_levee(this.lev_qual,this.lev_invest);
        	recordEvent(timeIndex);
        	
        }
        else if(t.getMonth() == floodDM_date[0] && t.getDay() == floodDM_date[1]) {
        	logger.debug("CityType1 is calculating floodDamage.");

        	this.FDam = calc_flood_damages(this.lev_qual,this.max_Q);
        	this.social_benefit = calc_social_benefit(FDam,this.SocialServiceInvest);
			
			recordEvent(timeIndex);
        }
	}
    
    /**
     * Record a city decision event in the DB. All variables are read
     * from instance parameters, except for the time of the event
     * @param timeIndex time of the event
     */
    private void recordEvent(String timeIndex) {

    	try{
    		String insertCommand = "INSERT INTO cityData_1997_2013 VALUES(NULL, '"
    				+ this.cityID +"','"+timeIndex +"',"+this.budget + ","+this.lev_invest+","
    				+ this.cur_sub_rate+","+this.lev_qual+","+this.FDam+","+this.SocialServiceInvest+","
    				+ this.social_benefit+","+this.max_Q+","
    				+ simParameters.getSimulationID()+")";
    		cityDataDB.insertDB(insertCommand);
    	}catch(Exception e){
    		logger.error("CityType1 insert event error: ",e);
    	}
    }
	
    /*
     * this function will be called by cityModel.java. to pass other DataTables to cityManager.
     * @see org.components.city.CityAgent#setDataTable(org.tables.HydrologyDataTable, org.tables.FarmerDataTable, org.tables.PrecipitationDataTable, org.tables.EconomicDataTable)
     */
    @Override
	protected void setDataTable(HydrologyDataTable hydrologyDataTable, FarmerDataTable farmerDataTable, PrecipitationDataTable precipDataTable, EconomicDataTable ecoDataTable){
    	this.c1_hydrologyDataTable = hydrologyDataTable;
    	this.c1_farmerDataTable = farmerDataTable;
    	this.c1_precipDataTable = precipDataTable;
    	this.c1_ecoDataTable = ecoDataTable;
    }
    
    /*
     * Each time step, city Manager will update external data from other agents through this method.
     * @see org.components.city.CityAgent#getData(org.utilities.time.SystemCalendar)
     */
   @Override
    protected void getData(SystemCalendar t) {
	   
	    if(t.getMonth() == makeDecision_date[0] && t.getDay() == makeDecision_date[1]){
	    	if(this.totalAreaOfFarmers == null){
				this.totalAreaOfFarmers = new double[this.numOfFarmer];
			}
	    	
	    	for(int j=0; j<numOfFarmer; j++)
			{
				try {
					totalAreaOfFarmers[j] = c1_farmerDataTable.getTotalAreaByFarmerID(this.farmerID[j]);
					retentAreaOfFarmers[j] = c1_farmerDataTable.getRetentAreaByFarmerID(this.farmerID[j]);
					isDead[j] = c1_farmerDataTable.getIsDeadStatusByFarmerID(this.farmerID[j]);
					for(int ns = 0; ns < this.la_CP_num; ns++){
					this.la_CP_Scenario[ns] = this.c1_ecoDataTable.getCropPriceScenario(this.locatedSubBasin)[ns];
					this.la_CP_Prob[ns] = this.c1_ecoDataTable.getCropPriceProb(this.locatedSubBasin)[ns];
                    this.var_seedCostPerAcre = this.c1_ecoDataTable.getCurrentSeedCost(this.locatedSubBasin);
					this.la_MQ_Prob[ns] = this.c1_precipDataTable.getMaxDischargeProbValue(this.locatedSubBasin)[ns];
					this.la_GP_Scenario[ns] = this.c1_precipDataTable.getGrowPrecipScenarioValue(this.locatedSubBasin)[ns];
					// we don't need input cost scenario, it is realized at the beginning of the year.
					this.la_SC_Scenario[ns] = this.c1_ecoDataTable.getSeedCostScenario(this.locatedSubBasin)[ns];
					this.la_SC_Prob[ns] = this.c1_ecoDataTable.getSeedCostProb(this.locatedSubBasin)[ns];
					}
					
					// for debug
//					System.out.println("retentArea = "+retentAreaOfFarmers[j]+" FallowArea = "+c1_farmerDataTable.getFallowAreaByFarmerID(this.farmerID[j]));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	    }
	    
		if(t.getMonth() == budgetArrange_date[0] && t.getDay() == budgetArrange_date[1]){

				if(this.retentAreaOfFarmers==null){
					this.retentAreaOfFarmers = new double[this.numOfFarmer];
				}
				
				for(int j=0; j<numOfFarmer; j++)
				{
					try {
						retentAreaOfFarmers[j] = c1_farmerDataTable.getRetentAreaByFarmerID(this.farmerID[j]);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
		}else if(t.getMonth() == floodDM_date[0] && t.getDay()==floodDM_date[1]){

			this.setMaxDischargeOfYear(c1_hydrologyDataTable.getMaxYearlyDischarge());
		}
   }
	
   
    @Override
	protected void provide(SystemCalendar t) {
       
	}
	
    @Override
	protected void store(SystemCalendar t) {

	}
	
    /*
     * analysis the best decisions for city manager
     */
    private void la_decisionSelection(){
    	ArrayList<sdlevInvestNode> soluSet = new ArrayList<sdlevInvestNode>();
    	double cityRandDouble = rnd1.nextDouble();
    	if(isDead[0]==false){
	        for(int i=0; i<this.la_Sub_dD.length; i++){   // number of decisions for selecting subsidy percent
	        	// for each subsidy rate, estimate retentCropArea farmer would choose
	        	double et_sdRate = this.la_Sub_dD[i]*this.budget/(totalAreaOfFarmers[0]*this.var_maxRetentPercent);  // calculate estimated subsidy rate
	    		List<retentCropAreaNode> retentCropArea = simpleRuleBased_decisionSelect(et_sdRate); // estimate farmer's selection on water retention area given subsidy rate
								
	        	for(int j=0; j<this.la_lev_dD.length; j++){  // number of decisions for selecting levee invest percent
	        		if(la_Sub_dD[i]+la_lev_dD[j]<=1){
		        		double final_expected_sw = 0;       			
		        		for(int fi=0; fi<retentCropArea.size(); fi++){
		        			    double expected_sw=0;
					    		double et_bestRetentPercent = retentCropArea.get(fi).retentPercent;
								double et_bestCropArea = retentCropArea.get(fi).cropArea;
								double et_subsidyCost = et_bestRetentPercent*totalAreaOfFarmers[0]*et_sdRate;
			        			for (int yc = 0; yc < this.la_MQ_num; yc++){
			        		        		double et_cropPercent = et_bestCropArea/totalAreaOfFarmers[0];
			        				        double et_MQ = this.la_DEst(yc, et_bestRetentPercent,et_cropPercent); // given weather condition, for each water retention area and crop percentage, find the true maximum discharge. 
			        		        		double et_leQ = this.maintain_levee(this.lev_qual, this.budget*this.la_lev_dD[j]);
			        		        		double et_FD = this.calc_flood_damages(et_leQ, et_MQ);
			        		        		double et_SW = this.calc_social_benefit(et_FD, this.budget*(1-la_lev_dD[j])-et_subsidyCost);
			        		        		expected_sw = expected_sw + et_SW * this.la_MQ_Prob[yc];
			        			}
				        		final_expected_sw +=expected_sw*1/retentCropArea.size();
		        		}
		        		soluSet.add(new sdlevInvestNode(final_expected_sw,la_Sub_dD[i],la_lev_dD[j]));
	        	    }
				}
	        }
    	}else{
    		for(int j=0; j<this.la_lev_dD.length; j++){  // number of decisions for selecting levee invest percent
        			double expected_sw = 0;
        			for (int yc = 0; yc < this.la_MQ_num; yc++){
        		        		double et_cropPercent = 0;
        				        double et_MQ = this.la_DEst(yc, retentAreaOfFarmers[0]/totalAreaOfFarmers[0],et_cropPercent); // given weather condition, for each water retention area and crop percentage, find the true maximum discharge. 
        		        		double et_leQ = this.maintain_levee(this.lev_qual, this.budget*this.la_lev_dD[j]);
        		        		double et_FD = this.calc_flood_damages(et_leQ, et_MQ);
        		        		double et_SW = this.calc_social_benefit(et_FD, this.budget*(1-la_lev_dD[j]));
        		        		expected_sw = expected_sw + et_SW * this.la_MQ_Prob[yc];
        			}
        			soluSet.add(new sdlevInvestNode(expected_sw,0,la_lev_dD[j]));
        		
        	}
    	}
        
        // sort the soluSet from large to small
        Comparator<sdlevInvestNode> comp = new Comparator<sdlevInvestNode>(){
			@Override
			public int compare(sdlevInvestNode s1, sdlevInvestNode s2){
				 if(s2.socialW - s1.socialW==0)
					 return 0;
				 else if(s2.socialW - s1.socialW>0)
					 return 1;
				 else
					 return -1;
			}
        };
        
        Collections.sort(soluSet, comp);
        
        // count the set of node of maximum social welfare
        int count = 1;
        for(int i=0; i<soluSet.size()-1; i++){
        	if(soluSet.get(i).socialW==soluSet.get(i+1).socialW){
        		count++;
        	}else
        		break;
        }
        
        if(count==1){
        	this.subsidy_percent = soluSet.get(0).subsidyPercent;
        	this.levInv_percent = soluSet.get(0).leveeInvPercent;
        }else{
        	int randIndex = (int)Math.floor(cityRandDouble*count);
        	this.subsidy_percent = soluSet.get(randIndex).subsidyPercent;
        	this.levInv_percent = soluSet.get(randIndex).leveeInvPercent;
        }
    }
    
    
    /*
     * Farmer's two stage optimization algorithm
     */
	private List<retentCropAreaNode> simpleRuleBased_decisionSelect(double et_sdRate){
		
//		System.out.println("//////////////in city manager ruleMaking//////////////////////////////");

		double moneyBalanceFarmer = 0;
		try {
			moneyBalanceFarmer = this.c1_farmerDataTable.getMoneybalanceByFarmerID(this.farmerID[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<retentCropAreaNode> soluNode = new ArrayList<retentCropAreaNode>();
		double expectedCornPrice = 0;
		for(int i=0; i<this.la_CP_Scenario.length; i++){
			expectedCornPrice += la_CP_Scenario[i]*la_CP_Prob[i];
		}
//		System.out.println("expectedCornPrice ="+expectedCornPrice);
		double expectedHarvest = 0;
		for(int i=0; i<this.la_GP_Scenario.length; i++){
			expectedHarvest += var_harvestCrop(la_GP_Scenario[i])*la_MQ_Prob[i];
		}
//		System.out.println("expectedHarvest ="+expectedHarvest);

		double expectedCropProfitPerAcre = expectedCornPrice*expectedHarvest-this.var_seedCostPerAcre;
		
//		System.out.println("expectedCropProfitPerAcre ="+expectedCropProfitPerAcre);

		// case 1
		if(expectedCropProfitPerAcre>et_sdRate && et_sdRate>0){
			if(moneyBalanceFarmer/var_seedCostPerAcre>= this.totalAreaOfFarmers[0]){
			    soluNode.add(new retentCropAreaNode(0,0,this.totalAreaOfFarmers[0]));
			}else{
				double tmpBeta = Math.min(this.var_maxRetentPercent, (var_seedCostPerAcre*this.totalAreaOfFarmers[0]-moneyBalanceFarmer)/(et_sdRate*this.totalAreaOfFarmers[0]+var_seedCostPerAcre*this.totalAreaOfFarmers[0]));
				double tmpCropArea = Math.floor((1-tmpBeta)*this.totalAreaOfFarmers[0]);
				soluNode.add(new retentCropAreaNode(0,tmpBeta,tmpCropArea));
			}
		}
		// case 2
		if(expectedCropProfitPerAcre>et_sdRate && et_sdRate==0){
			double tmpCropArea = Math.min(Math.floor(moneyBalanceFarmer/this.var_seedCostPerAcre),this.totalAreaOfFarmers[0]);
			soluNode.add(new retentCropAreaNode(0,Math.min(this.var_maxRetentPercent, (this.totalAreaOfFarmers[0]-tmpCropArea)/this.totalAreaOfFarmers[0]),tmpCropArea));
			soluNode.add(new retentCropAreaNode(0,0,tmpCropArea));
		}
		
		// case 5
		if(et_sdRate>=expectedCropProfitPerAcre && expectedCropProfitPerAcre>0){
			soluNode.add(new retentCropAreaNode(0,this.var_maxRetentPercent,Math.min(Math.floor((moneyBalanceFarmer+et_sdRate*this.var_maxRetentPercent*this.totalAreaOfFarmers[0])/var_seedCostPerAcre),(1-this.var_maxRetentPercent)*this.totalAreaOfFarmers[0])));
		}
		// case 3
		if(et_sdRate>0 && expectedCropProfitPerAcre<0){
			soluNode.add(new retentCropAreaNode(0,this.var_maxRetentPercent,0));
		}
		// case 4
		if(et_sdRate==0 && expectedCropProfitPerAcre<=0){
			soluNode.add(new retentCropAreaNode(0,this.var_maxRetentPercent,0));
			soluNode.add(new retentCropAreaNode(0,0,0));
		}
		
		return soluNode;
	}
 
	
    /*
     * farmer's single yearly maximization decision process
     */
    private ArrayList<retentCropAreaNode> singleYearMax_DecisionMakeFarmer(double sd_rate, double seed_cost){
    	double[] et_retentDomain = {0,0.2,0.4,0.6,0.8,1};
    	double[] et_cropDomain = {0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};

    	double et_futureBalance = 0;
    	
		double et_totalArea = totalAreaOfFarmers[0];
		double moneyBalanceFarmer=0;
		
    	ArrayList<retentCropAreaNode> soluSet = new ArrayList<retentCropAreaNode>();
		
		try {
			moneyBalanceFarmer = this.c1_farmerDataTable.getMoneybalanceByFarmerID(this.farmerID[0]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// loop over each possible decisions, and calculate the expected utility of consumption
		for(int i=0; i<et_retentDomain.length; i++){
	        for(int j=0; j<et_cropDomain.length; j++){
				double et_retentPrecent = et_retentDomain[i]*this.var_maxRetentPercent;

			    if(et_retentPrecent+et_cropDomain[j]<=1){  // sum of decision percentage should be less or equal than 1

					double et_retentRev = (sd_rate-var_retentCostPerAcre)*et_retentPrecent*et_totalArea;
					double cropArea_af = 0;   // farmer's affordable crop area. 
					cropArea_af = Math.min(Math.floor((moneyBalanceFarmer+et_retentRev)/seed_cost),et_cropDomain[j]*et_totalArea);

					double expected_UOC = 0;
					
					for (int yc = 0; yc < this.la_MQ_num; yc++){
        				for (int cc = 0; cc < this.la_CP_num; cc++){
							double bushels_per_acre = var_harvestCrop(this.la_GP_Scenario[yc]);						        
						        	
						        
						        double profit = et_retentRev + (this.la_CP_Scenario[cc]*bushels_per_acre-seed_cost)*cropArea_af;
				                double ConsF = 0;
				                double subsistence = this.la_CP_Scenario[cc]*var_farmerCornCons;
				                // the money leftover to next year is constant as below, var_f set as 1000 will guarantee a large portion of money left over.
				                et_futureBalance = var_f*var_expectedCornCons;
				                
				                // the rest of the money will be consumed. 
				                if(moneyBalanceFarmer+profit-et_futureBalance >= subsistence)
				                	ConsF = (moneyBalanceFarmer+profit-et_futureBalance)/this.la_CP_Scenario[cc];
				                else if((moneyBalanceFarmer+profit>= subsistence) && (subsistence > moneyBalanceFarmer+profit-et_futureBalance))
				                	ConsF = var_farmerCornCons;
				                else
				                	ConsF = 0;
				                
				                // function to calculate utility of consumption given the consumption of corns. 
							    double UOC = Math.log(ConsF-this.var_farmerCornCons+var_D);
				               
							    expected_UOC = expected_UOC + UOC * this.la_CP_Prob[cc]*this.la_MQ_Prob[yc];
        				}
					}
					soluSet.add(new retentCropAreaNode(expected_UOC,et_retentPrecent,cropArea_af));		
				}
			}
		}
		
		// sort the soluSet from large to small
        Comparator<retentCropAreaNode> comp = new Comparator<retentCropAreaNode>(){
			@Override
			public int compare(retentCropAreaNode s1, retentCropAreaNode s2){
				if(s2.UOC-s1.UOC == 0)
					return 0;
				else if(s2.UOC - s1.UOC >0)
					return 1;
				else
					return -1;
			}
        };
        Collections.sort(soluSet, comp);
    
        // record the maximum solutions into optiSoluSet and return it.
        ArrayList<retentCropAreaNode> optSoluSet  = new ArrayList<retentCropAreaNode>();
    	optSoluSet.add(new retentCropAreaNode(soluSet.get(0).UOC,soluSet.get(0).retentPercent,soluSet.get(0).cropArea));
        for(int i=1; i<soluSet.size(); i++){
        	if(soluSet.get(i).UOC==soluSet.get(i-1).UOC){  // multiple maximum solutions
        		
            	optSoluSet.add(new retentCropAreaNode(soluSet.get(i).UOC,soluSet.get(i).retentPercent,soluSet.get(i).cropArea));
        	}else
        		break;
        }
        return optSoluSet;
    }
    
	private double var_harvestCrop(double growPrecip){
		double avg_precip = 26.72; // in
		int bushel_max = 168;     // bu/acre   // original=159
		int std = 5;              // standard deviation inches of precip
		return (bushel_max*0.8) + (0.2*bushel_max)*(Math.exp(-Math.pow((growPrecip - avg_precip), 2)/(Math.pow(std, 2))));
	}
    /*
     * the table of weather condition, retention and crop area to maximum discharge map. 
     * low 07, normal 03, high 09, the low discharge year, normal discharge year, high discharge year. 
     */
    private double la_DEst(int flag, double retent_percent, double crop_percent){
       	if(flag == 0){ //99
     	   double[][] MQ = {{174.55,184.136,194.08,204.38,215.7,237.05,259.94,284.51,310.99,339.38,369.8}, // 0
     			   {141.68,149.98,158.62,167.59,177.25,195.79,215.7,237.05,259.94,284.51},  //0.125
     			   {113.4,120.51,127.93,135.66,144.05,160.03,177.25,195.79,215.7,237.05},
     			   {89.46,95.44,101.71,108.28,115.62,129.26,144.05,160.03,177.25},
     			   {69.64,74.53,79.71,85.17,91.59,103.08,115.62,129.26,144.05},
     			   {53.82,57.65,61.77,66.16,71.69,81.13,91.59,103.08}};  //0.25
     	   return MQ[(int)(retent_percent/0.25*5)][(int)(crop_percent/0.1)];
        }else if(flag == 1){ //07
     	   double[][] MQ = {{206.80,225.75,245.8,267.0,289.41,313.05,337.98,364.24,391.91,421.07,451.8},
     			   {169.87,186.47,204.18,222.97,242.87,263.9,286.13,309.6,334.33,360.4},
     			   {137.6,152.12,167.58,184.04,201.59,220.22,239.95,260.82,282.88,306.17},
     			   {109.74,122.22,135.63,149.99,165.31,181.62,199.0,217.5,237.07},
     			   {86.08,96.6,108.03,120.38,133.66,147.88,163.06,179.23,196.46},
     			   {66.53,75.14,84.65,95.05,106.34,118.56,131.7,145.79}};
 		   return MQ[(int)(retent_percent/0.25*5)][(int)(crop_percent/0.1)];
        }else if(flag == 2){ //05
     	   double[][] MQ = {{543.4,562.93,582.84,603.152,623.86,644.97,666.49,688.41,710.76,733.52,756.72},
     			   {473.59,491.68,510.15,529.0,548.24,567.87,587.88,608.29,629.1,650.31},
     			   {409.14,425.81,442.86,460.28,478.08,496.26,514.82,533.78,553.11,572.84},
     			   {349.78,365.14,380.84,396.87,413.27,430.04,447.18,464.69,482.59},
     			   {295.16,309.27,323.71,338.48,353.59,369.03,384.81,400.94,417.43},
     			   {245.2,258.07,271.27,284.8,298.66,312.85,327.37,342.23}};
 		   return MQ[(int)(retent_percent/0.25*5)][(int)(crop_percent/0.1)];
        }
        return 0;
    }
    
	private double maintain_levee(double pre_lev_qual, double levInvest){
		return (1-lev_depr_rate)*pre_lev_qual + g*levInvest; // g here is a very low percentage. 
	}
	
    private void budgetArrangement() {
		double totalRetentArea = 0;
		for(int i=0; i<retentAreaOfFarmers.length; i++){
			totalRetentArea+=retentAreaOfFarmers[i];
		}
		this.SocialServiceInvest = this.budget-this.cur_sub_rate*totalRetentArea-this.lev_invest;
	}
	
	private double calc_flood_damages(double lQ,double mQ){
		double Q1_nolev = 369.8;             //  Flow at which damage is 1% of maximum when there are no levees (cfs)
		double Q99_nolev = 756.72;            //  Flow at which damage is 99% of maximum when there are no levees (cfs)
		double s01 = LQE;            //  Slope of relationship between 1%-max-damage flow and levee quality (cfs/quality)
		double s99 = LQE;            //  Slope of relationship between 99%-max-damage flow and levee quality (cfs/quality)

		double Q1_lev = Q1_nolev + s01*lQ;        //  Flow at which damage is 1% of maximum for given levee quality
		double Q99_lev = Q99_nolev + s99*lQ;        //  Flow at which damage is 99% of maximum for given levee quality

		double Q50_lev = (Q1_lev + Q99_lev)/2;        //  Flow at which damage is 50% of maximum for given levee quality
		double dQ = (Q99_lev - Q1_lev)/9.2;     //  Width of transition of flood damage curve

		return FD_max/(1 + Math.exp(-(mQ - Q50_lev)/dQ));
	}
	
	private double calc_social_benefit(double fd, double ssI){
		return ssI+phi*(FD_max-fd);
	}


	private void set_subsidy_Rate_Levee_Percent(){
		// when CM is ON, uncomment below. 
		if (simParameters.isCM_ON()) {
			la_decisionSelection();
		}
		
		// city manager not learning scenario
//		subsidy_percent = 0.000005;
//		cur_sub_rate = 7.5;
//		levInv_percent = 0.0125;
		// city manager not learning scenario
		
		double totalFarmerArea = 0;
		for(int i=0; i<totalAreaOfFarmers.length; i++){
			totalFarmerArea+=totalAreaOfFarmers[i];
		}
		this.cur_sub_rate  = (this.budget*this.subsidy_percent)/(totalFarmerArea*this.var_maxRetentPercent);
		this.lev_invest = this.levInv_percent*this.budget;
		
		
		// when CM is OFF, uncomment below, comment above		
		if (!simParameters.isCM_ON()) {
//			try {
//				String[] offDecisionPara = (offDecisionReader.readLine()).split("	");
//				this.lev_invest = Double.parseDouble(offDecisionPara[0]);
//				cur_sub_rate = Double.parseDouble(offDecisionPara[1]);

				levInv_percent = 0.0125;
				cur_sub_rate = 7.5;
				
				//			System.out.println("lev_invest = " + this.lev_invest);
				//			System.out.println("cur_sub_rate = " + this.cur_sub_rate);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
	}
	
	private void set_ThisYear_budget(){
		this.budget = 1000000;
	}
	
	@Override
	protected void setInitYearBudget(double value){
		this.budget = value;
	}
	
	@Override
	protected void setInitBudgetArrange(double subsidyPercentValue, double leveePercentValue,double subsidyRateValue){
		this.subsidy_percent = subsidyPercentValue;
		this.levInv_percent = leveePercentValue;
		this.cur_sub_rate = subsidyRateValue;
	}
	
	@Override
	public double get_subsidy_rate(){
		return cur_sub_rate;
	}
	
	@Override
	public void setMaxDischargeOfYear(double value){
		this.max_Q = value;
	}
}
