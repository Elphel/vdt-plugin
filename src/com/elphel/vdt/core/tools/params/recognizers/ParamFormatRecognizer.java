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
package com.elphel.vdt.core.tools.params.recognizers;

import com.elphel.vdt.core.Utils;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.generators.AbstractGenerator;


public class ParamFormatRecognizer implements Recognizer {
    static final String FORMAT_PARAM_NAME_MARK = "ParamName"; 
    static final String FORMAT_PARAM_VALUE_MARK = "ParamValue"; 

    private static final String FORMAT_MARKER = "%"; 
//    private static final String FORMAT_MARKER_OPEN = "{"; 
//    private static final String FORMAT_MARKER_CLOSE = "}"; 

    private static final int FORMAT_MARKER_LEN = FORMAT_MARKER.length(); 
//    private static final int FORMAT_MARKER_OPEN_LEN = FORMAT_MARKER_OPEN.length(); 
//    private static final int FORMAT_MARKER_CLOSE_LEN = FORMAT_MARKER_CLOSE.length(); 

    private final Parameter param;
    
    public ParamFormatRecognizer(Parameter param) {
        this.param = param;
    }
    
    public RecognizerResult recognize(String template, int startPos, FormatProcessor topProcessor) {
        RecognizerResult result = new RecognizerResult();
        String genName;
        int newPos = -1;
        
//        if(template.startsWith(FORMAT_MARKER_OPEN, startPos)) {
//            int endPos = template.indexOf(FORMAT_MARKER_CLOSE, startPos);
//
//            if(endPos < 0)
//                return result;
//            
//            // cut the identifier between open and close symbols
//            genName = template.substring(startPos + FORMAT_MARKER_OPEN_LEN, endPos);
//            newPos = endPos + FORMAT_MARKER_CLOSE_LEN;
//        } else 
        if(template.startsWith(FORMAT_MARKER, startPos)){
            startPos += FORMAT_MARKER_LEN;
            newPos = Utils.findBoundary(template, startPos);
            genName = template.substring(startPos, newPos);
        } else {
            return result;
        }
        
        assert genName != null;
        assert newPos >= 0; 
                
        if(genName.equals(FORMAT_PARAM_NAME_MARK)) {
            result.set(new AbstractGenerator(topProcessor) {
                           public String getName() {
                               return "ParamName (parameter '" + param.getID() + 
                                      "' of context '" + param.getContext().getName() +
                                      "')";
                           }
                           
                           protected String[] getStringValues() {
                               return new String[]{param.getOutID()};
                           }
                       },
            
                       newPos, topProcessor);
        } else if(genName.equals(FORMAT_PARAM_VALUE_MARK)) {
            result.set(new AbstractGenerator(topProcessor) {
                           public String getName() {
                               return "ParamValue (parameter '" + param.getID() +
                                      "' of context '" + param.getContext().getName() +
                                      "')";
                           }
                
                           protected String[] getStringValues() {
                               return new String[]{param.getExternalValueForm(topProcessor).get(0)};
                           }
                       },
            
                       newPos, topProcessor);
        }
        
        return result;
    }
}
