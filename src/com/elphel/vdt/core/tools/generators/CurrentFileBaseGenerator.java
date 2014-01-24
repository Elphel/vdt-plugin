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

import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IResource;
//import org.eclipse.ui.IPageLayout;

import org.eclipse.core.runtime.Path;

import com.elphel.vdt.VDT;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

public class CurrentFileBaseGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_CURRENT_BASE;

    public String getName() {
        return NAME;
    }
    
    protected String[] getStringValues() {
//        IResource resource = SelectedResourceManager.getDefault().getSelectedResource();
        String name=SelectedResourceManager.getDefault().getChosenShort(); // last segment of the file name
        if (name!=null){
        	int dot = name.lastIndexOf('.');
            return new String[] { (dot>=0)? name.substring(0, dot): name };
        }
        return new String[] { "" };
    }
}



