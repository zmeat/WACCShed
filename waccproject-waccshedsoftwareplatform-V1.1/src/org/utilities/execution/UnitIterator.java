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
 * $Log: UnitIterator.java,v $
 * Revision 1.1.1.1  2008/08/06 08:26:26  mludwig
 * Imported sources
 *
 * Revision 1.1.1.1.2.1  2007/10/14 21:46:13  mludwig
 * *** empty log message ***
 *
 * Revision 1.1.1.1  2007/08/21 09:50:09  mludwig
 * Imported DANUBIA 2.0 core sources
 *
 * Revision 1.4  2006/05/02 17:42:40  mludwig
 * Refactored import statements in packages simulation and wrapper: removed wildcard imports when importing from org.glowa.danube
 *
 * Revision 1.3  2003/04/02 13:37:40  krausa
 * Changed signature for unit iterator computeUnit()
 *
 * Revision 1.2  2003/04/02 12:04:09  krausa
 * Changed signature for unit iterator from compute() to computeUnit()
 *
 * Revision 1.1  2003/04/01 16:33:58  krausa
 * Introduced sequential and parallel unit iterators
 *
 */
package org.utilities.execution;
import org.simulation.model.unit.UnitTable;
import org.utilities.time.SystemCalendar;
/**
 * The interface <tt>UnitIterator</tt> is implemented by objects obtained from the <tt>UnitIteratorFactory</tt>
 * methods and is used to iterate all unit of a unit table, i.e. calling the compute() method.
 *
 * @author Andreas Kraus
 * @version $Id: UnitIterator.java,v 1.1.1.1 2008/08/06 08:26:26 mludwig Exp $
 */
public interface UnitIterator
{
  /**
   * Call the compute method off all unit "inside" a unit table
   * @param ptbl UnitTable
   * @param actTime Actual time
   * @param data Object with custom data
   */
  public void compute( UnitTable ptbl, SystemCalendar actTime, Object data );
}
