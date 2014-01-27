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
    private String destination;
    private String separator;
    private KIND kind; //command file name or console name
        
    public CommandLinesBlock(String contextName,
                             String name,
                             String destination,
                             KIND kind,
                             String sep,
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
        
        if(separator != null) {
            separator = separator.replace("\\n", "\n");
            separator = separator.replace("\\t", "\t");
        }
    }

    public CommandLinesBlock(CommandLinesBlock block) {
        this(block.contextName,
             block.name,
             block.destination,
             block.kind,
             block.separator,
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
            		System.out.println("Got string text kind for command block (for console name)");
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

    public String getDestination() {
        return destination;
    }

    public KIND getKind() {
        return kind;
    }

    public boolean isFileKind() {
        return kind ==  ParamTypeString.KIND.FILE;
    }

    public boolean isConsoleKind() {
        return kind ==  ParamTypeString.KIND.TEXT;
    }

    public List<String> getLines() {
        return ConditionUtils.resolveConditionStrings(strings);
    }
    
    public String getName() {
        return name;
    }    
    
    public String getSeparator() {
        return separator;
    }    
    
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
