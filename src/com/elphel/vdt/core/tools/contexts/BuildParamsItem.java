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
package com.elphel.vdt.core.tools.contexts;

import java.util.Iterator;
import java.util.List;

public class BuildParamsItem implements Cloneable{
	private String [] params;
	private String consoleName;  // null for external tools running in a new console
    private String nameAsParser; // name as a parser, null if not used as a parser
//    private String mark;         // remove this sequence on the output only (to preserve white spaces) Already applied
    private String toolErrors;   // Eclipse pattern for pattern recognizer
    private String toolWarnings; // Eclipse pattern for pattern recognizer
    private String toolInfo;     // Eclipse pattern for pattern recognizer
    // for commands being sent to opened remote console:
    private String prompt;       // relevant for commands sent to remote console - double prompt means "done" (extra separator on input) 
    private String interrupt;    // control character(s) to interrupt console command 
    private String stderr;       // name of the command to (command line block) to launch in a separate process/console
                                 // and connect to stderr of the terminal session 
    private String stdout;       // name of the command to (command line block) to launch in a separate process/console
                                 // and connect to stderr of the terminal session

	
	
	public BuildParamsItem (
			String [] params,
			String consoleName,
		    String nameAsParser,
//		    String mark,
		    String toolErrors,
		    String toolWarnings,
		    String toolInfo,
		    String prompt, 
		    String interrupt, 
		    String stderr,
		    String stdout
			) {
		this.consoleName=consoleName;
		this.params=params; // no need to clone?
		this.nameAsParser=nameAsParser;
//		this.mark=mark;
		this.toolErrors=toolErrors;
		this.toolWarnings=toolWarnings;
		this.toolInfo=toolInfo;
		this.prompt=prompt; 
		this.interrupt=interrupt; 
		this.stderr=stderr;
		this.stdout=stdout;
		
	}
	public BuildParamsItem (BuildParamsItem item){
		this (
				item.params,
				item.consoleName,
				item.nameAsParser,
//				item.mark,
				item.toolErrors,
				item.toolWarnings,
				item.toolInfo,
				item.prompt,
				item.interrupt,
				item.stderr,
				item.stdout
				);
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
/*	
	public void applyMark(){
		if ((mark==null) || (mark.length()==0)) return;
		if (params!=null) {
			for (int i=0;i<params.length;i++){
				params[i].replace(mark, "");
			}
		}
		if (prompt!=null) prompt.replace(mark, "");
	}
*/	
	public void removeNonParser(List<BuildParamsItem> items){
		//		if (items==null) return; should never happen as the list includes itself
		if (nameAsParser==null) return;
		if (consoleName==null) {  // console script can not be a parser
	        Iterator<BuildParamsItem> itemsIter = items.iterator(); // command lines block is empty (yes, there is nothing in project output)
	        while(itemsIter.hasNext()) {
	        	BuildParamsItem item = (BuildParamsItem)itemsIter.next();
				if(	
						nameAsParser.equals(item.stderr) ||
						nameAsParser.equals(item.stdout)){
					return; // do nothing - keep nameAsParser
				}
			}
		}
		nameAsParser=null;
	}

	public String getNameAsParser(){ return nameAsParser; }
//	public String getMark()        { return mark; }
	public String getErrors()      { return toolErrors; }
	public String getWarnings()    { return toolWarnings; }
	public String getInfo()        { return toolInfo; }
	public String getPrompt()      { return prompt; }
	public String getInterrupt()   { return interrupt; }
	public String getStderr()      { return stderr; }
	public String getStdout()      { return stdout; }
}
