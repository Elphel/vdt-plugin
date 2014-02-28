/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.elphel.vdt.core.launching;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.console.IConsole;
//import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
//import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

//import com.elphel.vdt.core.Utils;
//import org.eclipse.ui.console.IConsoleListener;

/**
 * Verilog development tool runner.
 * 
 * Created: 22.12.2005
 * @author  Lvov Konstantin
 */

public class VDTProgramRunner {
	
    /**
     * Returns a new process aborting if the process could not be created.
     * @param launch the launch the process is contained in
     * @param p the system process to wrap
     * @param label the label assigned to the process
     * @param attributes values for the attribute map
     * @return the new process
     * @throws CoreException problems occurred creating the process
     */
    protected IProcess newProcess(ILaunch launch, Process p, String label, Map<String, String> attributes) throws CoreException {
        IProcess process= DebugPlugin.newProcess(launch, p, label, attributes);
        if (process == null) {
            p.destroy();
            abort(Txt.s("Launch.Process.Error"), null, IStatus.ERROR);
        }
        return process;
    }

    
    /** @see DebugPlugin#exec(String[], File, String[]) */
    // before actual launching?
    protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
        return DebugPlugin.exec(cmdLine, workingDirectory, envp);
    }   
    
    private String combinePatterns (String thisPattern, String toolPattern){
    	String pattern=thisPattern;
    	if (pattern==null) pattern=toolPattern;
    	if ((pattern!=null) && (pattern.length()==0)) pattern=null;
    	return pattern;
    }
    /**
     * Launches a Verilog development tool as specified in the given 
     * configuration, contributing results (processes), to the given 
     * launch.
     *
     * @param configuration the configuration settings for this run
     * @param launch the launch to contribute to
     * @param monitor progress monitor or <code>null</code>
     * @exception CoreException if an exception occurs while launching
     */
    public IProcess run( VDTRunnerConfiguration runConfig
    		   , String consoleLabel
               , ILaunch launch
               , IProgressMonitor monitor
               , int numItem
               ) throws CoreException
    {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
//		int numItem=runConfig.getBuildStep();
    	VDTRunner runner = VDTLaunchUtil.getRunner();
        BuildParamsItem buildParamsItem = runConfig.getArgumentsItemsArray()[numItem]; // uses already calculated
        String patternErrors=  combinePatterns(buildParamsItem.getErrors(),  runConfig.getPatternErrors()) ;
        String patternWarnings=combinePatterns(buildParamsItem.getWarnings(),runConfig.getPatternWarnings()) ;
        String patternInfo=    combinePatterns(buildParamsItem.getInfo(),    runConfig.getPatternInfo()) ;
        
        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask(Txt.s("Launch.Message.Launching"), 2);
        subMonitor.subTask(Txt.s("Launch.Message.ConstructingCommandLine"));
        
        String toolTolaunch = runConfig.getToolToLaunch();
        String[] arguments = runConfig.getToolArguments();
    	boolean isShell= runConfig.getIsShell();
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
        	System.out.println("patternErrors= \""+  patternErrors+"\"");
        	System.out.println("patternWarnings= \""+patternWarnings+"\"");
        	System.out.println("patternInfo= \""    +patternInfo+"\"");
        	System.out.println((isShell?"Shell":"Tool")+" to launch=\""+toolTolaunch+"\"");
        	if (arguments!=null){
        		for (int i=0;i<arguments.length;i++){
        			System.out.println("Argument "+i+" = \""+arguments[i]+"\"");
        		}

        	}
        }

        String[] cmdLine;
        if (isShell && (arguments != null) && (arguments.length > 0)){ /* first argument is passed as a parameter to shell*/
            StringBuilder builder = new StringBuilder();
            cmdLine = new String[3];
            cmdLine[0]=toolTolaunch;
            cmdLine[1]=(arguments[0].equals("@EMPTY"))?"":arguments[0]; // Can not be set to empty 
            for (int i=1;i<arguments.length;i++) {
                builder.append(" ");
                builder.append(arguments[i]);
            }
            cmdLine[2]=builder.toString().trim();
        } else {
            int cmdLineLength = 1;
            if (arguments != null) {
                cmdLineLength += arguments.length;
            }
            cmdLine = new String[cmdLineLength];
            cmdLine[0] = toolTolaunch;
            if (arguments != null) {
                System.arraycopy(arguments, 0, cmdLine, 1, arguments.length);
            }
        }
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
        	for (int i=0;i<cmdLine.length;i++){
        		System.out.println("cmdLine["+i+"] = \""+cmdLine[i]+"\"");
        	}
        }
        String[] controlFiles = runConfig.getControlFiles();
        runner.log(null,cmdLine, controlFiles, false, true); /* Appears in the console of the target Eclipse (immediately erased) */
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) runner.log(null,cmdLine, controlFiles, false, false); /* Appears in the console of the parent Eclipse */

        String[] envp = runConfig.getEnvironment();
        
        subMonitor.worked(1);

        // check for cancellation
        if (monitor.isCanceled()) {
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
            return null;
        }

        subMonitor.subTask(Txt.s("Launch.Message.Starting"));
        File workingDir = getWorkingDir(runConfig); /* /data/vdt/runtime-EclipseApplication/x353 */
        Process p = exec(cmdLine, workingDir, envp);
        if (p == null) {
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
            return null;
        }

        // check for cancellation
        if (monitor.isCanceled()) {
            p.destroy();
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
            return null;
        }       
    		
        VDTErrorParser parser= VerilogPlugin.getVDTErrorParser();
        
		 /* next actually launches the process */
		/* IProcess may set/get client parameters */
        IProcess process= newProcess( launch
        		, p
        		, consoleLabel // renderProcessLabel(runConfig.getToolName())
        		, getDefaultProcessAttrMap(runConfig));
        parser.parserSetup(
        		runConfig,
        		process,
        		patternErrors,
        		patternWarnings,
        		patternInfo
        		);
        
        subMonitor.worked(1);
        subMonitor.done();

        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)){
            IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager(); // debugging
            IConsole[] consoles=(IConsole[]) man.getConsoles();
        	System.out.println(consoles.length+" consoles, processes="+launch.getChildren().length);
        }
        return process;

    } // run()
    /**
     * Returns the default process attribute map for tool processes.
     * 
     * @return default process attribute map for Java processes
     */
    protected Map<String, String> getDefaultProcessAttrMap(VDTRunnerConfiguration config) {
        Map<String, String> map = new HashMap<String, String>();
//        map.put(IProcess.ATTR_PROCESS_TYPE, Utils.getPureFileName(config.getToolToLaunch()));
        map.put(IProcess.ATTR_PROCESS_TYPE,config.getToolName());
        return map;
    }
    
    /**
     * Returns the working directory to use for the launched tool,
     * or <code>null</code> if the working directory is to be inherited
     * from the current process.
     * 
     * @return the working directory to use
     * @exception CoreException if the working directory specified by
     *  the configuration does not exist or is not a directory
     */ 
    protected File getWorkingDir(VDTRunnerConfiguration config) throws CoreException {
        String path = config.getWorkingDirectory();
        if (path == null) {
            return null;
        }
        File dir = new File(path);
        if (!dir.isDirectory()) {
            abort(Txt.s("Launch.Error.InvalidWorkingDir", new String[] {path}), null, IStatus.ERROR);
        }
        return dir;
    }

    /**
     * Throws a core exception with an error status object built from
     * the given message, lower level exception, and error code.
     * 
     * @param message the status message
     * @param exception lower level exception associated with the
     *  error, or <code>null</code> if none
     * @param code error code
     * @throws CoreException The exception encapsulating the reason for the abort
     */
    protected void abort(String message, Throwable exception, int code) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, VerilogPlugin.getVdtId(), code, message, exception));
    }
    
} // class VDTProgramRunner
