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
package com.elphel.vdt.core.tools.generators;


import org.eclipse.core.resources.IResource;

import com.elphel.vdt.VDT;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

/**
 * Generate the name of current project. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public class ProjectNameGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_PROJECT_NAME; 
    
    public String getName() {
        return NAME;
    }

    public ProjectNameGenerator()
    {
    	super(null); // null for topFormatProcessor - this generator can not reference other parameters
    }

    protected String[] getStringValues() {
        String[] value = null;
        IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
        if (resource != null) {
            String project_name = resource.getProject().getName(); 
            value = new String[]{project_name};
        } else {
//            fault("There is no selected project");
            System.out.println(getName()+": no project selected");
            return new String[] {""};
            
        }
        return value;
    }

} // class ProjectNameGenerator
