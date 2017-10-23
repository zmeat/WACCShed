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
 * $Log: UnitIteratorFactory.java,v $
 * Revision 1.1.1.1  2008/08/06 08:26:26  mludwig
 * Imported sources
 *
 * Revision 1.1.1.1  2007/08/21 09:50:09  mludwig
 * Imported DANUBIA 2.0 core sources
 *
 * Revision 1.10  2006/05/02 17:42:40  mludwig
 * Refactored import statements in packages simulation and wrapper: removed wildcard imports when importing from org.glowa.danube
 *
 * Revision 1.9  2005/12/14 14:37:22  krausa
 * Now dynamically querying number of processors
 *
 * Revision 1.8  2004/09/07 17:37:57  krausa
 * Removed remote and agent packet
 *
 * Revision 1.7  2003/06/24 09:28:27  krausa
 * Changed number of parallel worker threads to 4 for Xeon node
 *
 * Revision 1.6  2003/06/18 13:57:12  krausa
 * Changed number of parallel unit iterators to 2 to be secure
 *
 * Revision 1.5  2003/06/18 13:33:32  krausa
 * Changed number of parallel unit iterators to 4 due to the Xeon node
 *
 * Revision 1.4  2003/04/10 10:44:17  krausa
 * Introduced remote computation iterator (bugfix)
 *
 * Revision 1.3  2003/04/10 10:35:15  krausa
 * Introduced remote computation iterator
 *
 * Revision 1.2  2003/04/10 06:58:29  krausa
 * First checkin for agent based unit computing
 *
 * Revision 1.1  2003/04/01 16:33:58  krausa
 * Introduced sequential and parallel unit iterators
 *
 */

package org.utilities.execution;


/**
 * The class <tt>UnitIteratorFactory</tt> contains (static) factory methods for creating <tt>UnitIterator</tt>s..
 *
 * @author Andreas Kraus
 * @version $Id: UnitIteratorFactory.java,v 1.1.1.1 2008/08/06 08:26:26 mludwig Exp $
 */

public class UnitIteratorFactory
{

  /**
   * Create a UnitIterator for the sequential iteration of units
   */
  public static UnitIterator createSequentialUnitIterator()
  {
    return new SequentialUnitIterator();
  }

  /**
   * Create a UnitIterator for the parallel iteration of units
   */
  public static UnitIterator createParallelUnitIterator()
  {
    return new ParallelUnitIterator( Runtime.getRuntime().availableProcessors() );
  }

  /**
   * Create a UnitIterator for the parallel iteration of units
   * @param numberOfThreads number of parallel computing threads
   */
  public static UnitIterator createParallelUnitIterator(int numberOfThreads)
  {
    return new ParallelUnitIterator( numberOfThreads );
  }
}
