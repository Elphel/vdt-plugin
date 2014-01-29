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
	private final VDTRunnerConfiguration runConfig;
	private IProcess processErr=null;
	private IProcess processOut=null;
	private IStreamsProxy stdoutStreamProxy=null;
	private IStreamsProxy stderrStreamProxy=null;
	private IStreamsProxy sendErrorsToStreamProxy=null;
	private Object errorListener=null; //+
	private Object outputListener=null; //+
	private IOConsole iCons=null;
	private IProcess process=null;
    private IStreamsProxy consoleInStreamProxy= null;

	
	public VDTConsoleRunner (VDTRunnerConfiguration runConfig){
		this.runConfig=runConfig;
	}
/*
 * 	private BuildParamsItem getParser( String parserName){
		if (parserName==null) return null;
		BuildParamsItem[] buildParamsItems = runConfig.getArgumentsItemsArray(); // uses already calculated
		if (buildParamsItems==null) return null;
		for (int i=0;i<buildParamsItems.length;i++){
			if (parserName.equals(buildParamsItems[i].getNameAsParser()))
				return buildParamsItems[i];
		}
		return null;
	}

 */
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
    	VDTRunner runner = VDTLaunchUtil.getRunner();
		int numItem=runConfig.getBuildStep();
		BuildParamsItem buildParamsItem = runConfig.getArgumentsItemsArray()[numItem]; // uses already calculated
		//TODO: Handle monitor
		// Find console with name starting with consolePrefix
		IConsoleManager man = ConsolePlugin.getDefault().getConsoleManager(); // debugging
		IConsole[] consoles=(IConsole[]) man.getConsoles();
//		IOConsole iCons=null;
		iCons=null;
		consoleInStreamProxy=null;
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
        String[] arguments = runConfig.getToolArguments();
        if (arguments == null) arguments=new String[0];
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
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

//        IOConsoleInputStream inStream= iCons.getInputStream();
        IOConsoleOutputStream 	outStream= iCons.newOutputStream();
//        IProcess process=((ProcessConsole)iCons).getProcess();
        process=((ProcessConsole)iCons).getProcess();
//        IStreamsProxy consoleInStreamProxy= process.getStreamsProxy();
        consoleInStreamProxy= process.getStreamsProxy();
        int stderrParserIndex=getParserIndex(buildParamsItem.getStderr());
        int stdoutParserIndex=getParserIndex(buildParamsItem.getStdout());
        BuildParamsItem stderrParser=(stderrParserIndex>=0)?runConfig.getArgumentsItemsArray()[stderrParserIndex]:null;
        BuildParamsItem stdoutParser=(stdoutParserIndex>=0)?runConfig.getArgumentsItemsArray()[stdoutParserIndex]:null;
//        BuildParamsItem stderrParser=getParser(buildParamsItem.getStderr()); // re-parses all - why?
//        BuildParamsItem stdoutParser=getParser(buildParamsItem.getStdout());
        
        System.out.println("Using parser for stderr: "+((stderrParser!=null)?stderrParser.getNameAsParser():"none")); // actually may be the same as stdout
        System.out.println("Using parser for stdout: "+((stdoutParser!=null)?stdoutParser.getNameAsParser():"none"));
        
        processErr=null;
        processOut=null;
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
            stdoutStreamProxy= processOut.getStreamsProxy();
//TODO: Add error parsers            
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
            stderrStreamProxy= processErr.getStreamsProxy();
          //TODO: Add error parsers            
        }
        
        sendErrorsToStreamProxy=(stderrStreamProxy!=null)?stderrStreamProxy:stdoutStreamProxy;
        final IStreamsProxy fSendErrorsToStreamProxy=sendErrorsToStreamProxy;
        final IStreamsProxy fSendOutputToStreamProxy= stdoutStreamProxy;

// connect input streams of the parsers to the out from the console process         
        IStreamMonitor consoleOutStreamMonitor=null;
        IStreamMonitor consoleErrStreamMonitor=null;
        runConfig.resetConsoleText();
        String interrupt=buildParamsItem.getInterrupt(); // Not yet used
        runConfig.setConsoleFinish(buildParamsItem.getPrompt());
		System.out.println("Using console program termination string: \""+buildParamsItem.getPrompt()+"\"");
		errorListener=null;
        if (fSendErrorsToStreamProxy!=null){
        	consoleErrStreamMonitor=consoleInStreamProxy.getErrorStreamMonitor();
//        	IStreamListener errorListener=null;
        	errorListener=new IStreamListener(){
        		public void streamAppended(String text, IStreamMonitor monitor){
 //       			System.out.println("Err:'"+text+"'");
        			try {
        				fSendErrorsToStreamProxy.write(text);

        			} catch (IOException e) {
        				System.out.println("Can not write errors"); //happens for the last prompt got after finish marker 
        			}
        			if (runConfig.addConsoleText(text)){
        				System.out.println("Got finish sequence");
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
//        			System.out.println("Out:'"+text+"'");
        			try {
        				fSendOutputToStreamProxy.write(text);
        			} catch (IOException e) {
        				System.out.println("Can not write output");
        			}
        			if (runConfig.addConsoleText(text)){
        				System.out.println("Got finish sequence");
        				// TODO: launch continuation of the build process
        				finishConsolescript();
        			}
        		}
       		};
        	consoleOutStreamMonitor.addListener((IStreamListener) outputListener );
        }
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
        if (timeout>0){
        	System.out.println("Setting timeout "+timeout);
        	final int fTimeout = timeout;
        	new Timer().schedule(new TimerTask() {          
        		@Override
        		public void run() {
        			System.out.println(">>Timeout<<");
        			finishConsolescript();
        		}
        	}, fTimeout*1000);
        }
        return iCons;
	}

	
	// TODO: remove unneeded global vars
	
    public void finishConsolescript() {
		System.out.println("finishConsolescript()");
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
    	if (processErr!=null){
    		try {
				processErr.terminate();
			} catch (DebugException e) {
				System.out.println("Failed to reminate processErr parser process");
			}
    	}
    	if (processOut!=null){
    		try {
				processOut.terminate();
			} catch (DebugException e) {
				System.out.println("Failed to reminate processOut parser process");
			}
    	}
    	// Is that all?
//    	runConfig.setBuildStep(runConfig.getBuildStep()+1); // next task to run
    	int thisStep=runConfig.getBuildStep();
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
