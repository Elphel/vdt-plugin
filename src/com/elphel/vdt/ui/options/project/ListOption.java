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
package com.elphel.vdt.ui.options.project;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Combo;

/**
 * Option represented by the STW Combo control
 * 
 * Created: 16.02.2006
 * @author  Lvov Konstantin
 */
public class ListOption implements IOption {

    private Combo combo;
    private String[] items;
    private Option option;
    
    ListOption(Combo combo, QualifiedName name, String[] items, int defaultValue) {
        option = new Option(name, items[defaultValue]); 
        this.combo = combo;
        this.items = items;
        this.combo.setItems(items);
        this.combo.select(0);
    }
    
    /**
     * Return core option (option without UI elements)
     */
    public Option getOption() {
        return option;
    }
    
    public void setPreferenceStore(IPreferenceStore store) {
        option.setPreferenceStore(store);
    }
    
    public void setResourceStore(IResource resource) {
        option.setResourceStore(resource);
    }
    
    /**
     * Set default value to STW control
     */
    public void reset() {
        option.reset();
        setSelection(option.getDefaultValue());
    } // reset()

    /**
     * Set value to STW control
     */
    public void read() {
        option.read();
        setSelection(option.getValue());
    } // read()

    /**
     * Save value from STW control
     */
    public boolean save() {
        String value = combo.getText();
        option.setValue(value);
        return option.save();
    } // save()

    private void setSelection(String value) {
        if (value != null) {
		    for (int i=0; i < items.length; i++) {
		        if (value.equals(items[i])){
		            combo.select(i);
		            return;
		        }
		    }
        }
        combo.select(0);
    } // setSelection()
    
} // class ListOption
