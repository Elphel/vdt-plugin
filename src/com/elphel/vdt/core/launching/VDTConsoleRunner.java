/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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
package com.elphel.vdt.core.launching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.debug.internal.ui.views.console.ProcessConsole;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy2;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.Tool.TOOL_STATE;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

public class VDTConsoleRunner{
	private final VDTRunnerConfiguration runConfig; //*
	private IProcess processErr=null; //*
	private IProcess processOut=null; //*
	private IStreamsProxy2 sendErrorsToStreamProxy=null;
//	private Object errorListener=null; //+
	private IStreamListener errorListener=null; //+
	private IStreamListener outputListener=null; //+
    private IStreamsProxy2 consoleInStreamProxy= null; //+
    private Timer timer;
    private IStreamsProxy2 stdoutStreamProxy=null;
    private IStreamsProxy2 stderrStreamProxy=null;
    private ToolLogFile toolLogFile =null;

    
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

	
	public IOConsole runConsole(
			String consolePrefix,
			ILaunch launch,
			IProgressMonitor monitor 
			) throws CoreException{
        final boolean debugPrint=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING);
    	VDTRunner runner = VDTLaunchUtil.getRunner();
    	VDTProgramRunner programRunner = runConfig.getProgramRunner();
		int numItem=runConfig.getPrevBuildStep();
		
