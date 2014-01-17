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
        
    public ParamGroup(String name,
                      String label,
                      boolean visible,
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
    }

    public ParamGroup(ParamGroup paramGroup) {
        this(paramGroup.name,
             paramGroup.label,
             paramGroup.visible,
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
    
    public boolean isRelevant() {
        return relevant == null || relevant.isTrue();
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

        super.update(from);
    }

}
