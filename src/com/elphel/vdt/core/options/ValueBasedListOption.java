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

/**
 * Persistent option based on list of string value 
 * 
 * Created: 11.04.2006
 * @author  Lvov Konstantin
 */

public class ValueBasedListOption extends Option {

    private List<String> currentValue;
    private List<String> defaultValue;

    public ValueBasedListOption(final String key, final List<String> defaultValue) {
        this(key, null, defaultValue);
    }

    public ValueBasedListOption( final String key
                               , final String contextID
                               , final List<String> defaultValue ) {
        super(key, contextID);
        this.currentValue = null;
        this.defaultValue = defaultValue;
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
    
    protected void setValue(List<String> list) {
        currentValue = list;
    }

    public List<String> getValueList() {
        if (isDefault())
            return defaultValue;
        else
            return currentValue;
    }

    public void setToDefault() {
        currentValue = null;
    }
    
    public boolean isDefault() {
        return currentValue == null;
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
     * @return the sequnce of the list items separated by SEPARATOR.
     */
    public String doLoadDefault() {
        List<String> list = doLoadDefaultList();
        return OptionsUtils.convertListToString(list);
    }
    
    public List<String> doLoadDefaultList() {
        List<String> list = defaultValue;
        doClear();
        return list;
    }

    /**
     * Save value to persistent storage
     * 
     * @param value the sequnce of the list items separated by SEPARATOR.
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
        if (isDefault()) {
            super.doClear();
            return true;
        } else
            return super.doStore(OptionsUtils.convertListToString(currentValue));
    }
    
} // class ParamBasedOption
