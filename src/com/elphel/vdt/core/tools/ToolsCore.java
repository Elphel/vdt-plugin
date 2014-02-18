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
package com.elphel.vdt.core.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import org.eclipse.ui.IMemento;

//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.xml.XMLConfig;
import com.elphel.vdt.core.tools.contexts.ContextManager;
import com.elphel.vdt.core.tools.contexts.InstallationContext;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.menu.DesignMenu;
import com.elphel.vdt.core.tools.menu.DesignMenuManager;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.MessageUI;

/**
 * Manager of tools configuration
 * 
 * Created: 14.01.2006
 * @author  Lvov Konstantin
 */

public class ToolsCore {
    // singleton
    private static Config config;
    
    private static final String CONFIG_DIR = "tools";

    public static void updateConfig(String configFileName) {
        try {
            config = new XMLConfig(configFileName);
            OptionsCore.doLoadContextOptions(config.getContextManager().getInstallationContext());
            
            for (PackageContext packageContext : config.getContextManager().getPackageContexts())
                OptionsCore.doLoadContextOptions(packageContext);
        } catch(Exception e) {
            MessageUI.error(e);
        }
    } 

    public static final Config getConfig() {
        if (config == null)
            updateConfig(VerilogPlugin.getInstallLocation() + File.separator + CONFIG_DIR);

        return config;
    }
    
    public static Tool getTool(String toolID) {
        for (Tool tool : getConfig().getContextManager().getToolList())
            if (toolID.equals(tool.getName()))
                return tool;
        
        return null;
    }
    
    public static void saveToolsState(IMemento memento){
        for (Tool tool : getConfig().getContextManager().getToolList())
        	tool.saveState(memento);
    }
    
    public static void restoreToolsState(IMemento memento){
        for (Tool tool : getConfig().getContextManager().getToolList())
        	tool.restoreState(memento);
    }
    

    public static Tool getToolWorkingCopy(String toolID) {
        Tool tool = getTool(toolID);
        if (tool != null) {
            return (Tool)tool.clone();
        }   
        return null;
    }
    
    public static List<Tool> getTools(PackageContext context) {
        List<Tool> list = new ArrayList<Tool>();

        for (Tool tool : getContextManager().getToolList()) {
            PackageContext toolPackage = tool.getParentPackage(); 

            if (toolPackage == context ||
               ((toolPackage != null) && (context != null) && context.getName().equals(toolPackage.getName())))
            {
                list.add(tool);
            }
        }
        
        return list;
    }

    public static List<Tool> getStandaloneTools() {
        return getTools(null);
    }
    
    public static DesignMenu getDesignMenu() {
        return getConfig().getContextManager().getDesignMenu();
    }
        
    public static DesignMenuManager getDesignMenuManager() {
        return getConfig().getDesignMenuManager();
    }

    public static ContextManager getContextManager() {
        return getConfig().getContextManager();
    }

    public static InstallationContext getInstallationContext() {
        return getConfig().getContextManager().getInstallationContext();
    }
    
} // class ToolsCore
