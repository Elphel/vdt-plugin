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
package com.elphel.vdt.core.tools.params.recognizers;

import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.generators.StringsGenerator;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;


public class ToolParamRecognizer extends ParamRecognizer {
    private Tool tool;
    
    public ToolParamRecognizer(Tool tool) {
        this.tool = tool;
    }
    
    protected Parameter findParam(String paramID) {
        return tool.findParam(paramID);
    }
    
    protected AbstractGenerator getGenerator(final Parameter param) throws ToolException {
        return new StringsGenerator(param.getCommandLine()) {
        	String toolName=(tool==null)?"<null>":tool.getName();
            public String getName() {
                return "Param '" + param + 
                       "' of tool '" + toolName + 
                       "' command line";
            }
        };
    }
}
