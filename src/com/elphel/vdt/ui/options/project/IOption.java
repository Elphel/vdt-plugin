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
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Interface for option manipulation: read, save, reset 
 * 
 * Created: 19.02.2006
 * @author  Lvov Konstantin
 */
public interface IOption {

    /**
     * Return core option (optiuon without UI elements)
     */
    public Option getOption();

    /**
     * Sets the preference store used by this option.
     */
    public void setPreferenceStore(IPreferenceStore store);
    
    /**
     * Sets the resource which persistent storage used by this option.
     */
    public void setResourceStore(IResource resource);
    
    /**
     * Set default value and save it to persistent storage
     */
    public void reset();

    /**
     * Read value from persistent storage
     */
    public void read();

    /**
     * Save value to persistent storage
     */
    public boolean save();
    
} // interface IOption
