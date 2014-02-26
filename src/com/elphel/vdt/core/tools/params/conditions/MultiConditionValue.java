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
package com.elphel.vdt.core.tools.params.conditions;

import java.util.*;

import com.elphel.vdt.core.tools.params.FormatProcessor;

public class MultiConditionValue implements AbstractConditonValue {
    private List<ConditionValue> conditionValues;
    private String defaultString;
    
    public MultiConditionValue(List<ConditionValue> conditionValues) {
        this.conditionValues = conditionValues;
    }
    
    public MultiConditionValue(List<ConditionValue> conditionValues, String defaultString) {
        this(conditionValues);
        
        this.defaultString = defaultString;
    }
    
    public String getValue(FormatProcessor topProcessor) {
        for(Iterator<ConditionValue> i = conditionValues.iterator(); i.hasNext();) {
            String value = ((ConditionValue)i.next()).getValue(topProcessor);
            
            if(value != null)
                return value;
        }

        if(defaultString != null)
            return defaultString;
        
        return null;
    }

    public List<String> getDependencies() {
        List<String> deps = new ArrayList<String>();

        for(ConditionValue conditionValue : conditionValues)
            deps.addAll(conditionValue.getDependencies());
        
        deps.add(defaultString);
        
        return deps;
    }
}
