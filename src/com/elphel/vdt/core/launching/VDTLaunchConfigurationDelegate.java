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


import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.swt.widgets.Display;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;


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
    	VDTRunner runner = VDTLaunchUtil.getRunner();

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
        runConfig.setConfiguration(configuration); // to be resumed
        
        runConfig.setLaunch(launch); // to be resumed
        runConfig.setMonitor(monitor); // to be resumed
        
        // done the half of creating arguments phase
        monitor.worked(1);  

        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }               
        // done the creating arguments phase
        monitor.worked(2);  
                
        
        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }       
        // done the creating arguments phase
        monitor.worked(3);  

        // resolve arguments, save them
		runConfig.setArgumentsItemsArray(VDTLaunchUtil.getArguments(configuration));  // calculates all parameters modifies the tool parameters!
    	runConfig.setIsShell(VDTLaunchUtil.getIsShell(configuration));
    	runConfig.setPatternErrors(VDTLaunchUtil.getPatternErrors(configuration));
    	runConfig.setToolName(VDTLaunchUtil.getToolName(configuration));
    	runConfig.setPatternWarnings(VDTLaunchUtil.getPatternWarnings(configuration));
    	runConfig.setPatternInfo(VDTLaunchUtil.getPatternInfo(configuration));
    	runConfig.setToolLogDir(VDTLaunchUtil.getToolLogDir(configuration));
    	
    	runConfig.setToolProjectPath(VDTLaunchUtil.getToolProjectPath(configuration));

    	//    	runConfig.setBuildStep(0);
    	runConfig.resetBuildStep();

    	List<String> controlFiles = VDTLaunchUtil.getControlFiles(configuration);
    	runConfig.setControlFiles((String[])controlFiles.toArray(new String[controlFiles.size()]));
//        String consoleName=VDTRunner.renderProcessLabel(runConfig.getToolName());
    	
        final String consoleName=runConfig.getOriginalConsoleName();
        runner.getRunningBuilds().saveUnfinished(consoleName, runConfig ); // has to remove it after playback!!! (others are removed by closing consoles)
        
        String playBackStamp=VDTLaunchUtil.getLogBuildStamp(configuration); // got null
        runConfig.setPlayBackStamp(playBackStamp); // null
        
        if (playBackStamp==null){
        	// Causes "Invalid thread access" when trying to  write to console output if got there directly, not through console event
//        	runner.resumeLaunch(consoleName); // actual run of the tools
        	// try from Display thread
        	Display.getDefault().syncExec(new Runnable() {
        		public void run() {
					try {
						if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
							System.out.println("VDTLaunchConfigurationDelegate#doLaunch("+consoleName+"), threadID="+Thread.currentThread().getId());
						VDTLaunchUtil.getRunner().resumeLaunch(consoleName,0);
					} catch (CoreException e) {
						System.out.println("Failed to resumeLaunch");
					} //, fiCons, this); // replace with console
        		}
        	});
        	
        } else {
        	runConfig.invalidateBuildStep();
//        	runConfig.setBuildStep(-1); // to cause errors if will try to continue
        	runner.logPlaybackLaunch(consoleName); // tool logs playback with parsing
        }
        return;
    }

    public void launch( ILaunchConfiguration configuration
                      , String mode
                      , ILaunch launch
                      , IProgressMonitor monitor
                      ) throws CoreException 
    {
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_THREAD_CONFICT))
			System.out.println("-=-=-=- launch: "+VDTLaunchUtil.getToolName(configuration)+" threadID="+Thread.currentThread().getId());
        try {
            doLaunch(configuration, mode, launch, monitor);
        } catch(Exception e) {
            MessageUI.error(e);

            if(e instanceof CoreException)
                throw (CoreException)e;
        }
    }

 
} // class VDTLaunchConfigurationDelegate
