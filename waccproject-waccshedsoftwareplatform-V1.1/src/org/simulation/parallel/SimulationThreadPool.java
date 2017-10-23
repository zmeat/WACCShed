package org.simulation.parallel;

import java.util.ArrayList;

public class SimulationThreadPool {
	
	private ArrayList<Integer> pool;
	private int maxPoolSize;
	private int finishedCount;
	
	public SimulationThreadPool(int maxSize) {
		this.maxPoolSize = maxSize;
		this.pool = new ArrayList<Integer>();
		this.finishedCount = 0;
	}
	
	public void notifyStart(Integer simulationID) throws InterruptedException  {
		synchronized(pool) {
			while(pool.size() == maxPoolSize) {
				pool.wait();
			}
			pool.add(simulationID);
		}
	}


	public void notifyFinish(Integer simulationID) throws InterruptedException{
		synchronized(pool) {
			pool.remove(simulationID);
			this.finishedCount++;
			pool.notify();
		}
	}
	
	public void waitForFinish() throws InterruptedException {
		synchronized(pool) {
			while(pool.size() == maxPoolSize) {
				pool.wait();
			}
		}
	}

	public boolean isFull() {
	  return (this.pool.size() == this.maxPoolSize);	
	}
	
	public int getFinishedCount() {
		return finishedCount;
	}
	
	public int getSize() {
		return pool.size();
	}

}
