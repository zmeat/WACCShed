/*
 * Author: David Dziubanski, Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */


package org.components.hydrology;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.math3.analysis.function.Min;
import org.utilities.time.SystemCalendar;

public class RunOff {
	
	/* 6-17-2014: Every computation in this class has been performed by hand
	 * for 3 precip time steps. Every computation in this class has been confirmed
	 * to be correct. 
	 */
	
	private double imperv;
	private double perv;
	private double weightedCN; 
	private double past_weightedCN;
	private double min_precip;
	private double cuml_precip;
	private double cuml_runoff;

	private double CN2;

	public List<Double> scs_runoff;
	public List<Double> scs_loss;
	private double run_tot;
	private List<Double> fin_runoff;
	
	public List<Double> imp_run;
	public List<Double> perv_run;
	
	private Queue<Double> fivedayP;
	private double AMCtime;
	private double RecentPtime;

	
	public RunOff(){
		fivedayP = new LinkedList<>();
		fin_runoff = new ArrayList<>();
		imp_run = new ArrayList<>();
		perv_run = new ArrayList<>();
		scs_runoff = new ArrayList<Double>();
		scs_loss = new ArrayList<Double>();
	}
	
	protected void PercentImp(double I, double P, double Precip){
		
		imperv = Precip * I;
		imp_run.add(imperv);
		perv = Precip * P;	
		perv_run.add(perv);
	}
	
	protected void weighted_CN(Double[] integers, Double[] CN_weights){
    	double sum = 0.0;
    	for(int i = 0; i < integers.length; i++){
    		double var = integers[i]*CN_weights[i];
    		sum = sum + var;
    	}
    	
    	CN2 = sum;
    }
	
	private double AMC_cond(double p) {
    	AMCtime  = AMCtime + 1;  // 1 is timestep
    	
    	if (AMCtime < 120.0) {
    		fivedayP.add(p);
		} else {
			fivedayP.add(p);
			fivedayP.remove();
		}
    	double tot5day = 0.0;
    	for (double each : fivedayP) {
			tot5day = tot5day + each;
		}
    	return tot5day;
    }
	
	protected void SCS_CN_runoff(double p){
		
		// Adjust CN based on 5 day antecedant rainfall
		/*
		if (AMC_cond(p) < 1.4) {
			weightedCN = (4.2 * CN2)/(10 - 0.058 * CN2);
		} else if (AMC_cond(p) >= 1.4 && AMC_cond(p) <= 2.1) {
			weightedCN = CN2;
		} else {
			weightedCN = (23 * CN2)/(10 + 0.13 * CN2);
		} */
		
		weightedCN = CN2;
		
		/* If the current CN is different than the previous CN, 
		 * the min precip at which no runoff occurs changes
		 */
		
		if (weightedCN != past_weightedCN) {
			/*
			 * Determining point where curve reaches 0. Below this point,
			 * any rainfall will not induce runoff.
			 */
			double S = (1000.0/weightedCN) - 10.0;
			double Ia = 0.2 * S;
			
			min_precip = Ia;
			//setMin_precip(min_precip);
		}   
		
		cuml_precip = cuml_precip + perv;
		
		if (perv == 0.0 & cuml_precip > 0.0) {
			RecentPtime  = RecentPtime + 1; // 1 is the timestep
			if (RecentPtime >= 2.0) {
				cuml_precip = 0.0;
			}
		} else if (cuml_precip == 0.0) {
			cuml_precip = 0.0;
		}
		
		if (cuml_precip > min_precip) {
			double S = (1000.0/weightedCN) - 10.0;
            double tot_runoff = Math.pow((cuml_precip - min_precip), 2)/(cuml_precip + 0.8*S);
            double step_runoff = tot_runoff - cuml_runoff;
            scs_runoff.add(step_runoff);
            
            double max_reten = perv - step_runoff;
            scs_loss.add(max_reten);
            
            cuml_runoff = cuml_runoff + step_runoff;
            
		} else {
			double increm_loss = perv;
			scs_loss.add(increm_loss);
			
			double step_runoff = 0.0;
    		scs_runoff.add(step_runoff);
    		cuml_runoff = 0.0;
		}
		
		past_weightedCN = weightedCN;
    }
	
	public void runoff_tot(){ 
		
		run_tot = scs_runoff.get(scs_runoff.size() - 1) + imperv;
		fin_runoff.add(run_tot);    	
	}
	
	public double get_Perv_runoff(){
		return scs_runoff.get(scs_runoff.size() - 1);
	}
	
	public double get_Tot_runoff(){
		return run_tot;
	}

	public double getMin_precip() {
		return min_precip;
	}

	public void setMin_precip(double min_precip) {
		this.min_precip = min_precip;
	}

