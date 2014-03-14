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
package com.elphel.vdt.core.tools.contexts;

import java.util.List;

import com.elphel.vdt.core.tools.params.*;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.menu.DesignMenu;


public class ProjectContext extends Context {
    private String parentPackageName;
    private String designMenuName;
    private DesignMenu designMenu; 
    private PackageContext parentPackage;

    public ProjectContext(String name,
                          String controlInterfaceName,
                          String label,
                          String iconName,            
                          String inputDialogLabel, // null
                          String parentPackageName,
                          List<Parameter> params, // null
                          List<ParamGroup> paramGroups, // null
                          List<CommandLinesBlock> commandLinesBlocks, //null
                          String designMenuName) 
        throws ConfigException
    {
        super(name, 
              controlInterfaceName, 
              label,
              iconName,            
              inputDialogLabel, // null
              params, // null
              paramGroups, // null
              commandLinesBlocks); // null
        
        this.parentPackageName = parentPackageName;
        this.designMenuName = designMenuName;
    }

    public void init(Config config) throws ConfigException {
        super.init(config);
        
        if(designMenuName != null) {
            designMenu = config.getDesignMenuManager().findDesignMenu(designMenuName);
        
            if(designMenu == null)
                throw new ConfigException("Design menu '" + designMenuName + 
                                          "' used in project context '" + name +
                                          "' is not found");
        }
        
        initParentPackage();
    }
    
    public DesignMenu getDesignMenu() {
        return designMenu;
    }
        
    public PackageContext getParentPackage() {
        return parentPackage;
    }

    public String getDesignMenuName() {
        return designMenuName;
    }

    public Parameter findParam(String paramID) {
        Parameter param = super.findParam(paramID);
        
        if(param == null && parentPackage != null)
            param = parentPackage.findParam(paramID);
        
        if(param == null) {
            InstallationContext installation = config.getContextManager().getInstallationContext();
            
            if(installation != null)
                param = installation.findParam(paramID);
        }
        
        return param;
    }
    
    private void initParentPackage() throws ConfigException {
        if(parentPackageName != null) {
            parentPackage = 
                (PackageContext)config.getContextManager().findContext(parentPackageName);
            
            if(parentPackage == null) {
                throw new ConfigException("Parent package context '" + parentPackageName + 
                                          "' of project '" + name + 
                                          "' is absent");
            } else if(!ControlInterface.isInheritedOrSame(parentPackage.getControlInterface(), controlInterface)) { 
                throw new ConfigException("Control interface of parent package context '" + parentPackageName + 
                                          "' of project '" + name + 
                                          "' is neither equal to nor base of control interface of the project");
            }
        }
    }    
}
