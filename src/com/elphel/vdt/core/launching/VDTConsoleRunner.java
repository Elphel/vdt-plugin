/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
//import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
//import org.eclipse.debug.ui.DebugUITools;
//import org.eclipse.ui.console.ConsolePlugin;
//import org.eclipse.ui.console.IConsoleManager;
//import org.eclipse.ui.console.IPatternMatchListener;
//import org.eclipse.ui.console.MessageConsole;

import org.eclipse.debug.core.model.IStreamMonitor;
//import org.eclipse.debug.core.model.IStreamListener;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.console.IConsole;
//import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.sun.net.ssl.internal.www.protocol.https.Handler;

public class VDTConsoleRunner{
	private final VDTRunnerConfiguration runConfig; //*
	private IProcess processErr=null; //*
	private IProcess processOut=null; //*
	private IStreamsProxy2 sendErrorsToStreamProxy=null;
	private Object errorListener=null; //+
	private Object outputListener=null; //+
    private IStreamsProxy2 consoleInStreamProxy= null; //+
    private Timer timer;
    private IStreamsProxy2 stdoutStreamProxy=null;
    private IStreamsProxy2 stderrStreamProxy=null;

    
	public VDTConsoleRunner (VDTRunnerConfiguration runConfig){
		this.runConfig=runConfig;
	}

	private int getParserIndex( String parserName){
		if (parserName==null) return -1;
		BuildParamsItem[] buildParamsItems = runConfig.getArgumentsItemsArray(); // uses already calculated
		if (buildParamsItems==null) return -1;
		for (int i=0;i<buildParamsItems.length;i++){
			if (parserName.equals(buildParamsItems[i].getNameAsParser()))
				return i;
		}
		return -1;
	}

	
	public IOConsole runConsole(String consolePrefix
			, ILaunch launch
			, IProgressMonitor monitor 
			) throws CoreException{
        final boolean debugPrint=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING);

    	VDTRunner runner = VDTLaunchUtil.getRunner();
		int numItem=runConfig.getBuildStep();
		BuildParamsItem buildParamsItem = runConfig.getArgumentsItemsArray()[numItem]; // uses already calculated
		//TODO: Handle monitor
		// Find console with name starting with consolePrefix
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager(); // debugging
		IConsole[] consoles=(IConsole[]) man.getConsoles();
		IOConsole iCons=null;
		consoleInStreamProxy=null;
		String consoleStartsWith=consolePrefix+" ("; // space and start of date
		for (int i=0;i<consoles.length;i++){
//			if (consoles[i].getName().startsWith(consolePrefix)){
			if (consoles[i].getName().startsWith(consoleStartsWith)){
				iCons=(IOConsole) consoles[i];
				break;
			}
		}
		if (iCons==null) {
			MessageUI.error("Specified console: "+consolePrefix+" is not found (was looking for \""+consoleStartsWith+"\"");
			return null;
		}
		// try to send 
        String[] arguments = runConfig.getToolArguments();
        if (arguments == null) arguments=new String[0];
        if (debugPrint) {
//        	System.out.println("patternErrors= \""+  runConfig.getPatternErrors()+"\"");
//        	System.out.println("patternWarnings= \""+runConfig.getPatternWarnings()+"\"");
//        	System.out.println("patternInfo= \""    +runConfig.getPatternInfo()+"\"");
        	if (arguments!=null){
        		for (int i=0;i<arguments.length;i++){
        			System.out.println("Console line "+i+" = \""+arguments[i]+"\"");
        		}
        	}
        }
        runner.log("Writing to console "+iCons.getName()+":", arguments, null, false, true); /* Appears in the console of the target Eclipse (immediately erased) */
        runner.log("Writing to console "+iCons.getName()+":", arguments, null, false, false); /* Appears in the console of the parent Eclipse */
        IOConsoleOutputStream 	outStream= iCons.newOutputStream();
        IProcess process=((ProcessConsole)iCons).getProcess();
        consoleInStreamProxy= (IStreamsProxy2)process.getStreamsProxy();
        int stderrParserIndex=getParserIndex(buildParamsItem.getStderr());
        int stdoutParserIndex=getParserIndex(buildParamsItem.getStdout());
        BuildParamsItem stderrParser=(stderrParserIndex>=0)?runConfig.getArgumentsItemsArray()[stderrParserIndex]:null;
        BuildParamsItem stdoutParser=(stdoutParserIndex>=0)?runConfig.getArgumentsItemsArray()[stdoutParserIndex]:null;
        if (debugPrint) {
        	System.out.println("Using parser for stderr: "+((stderrParser!=null)?stderrParser.getNameAsParser():"none")); // actually may be the same as stdout
        	System.out.println("Using parser for stdout: "+((stdoutParser!=null)?stdoutParser.getNameAsParser():"none"));
        }        
        processErr=null;
        processOut=null;
//        IStreamsProxy2 stdoutStreamProxy=null;
//        IStreamsProxy2 stderrStreamProxy=null;
        stdoutStreamProxy=null;
        stderrStreamProxy=null;
        if (stdoutParser!=null){
			List<String> toolArgumentsStdout = new ArrayList<String>();
			List<String> stdoutArguments=stdoutParser.getParamsAsList();
			if (stdoutArguments != null)
				toolArgumentsStdout.addAll(stdoutArguments);
			// overwriting runConfig, but this is done sequentially, so OK
			runConfig.setToolArguments((String[])toolArgumentsStdout.toArray(new String[toolArgumentsStdout.size()]));
        	processOut=runner.run(runConfig,
        			"OUT for "+iCons.getName(),
        			launch,
        			null, //monitor
        			stdoutParserIndex);
            stdoutStreamProxy= (IStreamsProxy2) processOut.getStreamsProxy();
        }
        