		// TODO: process - null- normal run, "" - playback latest log, or timestamp - play selected log file(s)
		String playBackStamp=runConfig.getPlayBackStamp();
		if (playBackStamp!=null){
			System.out.println("Wrong, it should be playback, not run, as playBackStamp = "+playBackStamp+ "(not null)");
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
		}
		
		
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
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
			return null;
		}
		// try to send 
        String[] arguments = runConfig.getToolArguments();
        if (arguments == null) arguments=new String[0];
        if (debugPrint) {
        	if (arguments!=null){
        		for (int i=0;i<arguments.length;i++){
        			System.out.println("Console line "+i+" = \""+arguments[i]+"\"");
        		}
        	}
        }
        runner.log("Writing to console "+iCons.getName()+":", arguments, null, false, true); // Appears in the console of the target Eclipse (may be immediately erased)
        if (debugPrint) runner.log("Writing to console "+iCons.getName()+":", arguments, null, false, false); // Appears in the console of the parent Eclipse
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
        stdoutStreamProxy=null;
        stderrStreamProxy=null;
        if (stdoutParser!=null){
			List<String> toolArgumentsStdout = new ArrayList<String>();
			List<String> stdoutArguments=stdoutParser.getParamsAsList();
			if (stdoutArguments != null)
				toolArgumentsStdout.addAll(stdoutArguments);
			// overwriting runConfig, but this is done sequentially, so OK
			runConfig.setToolArguments((String[])toolArgumentsStdout.toArray(new String[toolArgumentsStdout.size()]));
        	processOut=programRunner.run(runConfig,
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
        	processErr=programRunner.run(runConfig,
        			"ERR for "+iCons.getName(),
        			launch,
        			null, //monitor);
        			stderrParserIndex);
            stderrStreamProxy= (IStreamsProxy2) processErr.getStreamsProxy();
          //TODO: Add error parsers            
        }
        final boolean fHasStdout=(stdoutParser!=null);
        final boolean fHasStderr=(stderrParser!=null);
        
        sendErrorsToStreamProxy=(stderrStreamProxy!=null)?stderrStreamProxy:stdoutStreamProxy;
        final IStreamsProxy2 fSendErrorsToStreamProxy=sendErrorsToStreamProxy;
        final IStreamsProxy2 fSendOutputToStreamProxy= stdoutStreamProxy;

// connect input streams of the parsers to the out from the console process         
        runConfig.resetConsoleText();
        String interrupt=buildParamsItem.getInterrupt(); // Not yet used
        runConfig.setConsoleFinish(buildParamsItem.getPrompt());
        runConfig.setConsoleBad(buildParamsItem.getFailureString());
        runConfig.setConsoleGood(buildParamsItem.getSuccessString());
        boolean keepOpen=     buildParamsItem.keepOpen();
        if (keepOpen){
        	// TODO: Reuse keepOpen for other meaning?
        	MessageUI.error("keep-open is not supported for terminal scripts (it always keeps it open, ignoring");
        }

        if (debugPrint) {
        	System.out.println("Using console program termination string: \""+buildParamsItem.getPrompt()+"\"");
        	System.out.println("Using success string: \""+buildParamsItem.getSuccessString()+"\"");
        	System.out.println("Using failure string: \""+buildParamsItem.getFailureString()+"\"");
        }
        String timeStamp=ToolsCore.getTool(runConfig.getToolName()).getTimeStamp();
        toolLogFile=(((fSendOutputToStreamProxy!=null) || (fSendErrorsToStreamProxy!=null)))?
        		(new ToolLogFile (
        				true,
        				runConfig.getLogDir(),
        				runConfig.getToolName(),
        				buildParamsItem.getLogName(),
        				null,    // extension - use default
        				fHasStdout, // fSendOutputToStreamProxy!=null, //boolean useOut,
        				fHasStderr, // fSendErrorsToStreamProxy!=null, //boolean useErr, WRONG
        				timeStamp)) : null;//String buildStamp - use the one from the tool

        final IStreamMonitor consoleErrStreamMonitor=consoleInStreamProxy.getErrorStreamMonitor();
        errorListener=new IStreamListener(){
        	public void streamAppended(String text, IStreamMonitor monitor){
        		if (fSendErrorsToStreamProxy!=null) {
        			try {
        				fSendErrorsToStreamProxy.write(text);

        			} catch (IOException e) {
        				if (debugPrint) 	System.out.println("Can not write errors"); //happens for the last prompt got after finish marker
        			}
        		}
        		if (toolLogFile!=null){
        			toolLogFile.appendErr(text);
        		}
        		if (runConfig.addConsoleText(text)){
        			if (debugPrint)  System.out.println("Got finish sequence");
        			consoleErrStreamMonitor.removeListener(errorListener);
        			finishConsolescript(); // got here when computer running Vivado was disconnected
        		}
        	}
        };
        if (processErr!=null) VDTLaunchUtil.getRunner().getRunningBuilds().addMonListener( // to remove listener when parser is terminated
        		DebugUITools.getConsole(processErr), //IConsole parserConsole,
        		consoleErrStreamMonitor,
        		errorListener);
        consoleErrStreamMonitor.addListener(errorListener);
        outputListener=null;
        //        if (fSendOutputToStreamProxy!=null){
        final IStreamMonitor consoleOutStreamMonitor=consoleInStreamProxy.getOutputStreamMonitor();
        outputListener=new IStreamListener(){
        	public void streamAppended(String text, IStreamMonitor monitor){
        		if (fSendOutputToStreamProxy!=null){
        			try {
        				fSendOutputToStreamProxy.write(text);
        			} catch (IOException e) {
        				if (debugPrint) System.out.println("Can not write output");
        			}
        		}
        		if (toolLogFile!=null){
        			toolLogFile.appendOut(text);
        		}
        		if (runConfig.addConsoleText(text)){
        			if (debugPrint) System.out.println("Got finish sequence");
        			// TODO: launch continuation of the build process
        			consoleOutStreamMonitor.removeListener(outputListener);
        			finishConsolescript();
        		}
        	}
        };
        if (processOut!=null) VDTLaunchUtil.getRunner().getRunningBuilds().addMonListener( // to remove listener when parser is terminated null pointer
        		DebugUITools.getConsole(processOut), //IConsole parserConsole,
        		consoleOutStreamMonitor,
        		outputListener);
        
        consoleOutStreamMonitor.addListener(outputListener ); // got frozen here
        //       }
        // Problems occurred when invoking code from plug-in: "org.eclipse.ui.console".
        // Exception occurred during console property change notification.
        // Trying to fix Illegal THread Access
        
        final String [] fArguments=arguments;
        final IOConsoleOutputStream fOutStream=	outStream;
        final IOConsole fICons=iCons;
    	Display.getDefault().syncExec(new Runnable() {
    		public void run() {
    			fOutStream.setColor(new Color(null, 128, 128, 255)); // org.eclipse.swt.SWTException: Invalid thread access
    	        try {
    	        	for (int i=0;i<fArguments.length;i++){
    	        		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.LOCAL_ECHO)) {
    	        			fOutStream.write(fArguments[i]+"\n"); // writes to console itself
//    	        			System.out.println("--->"+fArguments[i]+"\n");
    	        		}
    	        		consoleInStreamProxy.write(fArguments[i]+"\n");
    	        	}
    	        } catch (IOException e) {
    	        	System.out.println("Can not write to outStream of console "+fICons.getName());
    	        }
    		}
    	});
        
        int timeout=buildParamsItem.getTimeout();
        if ((timeout==0) && (buildParamsItem.getPrompt()==null)) timeout=1;// should specify at least one of timeout or prompt
        timer=null;
        if (timeout>0){
        	if (debugPrint) System.out.println("Setting timeout "+timeout);
        	final int fTimeout = timeout;
        	timer=new Timer();
        	System.out.println("VDTConsoleRunner(): setting timer "+fTimeout*1000);
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
        final boolean debugPrint=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING);
    	if (debugPrint) System.out.println("finishConsolescript()");
		String playBackStamp=runConfig.getPlayBackStamp();
		if (playBackStamp!=null){
			// happened when Vivado console was disconnected with old console listener still attached
			// they should be removed when a parser process is terminated
			System.out.println("Wrong, it should be playback, not run, as playBackStamp = "+playBackStamp+ "(not null)");
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
			return;
		}
		if (timer!=null){
			timer.cancel();
		}
        if (toolLogFile!=null){
        	toolLogFile.close();
        	if (debugPrint) System.out.println("Closing log file writers");
        }
    	if (consoleInStreamProxy==null) {
    		System.out.println("Bug: consoleInStreamProxy == null");
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
    		return; // or continue other commands?
    	}
    	if (errorListener !=null) { // disconnect error stream listener
    		IStreamMonitor consoleErrorStreamMonitor=consoleInStreamProxy.getOutputStreamMonitor();
    		consoleErrorStreamMonitor.removeListener(errorListener);
    	}
    	if (outputListener !=null) { // disconnect output stream listener
    		IStreamMonitor consoleOutStreamMonitor=consoleInStreamProxy.getOutputStreamMonitor();
    		consoleOutStreamMonitor.removeListener(outputListener);
    	}
    	// terminate parser(s). Do those console listeners (parsers) have to be removed too?
    	// TODO: Maybe wait for the process (small time) to terminate by disconnecting it's stdin
    	// Now end of the parsing may be lost if it did not have enough time to finish.
    	//IStreamsProxy2.closeInputStream()
    	// Disconnection worked !!
    	
    	if (stderrStreamProxy!=null){
    		try {
            	// Checked that all is sent through write() method, by end of data is often lost when calling closeInputStream() too soon AFTER
            	if(debugPrint) System.out.println("mitigating possible closeInputStream() bug - sleeping "+VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY+" ms");
            	try {
					Thread.sleep(VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY);
				} catch (InterruptedException e) {
				}
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
            	// Checked that all is sent through write() method, by end of data is often lost when calling closeInputStream() too soon AFTER
            	if(debugPrint) System.out.println("mitigating possible closeInputStream() bug - sleeping "+VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY+" ms");
            	try {
					Thread.sleep(VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY);
				} catch (InterruptedException e) {
					System.out.println("Failed Thread.sleep  for "+ VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY);
				}
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

//    	int thisStep=runConfig.getAndIncBuildStep(); // points to current, increments pointer
    	int thisStep=runConfig.getPrevBuildStep(); // points to current, increments pointer
    	if (debugPrint) System.out.println("Finished console task, step was "+thisStep);
    	// Update tool status - until it is running there will be no actual changes on the icon view - will still spin
    	Tool tool=ToolsCore.getTool(runConfig.getToolName());
		BuildParamsItem buildParamsItem = runConfig.getArgumentsItemsArray()[thisStep];
		
		if (debugPrint)  {
    		System.out.println("gotBad()="+runConfig.gotBad()+" gotGood()="+runConfig.gotGood());
    		System.out.println("consoleBuffer=\""+runConfig.getConsoluBuffer()+"\"");
    	}
		
    	if (runConfig.gotBad()|| (
    					!runConfig.gotGood() &&
    					(buildParamsItem.getSuccessString()!=null) &&
    					(buildParamsItem.getFailureString()==null))){
    		VDTLaunchUtil.getRunner().abortLaunch(runConfig.getOriginalConsoleName());    		
    		return;
    	}
    	if (runConfig.gotGood() || 
    			((buildParamsItem.getFailureString()!=null) && // was only looking for bad, otherwise OK
    			 (buildParamsItem.getSuccessString()==null))
    			){
    		tool.setDirty(false);
    		tool.setState(TOOL_STATE.SUCCESS);
    	} else 	if (buildParamsItem.getSuccessString()!=null){
    		tool.setDirty(false);
    		tool.setState(TOOL_STATE.UNKNOWN);
    	}
    	// Go on, continue with the sequence (maybe nothing is left
    	
//    	runConfig.setBuildStep(thisStep+1); // next task to run
//    	runConfig.updateBuildStep(); // set buildstep to incremented AtomicInteger, enable continuation;
    	VDTLaunchUtil.getRunner().getRunningBuilds().saveUnfinished(runConfig.getOriginalConsoleName(), runConfig );
    	try {
    		VDTLaunchUtil.getRunner().resumeLaunch(runConfig.getOriginalConsoleName(),thisStep+1); // replace with console
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    
    
 } // class VDTConsoleRunner
