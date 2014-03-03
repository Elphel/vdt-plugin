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
import com.elphel.vdt.core.tools.params.ParamGroup;
import com.elphel.vdt.core.tools.params.conditions.Condition;
import com.elphel.vdt.core.tools.params.conditions.ConditionalStringsList;
import com.elphel.vdt.core.tools.params.conditions.NamedConditionalStringsList;

public class ParamGroupNodeReader extends AbstractConditionNodeReader {
    List<ParamGroup> paramGroups = new ArrayList<ParamGroup>();

    public ParamGroupNodeReader(XMLConfig config, Context context) {
        super(config, context);
    }
    
    public List<ParamGroup> getParamGroups() {
        return paramGroups;
    }

    public void readNode(Node node, Condition condition) throws ConfigException {
        if(XMLConfig.isElemNode(node, XMLConfig.PARAMGROUP_TAG)) {
            try {
                paramGroups.add(readParamGroup(node, condition));
            } catch(ConfigException e) {
                config.logError(e);
            }
        }
    }

    private ParamGroup readParamGroup(Node node, Condition condition)
        throws ConfigException 
    {
        String name    = XMLConfig.getAttributeValue(node, XMLConfig.PARAMGROUP_NAME_ATTR);
        String label   = XMLConfig.getAttributeValue(node, XMLConfig.PARAMGROUP_LABEL_ATTR);
        String visible = XMLConfig.getAttributeValue(node, XMLConfig.PARAMGROUP_VISIBLE_ATTR);
        String weightString = XMLConfig.getAttributeValue(node, XMLConfig.PARAMGROUP_WEIGHT_ATTR);
        
        
        
        if(name == null && label == null)
            throw new ConfigException("Parameter group in context '" + context.getName() + 
                                      "' has neither name nor label attribute");        
        
        boolean isVisible;
        
        if(visible != null) {
            XMLConfig.checkBoolAttr(visible, XMLConfig.TYPEDEF_LIST_ATTR);
            isVisible = XMLConfig.getBoolAttrValue(visible);
        } else {
            isVisible = true;
        }
        double weight=1.0;
        if (weightString!=null){
        	try {
        		weight=Double.parseDouble(weightString);
        	} catch (Exception e){
                throw new ConfigException("Parameter group in context '" + context.getName() + 
                        "' has invalid weight string '"+weightString+"' - floating point value is expected.");        
        	}
        }
        
        ConditionalStringsList params = 
            config.readConditionalStringsNode(node, context, condition);
        ConditionalStringsList deleteParams = 
            config.readDeleteStringsNode(node, context, condition);
        List<NamedConditionalStringsList> insertParams = 
            config.readInsertStringsNode(node, context, condition);
        
        return new ParamGroup(name, 
                              label,
                              isVisible,
                              weight,
                              params,
                              deleteParams,
                              insertParams,
                              condition);
    }
}
