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

import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.ui.MessageUI;

public class ConditionUtils {
    private static HashMap<Context, HashMap<String, MultiConditionValue>> contextConditions =
        new HashMap<Context, HashMap<String, MultiConditionValue>>();
    
    public static List<String> resolveConditionStrings(ConditionalStringsList condStrings) {
        List<String> relevantStrings = new ArrayList<String>();
        
        for(ConditionalStringsList.Entry entry : condStrings.getEntries()) {
            Condition relevant = entry.getCondition();
            List<String> strings = entry.getStrings();
            
            if(relevant == null || relevant.isTrue(null))
                relevantStrings.addAll(strings);                
        }
        
        return relevantStrings;
    }
    
    public static MultiConditionValue getContextCondition(Context context, String condString) {
        if(!StringConditionParser.isConditionString(condString))
            return null;
        
        // use hash map to avoid repeated parsing of same string
        HashMap<String, MultiConditionValue> conditions = contextConditions.get(context);
        
        if(conditions == null) {
            conditions = new HashMap<String, MultiConditionValue>();

            contextConditions.put(context, conditions);
        }
            
        MultiConditionValue condValue = conditions.get(condString);
        
        if(condValue == null) {
            try {
                condValue = context.getConditionParser().parse(condString);
                
                conditions.put(condString, condValue);
            } catch(ParseError e) {
                MessageUI.error(e);
            }
        }
        
        return condValue;
    }

    public static String resolveContextCondition(Context context, String condString, FormatProcessor topProcessor) {
        MultiConditionValue condValue = getContextCondition(context, condString);
        
        if(condValue != null) 
            return condValue.getValue(topProcessor);
                
        return condString;
    }
    
    public static boolean conditionsEqual(Condition a, Condition b) {
        if((a == null && b != null) || (a != null && b == null))
             return false;
             
        return (a == b) || a.equals(b);
    }
}
