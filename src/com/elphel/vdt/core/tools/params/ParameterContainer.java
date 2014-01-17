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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.contexts.Context;


public class ParameterContainer implements Cloneable {
    private List<Parameter> params;

    public ParameterContainer(List<Parameter> params) {
        this.params = params;
    }
    
    public void init(Config config, Context context) throws ConfigException {
        for(Iterator<Parameter> p = params.iterator(); p.hasNext();) {
            try {
                ((Parameter)p.next()).init(context);
            } catch(ConfigException e) {
                config.logError(e);
            }
        }
    }
    
    public List<Parameter> getParams() {
        return params;
    }
    
    public void addParam(Parameter param) {
        params.add(param);
    }
    
    public Parameter findParam(String paramID) {
        for (Iterator i = params.iterator(); i.hasNext(); ) {
            Parameter param = (Parameter)i.next();
            
            if(param.isSame(paramID))
                return param;
        }
        
        return null;
    }
        
    public Object clone() {
        ParameterContainer newContainer = null;
        
        try {
            newContainer = (ParameterContainer)super.clone();
        } catch (CloneNotSupportedException e) {
            assert false;
        }

        newContainer.params = new ArrayList<Parameter>();
        
        for(Iterator<Parameter> pi = params.iterator(); pi.hasNext();)
            newContainer.params.add((Parameter)(pi.next()).clone());
        
        return newContainer;
    }    
}
