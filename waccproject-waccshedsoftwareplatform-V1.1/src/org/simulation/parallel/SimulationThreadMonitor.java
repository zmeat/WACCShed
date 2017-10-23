package org.simulation.parallel;

import org.utilities.time.SystemCalendar;

public class SimulationThreadMonitor extends Thread {
	
	private SimulationThreadPool pool;
	private long start;
	private int totalSimulations;
	
	public SimulationThreadMonitor(SimulationThreadPool pool, SystemCalendar start, int totalSimulations) {
		super();
		this.pool = pool;
		this.start = start.getTimeInMillis();
		this.totalSimulations = totalSimulations;
	}
	
	@Override
	public void run() {
		while(pool.getFinishedCount() < totalSimulations) {
			try {
				long nowMs = System.currentTimeMillis();
				double progress = pool.getFinishedCount()/totalSimulations;
				String status = "["+new SystemCalendar(System.currentTimeMillis()).CalendarToTimestamp()+"]";
				status = status+" "+ String.format("%.2f", progress)+"%"
						+"("+pool.getFinishedCount()+" of "+totalSimulations+")";
				status = status+" - Time elapsed: "+SystemCalendar.GetFormattedInterval(nowMs - start);
				status = status+" ("+pool.getSize()+" Running)";
				System.out.println(status);
				
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
