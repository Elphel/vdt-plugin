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
package com.elphel.vdt.ui.launching;


import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import com.elphel.vdt.VDT;
import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.ui.tools.ToolUI;

/**
 * Common engine for verilog launch configuration tab groups.
 * 
 * Created: 30.12.2005
 * @author  Lvov Konstantin
 */

public abstract class AbstractToolUITabGroup extends AbstractLaunchConfigurationTabGroup {

    private ToolUI toolUI;

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        super.setDefaults(configuration);
        try {
            LaunchCore.updateLaunchConfiguration(configuration, toolUI.getTool());
        } catch (Exception e) {}
        LaunchCore.setResource(configuration, VDT.VARIABLE_RESOURCE_NAME);
    }

    protected abstract ToolUI getToolUI();
    
    /**
     * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog, java.lang.String)
     */
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        toolUI = getToolUI();
        ILaunchConfigurationTab[] tabs = toolUI.getLaunchConfigurationTabs();
        setTabs(tabs);
        
//        ILaunchConfigurationTab[] toolTabs = toolUI.getLaunchConfigurationTabs();
//        ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[toolTabs.length+3];
//        for (int i=0; i< toolTabs.length; i++)
//              tabs[i] = toolTabs[i];
//        tabs[toolTabs.length]   = new RefreshTab();       
//        tabs[toolTabs.length+1] = new EnvironmentTab();       
//        tabs[toolTabs.length+2] = new CommonTab();       
//        setTabs(tabs);
    }

} // class MyToolTabGroup
