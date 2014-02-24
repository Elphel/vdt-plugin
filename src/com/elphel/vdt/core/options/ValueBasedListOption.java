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
