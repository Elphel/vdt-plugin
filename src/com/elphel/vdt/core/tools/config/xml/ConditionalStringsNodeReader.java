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

import org.w3c.dom.Node;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.conditions.Condition;
import com.elphel.vdt.core.tools.params.conditions.ConditionalStringsList;

public class ConditionalStringsNodeReader extends AbstractConditionNodeReader {
    ConditionalStringsList conditionalStrings = new ConditionalStringsList(); 

    public ConditionalStringsNodeReader(XMLConfig config, Context context) {
        super(config, context);
    }
    
    public ConditionalStringsList getConditionalStrings() {
        return conditionalStrings;
    }
    
    public void readNode(Node node, Condition condition) throws ConfigException {
        if(!XMLConfig.isTextNode(node))
            return;
        
        conditionalStrings.add(condition, XMLConfig.readStringsNode(node));
    }
}
