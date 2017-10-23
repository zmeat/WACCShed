/*
 * Author: Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */

package org.tables;

import java.util.ArrayList;
import java.util.List;

public class CityDataTable extends DataTable{

	private String[] cityIDs = null;
	private String[] locatedSubBasins = null;
	private double[] subsidyRateForFarmer = null;
	private int numOfCity = 0;
	private int[] farmerPerCity = null;
	
	private double[] cityCN = null;
	private double[] cityCN_weight = null;
	
	public CityDataTable(int numOfCity, int[] farmerPerCity, String[] cityIDs, String[] locatedSubBasins){
		this.numOfCity = numOfCity;
		this.farmerPerCity = new int [numOfCity];
		this.cityIDs = new String [numOfCity];
		this.locatedSubBasins = new String [numOfCity];
		this.subsidyRateForFarmer = new double[numOfCity];
		this.cityCN = new double[numOfCity];
		this.cityCN_weight = new double[numOfCity];
		for(int i=0; i<farmerPerCity.length; i++){
			this.farmerPerCity[i] = farmerPerCity[i];
			this.cityIDs[i] = cityIDs[i];
			this.locatedSubBasins[i] = locatedSubBasins[i];
		}
	}
	
	private int findIndex(String ID){
		for(int i=0; i<numOfCity; i++){
			if(cityIDs[i].equals(ID)){
				return i;
			}
		}
		return -1;
	}
	
	private List<Integer> findCityIndexOfSubBasin(String subBasinID){
		 List<Integer> cityIndexTemp= new ArrayList<Integer>();
		for(int i=0; i<numOfCity; i++){
			if(locatedSubBasins[i].equals(subBasinID)){
				cityIndexTemp.add(i);
			}
		}
		return cityIndexTemp;
	}
	
	public double getSubsidyRateForFarmerByCityID(String cityID) throws Exception{
		int cityIndex = findIndex(cityID);
		if(cityIndex==-1) throw new Exception(cityID +" does not exist");
		return this.subsidyRateForFarmer[cityIndex];
	}
	
	public double getCNByCityID(String cityID) throws Exception{
		int cityIndex = findIndex(cityID);
		if(cityIndex==-1) throw new Exception(cityID+"does not exist");
		return this.cityCN[cityIndex];
	}
	
	public double getCN_weightByCityID(String cityID) throws Exception{
		int cityIndex = findIndex(cityID);
		if(cityIndex==-1) throw new Exception(cityID+"does not exist");
		return this.cityCN_weight[cityIndex];
	}
	
	public double[] getCityCNBySubBasinID(String subBasinID){
		List<Integer> cityIndex = findCityIndexOfSubBasin(subBasinID);
		double[] cityCNTemp = new double[cityIndex.size()];
		for(int i=0; i<cityIndex.size(); i++){
			cityCNTemp[i] = cityCN[cityIndex.get(i)];
		}
		return cityCNTemp;
	}
	
	public double[] getCityCN_weightsBySubBasinID(String subBasinID){
		List<Integer> cityIndex = findCityIndexOfSubBasin(subBasinID);
		double[] cityCN_weightTemp = new double[cityIndex.size()];
		for(int i=0; i<cityIndex.size(); i++){
			cityCN_weightTemp[i] = cityCN_weight[cityIndex.get(i)];
		}
		return cityCN_weightTemp;
	}
	
	public void setSubsidyRateForFarmerByCityIndex(int cityIndex, double value){
		this.subsidyRateForFarmer[cityIndex] = value;
	}
	
	public void setSubsidyRateForFarmerByCityID(String cityID, double value) throws Exception{
		int cityIndex = findIndex(cityID);
		if(cityIndex==-1) throw new Exception(cityID +" does not exist");
		this.subsidyRateForFarmer[cityIndex] = value;
	}
	
	public void setCityCNByCityIndex(int cityIndex, double value){
		this.cityCN[cityIndex] = value;
	}
	
	public void setCityCN_weightByCityIndex(int cityIndex, double value){
		this.cityCN_weight[cityIndex] = value;
	}
	
	
}
