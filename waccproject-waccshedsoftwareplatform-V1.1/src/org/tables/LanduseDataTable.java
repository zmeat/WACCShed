/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.tables;


import org.metadata.AreaMetaData;

public class LanduseDataTable extends DataTable{

	
	private double[][] cns;   ///landuse types 
	private double[][] cn_weight;   ///landuse types 
	private int numOfLanduseTypes = 3;
	
	protected LanduseDataTable()
	{
		cns = new double[0][0];
		cn_weight = new double[0][0];
	}
	
    public LanduseDataTable( AreaMetaData amd )
	{
	   super( amd, LanduseDataTable.class );
	   cns = new double[amd.getNumOfsubBasin()][numOfLanduseTypes];
	   cn_weight = new double[amd.getNumOfsubBasin()][numOfLanduseTypes];
	}
    
    
    public LanduseDataTable( AreaMetaData amd, Class dataType )
    {
      super( amd, dataType );
      cns = new double[amd.getNumOfsubBasin()][numOfLanduseTypes];
      cn_weight = new double[amd.getNumOfsubBasin()][numOfLanduseTypes];

    }
 
    
    public double[] getCNsValue( String pid )
    {
       
    	int idx = amd.getArrayIndexByPID( pid );
       
    	double[] allzero = new double[numOfLanduseTypes];
    	for(int i=0; i<numOfLanduseTypes; i++)
    	{
    		allzero[i]=0;
    	}
    	
       // for debug
       
      if( idx >= 0 && idx < cns.length ) return cns[ idx ];
      else return allzero;
    }
    
    public double[] getCN_WeightValue( String pid )
    {
       
    	int idx = amd.getArrayIndexByPID( pid );
       
    	double[] allzero = new double[numOfLanduseTypes];
    	for(int i=0; i<numOfLanduseTypes; i++)
    	{
    		allzero[i]=0;
    	}
    	
       // for debug
       
      if( idx >= 0 && idx < amd.getNumOfsubBasin() ) return this.cn_weight[ idx ];
      else return allzero;
    }

    /**
     * Optimized setter
     * @param pid PID
     */
    public void setCNsValue( String pid, double[] value )
    {
      int idx = amd.getArrayIndexByPID( pid );
      if( idx >= 0 && idx < cns.length ) {
    	  for(int i=0; i<value.length;i++)
    		  cns[ idx ][i] = value[i];
      }
    }
    
    
    public void setCN_WeightValue( String pid, double[] value )
    {
      int idx = amd.getArrayIndexByPID( pid );
      if( idx >= 0 && idx < cn_weight.length ){
    	  for(int i=0; i<value.length;i++)
    	  cn_weight[ idx ][i] = value[i];
      }
    }
    /**
     * Optimized getter
     * @param idx Direct index into array
     * @return Value
     */
    public double[] getCNsValueByIndex( int idx )
    {
        
    	double[] allzero = new double[numOfLanduseTypes];
    	for(int i=0; i<numOfLanduseTypes; i++)
    	{
    		allzero[i]=0;
    	}
    	
       // for debug
       
      if( idx >= 0 && idx < cns.length ) return cns[ idx ];
      else return allzero;
    }
    

    public double[] getCN_WeightValueByIndex( int idx )
    {
        
    	double[] allzero = new double[numOfLanduseTypes];
    	for(int i=0; i<numOfLanduseTypes; i++)
    	{
    		allzero[i]=0;
    	}
    	
       // for debug
       
      if( idx >= 0 && idx < cn_weight.length ) return cn_weight[ idx ];
      else return allzero;
    }
    /**
     * Optimized setter
     * @param idx Direct index into array
     */
    public void setCNsValueByIndex( int idx, double[] value )
    {
    	 if( idx >= 0 && idx < cns.length ) 
         {
    		 for(int i=0; i<value.length;i++)
    			 cns[ idx ][i] = value[i];
         }
    }
    
    public void setCN_WeightValueByIndex( int idx, double[] value )
    {
    	 if( idx >= 0 && idx < cn_weight.length ) 
    	 {
    		 for(int i=0; i<value.length;i++)
        	   cn_weight[ idx ][i] = value[i];
         }
    }
    


}
    
