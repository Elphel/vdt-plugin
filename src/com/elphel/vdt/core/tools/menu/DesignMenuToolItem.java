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
package com.elphel.vdt.core.tools.menu;

import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.Tool;


public class DesignMenuToolItem extends DesignMenuItem {
    private String tcall;

    public DesignMenuToolItem(Config config,
                              DesignMenu parentMenu,
                              String afterItem,
                              String name,
                              String label,
                              String icon,
                              Boolean visible,
                              String tcall)
    {
        super(config,
              parentMenu,
              afterItem,
              name,
              label,
              icon,
              visible);
        
        this.tcall = tcall;
    }

    public void init() throws ConfigException {
        super.init();
        
        if(label == null)
            throw new ConfigException("Label of item '" + name + 
                                      "' of menu '" + parentMenu.getName() + 
                                      "' is absent");
        else if(tcall == null)
            throw new ConfigException("Tool name in item '" + name + 
                                      "' of menu '" + parentMenu.getName() + 
                                      "' is absent");
            
        Tool tool = config.getContextManager().findTool(tcall);
      
        if(tool == null)
            throw new ConfigException("Design menu '" + parentMenu.getName() + 
                                      "' item '" + name + 
                                      "' refers to non-existing tool '" + tcall + 
                                      "'");
    }

    public Object clone() {
        return new DesignMenuToolItem(config,
                                      parentMenu,
                                      afterItem,
                                      name,
                                      label,
                                      icon,
                                      visible,
                                      tcall);
    }
    
    
    public String getToolName() {
        return tcall;
    }    
}
