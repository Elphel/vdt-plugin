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
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
//import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
//import org.eclipse.debug.ui.DebugUITools;
//import org.eclipse.ui.console.ConsolePlugin;
//import org.eclipse.ui.console.IConsoleManager;
//import org.eclipse.ui.console.IPatternMatchListener;
//import org.eclipse.ui.console.MessageConsole;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsole;
//import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.ToolsCore;
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
	private RunningBuilds runningBuilds;
	public VDTRunner(){
		runningBuilds = new RunningBuilds();
	}
	
	public RunningBuilds getRunningBuilds(){
		return runningBuilds;
	}

    public void resumeLaunch(String consoleName) throws CoreException  {
    	try {
    		doResumeLaunch(consoleName);
    	} catch(Exception e) {
    		MessageUI.error(e);

    		if(e instanceof CoreException)
    			throw (CoreException)e;
    	}
    }
	    // make call it when console is closed
	private void doResumeLaunch(String consoleName ) throws CoreException {
		final VDTRunnerConfiguration runConfig=runningBuilds.resumeConfiguration(consoleName);
        final boolean debugPrint=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING);

		if (runConfig==null){
			System.out.println("Turned out nothing to do. Probably a bug");
			return;
		}
		
		String playBackStamp=runConfig.getPlayBackStamp();
		if (playBackStamp!=null){
			System.out.println("doResumeLaunch(): wrong, it should be playback, not run, as playBackStamp = "+playBackStamp+ "(not null)");
			return;
		}

		
		
		BuildParamsItem[] argumentsItemsArray = runConfig.getArgumentsItemsArray(); // uses already calculated
		runConfig.canceTimers(); // If some timers were set, but a task finished earlier
		int numItem=runConfig.getBuildStep();
		if (debugPrint) System.out.println("--------- resuming "+ consoleName+", numItem="+numItem+" ------------");
		ILaunch launch=runConfig.getLaunch();
		IProgressMonitor monitor=runConfig.getMonitor();
		for (;numItem<argumentsItemsArray.length;numItem++){
			runConfig.setBuildStep(numItem); // was not updated if was not sleeping
			List<String> toolArguments = new ArrayList<String>();
			List<String> arguments=argumentsItemsArray[numItem].getParamsAsList();
			if (arguments != null)
				toolArguments.addAll(arguments);
			//        if (resources != null)
			//            toolArguments.addAll(resources);
			runConfig.setToolArguments((String[])toolArguments.toArray(new String[toolArguments.size()]));
			if (argumentsItemsArray[numItem].getConsoleName()!=null){
				VDTConsoleRunner consoleRunner= runConfig.getConsoleRunner();
				consoleRunner.runConsole(argumentsItemsArray[numItem].getConsoleName(), launch, monitor);
				return; // Should be awaken by listener when the finish sequence will be output to console
				//continue;
			}
			if (argumentsItemsArray[numItem].getNameAsParser()!=null){
				// parsers should be launched by the console scripts, in parallel to them
				if (debugPrint) System.out.println("Skipping parser "+argumentsItemsArray[numItem].getNameAsParser());
				continue;
			}
			// Launch the configuration - 1 unit of work
			//        	VDTRunner runner = VDTLaunchUtil.getRunner();
//			IProcess process=run(
			IProcess process=runConfig.getProgramRunner().run(
					runConfig,
			        runConfig.getOriginalConsoleName(),
					launch,
					monitor,
					numItem
              );

			//Andrey: if there is a single item - launch asynchronously, if more - verify queue is empty

			// check for cancellation
			if (monitor.isCanceled() || (process==null)) {
				runningBuilds.removeConfiguration(consoleName); 
				return;
			}
//			if (numItem<(argumentsItemsArray.length-1)){ // Not for the last
			// find out if there are any non-parsers left
/*			
			boolean moreToProcess=false;
			for (int i=numItem+1;i<argumentsItemsArray.length;numItem++)
				if (argumentsItemsArray[numItem].getNameAsParser()==null) {
					moreToProcess=true;
					break;
			}
			
			moreToProcess=true; // TODO: remove later - should always wait if keep is not set
			if (moreToProcess){	
			*/
			IOConsole iCons=  (IOConsole) DebugUITools.getConsole(process); // had non-null fPatternMatcher , fType="org.eclipse.debug.ui.ProcessConsoleType"
			if (iCons==null){
				System.out.println("Could not get console for the specified process");
				continue;
			}
			if (debugPrint) System.out.println("originalConsoleName="+consoleName+
					"\nprocessConsole name="+iCons.getName());
			final IOConsole fiCons=iCons;
			//				final String    fConsoleName=fiCons.getName(); // actual console name - may be already "<terminated> ... "
			final String    fConsoleName=consoleName; // calculated console name - used for launching external program
			//				if (!fConsoleName.equals(consoleName)){ // terminated before we added listeners
			if (!fConsoleName.equals(fiCons.getName())){ // terminated before we added listeners
				if (debugPrint) System.out.println("Already terminated, proceed to the next item");
				continue; // proceed with the next item without pausing
			}
			/* Prepare to postpone next commands to be resumed by event*/
			runConfig.setBuildStep(numItem+1);
			runningBuilds.saveUnfinished(consoleName, runConfig );

			final IPropertyChangeListener fListener =new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (!fConsoleName.equals(fiCons.getName())){
						fiCons.removePropertyChangeListener(this);
						if (debugPrint) System.out.println(">>> "+fConsoleName+" -> "+fiCons.getName());
						try {
							resumeLaunch(fConsoleName); //, fiCons, this); // replace with console
						} catch (CoreException e) {
							System.out.println ("Failed to resume launch sequence");
						}
					}
				}
			};
			
			fiCons.addPropertyChangeListener(fListener);

			if (debugPrint) System.out.println("fiCons.getName()="+fiCons.getName()+"addPropertyChangeListener()");

			if (!fConsoleName.equals(consoleName)){ // terminated before we added listeners
				if (debugPrint)  System.out.println("Fire!");
				fiCons.firePropertyChange(fiCons,"org.eclipse.jface.text", consoleName, fConsoleName); 
			}
			if (debugPrint)  System.out.println("return - waiting to be awaken");
			int timeout=argumentsItemsArray[numItem].getTimeout();
			//keepOpen()
			final boolean fKeepOpen=argumentsItemsArray[numItem].keepOpen();
			if (fKeepOpen && (timeout<1)) timeout=1; // some minimal timeout
			if (timeout>0){
				if (debugPrint)  System.out.println ("timeout="+timeout+"s, keep-open="+ fKeepOpen);
				// implementation will require keeping track of it and canceling if program terminated earlier.
				// And for the programs it is easy to kill them manually with a red square button
				if (timeout>0) { //never with no warnings 
					final int fTimeout = timeout;
					final IProcess fProcess=process;
					// new Timer().schedule(new TimerTask() {          

					argumentsItemsArray[numItem].getTimer().schedule(new TimerTask() {
						@Override
						public void run() {
							if (debugPrint)  System.out.println(">> Got timeout after "+fTimeout+"sec <<");
							if (fKeepOpen) {
								fiCons.removePropertyChangeListener(fListener);
								if (debugPrint)  System.out.println("Timeout-initialted firePropertyChange on  "+fConsoleName);
					        	Display.getDefault().syncExec(new Runnable() {
					        		public void run() {
										try {
											resumeLaunch(fConsoleName);
										} catch (CoreException e) {
											System.out.println("Failed to resumeLaunch after timer"+fConsoleName);
										} //, fiCons, this); // replace with console
					        		}
					        	});
							} else {
								try {
									fProcess.terminate();
									if (debugPrint)  System.out.println(">> "+fConsoleName+": terminated by timeout <<");
								} catch (DebugException e) {
									System.out.println("Failed to terminate process on "+fConsoleName);
								}
							}
						}
					}, fTimeout*1000);
				}
			}

			return;

			//			}
		} //for (;numItem<argumentsItemsArray.length;numItem++){
		if (debugPrint)  System.out.println("All finished");
		monitor.done();
		ToolsCore.getTool(runConfig.getToolName()).setRunning(false);
		ToolsCore.getTool(runConfig.getToolName()).setFinishTimeStamp();
		ToolsCore.getTool(runConfig.getToolName()).updateViewStateIcon();
	}

    public void logPlaybackLaunch(String consoleName) throws CoreException  {
    	try {
    		doLogPlaybackLaunch(consoleName);
    	} catch(Exception e) {
    		MessageUI.error(e);

    		if(e instanceof CoreException)
    			throw (CoreException)e;
    	}
    }
	    // make call it when console is closed
	private void doLogPlaybackLaunch(String consoleName ) throws CoreException {
		final VDTRunnerConfiguration runConfig=runningBuilds.resumeConfiguration(consoleName);
        final boolean debugPrint=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING);

		if (runConfig==null){
			System.out.println("Turned out nothing to do. Probably a bug");
			return;
		}
		
		String playBackStamp=runConfig.getPlayBackStamp();
		if (playBackStamp==null){
			System.out.println("doResumeLaunch(): wrong, it should be run, not playback, as playBackStamp = null");
			return;
		}
		
		BuildParamsItem[] argumentsItemsArray = runConfig.getArgumentsItemsArray(); // uses already calculated
		runConfig.canceTimers(); // If some timers were set, but a task finished earlier
