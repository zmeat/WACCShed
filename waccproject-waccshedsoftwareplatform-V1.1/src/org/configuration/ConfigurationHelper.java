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
 * Created on 02.01.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.metadata.TimeStep;
import org.metadata.TimeStepUnit;
import org.utilities.internal.FilenameHelper;
import org.utilities.time.SystemCalendar;

public class ConfigurationHelper implements Serializable {

	private static Map<String, TimeStepUnit> allowedUnits = new HashMap<String, TimeStepUnit>();
    static {
      // allowedUnits.put("MS", TimeStepUnit.MS);
      // allowedUnits.put("S", TimeStepUnit.S);
      allowedUnits.put("MIN", TimeStepUnit.MIN);
      allowedUnits.put("H", TimeStepUnit.H);
      allowedUnits.put("DAY", TimeStepUnit.DAY);
      allowedUnits.put("MONTH", TimeStepUnit.MONTH);
      // allowedUnits.put("YEAR", TimeStepUnit.YEAR);
    }
	
	public static Properties loadProperties(String configFileName) throws IOException {
		Properties props;
		configFileName = FilenameHelper.equalizeFilename(configFileName);
		InputStream cfgStream = new FileInputStream(configFileName);
		props = new Properties();
		props.load(cfgStream);
		cfgStream.close();
		return props;
	}

	public static SystemCalendar parseDate(String s) throws Exception {
	      // match YYYY-MM-DD with arbitrary digits and delimiter
	      Pattern datePattern = Pattern.compile("\\d{4}.{1}\\d{2}.{1}\\d{2}");
	      Matcher m = datePattern.matcher(s);
	      if (! (m.matches())) {
		        throw new Exception("Invalid date specification: " + s);
		      }
	      try {
	    	    int year = ConfigurationHelper.parseInt(s.substring(0, 4));
	    	    int month = ConfigurationHelper.parseInt(s.substring(5, 7));
	    	    int day = ConfigurationHelper.parseInt(s.substring(8));
	    	    return (new SystemCalendar(year, month, day));
	    	  }
	    	  catch (Exception ex) {
	    	    throw new Exception("Invalid date specification: " + s, ex);
	    	  }
      
		
	}
	
	public static TimeStep parseTimeStep(String step, String unit) throws Exception {
		String errorMes = "";
		int s = 0;
		try {
			s = ConfigurationHelper.parseInt(step); 
		}
		catch (Exception e) {
			errorMes = "Timestep: " + e.getMessage() + " ";
		}
		if (!allowedUnits.containsKey(unit))
			errorMes = errorMes.concat("Unknown timestep unit: " + unit);
		if (errorMes.length()==0) 
			return new TimeStep(s, allowedUnits.get(unit));
		else throw new Exception(errorMes);
	}

	public static String[] parseList(String list) {
		if (list==null || list.trim().equals("")) return new String[0]; 
		else {
			String[] s = list.split(",");
			for (int i=0; i<s.length; i++) {
				s[i] = s[i].trim();
			}
			return s;
		}
	}
	
	public static int parseInt(String intString) throws Exception {
		try {
			return Integer.parseInt(intString);
		}
		catch (NumberFormatException e) {
			throw new Exception("No valid int value: " + intString);
		}
	}
	
	public static float parseFloat(String floatString) throws Exception {
		try {
			return Float.parseFloat(floatString);
		}
		catch (NumberFormatException e) {
			throw new Exception("No valid float value: " + floatString);
		}
	}
	
	public static double parseDouble(String doubleString) throws Exception {
		try {
			return Double.parseDouble(doubleString);
		}
		catch (NumberFormatException e) {
			throw new Exception("No valid double value: " + doubleString);
		}
	}
	
	public static double[] parseDoubleList(String doublelist) {
		if (doublelist==null || doublelist.trim().equals("")) return new double[0]; 
		else {
			String[] s = doublelist.split(",");
			double[] d = new double[s.length];
			for (int i=0; i<s.length; i++) {
				d[i] = Double.parseDouble(s[i].trim());
			}
			return d;
		}
	}
	
}
