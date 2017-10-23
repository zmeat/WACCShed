/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.tables;

import org.metadata.AreaMetaData;

public class EconomicDataTable extends DataTable{
	
	private double[] cropPrice = null;
//	private double[] plantCost = null;
	private double[] seedCost = null;
	private int numOfSubBasin = 0;
	
	private double[][] cropPriceScenario = null;
	private double[][] cropPriceProb = null;
	private double[][] seedCostScenario = null;
	private double[][] seedCostProb = null;
	private int numOfScenario = 3;
	
	protected EconomicDataTable()
	{
		cropPrice = new double[0];
		seedCost = new double[0];
		cropPriceScenario = new double[0][0];
		seedCostScenario = new double[0][0];
		cropPriceProb = new double[0][0];
		seedCostProb = new double[0][0];
	}
	
    public EconomicDataTable( AreaMetaData amd )
	{
	   super( amd, EconomicDataTable.class );
	   cropPrice = new double[amd.getNumOfsubBasin()];
	   seedCost = new double[amd.getNumOfsubBasin()];
	   numOfSubBasin = amd.getNumOfsubBasin();
	   cropPriceScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
	   seedCostScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
	   cropPriceProb = new double[amd.getNumOfsubBasin()][numOfScenario];
	   seedCostProb = new double[amd.getNumOfsubBasin()][numOfScenario];
	}
    
    
    public EconomicDataTable( AreaMetaData amd, Class dataType )
    {
      super( amd, dataType );
	   cropPrice = new double[amd.getNumOfsubBasin()];
	   seedCost = new double[amd.getNumOfsubBasin()];
	   numOfSubBasin = amd.getNumOfsubBasin();
	   cropPriceScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
	   seedCostScenario = new double[amd.getNumOfsubBasin()][numOfScenario];
	   cropPriceProb = new double[amd.getNumOfsubBasin()][numOfScenario];
	   seedCostProb = new double[amd.getNumOfsubBasin()][numOfScenario];
    }
 
    // should add mechanism for idx > numOfSubBasin
    public double getCurrentCropPrice(String pid){
    	int idx = amd.getArrayIndexByPID( pid );
    	if( idx >= 0 && idx < numOfSubBasin ) 
    		return cropPrice[ idx ];
    	else
    		return 0;
    }
    

    public double getCurrentSeedCost(String pid){
    	int idx = amd.getArrayIndexByPID( pid );
    	if( idx >= 0 && idx < numOfSubBasin ) 
    		return seedCost[ idx ];
    	else
    		return 0;
    }
    
    public double[] getCropPriceScenario(String pid){
    	int idx = amd.getArrayIndexByPID( pid );
    	if( idx >= 0 && idx < numOfSubBasin ) 
    		return cropPriceScenario[ idx ];
    	else
    		return null;
    }
    
    public double[] getCropPriceProb(String pid){
    	int idx = amd.getArrayIndexByPID( pid );
    	if( idx >= 0 && idx < numOfSubBasin ) 
    		return cropPriceProb[ idx ];
    	else
    		return null;
    }
    
    public double[] getSeedCostScenario(String pid){
    	int idx = amd.getArrayIndexByPID( pid );
    	if( idx >= 0 && idx < numOfSubBasin ) 
    		return seedCostScenario[ idx ];
    	else
    		return null;
    }
    
    public double[] getSeedCostProb(String pid){
    	int idx = amd.getArrayIndexByPID( pid );
    	if( idx >= 0 && idx < numOfSubBasin ) 
    		return seedCostProb[ idx ];
    	else
    		return null;
    }
    
    public void setCurrentCropPrice( String pid, double value )
    {    
    	int idx = amd.getArrayIndexByPID( pid );
 
      if( idx >= 0 && idx < this.numOfSubBasin ) {
    		  cropPrice[ idx ] = value;
      }
    }
    
    
    public void setCurrentSeedCost(String pid, double value){
    	int idx = amd.getArrayIndexByPID(pid);
    	
        if( idx >= 0 && idx < this.numOfSubBasin ) {
  		  seedCost[ idx ] = value;
    }
    }
    
    public void setCurrentCropPriceByIndex( int idx, double value ){
    	 if( idx >= 0 && idx < this.numOfSubBasin )
    	 {
    		 this.cropPrice[idx] = value;
    	 }
    }
    
    public void setCurrentSeedCostByIndex( int idx, double value ){
      	 if( idx >= 0 && idx < this.numOfSubBasin )
      	 {
      		 this.seedCost[idx] = value;
      	 }
      } 
    
    public void setCropPriceScenarioByIndex( int idx, double[] value ){
   	 if( idx >= 0 && idx < this.numOfSubBasin )
   	 {
   		 for(int i=0; i<value.length; i++)
   		     this.cropPriceScenario[idx][i] = value[i];
   	 }
   }
   
   public void setCropPriceProbByIndex( int idx, double[] value ){
  	 if( idx >= 0 && idx < this.numOfSubBasin )
  	 {
  		for(int i=0; i<value.length; i++)
  		     this.cropPriceProb[idx][i] = value[i];
  	 }
   }
    
	   
	    public void setCropPriceScenario( String pid, double[] value ){
	    	int idx = amd.getArrayIndexByPID( pid );
	      	 if( idx >= 0 && idx < this.numOfSubBasin )
	      	 {
	      		 for(int i=0; i<value.length; i++)
	      		     this.cropPriceScenario[idx][i] = value[i];
	      	 }
	      }
	      
	      public void setCropPriceProb( String pid, double[] value ){
	    	  int idx = amd.getArrayIndexByPID( pid );
	     	 if( idx >= 0 && idx < this.numOfSubBasin )
	     	 {
	     		for(int i=0; i<value.length; i++)
	     		     this.cropPriceProb[idx][i] = value[i];
	     	 }
	      }
	       
	       public void setSeedCostScenario( String pid, double[] value ){
	    	   int idx = amd.getArrayIndexByPID( pid );
	   	   	 if( idx >= 0 && idx < this.numOfSubBasin )
	   	   	 {
	   	   		 for(int i=0; i<value.length; i++)
	   	   		     this.seedCostScenario[idx][i] = value[i];
	   	   	 }
	   	   }
	   	   
	   	   public void setSeedCostProb( String pid, double[] value ){
	   		int idx = amd.getArrayIndexByPID( pid );
	   	  	 if( idx >= 0 && idx < this.numOfSubBasin )
	   	  	 {
	   	  		for(int i=0; i<value.length; i++)
	   	  		     this.seedCostProb[idx][i] = value[i];
	   	  	 }
	   	  }
	   	   
	   	public void setSeedCostScenarioByIndex( int idx, double[] value ){
		   	 if( idx >= 0 && idx < this.numOfSubBasin )
		   	 {
		   		 for(int i=0; i<value.length; i++)
		   		     this.seedCostScenario[idx][i] = value[i];
		   	 }
		   }
		   
		   public void setSeedCostProbByIndex( int idx, double[] value ){
		  	 if( idx >= 0 && idx < this.numOfSubBasin )
		  	 {
		  		for(int i=0; i<value.length; i++)
		  		     this.seedCostProb[idx][i] = value[i];
		  	 }
		  }
    
}
