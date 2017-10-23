/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.interfaces;

import org.tables.HydrologyDataTable;

public interface HydrologyToCity extends CommunicationInterface{
	
	public HydrologyDataTable getHydrologyDataTable();
}
