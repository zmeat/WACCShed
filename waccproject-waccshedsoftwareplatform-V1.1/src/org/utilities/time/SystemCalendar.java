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
 * Change: 2016/02/15 Diego
 * Changed format of CalendarToString to comply with HSQL
 * 
 * $Log: DanubiaCalendar.java,v $
 * Revision 1.1.1.1  2008/08/06 08:26:26  mludwig
 * Imported sources
 *
 * Revision 1.1.1.1.2.1  2008/02/04 19:38:44  mludwig
 * *** empty log message ***
 *
 * Revision 1.1.1.1  2007/08/21 09:50:09  mludwig
 * Imported DANUBIA 2.0 core sources
 *
 * Revision 1.7  2003/10/24 11:15:30  krausa
 * Access to DateFormat now synchronized
 *
 * Revision 1.6  2003/04/07 10:19:22  krausa
 * Bug in getHour() implemetation, now returning hour of day
 *
 * Revision 1.5  2003/04/05 11:33:00  krausa
 * Corrected
 *
 * Revision 1.4  2003/04/05 11:25:47  krausa
 * Now really static initializer
 *
 * Revision 1.3  2003/04/02 14:36:02  krausa
 * Update
 *
 * Revision 1.2  2003/04/01 15:23:57  krausa
 * Refined DanubiaCalendar usage
 *
 * Revision 1.1  2003/03/11 16:06:06  krausa
 * Changes due to Roberts new timecontroller implementation
 *
 *
 */

package org.utilities.time;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.metadata.TimeStep;
import org.metadata.TimeStepUnit;


/**
 * The class <code>DanubiaCalendar</code> is the calendar to be used within DANUBIA by the system and all components.
 * It ensures the use of a global timezone without daylight saving time.
 * The global timezone defaults to the CET timezone.
 *
 * @author Andreas Kraus
 * @version $Id: DanubiaCalendar.java,v 1.1.1.1 2008/08/06 08:26:26 mludwig Exp $
 */

