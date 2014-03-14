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
 *******************************************************************************/
package com.elphel.vdt.core.tools.config;

import java.util.*;

import com.elphel.vdt.core.tools.params.*;
import com.elphel.vdt.core.tools.contexts.*;
import com.elphel.vdt.core.tools.menu.*;


public abstract class Config {
    protected List<ControlInterface> controlInterfaceList = new ArrayList<ControlInterface>();
    protected ContextManager contextManager = new ContextManager();
    protected DesignMenuManager designMenuManager = new DesignMenuManager();
    
    protected enum ContextKind {
        INSTALLATION,
        PROJECT,
        PACKAGE,
        TOOL
    }
    
    public abstract void logError(Exception e) throws ConfigException;
    
    public List<ControlInterface> getControlInterfaceList() {
        return controlInterfaceList;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }
    
    public DesignMenuManager getDesignMenuManager() {
        return designMenuManager;
    }
    
    public ControlInterface findControlInterface(String name) {
        for(Iterator ci = controlInterfaceList.iterator(); ci.hasNext();) {
            ControlInterface interf = (ControlInterface)ci.next();
            
            if(interf.getName().equals(name))
                return interf;
        }
        
        return null;
    }

}
