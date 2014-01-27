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
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
//import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
//import org.eclipse.debug.ui.DebugUITools;
//import org.eclipse.ui.console.ConsolePlugin;
//import org.eclipse.ui.console.IConsoleManager;
//import org.eclipse.ui.console.IPatternMatchListener;
//import org.eclipse.ui.console.MessageConsole;

import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.IConsole;
//import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.ui.MessageUI;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;












//import com.elphel.vdt.core.Utils;
import org.eclipse.ui.console.IConsoleListener;

/**
 * Verilog development tool runner.
 * 
 * Created: 22.12.2005
 * @author  Lvov Konstantin
 */

public class VDTRunner {

	// TODO:Remove
	VDTRunnerConfiguration runningConfiguration; // multi-step configuration currently active
	int nextBuildStep=0;
	
	private Map<String, VDTRunnerConfiguration> unfinishedBuilds;
	
	public VDTRunner(){
		unfinishedBuilds = new HashMap<String, VDTRunnerConfiguration>();
		IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
// This is not used, just for testing		
		System.out.println("***Addded console listeners");
		manager.addConsoleListener(new IConsoleListener(){
			public void consolesAdded(IConsole[] consoles){
				VDTRunner runner = VDTLaunchUtil.getRunner();
				for (int i=0;i<consoles.length;i++){
					System.out.println("+++ Added: "+consoles[i].getName());
					// Only shows added consoles
				}
			}
			public void consolesRemoved(IConsole[] consoles){
				VDTRunner runner = VDTLaunchUtil.getRunner();
				for (int i=0;i<consoles.length;i++){
					System.out.println("--- Removed: "+consoles[i].getName());
					
				}
			}
		});
	}
	
	
	
	public boolean isUnfinished(String consoleName){
		return unfinishedBuilds.containsKey(consoleName);
	}
	
	public VDTRunnerConfiguration resumeConfiguration(String consoleName){
		VDTRunnerConfiguration conf=unfinishedBuilds.get(consoleName);
		unfinishedBuilds.remove(consoleName);
		return conf;
	}

	public void removeConfiguration(String consoleName){
		unfinishedBuilds.remove(consoleName);
	}

	public void saveUnfinished(String consoleName, VDTRunnerConfiguration configuration ){
		unfinishedBuilds.put(consoleName, configuration);
	}

	    // make call it when console is closed
	private void doResumeLaunch( String consoleName ) throws CoreException {
//		System.out.println("--------- resuming "+ consoleName+" ------------");
		VDTRunnerConfiguration runConfig=resumeConfiguration(consoleName);
		if (runConfig==null){
			System.out.println("Turned out nothing to do. Probably a bug");
			return;
		}
		ILaunchConfiguration configuration=runConfig.getConfiguration();
		BuildParamsItem[] argumentsItemsArray = VDTLaunchUtil.getArguments(configuration);
		int numItem=runConfig.getBuildStep();
		System.out.println("--------- resuming "+ consoleName+", numItem="+numItem+" ------------");
		ILaunch launch=runConfig.getLaunch();
		IProgressMonitor monitor=runConfig.getMonitor();

		for (;numItem<argumentsItemsArray.length;numItem++){
			List<String> toolArguments = new ArrayList<String>();
			List<String> arguments=argumentsItemsArray[numItem].getParamsAsList();
			if (arguments != null)
				toolArguments.addAll(arguments);
			//        if (resources != null)
			//            toolArguments.addAll(resources);
			runConfig.setToolArguments((String[])toolArguments.toArray(new String[toolArguments.size()]));
			if (argumentsItemsArray[numItem].getConsoleName()!=null){
				runConsole(argumentsItemsArray[numItem].getConsoleName(),runConfig, launch, monitor);
				continue;
			}

			// Launch the configuration - 1 unit of work
			//        	VDTRunner runner = VDTLaunchUtil.getRunner();
			IProcess process=run(runConfig, launch, monitor);

			//Andrey: if there is a single item - launch asynchronously, if more - verify queue is empty
			// will not change
			//            String consoleName=renderProcessLabel(runConfig.getToolName());

			// check for cancellation
			if (monitor.isCanceled() || (process==null)) {
				removeConfiguration(consoleName); 
				return;
			}
			if (numItem<(argumentsItemsArray.length-1)){ // Not for the last
				//                IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager(); // debugging
				//                IConsole[] consoles=(IConsole[]) man.getConsoles();

				IOConsole iCons=  (IOConsole) DebugUITools.getConsole(process); // had non-null fPatternMatcher , fType="org.eclipse.debug.ui.ProcessConsoleType"
				if (iCons==null){
					System.out.println("Could not get a console for the specified process");
					continue;
				}
				System.out.println("consoleName="+consoleName+
						"\nprocessConsole name="+iCons.getName());
				final IOConsole fiCons=iCons;
//				final String    fConsoleName=fiCons.getName(); // actual console name - may be already "<terminated> ... "
				final String    fConsoleName=consoleName; // calculated console name - used for launching external program
//				if (!fConsoleName.equals(consoleName)){ // terminated before we added listeners
				if (!fConsoleName.equals(fiCons.getName())){ // terminated before we added listeners
					System.out.println("Already terminated, proceed to the next item");
					continue; // proceed with the next item without pausing
				}
				/* Prepare to postpone next commands to be resumed by event*/
				runConfig.setBuildStep(numItem+1);
				saveUnfinished(consoleName, runConfig );

				iCons.addPropertyChangeListener( new IPropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						if (!fConsoleName.equals(fiCons.getName())){
							fiCons.removePropertyChangeListener(this);
							System.out.println(">>> "+fConsoleName+" -> "+fiCons.getName());
					    	VDTRunner runner = VDTLaunchUtil.getRunner();
					    	try {
								runner.resumeLaunch(fConsoleName);
							} catch (CoreException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});
				if (!fConsoleName.equals(consoleName)){ // terminated before we added listeners
					System.out.println("Fire!");
					iCons.firePropertyChange(fiCons,"org.eclipse.jface.text", consoleName, fConsoleName); 
				}
				System.out.println("return - waiting to be awaken");
				return;
				
			}
		}
		monitor.done();
	}
	
	
	public IOConsole runConsole(String consolePrefix
			, VDTRunnerConfiguration configuration
			, ILaunch launch
			, IProgressMonitor monitor 
			) throws CoreException{
		//TODO: Handle monitor
		// Find console with name starting with consolePrefix
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager(); // debugging
		IConsole[] consoles=(IConsole[]) man.getConsoles();
		IOConsole iCons=null;
		for (int i=0;i<consoles.length;i++){
			if (consoles[i].getName().startsWith(consolePrefix)){
				iCons=(IOConsole) consoles[i];
				break;
			}
		}
		if (iCons==null) {
			MessageUI.error("Specified console: "+consolePrefix+" is not found");
			return null;
		}
		// try to send 
        String[] arguments = configuration.getToolArguments();
        if (arguments == null) arguments=new String[0];
        log("Writing to console "+iCons.getName()+":", arguments, null, false, true); /* Appears in the console of the target Eclipse (immediately erased) */
        log("Writing to console "+iCons.getName()+":", arguments, null, false, false); /* Appears in the console of the parent Eclipse */

//        IOConsoleInputStream inStream= iCons.getInputStream();
        IOConsoleOutputStream 	outStream= iCons.newOutputStream();
        IProcess process=((ProcessConsole)iCons).getProcess();
        IStreamsProxy iStreamProxy= process.getStreamsProxy();
        
        try {
        	for (int i=0;i<arguments.length;i++){
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.LOCAL_ECHO)) {
					outStream.write(arguments[i]+"\n"); // writes to console itself
				}
        		iStreamProxy.write(arguments[i]+"\n");
        	}
        } catch (IOException e) {
        	System.out.println("Can not write to outStream of console "+iCons.getName());
        }
        

