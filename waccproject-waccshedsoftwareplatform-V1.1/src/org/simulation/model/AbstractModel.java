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
/*
 * Created on 15.12.2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.simulation.model;

import org.components.interfaces.CommunicationInterface;
import org.components.interfaces.CommunicationInterfaceNotFoundException;
import org.simulation.component.AbstractComponent;
import org.simulation.model.unit.AbstractUnit;
import org.simulation.model.unit.UnitTable;
import org.simulation.model.unit.UnitTableImpl;
import org.utilities.execution.ParallelUnitIterator;
import org.utilities.execution.SequentialUnitIterator;
import org.utilities.execution.UnitIterator;
import org.utilities.execution.UnitIteratorFactory;
import org.utilities.time.SystemCalendar;

public abstract class AbstractModel<P extends AbstractUnit> extends AbstractComponent {

	private UnitTable<P> unitTable;
	private UnitIterator unitIterator = null;
	
	protected AbstractModel() {
	}
		
	final void setUnitTable(UnitTableImpl<P> pt) {
		unitTable = pt;
	}
	
	// plug points
	protected void getData(SystemCalendar t) {
		
	}
	
	protected void compute(SystemCalendar t) {
		
	}
	
	protected void provide(SystemCalendar t) {
		
	}
	
	protected void store(SystemCalendar t) {
		
	}

			
	@Override
	protected <I extends CommunicationInterface> I getImplementor(String interfaceName) throws CommunicationInterfaceNotFoundException {
		return (I) super.getImplementor(interfaceName);
	}

	// queries
	protected final UnitTable<P> unitTable() {
		return unitTable;
	}
	
	protected final P unit(String pid) {
		return unitTable().getUnit(pid);
	}

	
	protected final String[] pids() {
		return unitTable().insidePIDs();
	}
	
	// modifiers
	
	protected final void computeUnitsParallel(SystemCalendar t, Object data) {
		if (!(unitIterator instanceof ParallelUnitIterator))
			unitIterator = UnitIteratorFactory.createParallelUnitIterator();
		unitIterator.compute(unitTable, t, data);	
	}
	
	protected final void computeUnitsSequential(SystemCalendar t, Object data) {
		if (!(unitIterator instanceof SequentialUnitIterator))
			unitIterator = UnitIteratorFactory.createSequentialUnitIterator();
		unitIterator.compute(unitTable, t, data);
	}

}
