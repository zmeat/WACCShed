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
package org.metadata;
import java.io.Serializable;
import java.util.Arrays;
/**
 * The class <code>AreaMetaData</code> is used to describe a simulation area.
 * A simulation area is defined relative to a simualation site which is described
 * by a {@link SiteMetaData} object.
 * 
 * @author Matthias Ludwig
 * @version $Id: AreaMetaData.java,v 1.1.1.1 2008/08/06 08:26:24 mludwig Exp $
 *
 */
public class AreaMetaData implements Serializable {
	/** reference to corresponding <code>SiteMetaData</code> */
	/** Identifier */
	private String id;
	/** Description */
	private String description;

	/** Number of rows */
//	private int nRows;
	/** Number of columns */
//	private int nCols;
	/** Column of upper left corner */
//	private int ulCol;
	/** Row of upper left corner */
//	private int ulRow;
	
	private int numOfsubBasin;
	private String[] subBasins;
	
	//area of the basins
	private double[] basinAreas;
	
	/**
	 * Creates an <code>AreaMetaData</code> object from the given parameters.
	 * @param siteMetaData
	 * @param id
	 * @param description
	 * @param nCols
	 * @param nRows
	 * @param ulCol
	 * @param ulRow
	 */
	public AreaMetaData( String id, String description, int numOfsubBasin,String[] subBasins,double[] basinAreas) {
		this.id = id;
		this.description = description;
		this.numOfsubBasin = numOfsubBasin;
		this.subBasins = subBasins;
		this.basinAreas = basinAreas;
	}
	/**
	 * Creates an <code>AreaMetaData</code> object describing the whole simulation site.
	 * The simulation site is given by the parameter <code>siteMetaData</code>.
	 * @param siteMetaData
	 * @param id
	 * @param description
	 */
	public AreaMetaData(String id, String description) {
		this.id = id;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	public String getId() {
		return id;
	}
	
	public int getNumOfsubBasin() {
		return numOfsubBasin;
	}
	
	public String[] getSubBasins() {
		return subBasins;
	}
	
	public double[] getBasinAreas() {
		return basinAreas;
	}
	/**
	 * Translate column and row to unit id.
	 * @param c column
	 * @param r row
	 * @return the unit id of the given column and row
	 */

	/**
	 * Checks whether this <code>AreaMetaData</code> object is equal to the given one. 
	 * Two <code>AreaMetaData</code> objects are equal if their attributes
	 * except <code>description</code> are equal.   
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof AreaMetaData)) {
			return false;
		}
		AreaMetaData compare = (AreaMetaData) o;
		return (this.id.equals(compare.getId())
				&& this.numOfsubBasin == compare.getNumOfsubBasin() && Arrays.equals(subBasins, compare.getSubBasins()));
	}
	/**
	 * Checks wether this <code>AreaMetaData</code> object contains or is equal to the given
	 * one.
	 * 
	 * @param other the <code>AreaMetaData</code> object to be tested
	 * @return true, if <code>this<code> contains or is equal to <code>other</code>
	 */
	public boolean contains(AreaMetaData other) {
		// check first, if other is not null
		if (other == null)
			return false;
		// check, if areas are equal
		if (equals(other))
			return true;
		// check if SiteMetaData match
		// check if area described by other is contained in area described by
		// this
		if(this.getNumOfsubBasin()< other.getNumOfsubBasin())
			return false;
		
		return (Arrays.asList(this.getSubBasins()).containsAll(Arrays.asList(other.getSubBasins())));
	}
	

	
	
	/**
	 * Get index for unit id arranged in a 1d array.
	 * 
	 * @return array index, -1 if <code>pid</code> not inside area
	 */
	public int getArrayIndexByPID(String pid) {
		return Arrays.asList(this.getSubBasins()).indexOf(pid);
	}
	
	public String getPIDByArrayIndex(int Arrayindex){
		return this.getSubBasins()[Arrayindex];
	}
	
	
	/**
	 * Return string representation of object.
	 */
	@Override
	public String toString() {
		return "AreaMetaData[id="
				+ getId() + ",descr="
				+ getDescription() + ", numOfBasin=" + getNumOfsubBasin() + ", numOfBasin=" + getNumOfsubBasin() + ", subBasins={"+Arrays.toString(this.getSubBasins())+"} ]"+", basinAreas={"+Arrays.toString(this.getBasinAreas())+"} ]";
	}
	
	
	/**
	 * Query if PID inside area.
	 */
	public boolean isPIDInside(String pid) {
		return Arrays.asList(this.getSubBasins()).contains(pid);
	}
	
	/**
	 * Get array of unit ids of this area.
	 * This query returns an array that contains the ids of all units
	 * in the (rectangular) area described by this <code>AreaMetaData</code> object.
	 * Note that this array might contain unit ids which are not within the 
	 * simulation area!  
	 */
	public String[] getPIDArray() {

		return getSubBasins();
	}

}
