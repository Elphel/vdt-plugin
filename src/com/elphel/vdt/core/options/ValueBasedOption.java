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

/**
 * Persistent option based on string value 
 * 
 * Created: 20.04.2006
 * @author  Lvov Konstantin
 */

public class ValueBasedOption extends Option {

    private String currentValue;
    private String defaultValue;
    
    public ValueBasedOption(String key) {
        this(key, null);
    }

    public ValueBasedOption(String key, String defaultValue) {
        super(key, null);
        this.currentValue = null;
        this.defaultValue = defaultValue;
    }

    public void setValue(String value) {
        currentValue = value;
    }
    
    public String getValue() {
        if (isDefault())
            return defaultValue;
        else
            return currentValue;
    }

    public boolean isDefault() {
        return currentValue == null;
    }

    
    public void setToDefault() {
        currentValue = null;
    }
    
    public String doLoadDefault() {
        String value = defaultValue;
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
        } else
            return super.doStore(currentValue);
    }

} // class ValueBasedOption
