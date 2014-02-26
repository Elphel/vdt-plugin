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

public class ConditionString extends ConditionValue {
    private String value;
    
    public ConditionString(Condition condition, String value) {
        super(condition);
        
        this.value = value;
    }
    
    public String getValue(FormatProcessor topProcessor) {
        return condition.isTrue(topProcessor)? value : null;
    }

    public List<String> getDependencies() {
        List<String> deps = condition.getDependencies();
        
        deps.add(value);
        
        return deps;
    }
}
