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
package com.elphel.vdt.core.tools.menu;

import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.Tool;


public class DesignMenuToolItem extends DesignMenuItem {
    private String tcall;
    private String toolInstance;

    public DesignMenuToolItem(Config config,
                              DesignMenu parentMenu,
                              String afterItem,
                              String name,
                              String label,
                              String icon,
                              Boolean visible,
                              String tcall,
                              String toolInstance)
    {
        super(config,
              parentMenu,
              afterItem,
              name,
              label,
              icon,
              visible);
        
        this.tcall = tcall;
        this.toolInstance = toolInstance;
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
                                      tcall,
                                      toolInstance);
    }
    
    
    public String getToolName() {
        return tcall;
    }    
}
