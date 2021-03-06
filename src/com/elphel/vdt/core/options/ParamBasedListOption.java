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
package com.elphel.vdt.core.options;

import java.util.List;

import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.ui.MessageUI;

/**
 * Persistent option based on Context List Parameter 
 * 
 * Created: 11.04.2006
 * @author  Lvov Konstantin
 */

public class ParamBasedListOption extends ParamBasedOption {

    public ParamBasedListOption(Parameter param) {
        super(param);
    }

    /**
     * Set the option value
     * 
     * @param value the sequence of the list items separated by SEPARATOR.
     */
    public void setValue(String value) {
        List<String> list = OptionsUtils.convertStringToList(value);
        setValue(list);
    }
    
    public String getValue() {
        return null;
    }
    
    protected void setValue(List<String> list) {
        try {
            param.setCurrentValue(list);
        } catch(ToolException e) {
            MessageUI.fatalError(e.getMessage());
        }
    }

    public List<String> getValueList() {
        return param.getValue(null); // null for topFormatProcessor
    }
    
    public List<String> doLoadList() {
        List<String> list;
        String value = super.doLoad();
        if (value == null)
            list = getValueList();
        else        
            list = OptionsUtils.convertStringToList(value);
        return list;
    }

    /**
     * Load the default option value from persistent storage
     * 
     * @return the sequence of the list items separated by SEPARATOR.
     */
    public String doLoadDefault() {
        List<String> list = doLoadDefaultList();
        return OptionsUtils.convertListToString(list);
    }
    
    public List<String> doLoadDefaultList() {
        List<String> list = param.getDefaultValue(null); // null for topFormatProcessor
        doClear();
        return list;
    }

    /**
     * Save value to persistent storage
     * 
     * @param value the sequence of the list items separated by SEPARATOR.
     */
    public boolean doStore(String value) {
        List<String> list = OptionsUtils.convertStringToList(value);
        return doStore(list);
    }
    
    public boolean doStore(List<String> list) {
        setValue(list);
        String value = OptionsUtils.convertListToString(list);
        return super.doStore(value);
    }

    public boolean doStore() {
        if (param.isDefault()) {
            super.doClear();
            return true;
        } else
            return super.doStore(OptionsUtils.convertListToString(param.getValue(null))); // null for topFormatProcessor
    }

} // class ParamBasedOption
