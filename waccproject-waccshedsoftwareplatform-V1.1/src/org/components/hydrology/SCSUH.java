/*
 * Author: David Dziubanski, Yu Jie
 * Copyright (c) 2015, WACC and individual contributors as listed at
 * https://scicomm.las.iastate.edu/water-climate-change/
 * All rights reserved. 
 */


package org.components.hydrology;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
//NumberFormat df = DecimalFormat.getInstance();

public class SCSUH {
	
	/* 6-17-2014: Every computation in this class has been performed by hand
	 * for 3 precip time steps. Every computation in this class has been confirmed
	 * to be correct. 
	 */
	
	private Double tlag;
	private Double delta_t;
	private Double Tp;
	private Double Qp;
	private int C = 484;    // 200 is the value for rural, slight slopes
	private Double waterA;
	private double[] time;
	private double[] Q;
	private Double[] newT;
	private Double[] newQ;
	private List<Double> UHtimestep;
	private UnivariateInterpolator intp = new SplineInterpolator();
	private List<Double> hydrograph;
	private List<Double> observ_discharge;
	private double baseflow = 30;
	private int time_step;

	// SCS dimensionless unit hydrograph
	 
	Double SCS_t_tp[] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7,
				         0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5,
				         1.6, 1.7, 1.8, 1.9, 2.0, 2.2, 2.4, 2.6,
				         2.8, 3.0, 3.2, 3.4, 3.6, 3.8, 4.0, 4.5, 5.0};
	
	Double SCS_Q_Qp[] = {0.000, 0.030, 0.100, 0.190, 0.310, 0.470,
						 0.660, 0.820, 0.930, 0.990, 1.000, 0.990, 
						 0.930, 0.860, 0.780, 0.680, 0.560, 0.460, 
						 0.390, 0.330, 0.280, 0.207, 0.147, 0.107, 
						 0.077, 0.055, 0.040, 0.029, 0.021, 0.015, 
						 0.011, 0.005, 0.000};
	
	public SCSUH(double time_lag, double time_delta, double Area){
		tlag = time_lag;
		delta_t = time_delta;
		waterA = Area;	
		time = new double[SCS_t_tp.length];
		Q = new double[SCS_t_tp.length];
		time_step = -1;
		hydrograph = new ArrayList<>();
		
	}
	
	public void time_peak(){
		Tp = (delta_t/2.0) + tlag;
	}
	
	public void UH_peak(){
		Qp = C*(waterA/Tp);
	}
	
	public void find_t(){
		for(int i = 0; i < SCS_t_tp.length ; i++){
			double t = SCS_t_tp[i] * Tp;
			time[i] = t;
		}
		/* Using Math.ceil(time[time.length -1])+1 for interpolation
		 * purposes. If the end of the new time (last step of hydrograph)
		 * turns out to be 39.4 hours for example, and I want to interpolate
		 * for every hour (hour 0, 1, 2, 3, 4, etc.), I need to round it up
		 * for 40 hrs. I don't want to round down and interpolate only to 39
		 * hours b/c at hour 39, there is still flow in the stream. At hour
		 * 40, there is 0 flow. Then, since the hydrograph goes from hour 
		 * 0 to 40, we need an array that is 41 long. 
		 */
		int step_adjust = (int)(1/delta_t);
		newT = new Double[(int) (Math.ceil(time[time.length -1]))*step_adjust];
	}
	
	public void find_Q(){
		
		for(int i = 0; i < SCS_Q_Qp.length; i++){
			double q = SCS_Q_Qp[i] * Qp;
			Q[i] = q;
		}
		int step_adjust = (int)(1/delta_t);
		newQ = new Double[(int) (Math.ceil(time[time.length -1]))*step_adjust];
	}
	
	public void function() {
		UnivariateFunction function = intp.interpolate(time, Q);
		
		for(int i = 0; i < newT.length; i++){
			int last_index = newT.length;
			double newStep = i*delta_t;
			
			if(newStep<=time[time.length-1]){
				newT[i] = newStep;
				double y = function.value(i*delta_t);
				double newFlow =  y;
				newQ[i] = newFlow;
			}else{
				newT[newT.length-1] = newStep;
				newQ[newT.length-1] = 0.0;
			}
		}
	}
	
	
	public void UHtimeseries(double runoff){
		
		UHtimestep = new ArrayList<>();
		double excess = runoff;
		time_step++;
		for(int j = 0; j < newQ.length; j++){
			// creating hydro ordinates based on excess
			double hyord = excess * newQ[j];
			/* UHtimestep is the reponse for the current excess precip
			 * time step */
			UHtimestep.add(hyord);
		}
	}
	
	public List<Double> calcHyd() { 
		// Observed discharge is for the city agents
		observ_discharge = new ArrayList<>();
	    // ordsum is the sum of hydrograph ordinates
		double ordsum = 0;
		
		/* If it is the first time step of the model run, 
		 * the program just adds the hydro ordinates from UHtimestep
		 * into hydrograph plus the baseflow. */
		
		if (time_step == 0) {
			for (Double each : UHtimestep) {
				ordsum = each + baseflow;
				hydrograph.add(ordsum);
				observ_discharge.add(ordsum);
			}
		} else {
			/* If it is the second time step or later:
			 * 1. loop through UHtimestep
			 * 2. Retrieve the index from hydrograph starting at the current time step (so time step 2)
			 * 3. Add the ordinate from UHtimestep to the ordinate retrieved from hydrograph
			 * 4. Replace the ordinate in hydrograph with the new sum. 
			 * 5. Do all of the above until the end of hydrograph is reached.
			 * 6. Then, add the end ordinate of UHtimestep to the end of hydrograph and add baseflow. 
			 */
			int sum_index = time_step;
			for(Double each : UHtimestep) {
				if (sum_index < hydrograph.size()){
					ordsum = hydrograph.get(sum_index) + each;
					hydrograph.set(sum_index, ordsum);
					observ_discharge.add(ordsum);
				} else {
					ordsum = each + baseflow;
					hydrograph.add(ordsum);
					observ_discharge.add(ordsum);
				}				
				sum_index++;
			} 
		}
		return hydrograph;
	}
	
	private void print_list(List<Double> somelist){
		for(double each : somelist){
			System.out.println(each);
		}
	}
}














		/*
		for(int i = 0; i < runoff.size(); i++){
			System.out.println(Arrays.toString(UHtimestep[i]));
		}*//*
	
	/*
	//NumberFormat df = DecimalFormat.getInstance();
	double tlag;
	double delta_t;
	double Tp;
	double Qp;
	double C = 484;
	double waterA;
	double[] time;
	double[] Q;
	double[] newT;
	double[] newQ;
	double[][] UHtimestep;
	double[] hydrograph;
	UnivariateInterpolator intp = new SplineInterpolator();
	
	// SCS dimensionless unit hydrograph
	 
	double SCS_t_tp[] = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7,
				         0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5,
				         1.6, 1.7, 1.8, 1.9, 2.0, 2.2, 2.4, 2.6,
				         2.8, 3.0, 3.2, 3.4, 3.6, 3.8, 4.0, 4.5, 5.0};
	
	double SCS_Q_Qp[] = {0.000, 0.030, 0.100, 0.190, 0.310, 0.470,
						 0.660, 0.820, 0.930, 0.990, 1.000, 0.990, 
						 0.930, 0.860, 0.780, 0.680, 0.560, 0.460, 
						 0.390, 0.330, 0.280, 0.207, 0.147, 0.107, 
						 0.077, 0.055, 0.040, 0.029, 0.021, 0.015, 
						 0.011, 0.005, 0.000};
	
	public SCSUH(double time_lag, double time_delta, double Area){
		tlag = time_lag;
		delta_t = time_delta;
		waterA = Area;	
		time = new double[SCS_t_tp.length];
		Q = new double[SCS_t_tp.length];
		
	}
	*/
	
	/************ Time peak method for calculating time peak.
     *
     * Class Variables: delta_t - duration of rainfall (hr)
     * 					tlag - lag time (hr)
     * 
     * Outputs: Tp - time peak.
     ************/
	/*
	public void time_peak(){
		Tp = (delta_t/2.0) + tlag;
		//System.out.println(Tp);
	}
	*/
	/************ UH peak method for calculating UH peak.
    *
    * Class Variables: C - conversion constant
    * 				   waterA - watershed area
    * 				   Tp - time peak (hr)
    * 
    * Outputs: Qp - UH peak.
    ************/
	/*
	public void UH_peak(){
		Qp = C*(waterA/Tp);
		//System.out.println(Qp);
	}
	*/
	/************ Find T method for calculating time points of UH.
    *
    * Class Variables: SCS_t_tp[] - array of t/tp from SCS dimensionless UH.
    * 				   Tp - time peak (hr)
    * 
    * Outputs: time[] - array of time points.
    * 		   Initializing newT[] array - length is 1 longer b/c
    * 		   need to put hour 0 into array
    ************/
	/*
	public void find_t(){
		for(int i = 0; i < SCS_t_tp.length ; i++){
			double t = SCS_t_tp[i] * Tp;
			time[i] = t;
		}
		//System.out.println(Arrays.toString(time));
		newT = new double[(int) Math.ceil(time[time.length -1])+1];
	}
	*/
	/************ Find Q method for calculating Q points of UH.
    *
    * Class Variables: SCS_Q_Qp[] - array of Q/Qp from SCS dimensionless UH.
    * 				   Qp - UH peak (cfs)
    * 
    * Outputs: Q[] - array of Q points.
    *          Initializing newQ[] array - length is 1 longer b/c
    * 		   need to put hour 0 into array
    ************/
	/*
	public void find_Q(){
		for(int i = 0; i < SCS_Q_Qp.length; i++){
			double q = SCS_Q_Qp[i] * Qp;
			Q[i] = q;
		}
		//System.out.println(Arrays.toString(Q));
		newQ = new double[(int) Math.ceil(time[time.length -1])+1];
		//System.out.println(newQ.length);
	}
	*/
	/************ Function method for interpolating time and Q, then
	 *            determining new T and Q based on whole hours (1,2,3..)
    *
    * Class Variables: time - time of dimensionless UH. 
    * 				   Q - Q of dimensionless UH
    * 
    * Outputs: newT[] - array of new time points.
    * 		   newQ[] - array of new Q points.
    ************/
	/*
	public void function() {
		UnivariateFunction function = intp.interpolate(time, Q);
	*/	
		/*
		df.setRoundingMode(RoundingMode.DOWN);
		df.setMinimumFractionDigits(0);
		df.setMaximumFractionDigits(0);
		double a = 27.6;
		System.out.println(df.format(a));
		*/
		/*
		for(int i = 0; i < newT.length - 1; i++){
			newT[i] = i;
			double y = function.value(i);
			newQ[i] = y;
		}
		newT[newT.length - 1] = newT.length - 1;
		newQ[newT.length - 1] = 0.0;
		//System.out.println(Arrays.toString(newT));
		//System.out.println(Arrays.toString(newQ));
		/*
	}
	
	/************ Method for creating array that has stores all hydrographs 
	 * 			  for each time step to add the hydrographs up.
    * Inputs: len - length of precip time series
    * 		  
    * Class Variables: None
    * 
    * Outputs: UHtimestep[][] - 2D array for storing hydrograph for each time step.
    * 		  
    ************/
	/*
	public void createUHarray(int len){
		UHtimestep = new double[len][newQ.length + (len - 1)];
	}
	*/
	/************ Method for calculating the hydrograph for 
	 *            each time step based on the runoff.
    * Inputs: runoff - runoff list time series
    * 		  
    * Class Variables: newQ - array of new Q points.
    * 				   UHtimestep[][] - double array for storing hydrograph for each time step.
    * 
    * Outputs: UHtimestep[][] -2D array of hydrographs for each time step
    * 							based on runoff.		  
    ************/
	/*
	public void UHtimeseries(List<Double> runoff){
		for(int i = 0; i < runoff.size(); i++){
			// get runoff value
			double excess = runoff.get(i);
			for(int j = 0, k = i; j < newQ.length; j++, k++){
				// creating hydro ordinatre based on excess
				double hyord = excess * newQ[j];
				// inputing ordinate into ith inner array, kth element
				// k starting at i - time step offset to calc overall hydrograph
				UHtimestep[i][k] = hyord;
			}
		}
		/*
		for(int i = 0; i < runoff.size(); i++){
			System.out.println(Arrays.toString(UHtimestep[i]));
		}*//*
	}
	*/
	/************ Method for calculating the overall final hydrograph 
    * 
    * Outputs: hydrograph[] - array of final hydrograph points		  
    ************/
	/*
	public double[] calcHyd() {
		// initializing hydrograph
		hydrograph = new double[UHtimestep[0].length];
		for(int i = 0; i < UHtimestep[0].length; i++){
			double ordsum = 0;
			for(int j = 0; j < UHtimestep.length; j++){
				// adding first element of each inner array
				ordsum += UHtimestep[j][i];
			}
			hydrograph[i] = ordsum;
		}
		//System.out.println(Arrays.toString(hydrograph));
		return hydrograph;
	}
	*/

