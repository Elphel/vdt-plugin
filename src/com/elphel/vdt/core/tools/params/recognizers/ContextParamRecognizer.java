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
package com.elphel.vdt.core.tools.params.recognizers;

import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.generators.ValueGenerator;


public class ContextParamRecognizer extends ParamRecognizer {
    private Context context;
    
    public ContextParamRecognizer(Context context, FormatProcessor topProcessor) {
        this.context = context;
        this.topProcessor=topProcessor;
    }
    
    protected Parameter findParam(String paramID) {
        return context.findParam(paramID);
    }

    protected AbstractGenerator getGenerator(Parameter param) throws ToolException {
        return new ValueGenerator(param, "", "", "",topProcessor);
    }
}