public class SystemCalendar extends GregorianCalendar
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6709090948528723777L;

	//private static final long serialVersionUID = -2049002621660559448L;
	/** Logging instance */
	//private static DanubiaLogger logger = DanubiaLogger.getDanubiaLogger( DanubiaCalendar.class );

	private static TimeZone tz;
	private static DateFormat df = DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.SHORT, Locale.US );

	static
	{
		// static initializer for the timezone
		String tzId = System.getProperty( "DanubiaCalendar.timezone" );
		if( tzId == null )
		{
			tzId = "GMT";
			//logger.warn( "Timezone not explicitely defined in property DanubiaCalendar.timezone" );
		}
		tz = TimeZone.getTimeZone( tzId );
		//logger.info( "Using timezone " + tz.getID() + " (" + tz.getDisplayName() + ")" );
		//if( tz.useDaylightTime() ) logger.fatal( "Timezone " + tz.getID() + " (" + tz.getDisplayName() + ")" + " uses daylight time" );
		df.setTimeZone( tz );
	}

	/**
	 * Overridden constructor from GreogorianCalendar
	 * @see java.util.GregorianCalendar
	 */
	public SystemCalendar()
	{
		super();
		setTimeZone( tz );
	}

	/**
	 * Constructor for initialization with milliseconds
	 */
	public SystemCalendar( long time )
	{
		super();
		setTimeZone( tz );
		setTimeInMillis( time );
	}

	/**
	 * Overridden constructor from GreogorianCalendar
	 * @see java.util.GregorianCalendar
	 * @param year year
	 * @param month month (starting with 1)
	 * @param day day (starting with 1)
	 */
	public SystemCalendar( int year, int month, int day )
	{
		super();
		setTimeZone( tz );
		clear();
		set( year, month-1, day );
	}

	/**
	 * Overridden constructor from GreogorianCalendar
	 * @see java.util.GregorianCalendar
	 * @param year year
	 * @param month month (starting with 1)
	 * @param day day (starting with 1)
	 * @param hour hour
	 * @param minute minute
	 */
	public SystemCalendar( int year, int month, int day, int hour, int minute )
	{
		super();
		setTimeZone( tz );
		clear();
		set( year, month-1, day, hour, minute );
	}

	/**
	 * Overridden constructor from GreogorianCalendar
	 * @see java.util.GregorianCalendar
	 * @param year year
	 * @param month month (starting with 1)
	 * @param day day (starting with 1)
	 * @param hour hour
	 * @param minute minute
	 * @param second second
	 */
	public SystemCalendar( int year, int month, int day, int hour, int minute, int second )
	{
		super();
		setTimeZone( tz );
		clear();
		set( year, month-1, day, hour, minute, second );
	}

	/**
	 * Overridden constructor from GreogorianCalendar.
	 * This constructor is FORBIDDEN because it would violate the global timezone constraint.
	 * @see java.util.GregorianCalendar
	 */
	public SystemCalendar( Locale aLocale )
	{
		//logger.fatal( "Forbidden constructor called" );
		throw new Error( "Forbidden constructor called in " + getClass().getName() );
	}

	/**
	 * Overridden constructor from GreogorianCalendar.
	 * This constructor is FORBIDDEN because it would violate the global timezone constraint.
	 * @see java.util.GregorianCalendar
	 */
	public SystemCalendar( TimeZone zone )
	{
		//logger.fatal( "Forbidden constructor called" );
		throw new Error( "Forbidden constructor called in " + getClass().getName() );
	}

	/**
	 * Overridden constructor from GreogorianCalendar.
	 * This constructor is FORBIDDEN because it would violate the global timezone constraint.
	 * @see java.util.GregorianCalendar
	 */
	public SystemCalendar( TimeZone zone, Locale aLocale )
	{
		//logger.fatal( "Forbidden constructor called" );
		throw new Error( "Forbidden constructor called in " + getClass().getName() );
	}

	/**
	 * Get global timezone
	 */
	public static TimeZone getGlobalTimeZone()
	{
		return tz;
	}

	/**
	 * Get time in milliseconds
	 */
	@Override
	public long getTimeInMillis()
	{
		return super.getTimeInMillis();
	}

	/**
	 * Set time in milliseconds
	 */
	@Override
	public void setTimeInMillis( long time )
	{
		super.setTimeInMillis( time );
	}

	/**
	 * Get string representation in global timezone
	 */
	@Override
	public String toString()
	{
		synchronized( df )
		{
			return df.format( getTime() );
		}
	}

	/**
	 * Get year
	 */
	public int getYear()
	{
		return get( YEAR );
	}

	/**
	 * Get month (1..12)
	 */
	public int getMonth()
	{
		return get( MONTH ) + 1;
	}

	/**
	 * Get day (1..31)
	 */
	public int getDay()
	{
		return get( DAY_OF_MONTH );
	}

	/**
	 * Get day of year
	 */
	public int getDayOfYear()
	{
		return get( DAY_OF_YEAR );
	}

	/**
	 * Get hour (of day)
	 */
	public int getHour()
	{
		return get( HOUR_OF_DAY );
	}

	/**
	 * Get minute
	 */
	public int getMinute()
	{
		return get( MINUTE );
	}
	
	/**
	 * Get minute
	 */
	public int getSecond()
	{
		return get( SECOND );
	}
	
	/**
	 * Returns a DanubiaCalendar object that is increased by the time step
	 * given by the parameter t. The current DanubiaCalender object remains unchanged.
	 * 
	 * @param t The time step to increase the DanubiaCalendar
	 * @return a new DanubiaCalendar object that is increased to this DanubiaCalender 
	 * by the given time step
	 */
	public SystemCalendar getNextSystemCalendar(TimeStep t) {
		SystemCalendar dc = (SystemCalendar) this.clone();
		int ts = t.getValue();
		TimeStepUnit tu = t.getUnit();
		switch( tu )
		{
		/*
	      case MS:
	        int days = (int)( ts / (24*3600*1000) );
	        int ms = (int)( ts % (24*3600*1000) );
	        dc.add( Calendar.MILLISECOND, ms );
	        dc.add( Calendar.DAY_OF_YEAR, days );
	        break;

	      case S:
	        dc.add( Calendar.SECOND, (int)ts );
	        break; */

		case MIN:
			dc.add( Calendar.MINUTE, ts );
			break;

		case H:
			dc.add( Calendar.HOUR_OF_DAY, ts );
			break;

		case DAY:
			dc.add( Calendar.DAY_OF_YEAR, ts );
			break;

		case MONTH:
			dc.add( Calendar.MONTH, ts );
			break;

			/*
	      case YEAR:
	        dc.add( Calendar.YEAR, (int)ts );
	        break; */
		}
		return dc;
	}
	
	public String CalendarToString(){
		int year = this.getYear();
		int month = this.getMonth();
		int day = this.getDay();
		int hour = this.getHour();
		String smonth = String.valueOf(month).length()>1 ? String.valueOf(month):"0"+String.valueOf(month);
		String sday = String.valueOf(day).length()>1 ? String.valueOf(day):"0"+String.valueOf(day);
		String shour = String.valueOf(hour).length()>1 ? String.valueOf(hour):"0"+String.valueOf(hour);
		
		String timeIndex = String.valueOf(year)+"-"+smonth+"-"+sday+" "+shour+":00:00";
		
		return timeIndex;
	}
	
	/**
	 * Similar to CalendarToString, but includes minutes and seconds
	 * @return String formatted as 'yyyy-mm-dd hh:mm:ss'
	 */
	public String CalendarToTimestamp(){
		int year = this.getYear();
		int month = this.getMonth();
		int day = this.getDay();
		int hour = this.getHour();
		int min = this.getMinute();
		int sec = this.getSecond();
		
		String smonth = String.valueOf(month).length()>1 ? String.valueOf(month):"0"+String.valueOf(month);
		String sday = String.valueOf(day).length()>1 ? String.valueOf(day):"0"+String.valueOf(day);
		String shour = String.valueOf(hour).length()>1 ? String.valueOf(hour):"0"+String.valueOf(hour);
		String smin = String.valueOf(min).length()>1 ? String.valueOf(min):"0"+String.valueOf(min);
		String ssec = String.valueOf(sec).length()>1 ? String.valueOf(sec):"0"+String.valueOf(sec);
		
		String timeIndex = String.valueOf(year)+"-"+smonth+"-"+sday+" "+shour+":"+smin+":"+ssec;
		
		return timeIndex;
	}
	
	public static String GetFormattedInterval(final long ms) {
	    long x = ms / 1000;
	    long seconds = x % 60;
	    x /= 60;
	    long minutes = x % 60;
	    x /= 60;
	    long hours = x % 24;

	    return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}

}
