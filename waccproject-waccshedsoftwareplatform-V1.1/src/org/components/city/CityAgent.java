/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.city;

import org.tables.EconomicDataTable;
import org.tables.FarmerDataTable;
import org.tables.HydrologyDataTable;
import org.tables.PrecipitationDataTable;
import org.utilities.time.SystemCalendar;


/*
 * this is the generalized abstract CityAgent class, each different type of city agent will extend this class 
 * and implement following functions. 
 */
public abstract class CityAgent {
	
	protected abstract void setDataTable(HydrologyDataTable hydrologyDataTable, FarmerDataTable farmerDataTable, PrecipitationDataTable precipDataTable, EconomicDataTable economicDataTable);

	protected abstract void setMaxDischargeOfYear(double value);
	
	protected abstract void setInitYearBudget(double value);
	
	protected abstract void setInitBudgetArrange(double subsidyPercentValue, double leveePercentValue,double subsidyRateValue);
	
	protected abstract double get_subsidy_rate();
	
	protected abstract void getData(SystemCalendar t);
	
	protected abstract void compute(SystemCalendar t);
	
	protected abstract void provide(SystemCalendar t);
	
	protected abstract void store(SystemCalendar t);
	
}
