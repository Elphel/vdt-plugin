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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.types.ParamTypeNumber;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.tools.LaunchConfigurationTab;

import com.elphel.vdt.ui.options.component.NumberComponent;

/**
 * This tab component allows the user to edit tool parameters with
 * type Number.
 * 
 * Created: 24.12.2005
 * @author  Lvov Konstantin
 */

public class NumberTabComponent extends AbstractTabComponent {

    public NumberTabComponent( LaunchConfigurationTab tab
                             , Parameter toolParameter ) 
    {
        super(tab, new NumberComponent(toolParameter));
    } 
        
    public void createControl(Composite parent) {
        super.createControl(parent);
        getTextField().addModifyListener(new WidgetListener());
    }
    
    private Text getTextField() {
        return ((NumberComponent)component).getTextField();
    }
    
    public void initializeFrom(ILaunchConfiguration configuration) {
        String number = "";
        try {
            number = configuration.getAttribute(ATTR_NAME_VALUE, "");
        } catch (CoreException ce) {
            MessageUI.showErrorMessage(ce, Txt.s("LaunchTab.Error.ReadConfiguration"));
        }
        getTextField().setText(number);
    } // initializeFrom()

    public boolean isValid(boolean newConfig) {
        String number = getTextField().getText().trim();
        ParamTypeNumber type = (ParamTypeNumber)((NumberComponent)component).getParam().getType();
        
        if (number.length() < 1) {
            if (newConfig) {
                tab.setErrorMessage(null);
                tab.setMessage(Txt.s("LaunchTab.Message.Info", component.getParam().getLabel()));
            } else {
                tab.setErrorMessage(Txt.s("LaunchTab.Error.CannotBeEmpty", component.getParam().getLabel()));
                tab.setMessage(null);
            }
            return false;
        }

        int value;
        try {
            value = Integer.parseInt(number);
        } catch(NumberFormatException e) {
            if (!newConfig) {
                tab.setErrorMessage(Txt.s( "LaunchTab.Number.Error.IncorrectValue"
                                   , new Object[] {component.getParam().getLabel(), type.getLo(), type.getHi()}
                                   ));
            }
            return false;
         } // try
        
        if ((type.getLo() > value) || (value > type.getHi())) {
            if (!newConfig) {
                tab.setErrorMessage(Txt.s( "LaunchTab.Number.Error.IncorrectValue",
                                           new Object[] {component.getParam().getLabel(), 
                                                         type.getLo(), 
                                                         type.getHi()}
                                         ));
            }
            return false;
        }
        return true;
    } // isValid()
        
} // class NumberTabComponent