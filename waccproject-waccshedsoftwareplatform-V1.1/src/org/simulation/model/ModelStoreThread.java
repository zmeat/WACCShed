/*
* Copyright (c) 2011, GLOWA-Danube and individual contributors as listed at
* http://www.glowa-danube.de/de/opendanubia/framework_core.php
* All rights reserved. 
*
* Redistribution and use in source and binary forms, with or without 
* modification, are permitted provided that the following conditions 
* are met: 
* * Redistributions of source code must retain the above copyright notice, 
* this list of conditions and the following disclaimer. 
* * Redistributions in binary form must reproduce the above copyright notice, 
* this list of conditions and the following disclaimer in the documentation 
* and/or other materials provided with the distribution. 
* * Neither the name of GLOWA-Danube nor the names of its contributors 
* may be used to endorse or promote products derived from this software without
* specific prior written permission. 
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
* POSSIBILITY OF SUCH DAMAGE.
*/
package org.simulation.model;

import org.utilities.time.SystemCalendar;

public class ModelStoreThread extends Thread {

	private Object syncObject = new Object();
	
	public enum ModelStoreThreadState {
		INACTIVE, ACTIVE
	}

	private ModelStoreThreadState state = ModelStoreThreadState.INACTIVE;

	private ModelCore core;

	private SystemCalendar storeTime;
	private boolean abort = false;

	public ModelStoreThread(ModelCore mc, SystemCalendar start) {
		core = mc;
		storeTime = start;
	}

	@Override
	public void run() {
		while (!isAborted()) {
			enterStore();
			if (!isAborted()) core.store(storeTime);
			exitStore();
		}
	}
	
	public synchronized void enterStore() {
		while (getModelStoreThreadState().equals(ModelStoreThreadState.INACTIVE) && !isAborted())
			try {
				wait();
			} catch (InterruptedException e) {
			}
	}
	
	public synchronized void exitStore() {
			setModelStoreThreadState(ModelStoreThreadState.INACTIVE);
			notifyAll();
	}

	public synchronized void execute(SystemCalendar t) {
		storeTime = t;
		setModelStoreThreadState(ModelStoreThreadState.ACTIVE);
		notifyAll();
	}
	
	public synchronized void waitForFinish() {
		while (getModelStoreThreadState().equals(ModelStoreThreadState.ACTIVE)) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void setModelStoreThreadState(ModelStoreThreadState s) {
		state = s;
		notifyAll();
	}

	private synchronized ModelStoreThreadState getModelStoreThreadState() {
		return state;
	}

	public synchronized void abort() {
		abort = true;
		notifyAll();
	}

	public synchronized boolean isAborted() {
		return abort;
	}
}
