/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.farmer;

import org.utilities.time.SystemCalendar;

/*
 * this is the generalized abstract FarmerAgent class, each different type of farmer agent will extend this class 
 * and implement following functions. 
 */

public abstract class FarmerAgent {
	
	protected abstract void setCurrentPrecip(double value);
	
	protected abstract void setSubsidyRate(double value);
	
	protected abstract void setMoneyBalance(double value);
	
	protected abstract void setPrice_CostData(double cornPrice);  // dynamic changing each day
	
	protected abstract void setInitAreaDivision(double cropArea, double retentArea, double fallowArea);
	
	protected abstract void setLookAheadScenarios(double[] cropPriceScenario, double[] cropPriceProb, double seedCostPerAcre, double[] seedCostScenario, double[] seedCostProb); // set once every year
	
	protected abstract void setLookAheadPrecipScenarios(double[] yearlyPrecipProb, double[] growPrecipScenario); // set once every year

	protected abstract boolean getIsDeadStatus();

	protected abstract double getMoneyBalance();
	
	protected abstract double getRetentPercent();
	
	protected abstract double getCurrentCN();
	
	protected abstract double getRetentArea();
	
	protected abstract double getCropArea();
	
	protected abstract double getFallowArea();
	
	protected abstract void compute(SystemCalendar t);
	
	protected abstract void getData(SystemCalendar t);
	
	protected abstract void provide(SystemCalendar t);
	
	protected abstract void store(SystemCalendar t);
}
