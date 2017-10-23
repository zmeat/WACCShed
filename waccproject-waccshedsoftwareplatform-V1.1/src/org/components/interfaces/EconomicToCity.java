/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.interfaces;

import org.tables.EconomicDataTable;

public interface EconomicToCity extends CommunicationInterface{
	public EconomicDataTable getEconomicDataTable();
}
