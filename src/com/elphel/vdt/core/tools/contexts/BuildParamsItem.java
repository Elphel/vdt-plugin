/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *******************************************************************************/
package com.elphel.vdt.core.tools.contexts;

import java.util.List;

public class BuildParamsItem implements Cloneable{
	private String [] params;
	private String consoleName; // null for external tools running in a new console
	public BuildParamsItem (
			String [] params,
			String consoleName) {
		this.consoleName=consoleName;
		this.params=params; // no need to clone?
	}
	public BuildParamsItem (BuildParamsItem item){
		this (
				item.params,
				item.consoleName);
	}

	public BuildParamsItem clone () {
		return new BuildParamsItem(this);
	}
	
	public String [] getParams(){
		return params;
	}
	public List<String> getParamsAsList(){
	    List<String> arguments = new java.util.ArrayList<String>(params.length);
	    for(int i = 0; i < params.length; i++) {
	        arguments.add(params[i]);
	    }
	    return arguments;
	}
	/*            
    String[] paramArray = tool.buildParams();
	System.out.println("Andrey: called tool.buildParams() here (from VDTLaunchUtils.java");
    List<String> arguments = new ArrayList<String>(paramArray.length);
    for(int i = 0; i < paramArray.length; i++) {
        arguments.add(paramArray[i]);
    }
    return arguments;
*/

	public String getConsoleName(){
		return consoleName;
	}
	

}
