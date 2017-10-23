/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.economic;

import org.simulation.model.unit.AbstractUnit;
import org.utilities.time.SystemCalendar;

public class EconomicUnit extends AbstractUnit{

	/**
	 * this is the class for each subbasin. 
	 */
	private static final long serialVersionUID = 6002301837367869758L;
 	private double cropPrice = 0;
 	private double plantCost = 0;
 	private double seedCost = 0;

 	
	public void computeUnit(SystemCalendar actTime) {

	}
	
	public void setCropPrice(double value){
		this.cropPrice = value;
	}
	
	public void setPlantCost(double value){
		this.plantCost = value;
	}
	
	public void setSeedCost(double value){
		this.seedCost = value;
	}
	
	public double getCropPrice(){
		return this.cropPrice;
	}
	
	public double getPlantCost(){
		return this.plantCost;
	}
	
	public double getSeedCost(){
		return this.seedCost;
	}

}
