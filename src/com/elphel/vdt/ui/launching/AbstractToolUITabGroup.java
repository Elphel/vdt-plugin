/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of VDT plug-in.
 * VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining it
 * with Eclipse or Eclipse plugins (or a modified version of those libraries),
 * containing parts covered by the terms of EPL/CPL, the licensors of this
 * Program grant you additional permission to convey the resulting work.
 * {Corresponding Source for a non-source form of such a combination shall
 * include the source code for the parts of Eclipse or Eclipse plugins used
 * as well as that of the covered work.}
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
