/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.tables;

import java.util.ArrayList;
import java.util.List;

import org.metadata.AreaMetaData;

public class HydrologyDataTable extends DataTable{
	
/*	private float[] discharge;
	private float[][] subBasinValue;*/
	
	private List<Double> discharge= new ArrayList<Double>(); // one file result for the whole model
	private List<List<Double>> subBasinDischarge=null;
	private double[] tlag=null;
    private double[] tdelta=null;
    private double[] I=null;
    private double[] P=null;
    private int dischargeVectorSize = 29;
    private double maxYearlyDischarge = 0;
	
	
	
	protected HydrologyDataTable()
	{
		subBasinDischarge = new ArrayList<List<Double>>();
		subBasinDischarge.add(new ArrayList<Double>());
		tlag = new double[0];
		tdelta = new double[0];
		I = new double[0];
		P = new double[0];
	}
	
    public HydrologyDataTable( AreaMetaData amd )
	{
	   super( amd, HydrologyDataTable.class );
	   subBasinDischarge = new ArrayList<List<Double>>();
	   for(int i=0; i<amd.getNumOfsubBasin(); i++){
		   subBasinDischarge.add(new ArrayList<Double>());
	   }
//	   discharge = new double[dischargeVectorSize];
		tlag = new double[amd.getNumOfsubBasin()];
		tdelta = new double[amd.getNumOfsubBasin()];
		I = new double[amd.getNumOfsubBasin()];
		P = new double[amd.getNumOfsubBasin()];
	}
    
    
    public HydrologyDataTable( AreaMetaData amd, Class dataType )
    {
      super( amd, dataType );
	   subBasinDischarge = new ArrayList<List<Double>>();
	   for(int i=0; i<amd.getNumOfsubBasin(); i++){
		   subBasinDischarge.add(new ArrayList<Double>());
	   }
 //     discharge = new double[dischargeVectorSize];
		tlag = new double[amd.getNumOfsubBasin()];
		tdelta = new double[amd.getNumOfsubBasin()];
		I = new double[amd.getNumOfsubBasin()];
		P = new double[amd.getNumOfsubBasin()];      

    }
    
    public List<Double> getSubBasinDischarge( String pid )
    {
    	  int idx = amd.getArrayIndexByPID( pid );
    	       
      if( idx >= 0 && idx < subBasinDischarge.size() ) return subBasinDischarge.get(idx);
      else{
    	List<Double> allzero = new ArrayList<Double>();
      	for(int i=0; i<dischargeVectorSize; i++)
      	{
      		allzero.add(Double.valueOf(0));
      	}
    	  return allzero;
      }
    }
    

    
    public List<Double> getDischarge()
    {
    	if(this.discharge == null || this.discharge.size()==0){
    		List<Double> allzero = new ArrayList<Double>();
        	for(int i=0; i<dischargeVectorSize; i++)
        	{
        		allzero.add(Double.valueOf(0));
        	}
        	return allzero;
    	}
    	else
            return this.discharge;
    }
    
    public double getTlag(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );
       return this.tlag[idx];
    }
    
    public double getTdelta(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );
       return this.tdelta[idx];
    }
    
    public double getI(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );
        return this.I[idx];
    }
    
    public double getP(String pid)
    {
    	int idx = amd.getArrayIndexByPID( pid );
       return this.P[idx];
    }
    
    public double getMaxYearlyDischarge(){
    	return this.maxYearlyDischarge;
    }
    
    public void setMaxYearlyDischarge(double value){
    	this.maxYearlyDischarge = value;
    }
    
    
    /**
     * Optimized setter
     * @param pid PID
     */
    public void setSubBasinDischarge( String pid, List<Double> value )
    {
      int idx = amd.getArrayIndexByPID( pid );
//      if(subBasinDischarge[idx] == null){
//      }
      if( idx >= 0 && idx < subBasinDischarge.size() ) {
    	  subBasinDischarge.get(idx).clear();
    	  this.subBasinDischarge.get(idx).addAll(value);
      }
    	
    }
    
    public void setDischarge(List<Double> discharge)
    {
    	if(this.discharge==null){
           this.discharge = new ArrayList<Double>();
    	}else{
    		this.discharge.clear();
    	}
    	this.discharge.addAll(discharge);
    }
    
    public void setTlag(String pid,double[] tlag)
    {
        if(this.tlag == null){
        	this.tlag = new double[tlag.length];
        }
        int idx = amd.getArrayIndexByPID( pid );

        this.tlag[idx] = tlag[idx];
    	
    }
    
    public void setTdelta(String pid,double[] tdelta)
    {
        if(this.tdelta == null){
        	this.tdelta = new double[tdelta.length];
        }
        int idx = amd.getArrayIndexByPID( pid );
   
      	this.tdelta[idx] = tdelta[idx];
    }
    
    public void setI(String pid, double[] I)
    {
    	  if(this.I == null){
          	this.I = new double[I.length];
          }
         
    	  int idx = amd.getArrayIndexByPID( pid );
           this.I[idx] = I[idx];
    }
    
    public void setP(String pid, double[] P)
    {
    	
  	  if(this.P == null){
        	this.P = new double[P.length];
        }
       

	  int idx = amd.getArrayIndexByPID( pid );
       this.P[idx] = P[idx];
    	
    }
    

    /**
     * Optimized getter
     * @param idx Direct index into array
     * @return Value
     */
    public List<Double> getSubBasinDischargeByIndex( int idx )
    {
      return subBasinDischarge.get(idx);
    }
    
    public double getTlagByIndex( int idx )
    {
      return tlag[ idx ];
    }

    public double getTdelatByIndex( int idx )
    {
      return tdelta[ idx ];
    }
    
    public double getIByIndex( int idx )
    {
      return I[ idx ];
    }
    
    public double getPByIndex( int idx )
    {
      return P[ idx ];
    }
    /**
     * Optimized setter
     * @param idx Direct index into array
     */
    public void setSubBasinDischargeByIndex( int idx, List<Double> value )
    {
    	subBasinDischarge.remove(idx);
    	subBasinDischarge.add(idx, new ArrayList<Double>());
           this.subBasinDischarge.get(idx).addAll(value);
    }
	
    public void setTlagByIndex( int idx , double value)
    {
       tlag[ idx ]= value;
    }

    public void setTdelatByIndex( int idx ,double value )
    {
       tdelta[ idx ] = value;
    }
    
    public void setIByIndex( int idx ,double value )
    {
       I[ idx ] = value;
    }
    
    public void setPByIndex( int idx , double value )
    {
       P[ idx ] = value;
    }

}
