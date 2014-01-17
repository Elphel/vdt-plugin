/*******************************************************************************
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Eclipse VDT plug-in; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/
package com.elphel.vdt.core.launching;

/**
 * Verilog launch configuration to launch via Exteranl Tool plug-in.
 * 
 * Created: 26.12.2005
 * @author  Lvov Konstantin
 */

public class ExternalToolLauncherConfiguration extends VDTRunnerConfiguration {

	private String configurationName; 
    private boolean privateMode;
	
	public ExternalToolLauncherConfiguration( String configurationName
			                                , String toolToLaunch ) {
		super(toolToLaunch);
        this.configurationName = configurationName;
        this.privateMode       = false;
	} // ExternalToolLauncherConfiguration

	public String getConfigurationName() {
		return configurationName;
	}
	
	public void setPrivateMode(boolean privateMode) {
		this.privateMode = privateMode;
	}
	
	public boolean getPrivateMode() {
		return privateMode;
	}

} // class ExternalToolLauncherConfiguration
