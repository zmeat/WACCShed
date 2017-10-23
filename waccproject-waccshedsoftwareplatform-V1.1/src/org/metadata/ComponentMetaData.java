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
import java.util.Properties;

public class ComponentMetaData implements Serializable {
	private String id;
	private String description;
	private String version;
	private String author;
	private String compClass;
	private String unitClass;

	private String[] impInterfaces;
	private String[] expInterfaces;

	private int subcycleCount;
	private int subcycleOffset;
	private TimeStep timeStep;
	
	private Properties componentProperties;

	public ComponentMetaData(String id, String description, String version,
			String author, String compClass,
			String unitClass,
			String[] impInterfaces, String[] expInterfaces,
			 int subcycleCount, int subcycleOffset,
			 TimeStep timeStep,
			Properties componentProperties) {
		this.id = id;
		this.description = description;
		this.version = version;
		this.author = author;
		this.compClass = compClass;
		this.unitClass = unitClass;

		this.impInterfaces = impInterfaces;
		this.expInterfaces = expInterfaces;

		this.subcycleCount = subcycleCount;
		this.subcycleOffset = subcycleOffset;

		this.timeStep = timeStep;
		this.componentProperties = componentProperties;

	}

	public String getAuthor() {
		return author;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getVersion() {
		return version;
	}

	public String getCompClass() {
		return compClass;
	}
	


	public String[] getExpInterfaces() {
		return expInterfaces;
	}

	public String getExpInterfacesString() {
		if (expInterfaces == null)
			return "";
		String ret = "";
		for (int i = 0; i < expInterfaces.length; i++) {
			if (i > 0)
				ret += ", ";
			ret += expInterfaces[i];
		}
		return ret;
	}

	public String[] getImpInterfaces() {
		return impInterfaces;
	}

	public String getImpInterfacesString() {
		if (impInterfaces == null)
			return "";
		String ret = "";
		for (int i = 0; i < impInterfaces.length; i++) {
			if (i > 0)
				ret += ", ";
			ret += impInterfaces[i];
		}
		return ret;
	}



	public String getUnitClass() {
		return unitClass;
	}


	public int getSubcycleCount() {
		return subcycleCount;
	}

	public int getSubcycleOffset() {
		return subcycleOffset;
	}

	public TimeStep getTimeStep() {
		return timeStep;
	}

	public String getTimeStepString() {
		if (timeStep == null)
			return "";
		else
			return timeStep.toString();
	}

	public Properties getComponentProperties() {
		return componentProperties;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "[id=" + getId()
				+ ", description=" + getDescription() + ", version="
				+ getVersion() + ", author=" + getAuthor() + ", compClass=" + getCompClass()
				+ ", unitClass=" + getUnitClass()  + ", impInterfaces="
				+ Arrays.asList(getImpInterfaces()) + ", expInterfaces="
				+ Arrays.asList(getExpInterfaces()) +", subcycleCount="
				+ getSubcycleCount() + ", subcycleOffset="
				+ getSubcycleOffset() +  ", timeStep="
				+ getTimeStepString() + "]";
	}



	public boolean equalsId(ComponentMetaData other) {
		if (other!=null) 
			return this.getId().equals(other.getId());
		else return false;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (o == this) {
			return true;
		}
		if (!(o instanceof ComponentMetaData)) {
			return false;
		}
		ComponentMetaData compare = (ComponentMetaData) o;
		return (this.getId().equals(compare.getId()));
	}

}