	public List<Double> getFin_runoff() {
		return fin_runoff;
	}
	
}


	/*
	List<Double> wt_pr;
	List<Double> pr_imp;
	List<Double> pr_perv;
	List<Double> scs_runoff;
	Double CN_sum; 
	List<Double> f_runoff;

    public RunOff() {
    	wt_pr = new ArrayList<Double>();
    	pr_imp = new ArrayList<Double>();
    	pr_perv = new ArrayList<Double>();
    	scs_runoff = new ArrayList<Double>();
    	f_runoff = new ArrayList<Double>();
	} 
	*/
    
    /************ Theissen polygon method for calculating weighted precip.
     * Inputs: gw - array of gage weights
     * 		   v - array of precip at each gage. (multiple gages allowed)
     * Class Variables: None
     *
     * Top loop loops through each element in one array
     * in varargs. Inner loop loops across all arrays in
     * varargs based on the specified index from outer loop.
     * Example:
     * {{1,2,3}, {4,5,6}}
     * 
     * 1 4, 2 5, 3 6
     * 
     * Outputs: wt_pr - precip each time step based on gages.
     ************/
    /*
    public void Thiessen(Double[] gw, Double[] ... v){

        for(int i = 0; i < v[0].length; i++){
            double sum = 0;
            for(int j = 0; j < v.length; j++){
                double precip = v[j][i];
                double w_precip = gw[j] * precip;
                sum += w_precip;
            }        
            wt_pr.add(sum);
        }   
        //System.out.println(Arrays.deepToString(wt_pr.toArray()));
    }
    */
    /************ Percent Impervious method.
     * Inputs: I - Percent Impervious
     *         P - Percent Pervious.
     * Class variables: wt_pr - Precip from Thiessen Polygon Method.
     * 
     * Loops through precip and multiplies each precip value by
     * the percent pervious and impervious. Eaches each of these
     * values to separate lists. 
     * 
     * Outputs: pr_imp - depth of runoff from impervious surfaces.
     * 			pr_perv - depth of precip on pervious surfaces.
     ************/
	/*
    public void PercentImp(double I, double P){
    	
    	for(int i = 0; i < wt_pr.size(); i++){
    		double imperv = wt_pr.get(i) * I;
    		pr_imp.add(imperv);
    		
    		double perv = wt_pr.get(i) * P;
    		pr_perv.add(perv);
    	}
    	//System.out.println(Arrays.deepToString(pr_perv.toArray()));
    	//System.out.println(Arrays.deepToString(pr_imp.toArray()));
    }
    */
    /************ Method for determining curve number.
     * Inputs: CNs - Curve Numbers
     * 		   CN_weights - weights associated with CNs..
     * Class Variables: None.
     * 
     * Loops through curve numbers. 
     * Multiplies each curve number by its weights and adds
     * to a cumulative curve number sum. 
     * 
     * Outputs: CN_sum - final average curve number.
     ************/
    /*
    public void weighted_CN(Double[] CNs, Double[] CN_weights){
    	CN_sum = 0.0;
    	for(int i = 0; i < CNs.length; i++){
    		double var = CNs[i]*CN_weights[i];
    		CN_sum += var;
    	}
    	//System.out.println(CN_sum);
    }
    */
    /************ Method for determining runoff from CN.
     * Inputs: None
     * Class Variables: CN_sum - average curve number.
     * 		            pr_perv - weights associated with CNs.
     * 
     * Loops through list of precip values over pervious area
     * and determines runoff using SCS CN method.
     * 
     * Outputs: scs_runoff - runoff at each time step from the pervious area.
     ************/
	/*
    public void SCS_CN_runoff(Double CN_sum, List<Double> pr_perv){

        double S = (1000/CN_sum) - 10;
        double Ia = 0.2 * S;

        for(double each: pr_perv){
        	if(each > 0.0){

	            double Q = Math.pow((each - Ia), 2)/(each + 0.8*S);
	            scs_runoff.add(Q);
        	}
        	else{
        		scs_runoff.add(0.0);
        	}
        } 
        //System.out.println(scs_runoff);
    }
    */
    /************ Method for determining total runoff.
     * Inputs: None
     * Class Variables: scs_runoff - runoff from pervious area.
     * 		            pr_imp - runoff from impervious area.
     * 
     * Loops through each time step and adds together the runoff from
     * the impervious area and the runoff from the pervious area to
     * get the total runoff from each time step. 
     * 
     * Outputs: f_runoff - total runoff for each time step.
     ************/
    /* 
    public void runoff_tot(List<Double> scs_runoff, List<Double> pr_imp){
    	
    	for(int i = 0; i < scs_runoff.size(); i++){
    		System.out.println(scs_runoff.size());
    		Double rt = scs_runoff.get(i) + pr_imp.get(i);
    		f_runoff.add(rt);
    	}
    	//System.out.println(f_runoff);
    }
    
    public void clear_list(){
    	wt_pr.clear();
    	pr_imp.clear();
    	pr_perv.clear();
    	scs_runoff.clear();
    	f_runoff.clear();
    }
    */
    

