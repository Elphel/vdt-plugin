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
package com.elphel.vdt.ui.tools.params;


import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.options.component.BrowseableField;
import com.elphel.vdt.ui.options.component.FileComponent;
import com.elphel.vdt.ui.tools.LaunchConfigurationTab;

public class FileTabComponent extends AbstractTabComponent {

    public FileTabComponent( LaunchConfigurationTab tab
                           , Parameter toolParameter ) 
    {
        super(tab, new FileComponent(toolParameter));
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        WidgetListener listener = new WidgetListener();
        getLocationField().addSelectionListener(listener);
        getLocationField().addModifyListener(listener);
    }
    
    private BrowseableField getLocationField() {
        return ((FileComponent)component).getLocationField();
    }

    public void initializeFrom(ILaunchConfiguration configuration) {
        String location= "";
        try {
            location = configuration.getAttribute(ATTR_NAME_VALUE, "");
        } catch (CoreException ce) {
            MessageUI.showErrorMessage(ce, Txt.s("LaunchTab.Error.ReadConfiguration"));
        }
        getLocationField().getBrowsedNameField().setText(location);
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        String location = getLocationField().getBrowsedNameField().getText().trim();
        setAttributes(configuration, location);
    }

    public boolean isValid(boolean newConfig) {
        String location = getLocationField().getBrowsedNameField().getText().trim();
        if (location.length() < 1) {
            if (newConfig) {
                tab.setErrorMessage(null);
                tab.setMessage(Txt.s("LaunchTab.File.Message.Info", component.getParam().getLabel()));
            } else {
                tab.setErrorMessage(Txt.s("LaunchTab.Location.Error.CannotBeEmpty", component.getParam().getLabel()));
                tab.setMessage(null);
            }
            return false;
        }
        
        File file = new File(location);
        if (!file.exists()) { // The file does not exist.
            if (!newConfig) {
                tab.setErrorMessage(Txt.s("LaunchTab.Location.Error.DoesNotExist", component.getParam().getLabel()));
            }
            return false;
        }
        if (!file.isFile()) {
            if (!newConfig) {
                tab.setErrorMessage(Txt.s("LaunchTab.Location.Message.IsNotFile", component.getParam().getLabel()));
            }
            return false;
        }
        return true;
    } // isValid()
    
} //class FileTabComponent