		return iCons;
		
	}
        		
     
    public void resumeLaunch( String consoleName ) throws CoreException 
    {
    	try {
    		doResumeLaunch(consoleName);
    	} catch(Exception e) {
    		MessageUI.error(e);

    		if(e instanceof CoreException)
    			throw (CoreException)e;
    	}
    }

	
	
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
    // before actual launching?
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
    public IProcess run( VDTRunnerConfiguration configuration
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
        log(null,cmdLine, controlFiles, false, true); /* Appears in the console of the target Eclipse (immediately erased) */
        log(null,cmdLine, controlFiles, false, false); /* Appears in the console of the parent Eclipse */

        String[] envp = configuration.getEnvironment();
        
        subMonitor.worked(1);

        // check for cancellation
        if (monitor.isCanceled()) {
            return null;
        }

        subMonitor.subTask(Txt.s("Launch.Message.Starting"));
        File workingDir = getWorkingDir(configuration); /* /data/vdt/runtime-EclipseApplication/x353 */
        Process p = exec(cmdLine, workingDir, envp);
        if (p == null) {
            return null;
        }

        // check for cancellation
        if (monitor.isCanceled()) {
            p.destroy();
            return null;
        }       
    		
        VDTErrorParser parser= VerilogPlugin.getVDTErrorParser();

        IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager(); // debugging
        IConsole[] consoles=(IConsole[]) man.getConsoles();
//[Lorg.eclipse.ui.console.IConsole; cannot be cast to [Lorg.eclipse.debug.ui.console.IConsole;
        
		 /* next actually launches the process */
		/* IProcess may set/get client parameters */
        IProcess process= newProcess( launch
        		, p
//        		, renderProcessLabel(cmdLine)
        		, renderProcessLabel(configuration.getToolName())
        		, getDefaultProcessAttrMap(configuration));
        parser.parserSetup(
        		configuration,
        		process
        		);
        
        subMonitor.worked(1);
        subMonitor.done();
        man = ConsolePlugin.getDefault().getConsoleManager(); // debugging
        consoles=(IConsole[]) man.getConsoles();
        if (consoles.length>1){
//        	((IConsole) consoles[1]).setName("Python Consloe");
        }
        System.out.println(consoles.length+" consoles, processes="+launch.getChildren().length);
        return process;
        //= consoles
//setImageDescriptor
// 	getImageDescriptor()         

    } // run()
        
    private void log(String header,
    		         String[] strings, 
                     String[] controlFiles, 
                     boolean formatColumn, 
                     boolean printToUser) 
    {
        if(controlFiles!=null) {
        	println("Control files created:", printToUser);
        	if(controlFiles.length == 0) {
        		println("(none)", printToUser);
        	} else {
        		for(int i = 0; i < controlFiles.length; i++)
        			println(controlFiles[i], printToUser);
        	}
        }
        println(printToUser);
        if (header==null) {
        	header="Launching:";
        }
        println(header, printToUser);
        
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

    public static String renderProcessLabel(String[] commandLine) {
        String timestamp= DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
        return Txt.s("Launch.Process.LabelFormat", new String[] {commandLine[0], timestamp});
    }
    public static String renderProcessLabel(String toolName) {
        String timestamp= DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
        return Txt.s("Launch.Process.LabelFormat", new String[] {toolName, timestamp});
    }

    protected static String renderCommandLine(String[] commandLine) {
        if (commandLine.length == 0)
            return "";
        return " " + new File(commandLine[0]).getName();
    }
    
} // class VDTRunner
