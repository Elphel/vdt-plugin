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

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.core.launching.VDTLaunchUtil;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.ui.options.component.Component;
import com.elphel.vdt.ui.tools.LaunchConfigurationTab;
import com.elphel.vdt.ui.MessageUI;

/**
 * Common function for VDT tab components.
 * 
 * Created: 27.12.2005
 * @author  Lvov Konstantin
 */

public abstract class AbstractTabComponent implements ITabComponent {
        
    protected LaunchConfigurationTab tab;
    protected Component component;

    // Attribute name for command line representations of tool parameter.
    protected String ATTR_NAME_LAUNCH; 
    // Attribute name for value of tool parameter. This attribute is used 
    // to show parameter value in tab group.
    protected String ATTR_NAME_VALUE;
    
    public AbstractTabComponent(LaunchConfigurationTab tab, Component component) {
        this.tab = tab;
        this.component = component;
        
        createAttributeNames(component.getParam());
    }

    public void createControl(Composite parent) {
        component.createControl(parent);
    }
    
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        setAttributes(configuration, component.performApply());
    }
    
    protected void createAttributeNames(Parameter toolParameter) {
    	ATTR_NAME_LAUNCH = VDTLaunchUtil.getLaunchAttributeName(toolParameter);
    	ATTR_NAME_VALUE  = LaunchCore.getValueAttributeName(toolParameter);
    } // createAttributeName

    public String getAttributeName() {
        return ATTR_NAME_LAUNCH;
    } // getAttributeName()
    
    public Parameter getToolParameter() {
        return component.getParam();
    }
    
    /**
     * Initializes the given launch configuration with default values for this 
     * component.
     */ 
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    	setAttributes(configuration, component.getParam().getDefaultValue(null).get(0)); // null for topFormatProcessor
    }
    
    /**
     * Set value as launch configuration attributes.
     */ 
    protected void setAttributes( ILaunchConfigurationWorkingCopy configuration
                                , String value ) 
    {
        try {
            component.getParam().setCurrentValue(value);
        } catch(ToolException e) {
            MessageUI.error(e);
        }
        
        configuration.setAttribute(ATTR_NAME_VALUE, value);
    }
    
    /**
     * A listener to update for text modification and widget selection.
     */
    protected class WidgetListener extends SelectionAdapter implements ModifyListener {

        public void modifyText(ModifyEvent e) {
            tab.textModifyedNotification();
        } // modifyText()

        public void widgetSelected(SelectionEvent e) {
            tab.dirtyNotification();
        } // widgetSelected()

    } // class WidgetListener
        
} // class AbstractTabComponent
