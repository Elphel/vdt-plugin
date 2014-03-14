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
package com.elphel.vdt.core.options;

import java.util.*;

import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.core.tools.params.types.ParamType;
import com.elphel.vdt.ui.MessageUI;

/**
 * Persistent option based on Context Parameter 
 * 
 * Created: 11.04.2006
 * @author  Lvov Konstantin
 */

public class ParamBasedOption extends Option {

    protected Parameter param;
    
    public ParamBasedOption(Parameter param) {
        super(OptionsUtils.toKey(param), param.getContext().getName());
        this.param = param;
    }

    public void setValue(String value) {
        try {
            param.setCurrentValue(value);
        } catch(ToolException e) {
            MessageUI.error(e);
        }
    }
    
    public String getValue() {
        return param.getValue(null).get(0); // null for topFormatProcessor
    }
    
    public Parameter getParam() {
        return param;
    }

    public ParamType getType() {
        return param.getType();
    }
    
    public String getLabel() {
        return param.getLabel();
    }

    public boolean isDefault() {
        return param.isDefault();
    }

    public void setToDefault() {
        param.setToDefault();
    }
    
    public String doLoadDefault() {
        String value = param.getDefaultValue(true, null).get(0); // Andrey: ignore faults in TopModuleName generator // null for topFormatProcessor
        doClear();
        return value;
    }

    public String doLoadDefault(boolean menuMode) {
        String value = param.getDefaultValue(menuMode,null).get(0);
        doClear();
        return value;
    }

    public String doLoad() {
        String value = super.doLoad();
        if (value == null)
            value = getValue();
        return value;        
    }
    
    public boolean doStore(String value) {
        setValue(value);
        return super.doStore(value);
    }
    
    public boolean doStore() {
        if (isDefault()) {
            super.doClear();
            return true;
        } else {
            List<String> currentValue = param.getCurrentValue();
            String currentValueElem = null;
            
            if(currentValue != null)
                currentValueElem = currentValue.get(0);
            
            return super.doStore(currentValueElem);
        }
    }
    
} // class ParamBasedOption