        if (stderrParser!=null){
			List<String> toolArgumentsStderr = new ArrayList<String>();
			List<String> stderrArguments=stderrParser.getParamsAsList();
			if (stderrArguments != null)
				toolArgumentsStderr.addAll(stderrArguments);
			// overwriting runConfig, but this is done sequentially, so OK
			runConfig.setToolArguments((String[])toolArgumentsStderr.toArray(new String[toolArgumentsStderr.size()]));
        	processErr=runner.run(runConfig,
        			"ERR for "+iCons.getName(),
        			launch,
        			null, //monitor);
        			stderrParserIndex);
            stderrStreamProxy= (IStreamsProxy2) processErr.getStreamsProxy();
          //TODO: Add error parsers            
        }
        
        sendErrorsToStreamProxy=(stderrStreamProxy!=null)?stderrStreamProxy:stdoutStreamProxy;
        final IStreamsProxy2 fSendErrorsToStreamProxy=sendErrorsToStreamProxy;
        final IStreamsProxy2 fSendOutputToStreamProxy= stdoutStreamProxy;

// connect input streams of the parsers to the out from the console process         
        IStreamMonitor consoleOutStreamMonitor=null;
        IStreamMonitor consoleErrStreamMonitor=null;
        runConfig.resetConsoleText();
        String interrupt=buildParamsItem.getInterrupt(); // Not yet used
        runConfig.setConsoleFinish(buildParamsItem.getPrompt());
        if (debugPrint) {
        	System.out.println("Using console program termination string: \""+buildParamsItem.getPrompt()+"\"");
        }
        errorListener=null;
        
