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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy2;

import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

public class VDTConsolePlayback{
	private final VDTRunnerConfiguration runConfig;

    
	public VDTConsolePlayback (VDTRunnerConfiguration runConfig){
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

	
	public boolean runConsole(String consolePrefix
			, ILaunch launch
			, IProgressMonitor monitor 
			) throws CoreException{
        final boolean debugPrint=VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING);
    	VDTProgramRunner programRunner = runConfig.getProgramRunner();
		int numItem=runConfig.getPrevBuildStep(); // current is already icremented
		
		// TODO: process - null- normal run, "" - playback latest log, or timestamp - play selected log file(s)
		String playBackStamp=runConfig.getPlayBackStamp();
		if (playBackStamp==null){
			System.out.println("Wrong, it is not playback as playBackStamp==null ");
			return false;
		}
		BuildParamsItem buildParamsItem = runConfig.getArgumentsItemsArray()[numItem]; // uses already calculated
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
        // See if any parsers are configured       
        int stderrParserIndex=getParserIndex(buildParamsItem.getStderr());
        int stdoutParserIndex=getParserIndex(buildParamsItem.getStdout());
        BuildParamsItem stderrParser=(stderrParserIndex>=0)?runConfig.getArgumentsItemsArray()[stderrParserIndex]:null;
        BuildParamsItem stdoutParser=(stdoutParserIndex>=0)?runConfig.getArgumentsItemsArray()[stdoutParserIndex]:null;
        
        if (debugPrint) {
        	System.out.println("Using parser for stderr: "+((stderrParser!=null)?stderrParser.getNameAsParser():"none")); // actually may be the same as stdout
        	System.out.println("Using parser for stdout: "+((stdoutParser!=null)?stdoutParser.getNameAsParser():"none"));
        }
        
        if ((stderrParser==null) && (stdoutParser==null)){
        	return false; // no parsers, no playback
        }
        
        // Open logfiles for reading (if available)        
        boolean twoFiles=(stdoutParser!=null) && (stderrParser!=null); 
        ToolLogFile toolLogFile=new ToolLogFile (
        				runConfig.getLogDir(),
        				runConfig.getToolName(),
        				buildParamsItem.getLogName(),
        				null,    // extension - use default
        				(stdoutParser!=null), //boolean useOut,
        				(stderrParser!=null), //boolean useErr,
        				playBackStamp);//String buildStamp
        FileReader outLogReader=toolLogFile.getOutReader();
        FileReader errLogReader=toolLogFile.getErrReader();
        if ((outLogReader==null) && (errLogReader==null)){
            if (debugPrint) {
            	System.out.println(
            			"No logs available in "+runConfig.getLogDir()+
            			" for tool "+runConfig.getToolName()+
            			((buildParamsItem.getLogName()!=null)?(" ("+buildParamsItem.getLogName()+")"):"")+
            			((playBackStamp.length()>0)?(" timestamp: "+playBackStamp):""));
            }        	
        	return false; // no parsers, no playback
        }
        if (twoFiles){
        	if (outLogReader==null) stdoutParser=null; // no log available
        	if (errLogReader==null) stderrParser=null; // no log available
        }
        final String outLogName=toolLogFile.getOutLogName();
        final String errLogName=toolLogFile.getErrLogName();
    	VDTRunner runner = VDTLaunchUtil.getRunner();
    	String msg="Playing back log file from "+runConfig.getLogDir()+
    			" for tool "+runConfig.getToolName()+
    			((buildParamsItem.getLogName()!=null)?(" ("+buildParamsItem.getLogName()+")"):"")+
    			((playBackStamp.length()>0)?(" timestamp: "+playBackStamp):"");
        runner.log(msg, null, null, false, true); // Appears in the console of the target Eclipse (may be immediately erased)
        if (debugPrint) runner.log(msg, null, null, false, false); // Appears in the console of the parent Eclipse

        IProcess processErr=null;
        IProcess processOut=null;
        IStreamsProxy2 stdoutStreamProxy=null;
        IStreamsProxy2 stderrStreamProxy=null;
        if (stdoutParser!=null){
			List<String> toolArgumentsStdout = new ArrayList<String>();
			List<String> stdoutArguments=stdoutParser.getParamsAsList();
			if (stdoutArguments != null)
				toolArgumentsStdout.addAll(stdoutArguments);
			// overwriting runConfig, but this is done sequentially, so OK
			runConfig.setToolArguments((String[])toolArgumentsStdout.toArray(new String[toolArgumentsStdout.size()]));
        	processOut=programRunner.run(runConfig,
        			"replay log "+outLogName,
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
        			"replay err log "+errLogName,
        			launch,
        			null, //monitor);
        			stderrParserIndex);
            stderrStreamProxy= (IStreamsProxy2) processErr.getStreamsProxy();
          //TODO: Add error parsers            
        }
        
        IStreamsProxy2 sendErrorsToStreamProxy=(stderrStreamProxy!=null)?stderrStreamProxy:stdoutStreamProxy;
        final IStreamsProxy2 fSendErrorsToStreamProxy=sendErrorsToStreamProxy;
        final IStreamsProxy2 fSendOutputToStreamProxy= stdoutStreamProxy;
        
        BufferedReader outBufReader=null;
        BufferedReader errBufReader=null;
        try {
        	outBufReader=((outLogReader!=null) && (fSendOutputToStreamProxy!=null))? (new BufferedReader(outLogReader)):null;
        	errBufReader=((errLogReader!=null) && (fSendErrorsToStreamProxy!=null))? (new BufferedReader(errLogReader)):null;
        } catch (Exception e) {
        	System.out.println ("Failed to create BufferedReader");
        }
        // Read logs should be fast, not sure how much will be buffered in IStreamsProxy2, so sending logs
        // one after another in the same thread
        if (errBufReader!=null){
        	int dbgBytes=0;
        	String line;
        	try {
        		while ((line = errBufReader.readLine()) != null){
        			fSendErrorsToStreamProxy.write(line+"\n");
        			dbgBytes+=line.length()+1;
        			//if(debugPrint) System.out.println ("err->>"+line);

        		}
        	} catch (IOException e) {
        		System.out.println ("Failed to replay "+errLogName+" to error parser");
        	} finally {
        		try {
					errBufReader.close();
					if(debugPrint) System.out.println ("err->>CLOSED, got "+dbgBytes+" bytes");
				} catch (IOException e) {
	        		System.out.println ("Failed to close "+errLogName+" BufferedReader");
				}
        	}
        }
        
        if (outBufReader!=null){
        	int dbgBytes=0;
        	String line;
        	try {
        		while ((line = outBufReader.readLine()) != null){
        			fSendOutputToStreamProxy.write(line+"\n");
        			dbgBytes+=line.length()+1;
 //       			if(debugPrint) System.out.println ("out->>"+line);
        		}
        	} catch (IOException e) {
        		System.out.println ("Failed to replay "+outLogName+" to output parser");
        	} finally {
        		try {
					outBufReader.close();
					if(debugPrint) System.out.println ("out->>CLOSED, got "+dbgBytes+" bytes"); // bytes OK, always the same
				} catch (IOException e) {
	        		System.out.println ("Failed to close "+outLogName+" BufferedReader");
				}
        	}
        }

        if (stdoutStreamProxy!=null) try {
        	// Checked that all is sent through write() method, by end of data is often lost when calling closeInputStream() too soon AFTER
        	// Less visible when the source is doing something, more - here, when it is just a play back
        	if(debugPrint) System.out.println("mitigating possible closeInputStream() bug - sleeping "+VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY+" ms");
        	Thread.sleep(VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY);
        	stdoutStreamProxy.closeInputStream();
//        	System.out.println ("closed output stream proxy");
        } catch (IOException e){
        	System.out.println ("Failed to close output stream proxy");
        } catch (InterruptedException e) {
		}
        // next uses stderrStreamProxy, not fSendErrorsToStreamProxy as it can be the same as fSendOutputToStreamProxy
        if (stderrStreamProxy!=null) try {
        	if(debugPrint) System.out.println("mitigating possible closeInputStream() bug - sleeping "+VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY+" ms");
        	Thread.sleep(VDTLaunchUtil.CLOSE_INPUT_STREAM_DELAY);
        	stderrStreamProxy.closeInputStream(); 
        	System.out.println ("closed error stream proxy"); // ???
        } catch (IOException e){
        	System.out.println ("Failed to close error stream proxy");
        } catch (InterruptedException e) {
			e.printStackTrace();
		}

        return true;
	}
    
    
 } // class VDTConsolePlayback
