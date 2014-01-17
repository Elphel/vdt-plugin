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
package com.elphel.vdt.ui.tools;

import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.params.Tool;

/**
 * Manager of tool configuration
 * 
 * Created: 27.12.2005
 * @author  Lvov Konstantin
 */

public class ToolUIManager extends ToolsCore {

    private static ToolUI[] tools = null;
        
    public static ToolUI[] getToolUI() {
        if (tools == null) {
            Config config = getConfig();
            tools = new ToolUI[config.getContextManager().getToolList().size()];
            for (int i=0; i < tools.length; i++) {
                tools[i] = new ToolUI(config, (Tool)config.getContextManager().getToolList().get(i));
            }
        }
        return tools;  
    }
    
    public static ToolUI getToolUI(String launchType) {
//        ToolUI[] tools = getToolUI();
//        for (int i=0; i < tools.length; i++) {
//            if (launchType.equals(tools[i].getTool().getLaunchType()))
//                return tools[i];
//        }
        return null;
    } // getToolUI()

       
} // class ToolUIManager
