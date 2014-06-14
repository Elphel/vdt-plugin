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
package com.elphel.vdt.ui.wizards.project;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.elphel.vdt.VDT;
import com.elphel.vdt.veditor.HdlNature;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.Utils;
import com.elphel.vdt.core.options.OptionsCore;

/**
 * New Verilog project creation.
 * 
 * Created: 20.02.2006
 * @author  Lvov Konstantin
 */

public class NewProjectOperation implements IRunnableWithProgress {

    private ProjectData data; 

    private IResource elementToOpen;
    
    public NewProjectOperation(ProjectData data) {
        this.data   = data;
    }
    
    
    public void run(IProgressMonitor monitor) throws InvocationTargetException {
        if (monitor == null)
            monitor = new NullProgressMonitor();

        try {
            createProject(monitor);
        } catch (CoreException e) {
            throw new InvocationTargetException(e);
        } finally {
            monitor.done();
        }
    } // run()
    
    public IResource getElementToOpen() {
        return elementToOpen;
    }
    
    private void createProject(IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Create Verilog project", 8);
        IWorkspaceRoot root = VerilogPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(data.getProjectName());

        if (!project.exists())
            project.create(null, new SubProgressMonitor(monitor, 2));
        if (!project.isOpen())
            project.open(new SubProgressMonitor(monitor, 2));
  
        setProjectLocation(project, new SubProgressMonitor(monitor, 1));
 //       Utils.addNature(VDT.VERILOG_NATURE_ID, project, new SubProgressMonitor(monitor, 1));
        Utils.addNature(HdlNature.NATURE_ID, project, new SubProgressMonitor(monitor, 1));
        setProjectOptions(project, new SubProgressMonitor(monitor, 1));
        
        elementToOpen = null;
    } // createProject()
    
    private void setProjectOptions( IProject project
                                  , IProgressMonitor monitor ) throws CoreException 
    {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        OptionsCore.doStoreOption( VDT.OPTION_PROJECT_MENU
                                 , data.getDesignMenu().getName()
                                 , project );
        monitor.worked(1);
    } // setProjectOptions()
    
    private void setProjectLocation( IProject project
                                   , IProgressMonitor monitor ) throws CoreException 
    {
        if (monitor != null && monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        IProjectDescription description = project.getDescription();
        description.setLocation(null);
        project.setDescription(description, new SubProgressMonitor(monitor, 1));
    } // setProjectLocation()
    
} // class NewProjectOperation
