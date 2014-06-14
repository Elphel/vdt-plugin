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
        super(prefix, suffix, separator,null); // null for topFormatProcessor - this generator can not reference other parameters
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
