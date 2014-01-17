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
package com.elphel.vdt.core.launching;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import com.elphel.vdt.Txt;
import com.elphel.vdt.ui.MessageUI;


/**
 * Launch delegate for a Verilog development tools.
 * 
 * Created: 23.12.2005
 * @author  Lvov Konstantin
 */

public class VDTLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
    
    private void doLaunch( ILaunchConfiguration configuration
                         , String mode
                         , ILaunch launch
                         , IProgressMonitor monitor
                         ) throws CoreException
                         
    {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        monitor.beginTask(Txt.s("Launch.Beginning", new String[]{configuration.getName()}), 4);
        if (monitor.isCanceled()) { // check for cancellation
            return;
        }
        monitor.subTask(Txt.s("Launch.CreatingToolArguments"));
        
        // resolve tool location
        IPath toolToLaunch = VDTLaunchUtil.getToolToLaunch(configuration);
        
        // Create run config
        VDTRunnerConfiguration runConfig = new VDTRunnerConfiguration(toolToLaunch.toOSString());

        // resolve environment
        runConfig.setEnvironment(VDTLaunchUtil.getEnvironment(configuration));

        // resolve working directory
        runConfig.setWorkingDirectory(VDTLaunchUtil.getWorkingDirectory(configuration));
        runConfig.setProjectPath(VDTLaunchUtil.getProjectPath(configuration));
        
        // done the half of creating arguments phase
        monitor.worked(1);  

        // resolve arguments
        List<String> arguments = VDTLaunchUtil.getArguments(configuration);
        
        // get tool is a shell 
        boolean isShell=VDTLaunchUtil.getIsShell(configuration);
        
        // get patterns for Error parser
        String patternErrors=  VDTLaunchUtil.getPatternErrors(configuration);
        String patternWarnings=VDTLaunchUtil.getPatternWarnings(configuration);
        String patternInfo=    VDTLaunchUtil.getPatternInfo(configuration);

        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }               
        // done the creating arguments phase
        monitor.worked(2);  
                
        // resolve resources
//        List<String> resources = VDTLaunchUtil.getResources(configuration);
        
        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }       
        // done the creating arguments phase
        monitor.worked(3);  

        // resolve arguments
        List<String> toolArguments = new ArrayList<String>();
        if (arguments != null)
            toolArguments.addAll(arguments);
//        if (resources != null)
//            toolArguments.addAll(resources);
        
        runConfig.setToolArguments((String[])toolArguments.toArray(new String[toolArguments.size()]));
        runConfig.setIsShell(isShell);
        runConfig.setPatternErrors(patternErrors);
        runConfig.setToolName(VDTLaunchUtil.getToolName(configuration));
        runConfig.setPatternWarnings(patternWarnings);
        runConfig.setPatternInfo(patternInfo);
        runConfig.setToolProjectPath(VDTLaunchUtil.getToolProjectPath(configuration));
        
        List<String> controlFiles = VDTLaunchUtil.getControlFiles(configuration);
        
        runConfig.setControlFiles((String[])controlFiles.toArray(new String[controlFiles.size()]));
        
        // Launch the configuration - 1 unit of work
        VDTRunner runner = VDTLaunchUtil.getRunner();
        runner.run(runConfig, launch, monitor);
        
        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }       
        monitor.done();
    }

    public void launch( ILaunchConfiguration configuration
                      , String mode
                      , ILaunch launch
                      , IProgressMonitor monitor
                      ) throws CoreException 
    {
        try {
            doLaunch(configuration, mode, launch, monitor);
        } catch(Exception e) {
            MessageUI.error(e);

            if(e instanceof CoreException)
                throw (CoreException)e;
        }
    }

} // class VDTLaunchConfigurationDelegate
