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
package com.elphel.vdt.ui.wizards.module;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

/**
 * This class provide information captured in the 'New Verilog Module' wizard
 * pages as entered by the user.
 * The information is the provided to other consumers when generating content so
 * that the content can be configured/customized according to the data.
 * 
 * Created: 16.12.2005
 * @author  Lvov Konstantin
 */

class ModuleData {

    private String locationPath;
    private String fileName;
    private String moduleName;
    private IFile  file = null;
    private ModulePort[] ports;
    
    public String getLocationPath() {
        return locationPath;
    }

    public void setLocationPath(String locationPath) {
        file = null;
        this.locationPath = locationPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        file = null;
        this.fileName = fileName;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    public void setPorts(ModulePort[] ports) {
        this.ports = ports;
    }
    
    public ModulePort[] getPorts() {
        return ports;
    }
    
    public String getPureFileName() {
        if ((fileName == null) || (fileName.length() == 0))
            return "";
        int dot_pos = fileName.lastIndexOf(".");
        if (dot_pos == -1)
            return fileName;
        else
            return fileName.substring(0, dot_pos); 
    } // getPureFileName()
    
    public IFile getFile() throws CoreException {
        if (file != null)
            return file;
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IResource resource = root.findMember(new Path(locationPath));
        if (!resource.exists() || !(resource instanceof IContainer)) {
            throwCoreException("Container \"" + locationPath + "\" does not exist.");
        }
        IContainer container = (IContainer) resource;
        file = container.getFile(new Path(fileName));
        return file;
    } // isFileExists()     
        
    //-------------------------------------------------------------------------
    private void throwCoreException(String message) throws CoreException {
        IStatus status = new Status( IStatus.ERROR, "com.elphel.vdt.ui"
                                   , IStatus.OK, message, null );
        throw new CoreException(status);
    }
        
} // class ModuleData
