/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.atmosphere;

import org.simulation.model.unit.AbstractUnit;
import org.utilities.time.SystemCalendar;


/**
 * A class that controls the Atmosphere operation within one unit (subbasin) of the system.
 */
public class AtmosphereUnit extends AbstractUnit{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6002301837367869758L;


 	private double[] precipitation = null;
 	private double[] gageWeight = null;
 	private double yearlyPrecip = 0;
// 	private int numOfGage = 1;
	
	public void computeUnit(SystemCalendar actTime) {

	}
	
	public void setYearlyPrecip(double val){
		this.yearlyPrecip = val;
	}
	
	public double getYearlyPrecip(){
		return this.yearlyPrecip;
	}
	
	public void setPrecipitation(double[] precipitation){
		if(this.precipitation == null)
			this.precipitation = new double[precipitation.length];
		for(int i=0; i<precipitation.length; i++)
		    this.precipitation[i] = precipitation[i];
	}
	
	public double[] getPrecipitation(){
		return precipitation;
	}
	
	public void setGageWeight(double[] gageWeight){
		if(this.gageWeight == null)
			this.gageWeight = new double[gageWeight.length];
		for(int i=0; i<gageWeight.length; i++)
		    this.gageWeight[i] = gageWeight[i];
	}
	
	public double[] getGageWeight(){
		return gageWeight;
	}

}
