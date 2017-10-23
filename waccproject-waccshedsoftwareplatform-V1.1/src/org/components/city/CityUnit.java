/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */


package org.components.city;

import org.simulation.model.unit.AbstractUnit;
import org.utilities.time.SystemCalendar;

/* Just ignore this class for now. 
 * CityUnit is a class record the general information of the location located by the cityAgent.
 * This class hasn't been used because only consider one cityManager and this class will potentially 
 * be used when we consider different subbasins and each subbasin has one cityManager, etc. 
 */
public class CityUnit extends AbstractUnit{
	public void computeUnit(SystemCalendar actTime) {
	}
}
