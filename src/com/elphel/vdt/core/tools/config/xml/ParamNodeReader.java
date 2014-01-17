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
        String readOnly     = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_READONLY_ATTR);
        String visible      = XMLConfig.getAttributeValue(paramNode, XMLConfig.PARAMETER_VISIBLE_ATTR);
        
        if(id == null)
            throw new ConfigException("Context '" + context.getName() + ": Parameter id is absent");
                
        return new Parameter(id,
                             outid,
                             typeName,
                             formatName,
                             defaultValue,
                             label,
                             omitValue,
                             readOnly,
                             visible,
                             condition);
    }
}
