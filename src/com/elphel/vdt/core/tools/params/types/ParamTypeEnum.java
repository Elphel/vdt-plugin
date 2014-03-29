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
package com.elphel.vdt.core.tools.params.types;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.ControlInterface;


public class ParamTypeEnum extends ParamType {
    public static final String NAME = "enum";

    public static final String ELEM_LABEL_ID = "label";
    public static final String ELEM_VALUE_ID = "value";
    public static final String BASETYPE_ID = "base";

    private ParamType baseType;
    private String baseTypeName;
    private String[] labels;
    private String[] values;

    public ParamTypeEnum(String baseTypeName, String[] labels, String[] values) {        
        this.baseTypeName = baseTypeName;
        this.labels = labels;
        this.values = values;
    }
    
    public void init(ControlInterface controlInterface, String typedefName)
        throws ConfigException 
    {
        baseType = controlInterface.findParamType(baseTypeName);
        
        if(baseType == null)
            throw new ConfigException("Base type '" + baseTypeName + 
                                      "' of '" + NAME +
                                      "' type '" + typedefName +
                                      "' is not found in control interface '" + 
                                      controlInterface.getName() + "'");
        
        assert labels.length == values.length;
        
        for(int i = 0; i < labels.length; i++)
            for(int j = i+1; j < labels.length; j++) { 
                if(labels[i].equals(labels[j]))
                    throw new ConfigException("Label '" + labels[i] +
                                              "' in '" + NAME + 
                                              "' type '" + typedefName +
                                              "' is duplicated");

                if(values[i].equals(values[j]))
                    throw new ConfigException("Value '" + values[i] +
                                              "' in '" + NAME + 
                                              "' type '" + typedefName +
                                              "' is duplicated");
            }
    }
    
    public String getName() {
        return NAME;
    }

    public boolean isList() {
        return false;
    }
    
    public String getBaseTypeName() {
        return baseTypeName;
    }

    public String[] getLabels() {
        return labels;        
    }
    
    public String[] getValues() {
        return values;        
    }
    
    public int getLabelIndex(String label) {
        for(int i = 0; i < labels.length; i++)
            if(label.equals(labels[i]))
                return i;
        
        return -1;
    }

    public int getValueIndex(String paramValue) {
        for(int i = 0; i < values.length; i++)
            if(paramValue.equals(values[i]))
                return i;
        
        return -1;
    }

    public String toExternalForm(String paramValue) {
        int valueIndex = getValueIndex(paramValue);
        return valueIndex >= 0? values[valueIndex] : null;
    }


    public boolean equal(String value1, String value2) {
        return baseType.equal(value1, value2);
    }
    
    public String canonicalizeValue(String value) {
        return baseType.canonicalizeValue(value);
    }
    
    public void checkValue(String value) throws ConfigException {
        if(getValueIndex(value) < 0)
            throw new ConfigException("Value '" + value + 
                                      "' of type '" + NAME + 
                                      "' is not listed in the values list");
    }
}
