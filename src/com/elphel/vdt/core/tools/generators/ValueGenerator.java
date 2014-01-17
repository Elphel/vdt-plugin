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
package com.elphel.vdt.core.tools.generators;

import java.util.*;

import com.elphel.vdt.core.tools.params.Parameter;

public class ValueGenerator extends AbstractGenerator {
    private Parameter param;
     
    public ValueGenerator(Parameter param, 
                          String prefix, 
                          String suffix, 
                          String separator) 
    {
        super(prefix, suffix, separator);
         
        this.param = param;
    }
    
    public String getName() {
        return "value of parameter '" + param.getID() + 
               "' of context '" + param.getContext().getName() + 
               "'";
    }
     
    protected String[] getStringValues() {
        List<String> values = param.getValue();
        
        return values.toArray(new String[values.size()]);
    }

    public String[] generate() {
        if (!param.getType().isList()) 
            return new String[]{prefix + param.getValue().get(0) + suffix};
        else
            return super.generate();
    }
}
