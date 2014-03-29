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