        if (fSendErrorsToStreamProxy!=null){
        	consoleErrStreamMonitor=consoleInStreamProxy.getErrorStreamMonitor();
        	errorListener=new IStreamListener(){
        		public void streamAppended(String text, IStreamMonitor monitor){
        			try {
        				fSendErrorsToStreamProxy.write(text);

        			} catch (IOException e) {
        				if (debugPrint) 	System.out.println("Can not write errors"); //happens for the last prompt got after finish marker
        			}
        			if (runConfig.addConsoleText(text)){
        				if (debugPrint)  System.out.println("Got finish sequence");
        				// TODO: launch continuation of the build process
        				finishConsolescript();
        			}
        		}
       		};
        	consoleErrStreamMonitor.addListener((IStreamListener) errorListener);       		
       	}
        outputListener=null;
        if (fSendOutputToStreamProxy!=null){
        	consoleOutStreamMonitor=consoleInStreamProxy.getOutputStreamMonitor();
        	outputListener=new IStreamListener(){
        		public void streamAppended(String text, IStreamMonitor monitor){
        			try {
        				fSendOutputToStreamProxy.write(text);
        			} catch (IOException e) {
        				if (debugPrint) System.out.println("Can not write output");
        			}
        			if (runConfig.addConsoleText(text)){
        				if (debugPrint) System.out.println("Got finish sequence");
        				// TODO: launch continuation of the build process
        				finishConsolescript();
        			}
        		}
       		};
        	consoleOutStreamMonitor.addListener((IStreamListener) outputListener );
        }
        //Problems occurred when invoking code from plug-in: "org.eclipse.ui.console".
        //Exception occurred during console property change notification.
        outStream.setColor(new Color(null, 128, 128, 255)); 
        try {
        	for (int i=0;i<arguments.length;i++){
        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.LOCAL_ECHO)) {
        			outStream.write(arguments[i]+"\n"); // writes to console itself
        		}
        		consoleInStreamProxy.write(arguments[i]+"\n");
        	}
        } catch (IOException e) {
        	System.out.println("Can not write to outStream of console "+iCons.getName());
        }
        int timeout=buildParamsItem.getTimeout();
        if ((timeout==0) && (buildParamsItem.getPrompt()==null)) timeout=1;// should specify at least one of timeout or prompt
        timer=null;
        if (timeout>0){
        	if (debugPrint) System.out.println("Setting timeout "+timeout);
        	final int fTimeout = timeout;
        	timer=new Timer();
        	timer.schedule(new TimerTask() {          
        		@Override
        		public void run() {
        			if (debugPrint) System.out.println(">>Timeout<<");
        			finishConsolescript();
        		}
        	}, fTimeout*1000);
        }
        return iCons;
	}

	
	// TODO: remove unneeded global vars
	
    public void finishConsolescript() {
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
    		System.out.println("finishConsolescript()");
		if (timer!=null){
			timer.cancel();
		}
    	if (consoleInStreamProxy==null) {
    		System.out.println("Bug: consoleInStreamProxy == null");
    		return; // or continue other commands?
    	}
    	if (errorListener !=null) { // disconnect error stream listener
    		IStreamMonitor consoleErrorStreamMonitor=consoleInStreamProxy.getOutputStreamMonitor();
    		consoleErrorStreamMonitor.removeListener((IStreamListener) errorListener);
    	}
    	if (outputListener !=null) { // disconnect output stream listener
    		IStreamMonitor consoleOutStreamMonitor=consoleInStreamProxy.getOutputStreamMonitor();
    		consoleOutStreamMonitor.removeListener((IStreamListener) outputListener);
    	}
    	// terminate parser(s). Do those console listeners (parsers) have to be removed too?
    	// TODO: Maybe wait for the process (small time) to terminate by disconnecting it's stdin
    	// Now end of the parsing may be lost if it did not have enough time to finish.
    	//IStreamsProxy2.closeInputStream()
    	// Disconnection worked !!
    	
    	if (stderrStreamProxy!=null){
    		try {
    			stderrStreamProxy.closeInputStream();
			} catch (IOException e) {
				System.out.println("Failed to disconnect stdin of the processErr parser process");
		    	if (processErr!=null){
		    		try {
						processErr.terminate();
					} catch (DebugException te) {
						System.out.println("Failed to terminate processErr parser process");
					}
		    	}
			}
    	}
    	if (stdoutStreamProxy!=null){
    		try {
    			stdoutStreamProxy.closeInputStream();
			} catch (IOException e) {
				System.out.println("Failed to disconnect stdin of the processOut parser process");
		    	if (processOut!=null){
		    		try {
						processOut.terminate();
					} catch (DebugException te) {
						System.out.println("Failed to terminate processOut parser process");
					}
		    	}
			}
    	}

/*    	
    	if (processErr!=null){
    		try {
				processErr.terminate();
				
			} catch (DebugException e) {
				System.out.println("Failed to terminate processErr parser process");
			}
    	}
    	if (processOut!=null){
    		try {
				processOut.terminate();
			} catch (DebugException e) {
				System.out.println("Failed to terminate processOut parser process");
			}
    	}
*/    	
    	int thisStep=runConfig.getBuildStep();
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
    		System.out.println("Finished console task, step was "+thisStep);
    	runConfig.setBuildStep(thisStep+1); // next task to run
    	VDTLaunchUtil.getRunner().getRunningBuilds().saveUnfinished(runConfig.getOriginalConsoleName(), runConfig );
    	try {
    		VDTLaunchUtil.getRunner().resumeLaunch(runConfig.getOriginalConsoleName()); // replace with console
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
 } // class VDTConsoleRunner
