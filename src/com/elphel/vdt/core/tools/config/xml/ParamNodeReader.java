/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
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
package com.elphel.vdt.core.tools.config.xml;

import java.util.*;

import org.w3c.dom.Node;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.conditions.Condition;
import com.elphel.vdt.core.tools.contexts.Context;


public class ParamNodeReader extends AbstractConditionNodeReader {
    private List<Parameter> paramList = new ArrayList<Parameter>();

    public ParamNodeReader(XMLConfig config, Context context) {
        super(config, context);
    }
    
    public void readNode(Node node, Condition condition) throws ConfigException {
        if(XMLConfig.isElemNode(node, XMLConfig.PARAMETER_TAG)) {
            try {
                paramList.add(readParam(node, condition));
            } catch(ConfigException e) {
                config.logError(e);
            }
        }
    }
        
    public List<Parameter> getParamList() {
        return paramList;
    }
    
    private Parameter readParam(Node paramNode, Condition condition)
        throws ConfigException 
    {
        String id           = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_ID_ATTR);
        String outid        = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_OUTID_ATTR);
        String typeName     = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_TYPE_NAME_ATTR);
        String formatName   = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_FORMAT_NAME_ATTR);
        String defaultValue = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_DEFAULT_VALUE_ATTR);
        String omitValue    = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_OMIT_VALUE_ATTR);
        String label        = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_LABEL_ATTR);
        String tooltip      = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_TOOLTIP_ATTR);
        String readOnly     = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_READONLY_ATTR);
        String visible      = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_VISIBLE_ATTR);
        if(id == null)
            throw new ConfigException("Context '" + context.getName() + ": Parameter id is absent");
//getConfigFileName                
        return new Parameter(id,
                             outid,
                             typeName,
                             formatName,
                             defaultValue,
                             label,
                             tooltip,
                             omitValue,
                             readOnly,
                             visible,
                             condition,
                             getConfigFileName() //.getConfigFileName()
                             );
    }
}
