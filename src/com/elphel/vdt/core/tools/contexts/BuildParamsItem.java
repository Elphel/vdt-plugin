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
package com.elphel.vdt.core.tools.contexts;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

public class BuildParamsItem implements Cloneable{
	private String [] params;
	private String consoleName;  // null for external tools running in a new console
    private String name;         // name of a block
    private boolean is_parser;
    
    private String toolErrors;   // Eclipse pattern for pattern recognizer
    private String toolWarnings; // Eclipse pattern for pattern recognizer
    private String toolInfo;     // Eclipse pattern for pattern recognizer

    private String instCapture;  // RegEx to extract hierarchical name form tool output (may need extra "()" around)
    private String instSeparator;// RegEx to split name segments (i.e. "\.")
    private String instSuffix;   // RegEx to remove tool-generated name suffixes, like "_reg|_suffix2|suffix3" 
    
    // for commands being sent to opened remote console:
    private String prompt;       // relevant for commands sent to remote console - double prompt means "done" (extra separator on input) 
    private String interrupt;    // control character(s) to interrupt console command 
    private String stderr;       // name of the command to (command line block) to launch in a separate process/console
                                 // and connect to stderr of the terminal session 
    private String stdout;       // name of the command to (command line block) to launch in a separate process/console
                                 // and connect to stderr of the terminal session

	private int timeout;         // timeout for console tasks, in seconds
	private String success;
	private String failure;
	private boolean keep_open;
	private String logFile;
	
	private Timer timer=null;
	
	
	public BuildParamsItem (
			String [] params,
			String consoleName,
		    String name,
		    String toolErrors,
		    String toolWarnings,
		    String toolInfo,
		    String instCapture,
		    String instSeparator,
		    String instSuffix, 
		    String prompt, 
		    String interrupt, 
		    String stderr,
		    String stdout,
		    int timeout,
			String success,
			String failure,
			boolean keep_open,
			String logFile
			) {
		this.consoleName=consoleName;
		this.params=params; // no need to clone?
		this.name=name;
		this.is_parser=(name!=null); // true
		this.toolErrors=toolErrors;
		this.toolWarnings=toolWarnings;
		this.toolInfo=toolInfo;
        this.instCapture=instCapture;
        this.instSeparator=instSeparator;
        this.instSuffix=instSuffix; 
		this.prompt=prompt; 
		this.interrupt=interrupt; 
		this.stderr=stderr;
		this.stdout=stdout;
		this.timeout=timeout;
		this.success=success;
		this.failure=failure;
		this.keep_open=keep_open;
		this.logFile=logFile;
		
	}
	public BuildParamsItem (BuildParamsItem item){
		this (
				item.params,
				item.consoleName,
				item.name,
				item.toolErrors,
				item.toolWarnings,
				item.toolInfo,
				item.instCapture,
				item.instSeparator,
				item.instSuffix, 
				item.prompt,
				item.interrupt,
				item.stderr,
				item.stdout,
				item.timeout,
				item.success,
				item.failure,
				item.keep_open,
				item.logFile
				);
		this.is_parser=item.is_parser;
	}

	public BuildParamsItem clone () {
		return new BuildParamsItem(this);
	}
	
	public String [] getParams(){
		return params;
	}
	public List<String> getParamsAsList(){
	    List<String> arguments = new java.util.ArrayList<String>(params.length);
	    for(int i = 0; i < params.length; i++) {
	        arguments.add(params[i]);
	    }
	    return arguments;
	}

	public String getConsoleName(){
		return consoleName;
	}
	public void removeNonParser(List<BuildParamsItem> items){
//		if (nameAsParser==null) return;
		if (!is_parser) return;
		if (consoleName==null) {  // console script can not be a parser
	        Iterator<BuildParamsItem> itemsIter = items.iterator(); // command lines block is empty (yes, there is nothing in project output)
	        while(itemsIter.hasNext()) {
	        	BuildParamsItem item = (BuildParamsItem)itemsIter.next();
				if( name.equals(item.stderr) ||
					name.equals(item.stdout)){
					return; // do nothing - keep nameAsParser
				}
			}
		}
		is_parser=false;
	}

	public String getNameAsParser() { return is_parser?name:null; }
	public boolean isParser()       {return is_parser;}
	public String getName()         { return name; }
	public String getErrors()       { return toolErrors; }
	public String getWarnings()     { return toolWarnings; }
	public String getInfo()         { return toolInfo; }
	public String getinstCapture()  { return instCapture; }
	public String getInstSeparator(){ return instSeparator; }
	public String getInstSuffix()   { return instSuffix; }
	public String getPrompt()       { return prompt; }
	public String getInterrupt()    { return interrupt; }
	public String getStderr()       { return stderr; }
	public String getStdout()       { return stdout; }
	public int    getTimeout()      { return timeout; }
	
	public String getSuccessString(){ return success; }
	public String getFailureString(){ return failure; }
	public boolean keepOpen()       { return keep_open; }
	public String getLogName()      { return logFile; }
	
	//			String logFile

	
	public Timer getTimer(){
		if (timer==null){
			timer=new Timer();
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
				System.out.println("BuildParamsitem(): : making new timer");
		}
		return timer;
	}
	public void cancelTimer(){
		if (timer==null) return;
		timer.cancel();
		timer=null;
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
			System.out.println("BuildParamsitem(): canceled and nulled timer");
	}
	
	public void finalize() throws Throwable{
		cancelTimer();
		super.finalize();
	}
}
