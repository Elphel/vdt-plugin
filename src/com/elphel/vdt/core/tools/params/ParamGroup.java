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
package com.elphel.vdt.core.tools.params;

import java.util.*;

import com.elphel.vdt.core.tools.Updateable;
import com.elphel.vdt.core.tools.params.conditions.*;
import com.elphel.vdt.core.tools.config.ConfigException;

public class ParamGroup extends UpdateableStringsContainer 
                        implements Cloneable 
{
    private String name;
    private String label;
    private boolean visible;
    private Condition relevant;
    private double weight; // heigher the weight, later the group
        
    public ParamGroup(String name,
                      String label,
                      boolean visible,
                      double weight,
                      ConditionalStringsList params,
                      ConditionalStringsList deleteParams,
                      List<NamedConditionalStringsList> insertParams,
                      Condition relevant) 
    {
        super(params, deleteParams, insertParams);
        
        assert (label != null) || (name != null);
        
        if(label != null)
            this.label = label;
        else
            this.label = name;
        
        if(name != null)
            this.name = name;
        else
            this.name = label;

        this.visible = visible;
        this.relevant = relevant;
        this.weight=weight;
    }

    public ParamGroup(ParamGroup paramGroup) {
        this(paramGroup.name,
             paramGroup.label,
             paramGroup.visible,
             paramGroup.weight,
             paramGroup.strings != null? 
                     (ConditionalStringsList)paramGroup.strings.clone() : null,
             paramGroup.deleteStrings != null? 
                     (ConditionalStringsList)paramGroup.deleteStrings.clone() : null,
             paramGroup.insertStrings != null?
                     new ArrayList<NamedConditionalStringsList>(paramGroup.insertStrings) : null,
                     // TODO: review this line!!!
             paramGroup.relevant);
    }
    
    public Object clone() {
        return new ParamGroup(this);
    }
    
    public boolean matches(Updateable other) {
        ParamGroup otherGroup = (ParamGroup)other;
        
        return ConditionUtils.conditionsEqual(relevant, otherGroup.relevant) &&
               name.equals(otherGroup.name);
    }
    
    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public double getWeight(){
    	return weight;
    }
    
    public boolean isRelevant() {
        return relevant == null || relevant.isTrue(null); // null for topFormatProcessor (this value will not be used for other parameter value)
    }

    public List<String> getParams() {
        return ConditionUtils.resolveConditionStrings(strings);
    }
    
    public void update(Updateable from) throws ConfigException {
        ParamGroup paramGroup = (ParamGroup)from;
        
        if(name == null) 
            throw new NullPointerException("name == null");

        if(label == null)
            label = paramGroup.label;

        super.update(from,null);// null for topFormatProcessor (this value will not be used for other parameter value)
    }

}
