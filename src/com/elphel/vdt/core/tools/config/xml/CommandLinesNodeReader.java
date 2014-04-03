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
package com.elphel.vdt.core.tools.config.xml;

import java.util.*;

import org.w3c.dom.Node;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.CommandLinesBlock;
import com.elphel.vdt.core.tools.params.conditions.Condition;
import com.elphel.vdt.core.tools.params.conditions.ConditionalStringsList;
import com.elphel.vdt.core.tools.params.conditions.NamedConditionalStringsList;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.core.tools.params.types.ParamTypeString.KIND;

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
/*
 */
    private CommandLinesBlock readCommandLinesBlock(Node node, Condition condition)
        throws ConfigException 
    {
        String name =         XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_NAME_ATTR);
        String dest =         XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_DEST_ATTR);
        String sep  =         XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_SEP_ATTR);        
        String mark  =        XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_MARK_ATTR);
        String errors  =      XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_ERRORS_ATTR);
        String warnings  =    XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_WARNINGS_ATTR);
        String info  =        XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_INFO_ATTR);

        String inst_capture = XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_INSTANCE_CAPTURE);
        String inst_separator=XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_INSTANCE_SEPARATOR);
        String inst_suffix =  XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_INSTANCE_SUFFIX);

        String prompt  =      XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_PROMPT_ATTR);
        String interrupt  =   XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_INTERRUPT_ATTR);
        
        String stderr  =    XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_STDERR_ATTR);
        String stdout  =    XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_STDOUT_ATTR);
        String timeout =    XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_TIMEOUT_ATTR);
        
        String success =    XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_SUCCESS_ATTR);
        String failure =    XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_FAILURE_ATTR);
        String s_keep_open =XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_KEEP_OPEN_ATTR);
        String logPath =    XMLConfig.getAttributeValue(node, XMLConfig.CONTEXT_LINEBLOCK_LOGPATH_ATTR);

        boolean keep_open=false;
        
        if(s_keep_open != null) {
            XMLConfig.checkBoolAttr(s_keep_open, XMLConfig.CONTEXT_LINEBLOCK_KEEP_OPEN_ATTR);
            keep_open = XMLConfig.getBoolAttrValue(s_keep_open);
        }

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
                                     ParamTypeString.KIND.FILE, //Andrey - doesn't know "kind" here yet - TODO: change to attr
                                     sep,
                                     mark,
                                     errors,
                                     warnings,
                                     info,

                                     inst_capture,
                                     inst_separator,
                                     inst_suffix,

                                     prompt,
                                     interrupt,
                                     stderr,
                                     stdout,
                                     timeout,
                                     success,
                                     failure,
                                     keep_open,
                                     logPath,
                                     lines,
                                     deleteLines,
                                     insertLines);
    }
}
