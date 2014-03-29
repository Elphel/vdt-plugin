/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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
package com.elphel.vdt.core.tools.generators;

import com.elphel.vdt.VDT;
import com.elphel.vdt.core.tools.params.Tool;

/**
 * Generate the name of current project. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public class ToolNameGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_TOOL_NAME; 
    
    public String getName() {
        return NAME;
    }

    public ToolNameGenerator()
    {
    	super(null); // null for topFormatProcessor - this generator can not reference other parameters
    }

    protected String[] getStringValues() {
    	String toolName="";
    	if (topProcessor!=null){
    		Tool tool=topProcessor.getCurrentTool();
//    		System.out.println(", tool="+tool+" tool name="+((tool!=null)?tool.getName():null));
    		if (tool != null) {
//        		System.out.println(" tool="+tool.getName()+", ignoreFilter="+ignoreFilter);
    			toolName=tool.getName();
    		} else {
    			System.out.println("ToolNameGenerator():  topProcessor.getCurrentTool() is null");
    		}
    	} else {
    		System.out.println("ToolNameGenerator():  topProcessor is null");
    	}
    	return new String[]{toolName};
    }

} // class ProjectNameGenerator
