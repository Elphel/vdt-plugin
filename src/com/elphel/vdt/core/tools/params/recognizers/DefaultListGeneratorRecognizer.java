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
import com.elphel.vdt.core.tools.generators.*;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Tool;


public class DefaultListGeneratorRecognizer implements Recognizer {
    private static final String CONTROL_SEQ = "%"; 
    private static final int CONTROL_SEQ_LEN = CONTROL_SEQ.length();
    private AbstractGenerator[] generators;
//    public DefaultListGeneratorRecognizer(boolean menuMode, Tool tool,FormatProcessor topProcessor){
    public DefaultListGeneratorRecognizer(boolean menuMode, FormatProcessor topProcessor){
    	super();
    	AbstractGenerator[] generators=  new AbstractGenerator[]{
    			new SourceListGenerator("","",null),
    			new FilteredSourceListGenerator("","",null, topProcessor),
    			new FileListGenerator("","",null)
    	};
    	this.generators=generators;
    	for (int i=0;i<generators.length;i++){
    		generators[i].setMenuMode(menuMode);
//    		generators[i].setTool(tool);
    	}
    }
    public DefaultListGeneratorRecognizer(FormatProcessor topProcessor){
    	this(false,topProcessor);
    }
//    public DefaultListGeneratorRecognizer(boolean menuMode,FormatProcessor topProcessor){
//    	this(menuMode,null,topProcessor);
//    }
//    public DefaultListGeneratorRecognizer(Tool tool, FormatProcessor topProcessor){
//        public DefaultListGeneratorRecognizer(Tool tool, FormatProcessor topProcessor){
//    	this(false,tool,topProcessor);
//    }
	public RecognizerResult recognize(String template, int startPos, FormatProcessor topProcessor) {
        RecognizerResult result = new RecognizerResult(); 
                
        // first see if there is the control sequence
        if(template.startsWith(CONTROL_SEQ, startPos)) {
            startPos += CONTROL_SEQ_LEN;
            
            // read the identifier from startPos
            int newPos = Utils.findBoundary(template, startPos);

            String genName = template.substring(startPos, newPos);

            result.set(findGenerator(genName), newPos, topProcessor);        
        }
        
        return result;
    }

    private AbstractGenerator findGenerator(String genName) {
        for(int i = 0; i < generators.length; i++)
            if(genName.equals(generators[i].getName()))
                return generators[i];
    
        return null;
    }
}
