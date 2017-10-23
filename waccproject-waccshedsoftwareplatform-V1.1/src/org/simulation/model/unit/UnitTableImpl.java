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
 * Created on 18.06.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.simulation.model.unit;


import java.lang.reflect.Array;

import org.apache.log4j.Logger;
import org.metadata.AreaMetaData;


public class UnitTableImpl<P extends AbstractUnit> implements Cloneable, UnitTable<P>
{
  private static final long serialVersionUID = 5704395351240653231L;

  /** Logging instance */
  private transient Logger logger;
  protected AreaMetaData amd;
  
  /** Unitarray for storing the units */
  transient P[] pa = null;

  // PIDs of units inside
  String[] insidePids = null;

  
  // Unit type
  private Class unitType;
  
  // counts the units that change landuse
  private transient String modelId;

  
  /**
   * No arguments constructor for deserialization
   */
  protected UnitTableImpl()
  {
  }

  /**
   * Split the unit table into partitions
   * @param splitIndices Indices of the PID array that are used to split the table
   * @return Array of unit tables
   */
  public static <X extends AbstractUnit> UnitTableImpl<? extends AbstractUnit> create(
			String modelId, /*LanduseQuery landuseQuery,*/ AreaMetaData amd, Class<X> unitType) {
	  return new UnitTableImpl<X>(modelId,  /*landuseQuery,*/ amd, unitType);
  }
  
  public AreaMetaData getAreaMetaData()
  {
    return amd;
  }
  /**
   * Constructor
   * @param unitType Type of unit (Class object), must be subclass of Unit 
   */
  @SuppressWarnings("unchecked")
	protected UnitTableImpl(String modelId,  /*LanduseQuery landuseQuery,*/ AreaMetaData amd,
			Class<P> unitType)
  {
   this.amd = amd;
    this.modelId = modelId;
    this.unitType = unitType;

//    this.landuseQuery = landuseQuery;
    logger = Logger.getLogger(UnitTableImpl.class);
    logger.debug( "Initializing..." );

    // pa = (P[]) new Object[amd.getAreaNUnits()];
    pa = (P[]) Array.newInstance(unitType, amd.getNumOfsubBasin());
    insidePids = new String[amd.getNumOfsubBasin()];
    
    // Initialize unit array
    if(!AbstractUnit.class.isAssignableFrom(unitType)) throw new RuntimeException( "UnitTable must be initialized with a subtype of AbstractUnit!" );
    for(int i=0; i<amd.getNumOfsubBasin(); i++)
    {
 	   String pid = amd.getPIDByArrayIndex(i);
        try
        {
     	   P p = unitType.newInstance();
          p.setPID( pid );
          p.setUnitTable(this);
 
          putUnit( pid, p );
          
          p.setArea(amd.getBasinAreas()[i]);
          
          insidePids[i] = pid;
          
        }
        catch( Exception ex )
        {
        	logger.debug("Exception in UnitTable initialization: " + ex, ex);
          throw new RuntimeException( "Bad unit type in UnitTable constructor: " + ex.toString() );
        }
     
     
    }
    
  }
  
  /**
   * Put unit in table
   * @param pid unit id
   * @param p unit
   */
  private void putUnit(String pid, P p)
  {
    int idx = amd.getArrayIndexByPID( pid );
    if(idx >= 0 && idx < pa.length) pa[idx] = p;
  }

  @Override
public P getUnit(String pid)
  {
    int idx = amd.getArrayIndexByPID(pid);
    if(idx >= 0 && idx < pa.length) return pa[idx];
    else throw new RuntimeException("Unit with PID " + pid + " not in UnitTable!");
  }
  
  @Override
public String[] insidePIDs()
  {
    return insidePids;
  }

}
