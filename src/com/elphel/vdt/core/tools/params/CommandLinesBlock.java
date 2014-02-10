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
package com.elphel.vdt.core.tools.params;

import java.nio.charset.StandardCharsets;
import java.util.*;

import com.elphel.vdt.core.tools.Updateable;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.conditions.*;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.core.tools.params.types.ParamTypeString.KIND;
   
public class CommandLinesBlock extends UpdateableStringsContainer 
                               implements Cloneable 
{
    private String contextName;
    private String name;
    private String destination;  // now references either file for command file, or console name prefix to send data to
    private String separator;
    private KIND kind; //command file name or console name
    
    private String mark;         // remove this sequence on the output only (to preserve white spaces)
    private String toolErrors;   // Eclipse pattern for pattern recognizer
    private String toolWarnings; // Eclipse pattern for pattern recognizer
    private String toolInfo;     // Eclipse pattern for pattern recognizer
    // for commands being sent to opened remote console:
    private String prompt;       // relevant for commands sent to remote console - double prompt means "done" (extra separator on input) 
    private String stderr;       // name of the command to (command line block) to launch in a separate process/console
                                 // and connect to stderr of the terminakl session 
    private String stdout;       // name of the command to (command line block) to launch in a separate process/console
                                 // and connect to stderr of the terminal session
    // If both are specified and pointing to the same command block - two instances/consoles will be launched.
    // if only stdout - both stdout and stdin of a session will go to the same process/console
    private String interrupt;    // send this to remote terminal to interrupt execution (parses use \xNN)
	private String timeout;      // timeout for console tasks, in seconds

	private String success;
	private String failure;
	private boolean keep_open;
	private String logPath;
	
    public CommandLinesBlock(String contextName,
                             String name,
                             String destination,
                             KIND kind,
                             String sep,
                             String mark,
                             String toolErrors,
                             String toolWarnings,
                             String toolInfo,
                             String prompt, 
                             String interrupt, 
                             String stderr,
                             String stdout,
                             String timeout,
                         	 String success,
                         	 String failure,
                         	 boolean keep_open,
                         	 String logPath,
                             ConditionalStringsList lines,
                             ConditionalStringsList deleteLines,
                             List<NamedConditionalStringsList> insertLines)
    {
        super(lines, deleteLines, insertLines);
        
        this.contextName = contextName;
        this.name = name;
        this.destination = destination;
        this.kind=kind;
        this.separator = sep;
        this.mark=mark;
        this.toolErrors=toolErrors;
        this.toolWarnings=toolWarnings;
        this.toolInfo=toolInfo;
        this.prompt=prompt; 
        this.interrupt=interrupt;
        this.stderr=stderr;
        this.stdout=stdout;
        this.timeout=timeout;
        this.success=success;
        this.failure=failure;
        this.keep_open=keep_open;
        this.logPath=logPath;
        if(this.separator != null) {
        	this.separator = parseCntrl(this.separator);
        }
        if(this.interrupt != null) {
        	this.interrupt = parseCntrl(this.interrupt);
        }
        
    }

    public static String parseCntrl(String str){
    	if (str==null) return null;
    	str=str.replace("\\n" ,"\n");
    	str=str.replace("\\t", "\t");
    	while (true){
    		int start=str.indexOf("\\x");
    		if (start<0) break;
    		String s= new String(new byte[]{ Byte.parseByte(str.substring(start+2,start+4),16) }, StandardCharsets.US_ASCII);
    		str=str.replace(str.subSequence(start,start+4),s);
    	}
    	return str;
    }
    public String applyMark(String str){
    	if ((mark!=null) && (str!=null)){
    		str=str.replace(mark, "");
    	}
    	return str;
    }
    public CommandLinesBlock(CommandLinesBlock block) {
        this(block.contextName,
             block.name,
             block.destination,
             block.kind,
             block.separator,
             block.mark,
             block.toolErrors,
             block.toolWarnings,
             block.toolInfo,
             block.prompt, 
             block.interrupt, 
             block.stderr,
             block.stdout,
             block.timeout,
             block.success,
             block.failure,
             block.keep_open,
             block.logPath,
             block.strings != null?
                     (ConditionalStringsList)block.strings.clone() : null,
             block.deleteStrings != null?
                     (ConditionalStringsList)block.deleteStrings.clone() : null,
             block.insertStrings != null?
                     new ArrayList<NamedConditionalStringsList>(block.insertStrings) : null);
                     // TODO: review this line!!!
    }
    
    public void init(Config config) throws ConfigException {
        Context context = config.getContextManager().findContext(contextName);
        
        if(context == null)
            throw new ConfigException("Internal fault: parent context '" + contextName + 
                                    "' of command block '" + name + 
                                    "' is not found in the configuration");
            
        if(destination != null && !destination.equals("")) {
            Parameter param = context.findParam(destination);
                    
            if(param == null) {
                throw new ConfigException("Destination parameter '" + destination + 
                                          "' used for command line of context '" + context.getName() + 
                                          "' is absent");
            } else if(!(param.getType() instanceof ParamTypeString)) {
                throw new ConfigException("Destination parameter '" + destination + 
                                          "' used for command line of context '" + context.getName() +
                                          "' must be of type '" + ParamTypeString.NAME + 
                                          "'");                    
            } else {
            	kind=((ParamTypeString)param.getType()).getKind();
            	if(kind != ParamTypeString.KIND.FILE) {
            		if(((ParamTypeString)param.getType()).getKind() != ParamTypeString.KIND.TEXT) { // Andrey - adding console name option
            			throw new ConfigException("Destination parameter '" + destination + 
            					"' of type '" + ParamTypeString.NAME +
            					"' used for command line of context '" + context.getName() +
            					"' must be of kind '" + ParamTypeString.KIND_FILE_ID +
            					"' or '" + ParamTypeString.KIND_TEXT_ID +
            					"'");                    
            		}
 //           		System.out.println("Got string text kind for command block (for console name)");
            	}
            }
        }
    }
    
    public Object clone() {
        return new CommandLinesBlock(this);
    }
    
    public boolean matches(Updateable other) {
        return name.equals(((CommandLinesBlock)other).name);
    }

    public String getDestination() { return destination; }
    public KIND getKind() { return kind;}
    public boolean isFileKind() {return kind ==  ParamTypeString.KIND.FILE; }
    public boolean isConsoleKind() { return kind ==  ParamTypeString.KIND.TEXT; }
    public List<String> getLines() { return ConditionUtils.resolveConditionStrings(strings);  }
    // to distinguish between empty command block (program w/o any parameters) and conditionally removed one
    public boolean hadStrings() {return (strings!=null) && (strings.getEntries().size()>0); }
    public String getName()        { return name; }    
    public String getSeparator()   { return separator; }    
	public String getMark()        { return mark; }
	public String getErrors()      { return toolErrors; }
	public String getWarnings()    { return toolWarnings; }
	public String getInfo()        { return toolInfo; }
	public String getPrompt()      { return prompt; }
	public String getInterrupt()   { return interrupt; }
	public String getStderr()      { return stderr; }
	public String getStdout()      { return stdout; }
	public String getTimeout()     { return timeout; }
    
	public String getSuccessString() { return success; }
	public String getFailureString() { return failure; }
    public boolean isKeepOpen()      { return keep_open; }
	public String getLogPath() { return logPath; }
    
    public boolean isEnabled() {
        if(destination == null) // command line
            return true;
        return !destination.equals("");
    }
    
    public void update(Updateable from) throws ConfigException {
        CommandLinesBlock block = (CommandLinesBlock)from;
        
        if(name == null) 
            throw new NullPointerException("name == null");

        if(contextName == null)
            contextName = block.contextName;
        
        if(separator == null)
            separator = block.separator;

        super.update(from);
    }
}
