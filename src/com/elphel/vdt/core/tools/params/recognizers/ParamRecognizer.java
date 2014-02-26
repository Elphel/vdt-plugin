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
