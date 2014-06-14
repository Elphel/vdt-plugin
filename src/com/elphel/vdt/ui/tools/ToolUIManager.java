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
