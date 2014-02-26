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


import java.io.File;

import org.eclipse.core.resources.IResource;

import com.elphel.vdt.VDT;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

/**
 * Generate the absolute path of the current project. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public class ProjectPathGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_PROJECT_PATH; 
    public ProjectPathGenerator()
    {
    	super(null); // null for topFormatProcessor - this generator can not reference other parameters
    }
    
    public String getName() {
        return NAME;
    }

    protected String[] getStringValues() {
        String[] value = null;
        IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
        if (resource != null) {
            String workspaceRoot=resource.getWorkspace().getRoot().getLocation().toString();
            String project_name = workspaceRoot+resource.getProject().getFullPath().toString()+File.separator; 
            value = new String[]{project_name};
        } else {
            fault("There is no selected project");
        }
        return value;
    }

} // class ProjectPathGenerator
