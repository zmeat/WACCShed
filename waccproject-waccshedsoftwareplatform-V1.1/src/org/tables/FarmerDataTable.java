/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.tables;

import java.util.ArrayList;
import java.util.List;

public class FarmerDataTable extends DataTable{
	
	private String[] farmerID = null;
	private String[] locatedSubBasins = null;
	private double[] total_Area = null;
	private double[] retentPercent = null;
	private double[] retentArea = null;
	private double[] cropArea = null;
	private double[] fallowArea = null;
	private double[][] farmerCN = null;
	private double[][] farmerCN_weight = null;
	private int numOfFarmer = 0;
	private double[] money_balance = null;
	private boolean[] isDead = null;
	
	public FarmerDataTable(int numOfFarmer, String[] farmerID, String[] locatedSubBasins){
		this.numOfFarmer = numOfFarmer;
		this.farmerID = new String[numOfFarmer];
		this.locatedSubBasins = new String[numOfFarmer];
		for(int i=0; i<numOfFarmer; i++){
			this.farmerID[i] = farmerID[i];
			this.locatedSubBasins[i] = locatedSubBasins[i];
		}
		total_Area = new double[this.numOfFarmer];
		retentPercent = new double[this.numOfFarmer];
		retentArea = new double[this.numOfFarmer];
		cropArea = new double[this.numOfFarmer];
		fallowArea = new double[this.numOfFarmer];
		farmerCN = new double[this.numOfFarmer][2];
		farmerCN_weight = new double[this.numOfFarmer][2];
		money_balance = new double[this.numOfFarmer];
		isDead = new boolean[this.numOfFarmer];
	}
	
	private int findIndex(String ID){
		for(int i=0; i<numOfFarmer; i++){
			if(farmerID[i].equals(ID)){
				return i;
			}
		}
		return -1;
	}
	
	private List<Integer> findFarmerIndexOfSubBasin(String subBasinID){
		 List<Integer> farmerIndexTemp= new ArrayList<Integer>();
		for(int i=0; i<numOfFarmer; i++){
			if(locatedSubBasins[i].equals(subBasinID)){
				farmerIndexTemp.add(i);
			}
		}
		return farmerIndexTemp;
	}
	
	public boolean getIsDeadStatusByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID + "does not exist");
		return isDead[farmerIndex];
	}
	
	public double getMoneybalanceByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID + "does not exist");
		return money_balance[farmerIndex];
	}
	
	public double getTotalAreaByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID+" does not exist");
		return total_Area[farmerIndex];
	}
	
	public double getRetentPercentByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID+" does not exist");
		return retentPercent[farmerIndex];
	}
	
	public double getRetentAreaByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID+" does not exist");
		return retentArea[farmerIndex];
	}
	
	public double getCropAreaByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID+" does not exist");
		return cropArea[farmerIndex];
	}
	
	public double getFallowAreaByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID+" does not exist");
		return fallowArea[farmerIndex];
	}
	
	public double[] getCNByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID+" does not exist");
		return farmerCN[farmerIndex];
	}
	
	public double[] getCN_weightByFarmerID(String ID) throws Exception{
		int farmerIndex = findIndex(ID);
		if(farmerIndex==-1) throw new Exception(ID+" does not exist");
		return farmerCN_weight[farmerIndex];
	}
	
	public double[] getFarmerCNBySubBasinID(String subBasinID){
		List<Integer> farmerIndex = findFarmerIndexOfSubBasin(subBasinID);
		int farmerCNSize = farmerCN[0].length;
		double[] farmerCNTemp = new double[farmerIndex.size()*farmerCNSize];
		for(int i=0; i<farmerIndex.size(); i++){
			for(int j=0; j<farmerCN[farmerIndex.get(i)].length; j++)
			farmerCNTemp[i*farmerCNSize+j] = farmerCN[farmerIndex.get(i)][j];
		}
		return farmerCNTemp;
	}
	
	public double[] getFarmerCN_weightsBySubBasinID(String subBasinID){
		List<Integer> farmerIndex = findFarmerIndexOfSubBasin(subBasinID);
		int farmerCN_weightSize = farmerCN_weight[0].length;
		double[] farmerCN_weightTemp = new double[farmerIndex.size()*farmerCN_weightSize];
		for(int i=0; i<farmerIndex.size(); i++){
			for(int j=0; j<farmerCN_weight[farmerIndex.get(i)].length; j++)
			farmerCN_weightTemp[i*farmerCN_weightSize+j] = farmerCN_weight[farmerIndex.get(i)][j];
		}
		return farmerCN_weightTemp;
	}
	
	public void setIsDeadStatusByFarmerIndex(int farmerIndex, boolean value){
		if(this.isDead == null)
			isDead = new boolean[this.numOfFarmer];
		this.isDead[farmerIndex] = value;
	}
	
	public void setMoneyBalanceByFarmerIndex(int farmerIndex, double value){
		if(this.money_balance == null)
			money_balance = new double[this.numOfFarmer];
		this.money_balance[farmerIndex] = value;
	}
	
	public void setRetentPercentByFarmerIndex(int farmerIndex, double value){
		if(this.retentPercent == null)
			retentPercent = new double[this.numOfFarmer];
		this.retentPercent[farmerIndex] = value;
	}
	
	public void setTotalAreaByFarmerIndex(int farmerIndex, double value){
		this.total_Area[farmerIndex] = value;
	}
	
	public void setRetentAreaByFarmerIndex(int farmerIndex, double value){
		this.retentArea[farmerIndex] = value;
	}
	
	public void setCropAreaByFarmerIndex(int farmerIndex, double value){
		this.cropArea[farmerIndex] = value;
	}
	
	public void setFallowAreaByFarmerIndex(int farmerIndex, double value){
		this.fallowArea[farmerIndex] = value;
	}
	
	public void setCNByFarmerIndex(int farmerIndex, double[] value){
		for(int i=0; i<2; i++){
			this.farmerCN[farmerIndex][i] = value[i];
		}
	}
	
	public void setCN_weightByFarmerIndex(int farmerIndex, double[] value){
		for(int i=0; i<2; i++){
			this.farmerCN_weight[farmerIndex][i] = value[i];
		}
	}
	
	
}
