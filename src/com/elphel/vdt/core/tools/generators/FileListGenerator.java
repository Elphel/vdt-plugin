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

import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.elphel.vdt.VDT;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

public class FileListGenerator extends AbstractGenerator {
//    public static final String NAME = VDT.GENERATOR_ID_FILE_LIST;
    public static final String NAME = "FileList";
    
    public FileListGenerator(String prefix, 
                             String suffix, 
                             String separator) 
    {
        super(prefix, suffix, separator);
    }
    
    public String getName() {
        return NAME;
    }

    protected String[] getStringValues() {
        IProject project = SelectedResourceManager.getDefault().getSelectedProject();
        List<String> files = getContainedFiles(project);
        
        return files.toArray(new String[files.size()]);
    }
    
    private List<String> getContainedFiles(IContainer container) {
        IResource[] members = null;
        
        try {
             members = container.members();            
        } catch (CoreException e) {
            return null;
        }
        
        List<String> files = new ArrayList<String>(members.length); 
         
        for(int i = 0; i < members.length; i++)  {
            if(members[i] instanceof IContainer)
                files.addAll(getContainedFiles((IContainer)members[i]));
            else if(members[i] instanceof IFile)
                files.add(((IFile)members[i]).getProjectRelativePath().toOSString());
                //files.add(((IFile)members[i]).getLocation().toOSString());
        }
        
        return files;
    }
}
