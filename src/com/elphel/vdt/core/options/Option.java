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

import org.eclipse.jface.preference.IPreferenceStore;

/**
 * An abstract persistent option
 * 
 * Created: 11.04.2006
 * @author  Lvov Konstantin
 */

public abstract class Option {

    private String key;
    private String contextID;
    
    private IPreferenceStore store = null;
    
    public Option(final String key, final String contextID) {
        this.key = key;
        this.contextID = contextID;
    }
    
    /**
     * Return core option (option without UI elements)
     */
    public Option getOption() {
        return this;
    }

    /**
     * Sets the preference store which is used to store option.
     */
    public void setPreferenceStore(IPreferenceStore store) {
        this.store = store;
    }
    
    /**
     * Set the option value
     */
    public abstract void setValue(String value);

    /**
     * Sets the current value of the option back to its default value.
     */
    public abstract void setToDefault();
    
    /**
     * Returns whether the current value of the option has the default value.
     */
    public abstract boolean isDefault();

    /**
     * Returns whether the option value from persistent storage has the default value.
     */
    public boolean isStoredDefault() {
        if (store == null)
            return isDefault();

        return  ! store.contains(key);
    }

    /**
     * Load the option value from persistent storage
     */
    public String doLoad() {
        String value;
        if (store == null) {
            value = null;
        } else if (store.contains(key)) {
            value = store.getString(key);
            setValue(value);
        } else {
            value = doLoadDefault();
        }
        return value;    
    }
    
    /**
     * Load the default option value from persistent storage
     */
    public abstract String doLoadDefault();
//    public String doLoadDefault() {
//        if (store == null)
//            return null;
//        
//        String value = store.getDefaultString(key);
//        return value;
//    }
    
    /**
     * Save value to persistent storage
     */
    public boolean doStore(String value) {
        if (store != null && value != null) {
            store.setValue(key, value);
            OptionsUtils.addOption(key, contextID, store);
        }
        
        return true;
    }
    
    /**
     * Save current value to persistent storage
     */
    public abstract boolean doStore();

    /**
     * Clear the current value in the preference store.
     */
    public void doClear() {
        setToDefault();
        if ((store != null) && store.contains(key)) {
            store.setToDefault(key);
            OptionsUtils.removeOption(key, contextID, store);
        }
    }
} // class Option
