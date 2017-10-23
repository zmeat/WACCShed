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
 * $Log: ParallelUnitIterator.java,v $
 * Revision 1.1.1.1  2008/08/06 08:26:26  mludwig
 * Imported sources
 *
 * Revision 1.1.1.1.2.1  2007/10/14 21:46:13  mludwig
 * *** empty log message ***
 *
 * Revision 1.1.1.1  2007/08/21 09:50:09  mludwig
 * Imported DANUBIA 2.0 core sources
 *
 * Revision 1.17  2006/05/02 17:42:40  mludwig
 * Refactored import statements in packages simulation and wrapper: removed wildcard imports when importing from org.glowa.danube
 *
 * Revision 1.16  2004/08/25 06:04:11  krausa
 * Enhanced by getter and setter for block size
 *
 * Revision 1.15  2003/05/11 10:01:25  krausa
 * Changed thread synctime to 100ms
 *
 * Revision 1.14  2003/05/08 13:05:51  krausa
 * Refined debug messages to find error
 *
 * Revision 1.13  2003/05/08 08:28:15  krausa
 * Inserted debug messages
 *
 * Revision 1.12  2003/05/06 10:11:10  krausa
 * Removed debug messages because they slowed down everything, CHECK
 *
 * Revision 1.10  2003/04/10 17:49:18  krausa
 * Removed process logging
 *
 * Revision 1.9  2003/04/10 11:23:51  krausa
 * Debug
 *
 * Revision 1.8  2003/04/10 09:51:59  krausa
 * Better reusability
 *
 * Revision 1.7  2003/04/02 15:44:49  krausa
 * Changed cluster size
 *
 * Revision 1.6  2003/04/02 15:08:40  krausa
 * Changed cluster size
 *
 * Revision 1.5  2003/04/02 14:36:02  krausa
 * Update
 *
 * Revision 1.4  2003/04/02 13:37:40  krausa
 * Changed signature for unit iterator computeUnit()
 *
 * Revision 1.3  2003/04/02 13:13:55  krausa
 * Changed signature for unit iterator from compute() to computeUnit()
 *
 * Revision 1.2  2003/04/02 12:04:09  krausa
 * Changed signature for unit iterator from compute() to computeUnit()
 *
 * Revision 1.1  2003/04/01 16:33:58  krausa
 * Introduced sequential and parallel unit iterators
 *
 */
package org.utilities.execution;
import org.apache.log4j.Logger;
import org.simulation.model.unit.UnitTable;
import org.utilities.time.SystemCalendar;
/**
 * The class <tt>ParallelUnitIterator</tt> iterates sequentially over all the units in a unit table.
 *
 * @author Andreas Kraus
 * @version $Id: ParallelUnitIterator.java,v 1.1.1.1 2008/08/06 08:26:26 mludwig Exp $
 */
public class ParallelUnitIterator implements UnitIterator
{
  /** Logging instance */
  private static Logger logger = Logger.getLogger( ParallelUnitIterator.class );
  // Number of parallel threads
  private int nThreads;
  // Actual index in PID array
  private int pidIndex;
  // Count of active threads
  protected int threadCount = 0;
  // Model exception
  protected Exception modelEx;
  protected UnitTable ptbl;
  protected SystemCalendar actTime;
  protected Object data;
  protected String[] pids;
  protected int blockSize = 1000;
  /**
   * Constructor
   * @param nThread Number of threads
   */
  public ParallelUnitIterator( int nThreads )
  {
    this.nThreads = nThreads;
    logger.debug( "Created for " + nThreads + " threads, block size " + getBlockSize() );
    threadCount = 0;
  }
  /**
   * Get block size for parallel computation
   */
  public int getBlockSize()
  {
    return blockSize;
  }
  /**
   * Set block size for parallel computation
   */
  public void setBlockSize( int blockSize )
  {
    this.blockSize = blockSize;
  }
  /**
   * Call the compute method off all unit "inside" a unit table
   * @param ptbl UnitTable
   * @param actTime Actual time
   * @param data Object with custom data
   */
  @Override
public void compute( UnitTable ptbl, SystemCalendar actTime, Object data )
  {
    logger.debug( "Iterating units parallel" );
    this.ptbl = ptbl;
    this.actTime = actTime;
    this.data = data;
    pids = ptbl.insidePIDs();
    pidIndex = 0;
    modelEx = null;
    if( pids.length < blockSize * nThreads ) blockSize = ( pids.length + nThreads -1 ) / nThreads;
    else if( pids.length / 2 < blockSize * nThreads ) blockSize = ( pids.length / 2 + nThreads -1 ) / nThreads;
    // Create WorkerThreads
    for( int i=0; i<nThreads; i++ )
    {
      ( new WorkerThread( i+1 ) ).start();
    }
    while( true )
    {
      synchronized( this )
      {
        try
        {
          wait( 100 );
          if( modelEx != null || ( pidIndex >= pids.length && threadCount == 0 ) ) break;
        }
        catch( Exception ex )
        {
        }
      }
    }
    if( modelEx != null )
    {
      throw new RuntimeException(modelEx);
    }
    logger.debug( "Iterating units parallel finished" );
  }
  synchronized protected void incThreadCount()
  {
    threadCount++;
    logger.debug( "Now " + threadCount + " threads active" );
    notifyAll();
  }
  synchronized protected void decThreadCount()
  {
    threadCount--;
    logger.debug( "Now " + threadCount + " threads active" );
    notifyAll();
  }
  synchronized protected int getNextPIDIndex()
  {
    int idx = pidIndex;
    pidIndex += getBlockSize();
    if( pidIndex > pids.length ) pidIndex = pids.length;
    return idx;
  }
  /**
   * Worker thread
   */
  private class WorkerThread extends Thread
  {
    int num;
    /**
     * Constructor
     */
    WorkerThread( int num )
    {
      this.num = num;
      incThreadCount();
    }
    /**
     * Run method of Thread
     */
    @Override
	public void run()
    {
      outerloop: while( modelEx == null )
      {
        int startIdx = getNextPIDIndex();
        if( startIdx >= pids.length ) break;
        int endIdx = startIdx + getBlockSize() -1;
        if( endIdx >= pids.length ) endIdx = pids.length - 1;
        logger.debug( "WorkerThread[" + num + "] computes units " + startIdx + "-" + endIdx );
        for( int i=startIdx; i<=endIdx; i++ )
        {
          try
          {
            ptbl.getUnit( pids[ i ] ).computeUnit( actTime, data );
          }
          catch( Exception ex )
          {
            logger.error( "Caught exception in computeUnit(pid=" + pids[ i ] + "): " + ex.toString(), ex );
            modelEx = ex;
            break outerloop;
          }
        }
      }
      decThreadCount();
    }
  }
}
