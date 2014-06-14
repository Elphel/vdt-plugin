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
package com.elphel.vdt.ui.tools.params;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.options.component.ComboComponent;
import com.elphel.vdt.ui.tools.LaunchConfigurationTab;


public class ListTabComponent extends AbstractTabComponent {
    public ListTabComponent( LaunchConfigurationTab tab
                           , Parameter toolParameter ) 
    {
        super(tab, new ComboComponent(toolParameter));
        System.out.println("Created ListTabComponent, label= "+toolParameter.getLabel());
    }
    
    public void createControl(Composite parent) {
        super.createControl(parent);
        System.out.println("createControl(), parent= "+parent);
        WidgetListener listener = new WidgetListener();
        getComboField().addSelectionListener(listener);
        getComboField().addModifyListener(listener);
    }
    
    private Combo getComboField() {
        return ((ComboComponent)component).getComboField();
    }
    
    public void initializeFrom(ILaunchConfiguration configuration) {
        String selection = "";
        try {
            selection = configuration.getAttribute(ATTR_NAME_VALUE, "");
        } catch (CoreException ce) {
            MessageUI.showErrorMessage(ce, Txt.s("LaunchTab.Error.ReadConfiguration"));
        }
        
        ((ComboComponent)component).setSelection(selection);
    }

    public boolean isValid(boolean newConfig) {
        return getComboField().getText().trim().length() > 0;
    }
    
} // class ListTabComponent
