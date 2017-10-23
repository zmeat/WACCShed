package org.configuration;

import java.io.Serializable;
import org.utilities.time.SystemCalendar;

public class SimulationParameters implements Serializable{

	private static final long serialVersionUID = 1L;
	
	
	private SystemCalendar timestamp;

	private int simulationID;
	private int scenarioID;
	
	/**  Farmer savings-target [unit-free scalar] */
	private double theta;
	
	/** Levee Quality Effectiveness [cfs/ft] */
	private double lqe;
	
	/** Risk-aversion parameter */
	private double d;
	
	/** City Manager behavior Mode */
	private boolean cmMode;
	
	/** Farmer behavior Mode */
	private boolean fMode;
	
	
	
	public SimulationParameters(SystemCalendar timestamp,  int scenarioID, double theta, double lqe,
			double d, boolean cmMode, boolean fMode) {
		this.timestamp = timestamp;
		this.scenarioID = scenarioID;
		this.theta = theta;
		this.lqe = lqe;
		this.d = d;
		this.cmMode = cmMode;
		this.fMode = fMode;
		
	}
	
	public void setSimulationID(int simID) {
		this.simulationID = simID;
	}
	
	public SystemCalendar getTimestamp() {
		return timestamp;
	}

	public int getSimulationID() {
		return simulationID;
	}
	
	public int getScenarioID() {
		return scenarioID;
	}
	public double getTheta() {
		return theta;
	}
	public double getLQE() {
		return lqe;
	}
	
	public double getD() {
		return d;
	}

	public boolean isCM_ON() {
		return cmMode;
	}

	public boolean isF_ON() {
		return fMode;
	}

	public String toConsoleString() {
		String retStr = new String("Id:"+simulationID+" | Scenario:"+Integer.toString(scenarioID)+
				" | Theta:"+Double.toString(theta)+" | LQE:"+Double.toString(lqe)+" | D:"+Double.toString(d)+
				" | CM-ON:"+Boolean.toString(cmMode)+" | F-ON:"+Boolean.toString(fMode));
		return retStr;
	}
	
	public String toSQLString() {
		String retStr = new String("Id:"+simulationID+" | Scenario:"+Integer.toString(scenarioID)+
				" | Theta:"+Double.toString(theta)+" | LQE:"+Double.toString(lqe)+" | D:"+Double.toString(d)+
				" | CM-ON:"+Boolean.toString(cmMode)+" | F-ON:"+Boolean.toString(fMode));
		return retStr;
	}

	
}
