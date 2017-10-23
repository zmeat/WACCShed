/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.tables;

import org.metadata.AreaMetaData;

public class PrecipitationDataTable extends DataTable{
	
	private double[][] precipitation=null;
	private double[][] dailyPrecip = null;
	private double[][] gageWeight=null;  // reference rainfallsim
	
	private double[] yearlyPrecip = null;
	private double[] growPrecip = null;
/*	private double[][] yearlyPrecipScenario = null;
	private double[][] yearlyPrecipProb = null;*/
	private double[][] maxDischargeScenario = null;
	private double[][] maxDischargeProb = null;
	private double[][] growPrecipScenario = null;
	
	private int numOfScenario = 3;
	
	private int numOfGage = 1;
	protected PrecipitationDataTable()
	{
		precipitation = new double[0][0];
		dailyPrecip = new double[0][0];
		gageWeight = new double[0][0];
		yearlyPrecip = new double[0];
		growPrecip = new double[0];
	}
	
    public PrecipitationDataTable( AreaMetaData amd )
	{
	   super( amd, PrecipitationDataTable.class );
	   precipitation = new double[amd.getNumOfsubBasin()][];
	   dailyPrecip = new double[amd.getNumOfsubBasin()][];
	   gageWeight = new double[amd.getNumOfsubBasin()][];
	   yearlyPrecip = new double[amd.getNumOfsubBasin()];
	   growPrecip = new double[amd.getNumOfsubBasin()];
	}
    
    
    public PrecipitationDataTable( AreaMetaData amd, Class dataType )
    {
      super( amd, dataType );
      precipitation = new double[amd.getNumOfsubBasin()][];
      dailyPrecip = new double[amd.getNumOfsubBasin()][];
      gageWeight = new double[amd.getNumOfsubBasin()][];
	   yearlyPrecip = new double[amd.getNumOfsubBasin()];
	   growPrecip = new double[amd.getNumOfsubBasin()];
    }
    
    public double getYearlyPrecipValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	return yearlyPrecip[idx];
    }
    
    public void setYearlyPrecipValue( String pid, double value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.yearlyPrecip == null){
    		this.yearlyPrecip = new double[amd.getNumOfsubBasin()];
    	}
 
      yearlyPrecip[idx] = value;
  
    }
    
    public double getGrowPrecipValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	return growPrecip[idx];
    }
    
    public void setGrowPrecipValue( String pid, double value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.growPrecip == null){
    		this.growPrecip = new double[amd.getNumOfsubBasin()];
    	}
 
      growPrecip[idx] = value;
  
    }
    
    public double[] getDailyPrecipValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	double[] allzero = new double[numOfGage];
    	for(int i=0; i<numOfGage; i++)
    	{
    		allzero[i]=0;
    	}

    	
      if( idx >= 0 && idx < dailyPrecip.length ) return dailyPrecip[ idx ];
      else return allzero;
    }
    
    public void setDailyPrecipValue( String pid, double[] value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.dailyPrecip[idx] == null){
    		this.dailyPrecip[idx] = new double[value.length];
    	}
 
      if( idx >= 0 && idx < dailyPrecip.length ) {
    	  for(int i=0; i<dailyPrecip.length; i++)
    		  dailyPrecip[ idx ][i] = value[i];
      }
  
    }
    
    public double[] getPrecipitationValue( String pid )
    {
       
    	int idx = amd.getArrayIndexByPID( pid );

    	double[] allzero = new double[numOfGage];
    	for(int i=0; i<numOfGage; i++)
    	{
    		allzero[i]=0;
    	}

    	
      if( idx >= 0 && idx < precipitation.length ) return precipitation[ idx ];
      else return allzero;
    }
    
    public double[] getGageWeightValue( String pid )
    {
       
    	int idx = amd.getArrayIndexByPID( pid );
       
    	double[] allzero = new double[numOfGage];
    	for(int i=0; i<numOfGage; i++)
    	{
    		allzero[i]=0;
    	}
       
      if( idx >= 0 && idx < gageWeight.length ) return gageWeight[ idx ];
      else return allzero;
    }
    

    /**
     * Optimized setter
     * @param pid PID
     */
    public void setPrecipitationValue( String pid, double[] value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.precipitation[idx] == null){
    		this.precipitation[idx] = new double[value.length];
    	}
 
      if( idx >= 0 && idx < precipitation.length ) {
    	  for(int i=0; i<precipitation.length; i++)
    		  precipitation[ idx ][i] = value[i];
      }
  
    }
    
    public void setGageWeightValue( String pid, double[] value )
    {
      int idx = amd.getArrayIndexByPID( pid );
      
      if(this.gageWeight[idx] == null){
  		this.gageWeight[idx] = new double[value.length];
  	}
      if( idx >= 0 && idx < gageWeight.length ) 
      {
    	  for(int i=0; i<gageWeight.length; i++)
    	      gageWeight[ idx ][i] = value[i];
      }
    }

    /**
     * Optimized getter
     * @param idx Direct index into array
     * @return Value
     */
    public double[] getPrecipitationValueByIndex( int idx )
    {
        
    	double[] allzero = new double[numOfGage];
    	for(int i=0; i<numOfGage; i++)
    	{
    		allzero[i]=0;
    	}
    	
       // for debug
       
      if( idx >= 0 && idx < precipitation.length ) return precipitation[ idx ];
      else return allzero;
    }
    
    public double[] getGageWeightByIndex( int idx )
    {
    	double[] allzero = new double[numOfGage];
    	for(int i=0; i<numOfGage; i++)
    	{
    		allzero[i]=0;
    	}
    	
       // for debug
       
       if( idx >= 0 && idx < gageWeight.length ) return gageWeight[ idx ];
       else return allzero;
    }

    /**
     * Optimized setter
     * @param idx Direct index into array
     */
    public void setPrecipitationValueByIndex( int idx, double[] value )
    {
    	  if(this.precipitation[idx] == null){
    	  		this.precipitation[idx] = new double[value.length];
    	  	}
    	 if( idx >= 0 && idx < precipitation.length )
    	 {
    		 for(int i=0; i<precipitation[idx].length; i++)
    			 precipitation[ idx ][i] = value[i];
    	 }
    }
    
    public void setGageWeightByIndex( int idx, double[] value )
    {
    	  if(this.gageWeight[idx] == null){
  	  		this.gageWeight[idx] = new double[value.length];
  	  	}
    	if( idx >= 0 && idx < gageWeight.length ) 
    	{
    		for(int i=0; i<gageWeight[idx].length; i++)
    		      gageWeight[ idx ][i] = value[i];
    	}
    }
	
/*    public double[] getYearlyPrecipScenarioValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	return yearlyPrecipScenario[idx];
    }
    
    public void setYearlyPrecipScenarioValue( String pid, double[] value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.yearlyPrecipScenario == null){
    		this.yearlyPrecipScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		yearlyPrecipScenario[idx][i] = value[i];
    	}
  
    }
    
    public void setYearlyPrecipScenarioValueByIndex( int idx, double[] value )
    {    
    	if(this.yearlyPrecipScenario == null){
    		this.yearlyPrecipScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		yearlyPrecipScenario[idx][i] = value[i];
    	}
  
    }
    
    public double[] getYearlyPrecipProbValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	return yearlyPrecipProb[idx];
    }
    
    public void setYearlyPrecipProbValue( String pid, double[] value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.yearlyPrecipProb == null){
    		this.yearlyPrecipProb = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		yearlyPrecipProb[idx][i] = value[i];
    	}
  
    }
    
    public void setYearlyPrecipProbValueByIndex( int idx, double[] value )
    {    
    	if(this.yearlyPrecipProb == null){
    		this.yearlyPrecipProb = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		yearlyPrecipProb[idx][i] = value[i];
    	}
  
    }
    */
    public double[] getMaxDischargeScenarioValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	return maxDischargeScenario[idx];
    }
    
    public void setMaxDischargeScenarioValue( String pid, double[] value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.maxDischargeScenario == null){
    		this.maxDischargeScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		maxDischargeScenario[idx][i] = value[i];
    	}
  
    }
    
    public void setMaxDischargeScenarioValueByIndex( int idx, double[] value )
    {    
    	if(this.maxDischargeScenario == null){
    		this.maxDischargeScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		maxDischargeScenario[idx][i] = value[i];
    	}
  
    }
    
    public double[] getMaxDischargeProbValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	return maxDischargeProb[idx];
    }
    
    public void setMaxDischargeProbValue( String pid, double[] value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.maxDischargeProb == null){
    		this.maxDischargeProb = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		maxDischargeProb[idx][i] = value[i];
    	}
  
    }
    
    public void setMaxDischargeProbValueByIndex( int idx, double[] value )
    {    
    	if(this.maxDischargeProb == null){
    		this.maxDischargeProb = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		maxDischargeProb[idx][i] = value[i];
    	}
    }
    
    public double[] getGrowPrecipScenarioValue(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );

    	return growPrecipScenario[idx];
    }
    
    public void setGrowPrecipScenarioValue( String pid, double[] value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
    	if(this.growPrecipScenario == null){
    		this.growPrecipScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		growPrecipScenario[idx][i] = value[i];
    	}
  
    }
    
    public void setGrowPrecipScenarioValueByIndex( int idx, double[] value )
    {    
    	if(this.growPrecipScenario == null){
    		this.growPrecipScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
    	}
        
    	for(int i=0; i<numOfScenario; i++){
    		growPrecipScenario[idx][i] = value[i];
    	}
    }
}
