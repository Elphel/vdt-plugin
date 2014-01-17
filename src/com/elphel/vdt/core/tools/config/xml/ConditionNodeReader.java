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

import org.w3c.dom.*;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.ParamUtils;
import com.elphel.vdt.core.tools.params.conditions.*;


public class ConditionNodeReader extends AbstractConditionNodeReader {
    private AbstractConditionNodeReader nodeReader;
    private Condition initialCondition;
        
    public ConditionNodeReader(XMLConfig config, 
                               Context context, 
                               AbstractConditionNodeReader nodeReader) 
    {
        this(config, context, nodeReader, null);
    }

    public ConditionNodeReader(XMLConfig config, 
                               Context context, 
                               AbstractConditionNodeReader nodeReader,
                               Condition initialCondition) 
    {
        super(config, context);

        this.nodeReader = nodeReader;
        this.initialCondition = initialCondition;
    }
    
    public void readNode(Node node) throws ConfigException {
        readNode(node, initialCondition);
    }
        
    public void readNode(Node node, Condition condition) throws ConfigException {
        NodeList childNodes = node.getChildNodes();
        
        for(int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);

            // read the data needed by the incapsulated node reader
            nodeReader.readNode(child, condition);
            
            // find condition nodes
            // for each, create a condition object and call readNode(condNode, createdCondition)
            Comparison.COMPARE_OP compareOp; 
            Condition.BOOL_OP boolOp;
            
            if(XMLConfig.isElemNode(child, XMLConfig.CONDITION_IF_TAG)) {
                compareOp = Comparison.COMPARE_OP.EQ;
                boolOp = Condition.BOOL_OP.OR;
            } else if(XMLConfig.isElemNode(child, XMLConfig.CONDITION_NOT_TAG)) {
                compareOp = Comparison.COMPARE_OP.NEQ;
                boolOp = Condition.BOOL_OP.AND;
            } else if(XMLConfig.isElemNode(child, XMLConfig.CONDITION_AND_TAG)) {
                compareOp = Comparison.COMPARE_OP.EQ;
                boolOp = Condition.BOOL_OP.AND;
            } else {
                continue;
            }
            
            readNode(child, composeCondititon(condition, child, compareOp, boolOp));
        }            
    }
    
    private Condition composeCondititon(Condition topCondition,
                                        Node conditionNode,  
                                        Comparison.COMPARE_OP nodeCompareOp, 
                                        Condition.BOOL_OP nodeBoolOp)
        throws ConfigException
    {
        // read the nested condition and bind it with the top one 
        // using the "AND" boolean operation
        
        Condition subCondition = readCondition(conditionNode, nodeCompareOp, nodeBoolOp);
    
        if(topCondition != null)
            return new Condition(Condition.BOOL_OP.AND, 
                                 topCondition, 
                                 subCondition);
        else
            return subCondition;
    }
    
    private Condition readCondition(Node node, 
                                    Comparison.COMPARE_OP compareOp, 
                                    Condition.BOOL_OP boolOp)
        throws ConfigException 
    {
        NamedNodeMap attributes = node.getAttributes();
        
        if(attributes.getLength() == 0)
            throw new ConfigException("Condition block '" + node.getNodeName() + 
                                      "' doesn't have any attribute");
        
        Condition fullCondition = null;

        for(int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            
            String paramName = attr.getNodeName();
            String paramValue = attr.getNodeValue();
            
            Comparison comparison = new StringComparison(compareOp, 
                                                         context, 
                                                         ParamUtils.buildParamString(paramName),                                                         
                                                         paramValue);
        
            if(fullCondition == null)
                fullCondition = comparison;
            else
                fullCondition = new Condition(boolOp, 
                                              fullCondition, 
                                              comparison);
        }
                
        return fullCondition;
    }
}
