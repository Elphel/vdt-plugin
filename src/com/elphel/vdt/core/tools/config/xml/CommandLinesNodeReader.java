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
package com.elphel.vdt.core.tools.config.xml;

import java.util.*;

import org.w3c.dom.Node;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.CommandLinesBlock;
import com.elphel.vdt.core.tools.params.conditions.Condition;
import com.elphel.vdt.core.tools.params.conditions.ConditionalStringsList;
import com.elphel.vdt.core.tools.params.conditions.NamedConditionalStringsList;

public class CommandLinesNodeReader extends AbstractConditionNodeReader {
    List<CommandLinesBlock> commandLinesBlocks = new ArrayList<CommandLinesBlock>();

    public CommandLinesNodeReader(XMLConfig config, Context context) {
        super(config, context);
    }
    
    public List<CommandLinesBlock> getCommandLinesBlocks() {
        return commandLinesBlocks;
    }
    
    public void readNode(Node node, Condition condition) throws ConfigException {
        if(XMLConfig.isElemNode(node, XMLConfig.CONTEXT_LINEBLOCK_TAG)) {
            try {
                commandLinesBlocks.add(readCommandLinesBlock(node, condition));
            } catch(ConfigException e) {
                config.logError(e);
            }
        }
    }
    
    private CommandLinesBlock readCommandLinesBlock(Node node, Condition condition)
        throws ConfigException 
    {
        String name = XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_NAME_ATTR);
        String dest = XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_DEST_ATTR);
        String sep  = XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_SEP_ATTR);        
        
        if(name == null)
            throw new ConfigException("Unnamed lines block definition in context '" +
                                      context.getName() + "' definition");
        
        ConditionalStringsList lines = 
            config.readConditionalStringsNode(node, context, condition);        
        ConditionalStringsList deleteLines = 
            config.readDeleteStringsNode(node, context, condition);
        List<NamedConditionalStringsList> insertLines = 
            config.readInsertStringsNode(node, context, condition);
        
        return new CommandLinesBlock(context.getName(), 
                                     name, 
                                     dest, 
                                     sep,
                                     lines,
                                     deleteLines,
                                     insertLines);
    }
    


}
