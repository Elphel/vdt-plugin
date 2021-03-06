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
