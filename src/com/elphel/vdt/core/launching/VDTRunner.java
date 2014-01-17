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


import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.MessageConsole;

import com.elphel.vdt.Txt;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.elphel.vdt.core.Utils;

/**
 * Verilog development tool runner.
 * 
 * Created: 22.12.2005
 * @author  Lvov Konstantin
 */

public class VDTRunner {

    /**
     * Returns a new process aborting if the process could not be created.
     * @param launch the launch the process is contained in
     * @param p the system process to wrap
     * @param label the label assigned to the process
     * @param attributes values for the attribute map
     * @return the new process
     * @throws CoreException problems occurred creating the process
     */
    protected IProcess newProcess(ILaunch launch, Process p, String label, Map attributes) throws CoreException {
        IProcess process= DebugPlugin.newProcess(launch, p, label, attributes);
        if (process == null) {
            p.destroy();
            abort(Txt.s("Launch.Process.Error"), null, IStatus.ERROR);
        }
        return process;
    }

    
    /** @see DebugPlugin#exec(String[], File, String[]) */
    protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
        return DebugPlugin.exec(cmdLine, workingDirectory, envp);
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
    public void run( VDTRunnerConfiguration configuration
               , ILaunch launch
               , IProgressMonitor monitor 
               ) throws CoreException
    {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
            
        IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
        subMonitor.beginTask(Txt.s("Launch.Message.Launching"), 2);
        subMonitor.subTask(Txt.s("Launch.Message.ConstructingCommandLine"));
        
        String toolTolaunch = configuration.getToolToLaunch();
        String[] arguments = configuration.getToolArguments();
    	boolean isShell= configuration.getIsShell();
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
        	System.out.println("patternErrors= \""+  configuration.getPatternErrors()+"\"");
        	System.out.println("patternWarnings= \""+configuration.getPatternWarnings()+"\"");
        	System.out.println("patternInfo= \""    +configuration.getPatternInfo()+"\"");
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
        String[] controlFiles = configuration.getControlFiles();
        log(cmdLine, controlFiles, false, true); /* Appears in the console of the target Eclipse (immediately erased) */
        log(cmdLine, controlFiles, false, false); /* Appears in the console of the parent Eclipse */

        String[] envp = configuration.getEnvironment();
        
        subMonitor.worked(1);

        // check for cancellation
        if (monitor.isCanceled()) {
            return;
        }

        subMonitor.subTask(Txt.s("Launch.Message.Starting"));
        File workingDir = getWorkingDir(configuration); /* /data/vdt/runtime-EclipseApplication/x353 */
        Process p = exec(cmdLine, workingDir, envp);
        if (p == null) {
            return;
        }

        // check for cancellation
        if (monitor.isCanceled()) {
            p.destroy();
            return;
        }       
 /* next actually launches the process */
/* IProcess may set/get client parameters */
    		
        VDTErrorParser parser= VerilogPlugin.getVDTErrorParser();

        IProcess process= newProcess( launch
        		, p
        		, renderProcessLabel(cmdLine)
        		, getDefaultProcessAttrMap(configuration));
        parser.parserSetup(
        		configuration,
        		process
        		);
        
        subMonitor.worked(1);
        subMonitor.done();
    } // run()
        
    private void log(String[] strings, 
                     String[] controlFiles, 
                     boolean formatColumn, 
                     boolean printToUser) 
    {
        println("Control files created:", printToUser);
        
        if(controlFiles.length == 0) {
            println("(none)", printToUser);
        } else {
            for(int i = 0; i < controlFiles.length; i++)
                println(controlFiles[i], printToUser);
        }
        
        println(printToUser);
        println("Launching:", printToUser);
        
        if(formatColumn)
            println(printToUser);
        
        for(int i = 0; i < strings.length; i++)
            if(formatColumn)
                println("#" + i + ": '" + strings[i] + "'", printToUser);
            else
                print(strings[i] + " ", printToUser);
        
        if(!formatColumn)
            println(printToUser);

        println(printToUser);
        println("-----------------------------------------------------------------------", printToUser);
        println(printToUser);
    }
    
    private void println(boolean printToUser) {
        println("", printToUser);
    }
    
    private void println(String msg, boolean printToUser) {
        print(msg + "\n", printToUser);        
    }
    
    private void print(String msg, boolean printToUser) {
        if(printToUser)
        	VerilogPlugin.print(msg);
        else
            System.out.print(msg);
    }
    
    /**
     * Returns the default process attribute map for tool processes.
     * 
     * @return default process attribute map for Java processes
     */
    protected Map getDefaultProcessAttrMap(VDTRunnerConfiguration config) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(IProcess.ATTR_PROCESS_TYPE, Utils.getPureFileName(config.getToolToLaunch()));
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

    public static String renderProcessLabel(String[] commandLine) {
        String timestamp= DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
        return Txt.s("Launch.Process.LabelFormat", new String[] {commandLine[0], timestamp});
    }

    protected static String renderCommandLine(String[] commandLine) {
        if (commandLine.length == 0)
            return "";
        return " " + new File(commandLine[0]).getName();
    }
    
} // class VDTRunner
