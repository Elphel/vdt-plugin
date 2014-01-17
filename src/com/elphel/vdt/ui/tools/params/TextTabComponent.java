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


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.options.component.TextComponent;
import com.elphel.vdt.ui.tools.LaunchConfigurationTab;

public class TextTabComponent extends AbstractTabComponent {

    public TextTabComponent( LaunchConfigurationTab tab
                           , Parameter toolParameter ) 
    {
        super(tab, new TextComponent(toolParameter));
    }
    
    public void createControl(Composite parent) {
        super.createControl(parent);
        getTextField().addModifyListener(new WidgetListener());
    }
    
    private Text getTextField() {
        return ((TextComponent)component).getTextField();
    }
    
    public void initializeFrom(ILaunchConfiguration configuration) {
        String text = "";
        try {
            text = configuration.getAttribute(ATTR_NAME_VALUE, "");
        } catch (CoreException ce) {
            MessageUI.showErrorMessage(ce, Txt.s("LaunchTab.Error.ReadConfiguration"));
        }
        getTextField().setText(text);
    }

    public boolean isValid(boolean newConfig) {
        ParamTypeString type = (ParamTypeString)((TextComponent)component).getParam().getType();

        return getTextField().getText().trim().length() <= type.getMaxLength();
    }
    
} // class TextTabComponent
