/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.farmer;

import org.simulation.model.unit.AbstractUnit;
import org.utilities.time.SystemCalendar;

/* Just ignore this class for now. 
 * FarmerUnit is a class record the general information of the location located by the farmerAgent.
 * This class hasn't been used because only consider one farmer and this class will potentially 
 * be used when we consider different subbasins and each subbasin has one farmer, etc. 
 */

public class FarmerUnit extends AbstractUnit{
	public void computeUnit(SystemCalendar actTime) {

	}
}
