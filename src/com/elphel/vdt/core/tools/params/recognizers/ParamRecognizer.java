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
package com.elphel.vdt.core.tools.params.recognizers;

import com.elphel.vdt.core.Utils;
import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.ToolException;


public abstract class ParamRecognizer implements Recognizer {
	//, FormatProcessor topProcessor - not yet used?
	protected FormatProcessor topProcessor=null;
    public RecognizerResult recognize(String template, int startPos, FormatProcessor topProcessor) throws ToolException {
        RecognizerResult result = new RecognizerResult();
                
        int newPos = Utils.findBoundary(template, startPos);

        String paramID = template.substring(startPos, newPos);
        Parameter param = findParam(paramID);

        if(param != null)
            result.set(getGenerator(param), newPos,topProcessor);
        this.topProcessor=topProcessor;

        return result;
    }
    
    protected abstract Parameter findParam(String paramID);
    protected abstract AbstractGenerator getGenerator(Parameter param) throws ToolException;
}
