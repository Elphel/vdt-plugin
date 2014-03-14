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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.options.component.BoolComponent;
import com.elphel.vdt.ui.tools.LaunchConfigurationTab;

public class BoolTabComponent extends AbstractTabComponent {

    public BoolTabComponent( LaunchConfigurationTab tab
                           , Parameter toolParameter ) 
    {
        super(tab, new BoolComponent(toolParameter));
    }
        
    public void createControl(Composite parent) {
        super.createControl(parent);
        getCheckboxField().addSelectionListener( new WidgetListener() );
    }
    
    private Button getCheckboxField() {
        return ((BoolComponent)component).getCheckboxField();
    }
    
    public void initializeFrom(ILaunchConfiguration configuration) {
        String selection = "";
        try {
            selection = configuration.getAttribute(ATTR_NAME_VALUE, "");
        } catch (CoreException ce) {
            MessageUI.showErrorMessage(ce, Txt.s("LaunchTab.Error.ReadConfiguration"));
        }
        
        ((BoolComponent)component).setSelection(selection);
    }

    public boolean isValid(boolean newConfig) {
        return true;
    }
    
} // class BoolTabComponent
