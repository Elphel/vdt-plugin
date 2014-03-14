/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
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
package com.elphel.vdt.ui.tools.params;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;

/**
 * Tab Component is user interface (set of widget and logic)
 * to one tool parameter.
 * 
 * Created: 27.12.2005
 * @author  Lvov Konstantin
 */

public interface ITabComponent {

	/**
	 * Creates the top level control for this tab component under the 
	 * given parent composite.  This method is called once on  tab creation.
	 * 
	 * @param parent the parent composite
	 */
	public void createControl(Composite parent);
	
    /**
     * Initializes the given launch configuration with default values for this 
     * component.
     */ 
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration);

    /**
     * Updates the component's widgets to match the state of the given launch 
     * configuration.
     */
    public void initializeFrom(ILaunchConfiguration configuration);

    /**
     * Copies values from this component into the given launch configuration.
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration);
    
    /**
     * Validates the content of the component field.
     */
    public boolean isValid(boolean newConfig);
    
    /**
     * Returns the name of launch configuration attribute corresponding 
     * to tool parameter.
     */
    public String getAttributeName();
        
} // interface ITabComponent
