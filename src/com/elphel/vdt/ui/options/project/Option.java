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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * An option is a name + default value + read method + save method + reset method
 * 
 * Created: 16.02.2006
 * @author  Lvov Konstantin
 */
public class Option implements IOption {

    private QualifiedName name;
    private String key;
    
    private String defaultValue;
    private String value;

    private IPreferenceStore store = null;
    private IResource resource = null;
    
    Option(QualifiedName name, String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        key = name.getQualifier()+"."+name.getLocalName();
    }
    
    /**
     * Return core option (option without UI elements)
     */
    public Option getOption() {
        return this;
    }

    /**
     * Set current value
     */
     void setValue(String value) {
         this.value = value;
     }

     /**
     * Returns current value
     */
     public String getValue() {
         return value;
     }
    
     public String getDefaultValue() {
         return defaultValue;
     }

     
     /**
      * Sets the preference store used by this option.
      */
     public void setPreferenceStore(IPreferenceStore store) {
         this.store    = store;
         this.resource = null;
     }
     
     /**
      * Sets the resource which persistent storage used by this option.
      */
     public void setResourceStore(IResource resource) {
         this.store    = null;
         this.resource = resource;
     }
     
     /**
      * Set default value and save it to persistent storage
      */
     public void reset() {
    	 value = defaultValue;
    	 save();
     }

     /**
      * Read value from persistent storage
      */
     public void read() {
         if (store != null) {
             value = store.getString(key);
         } else if (resource != null) {
             try {
                 value = resource.getPersistentProperty(name);
             } catch (CoreException e) {
                 reset();
             }
         } else {
             reset();
         }
     } // read()

     /**
      * Save value to persistent storage
      */
     public boolean save() {
         if (store != null) {
             store.setValue(key, value);
         } else if (resource != null) {
             try {
                 resource.setPersistentProperty(name, value);
             } catch (CoreException e) {
                 // Nothing do do, we don't need to bother the user
             }
         }
         return true;    
     } // save()
         
} // class Option
