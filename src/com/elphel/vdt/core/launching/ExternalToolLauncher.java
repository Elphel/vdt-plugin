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

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;

/**
 * Launching Verilog development tools programmatically via
 * Exteranl Tool plug-in.
 * 
 * Created: 26.12.2005
 * @author  Lvov Konstantin
 */

public class ExternalToolLauncher {

	public static void launch( final ExternalToolLauncherConfiguration config
                             ) throws CoreException 
    {
        // get external tools launch configuration
        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();   
        ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);

        // set external tools launch configuration
        ILaunchConfigurationWorkingCopy workingCopy = type.newInstance(null, config.getConfigurationName());

        workingCopy.setAttribute(IExternalToolConstants.ATTR_LOCATION, config.getToolToLaunch());
        workingCopy.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, config.getWorkingDirectory());

        String toolArguments = "";
        String[] argumentList = config.getToolArguments();
        for (int i=0; i < argumentList.length; i++) {
        	toolArguments += argumentList[i] + " ";
        }
        workingCopy.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, toolArguments);
        
        workingCopy.setAttribute(IDebugUIConstants.ATTR_PRIVATE, config.getPrivateMode());
        workingCopy.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, true);
        
        System.out.println( "*** Launching tool through vdt/core/launching/ExternalToolLauncher.java ****\n"+
        		"Tool ("+config.getToolToLaunch()+") arguments are: "+toolArguments);
        

        // launch
        ILaunchConfiguration extToolConfig = workingCopy.doSave();
        DebugUITools.launch(extToolConfig, ILaunchManager.RUN_MODE);
    } // launch
	
	
} // class ExternalToolLauncher
