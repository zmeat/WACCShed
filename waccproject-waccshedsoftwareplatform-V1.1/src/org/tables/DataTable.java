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
 * $Log: DataTable.java,v $
 * Revision 1.1.1.1  2008/08/06 08:26:26  mludwig
 * Imported sources
 *
 * Revision 1.1.1.1  2007/08/21 09:50:09  mludwig
 * Imported DANUBIA 2.0 core sources
 *
 * Revision 1.8.4.2  2006/06/28 11:08:59  mludwig
 * Completed BaseDataTest and fixed some bugs in basedata
 *
 * Revision 1.8.4.1  2006/06/27 16:16:12  mludwig
 * Completed base data component, but test is incomplete!
 *
 * Revision 1.8  2006/05/02 17:42:41  mludwig
 * Refactored import statements in packages simulation and wrapper: removed wildcard imports when importing from org.glowa.danube
 *
 * Revision 1.7  2004/08/04 07:42:41  krausa
 * Fixed serialVersionUID to value of first reference run
 *
 * Revision 1.6  2003/04/09 17:42:27  krausa
 * First version of dataio package imported
 *
 * Revision 1.5  2002/12/12 15:08:39  krausa
 * Changes due to testenvironment development
 *
 * Revision 1.4  2002/09/19 14:08:38  krausa
 * Added optimized export access for primitive data tables
 *
 * Revision 1.3  2002/08/06 17:57:35  krausa
 * New datatype concept and Anjas recent changes to the database
 *
 * Revision 1.2  2002/07/17 19:02:24  krausa
 * Some more changes
 *
 * Revision 1.1.1.1  2002/07/11 12:32:40  krausa
 * Imported DANUBIA 0.9 Sources
 *
 *
 */

package org.tables;

import java.io.Serializable;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.metadata.AreaMetaData;




/**
 * The class <tt>DataTable</tt> is abstract base class for all data tables.
 *
 * @author Andreas Kraus
 * @version $Id: DataTable.java,v 1.1.1.1 2008/08/06 08:26:26 mludwig Exp $
 */

public abstract class DataTable implements Serializable
{
  private static final long serialVersionUID = -7162871174939375630L;
  private Logger logger = Logger.getLogger(DataTable.class);
  protected AreaMetaData amd;
  /** DataType stored in table */
  protected Class dataType;

  protected DataTable()
  {
  }

  /**
   * Constructor
   */
  public DataTable( AreaMetaData amd, Class dataType )
  {
	this.amd = amd;
    this.dataType = dataType;
  }

  public AreaMetaData getAreaMetaData()
  {
    return amd;
  }
  
  /**
   * Query datatype of table
   */
  public Class getDataType()
  {
    return dataType;
  }

  /**
   * Create a new DataTable of the same type as this DataTable
   */
  public DataTable createDataTable( AreaMetaData newAMD )
  {
    if( newAMD == null ) return null;
    try
    {
      Constructor constructor = this.getClass().getConstructor( new Class[] { AreaMetaData.class } );
      if( constructor == null ) return null;
      DataTable newDT = (DataTable)constructor.newInstance( new Object[] { newAMD } );
      return newDT;
    }
    catch( Exception ex )
    {
      return null;
    }
  }

}
