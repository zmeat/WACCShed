/*
 * Author: David Dziubanski, Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.components.hydrology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.simulation.model.unit.AbstractUnit;
import org.utilities.time.SystemCalendar;

public class HydrologyUnit extends AbstractUnit{
	
	/**
	 * 
	 */
	
	private static final long serialVersionUID = -2677234660945074048L;
	
	// precipitation datas
	private double[] precip = null;	
	private double[] precip_gageWeight = null;
	private int numOfGage = 1;
	
	//landuse parameters
	private Double[] CNs=null;
	private Double[] CN_weights=null;

//	private static int numOfLandType = 3;
	
	/// other parameters
    private RunOff rf;
    private SCSUH uh;
    private double Area;   // can defined in the base data
    public List<Double> output = null; // discharge output for subbasin
//    private static int preciVectorSize = 29;
    
    //from hydrologyDataTable
    private double tlag;
    private double tdelta;
    private double I;
    private double P;
	
	public void initUnit(){
		rf = new RunOff();
//		System.out.println("tlag = "+this.tlag + "tdelta = "+this.tdelta + "this.area = "+this.area());
	    uh = new SCSUH(this.tlag, this.tdelta, this.area());
	}
	
	public void computeUnit(SystemCalendar actTime) {
		this.calc_UH_vars();

		double precipTemp = 0;
		for(int i=0; i<numOfGage; i++){
			precipTemp += precip[i]*precip_gageWeight[i];
		}
//		precipTemp = 1.2;

		// All runoff computations
    	rf.PercentImp(this.I, this.P, precipTemp);
     	rf.weighted_CN(this.CNs,this.CN_weights);

    	rf.SCS_CN_runoff(precipTemp);
    	rf.runoff_tot();
    	uh.UHtimeseries(rf.get_Tot_runoff());
    	
    	output = uh.calcHyd();
//    	System.out.println(actTime.CalendarToString()+" precip:"+precip[0]);

	}
	
	/// method to be call in subbasin
    public void calc_UH_vars(){
    	uh.time_peak();
    	uh.UH_peak();
    	uh.find_t();
    	uh.find_Q();
    	uh.function();
    }
	
	public List<Double> getSubBasinDischarge()
	{ 
       return output;
	}
	
	public void setSubBasinDischarge(List<Double> subBasinDischarge)
	{
		if(output == null){
			output = new ArrayList<Double>();
		}
		for(Double value : subBasinDischarge)
			output.add(value);
	}
	
	// set atmosphere parameters
	public void setPrecipitation(double[] precipitation){
		if(this.precip == null)
			precip = new double[precipitation.length];
		for(int i=0; i<precipitation.length; i++){
			this.precip[i] = precipitation[i];
		}
	}
	
	public void setGageWeight(double[] gageWeight){
		if(this.precip_gageWeight == null){
			this.precip_gageWeight = new double[gageWeight.length];
		}
		for(int i=0; i<gageWeight.length; i++)
		    this.precip_gageWeight[i] = gageWeight[i];
	}
	
	//set landuse parameters
	public void setLanduse(double[] CNs, double[] CN_weights){
		if(this.CNs == null){
			this.CNs = new Double[CNs.length];
		}
		if(this.CN_weights == null){
			this.CN_weights = new Double[CN_weights.length];
		}
		int size = CNs.length;
		for(int i=0; i<size; i++){
			this.CNs[i] = CNs[i];
		    this.CN_weights[i] = CN_weights[i];
		}
		
	}
	
	public void setTlag(double tlag)
	{
		this.tlag = tlag;
	}
	
	public void setTdelta(double tdelta)
	{
		this.tdelta = tdelta;
	}
	
	public void setI(double I)
	{
		this.I = I;
	}
	
	public void setP(double P)
	{
		this.P = P;
	}
	
	
	public double getTlag()
	{
		return this.tlag;
	}
	
	public double getTdelta()
	{
		return this.tdelta;
	}
	
	public double getI()
	{
		return this.I ;
	}
	
	public double getP()
	{
		return this.P ;
	}
	
	public Double[] getCNs(){
		return this.CNs;
	}
	
	public Double[] getCN_weights(){
		return this.CN_weights;
	}

}