//		int numItem=runConfig.getBuildStep();
		// made buildStep for logs negative (it does not need to sleep and does everything in one call)
		// to catch stray resumes
		int numItem=0; //runConfig.getBuildStep();
		if (debugPrint) System.out.println("--------- re-playing log from "+ consoleName+", numItem="+numItem+" ------------");
		ILaunch launch=runConfig.getLaunch();
		IProgressMonitor monitor=runConfig.getMonitor();
		for (;numItem<argumentsItemsArray.length;numItem++){
			runConfig.setBuildStep(numItem); // was not updated if was not sleeping
			List<String> toolArguments = new ArrayList<String>();
			List<String> arguments=argumentsItemsArray[numItem].getParamsAsList();
			if (arguments != null)
				toolArguments.addAll(arguments);
			runConfig.setToolArguments((String[])toolArguments.toArray(new String[toolArguments.size()]));
			if (argumentsItemsArray[numItem].getConsoleName()!=null){
				VDTConsolePlayback consolePlayback= runConfig.getConsolePlayback();
				consolePlayback.runConsole(argumentsItemsArray[numItem].getConsoleName(), launch, monitor);
				continue; // No wait, will get back here
				//continue;
			}
			if (argumentsItemsArray[numItem].getNameAsParser()!=null){
				// parsers should be launched by the console scripts, in parallel to them
				if (debugPrint) System.out.println("Skipping parser "+argumentsItemsArray[numItem].getNameAsParser());
				continue;
			}
			if (debugPrint) System.out.println("Skipping program runner as playback is not implemented, arguments were "+argumentsItemsArray[numItem].getNameAsParser());
		} //for (;numItem<argumentsItemsArray.length;numItem++){
		System.out.println("All playbacks finished");
		monitor.done();
		ToolsCore.getTool(runConfig.getToolName()).setRunning(false);
		ToolsCore.getTool(runConfig.getToolName()).updateViewStateIcon();
	}

	
	
    public void log(String header,
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
        if (strings!=null){
        	for(int i = 0; i < strings.length; i++)
        		if(formatColumn)
        			println("#" + i + ": '" + strings[i] + "'", printToUser);
        		else
        			print(strings[i] + " ", printToUser);
        }
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
    
    public static String renderProcessLabel(String[] commandLine) {
        String timestamp= DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
        return Txt.s("Launch.Process.LabelFormat", new String[] {commandLine[0], timestamp});
    }
    public static String renderProcessLabel(String toolName) {
        String timestamp= DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
        return Txt.s("Launch.Process.LabelFormat", new String[] {toolName, timestamp});
    }
} // class VDTRunner
