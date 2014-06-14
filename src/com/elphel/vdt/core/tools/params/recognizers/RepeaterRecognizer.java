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
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.generators.FileListGenerator;
import com.elphel.vdt.core.tools.generators.FilteredSourceListGenerator;
import com.elphel.vdt.core.tools.generators.TopModulesNameGenerator;
import com.elphel.vdt.core.tools.generators.SourceListGenerator;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.elphel.vdt.core.tools.params.Parameter;


public class RepeaterRecognizer implements Recognizer {
    private static final String FORMAT_REPEATER_NAME_MARK = "%%"; 
    
    private static int FORMAT_REPEATER_NAME_MARK_LEN = FORMAT_REPEATER_NAME_MARK.length();

    private static final String FORMAT_REPEATER_OPEN  = "("; 
    private static final String FORMAT_REPEATER_SEP   = "%|"; 
    private static final String FORMAT_REPEATER_CLOSE = "%)"; 

    private static final int FORMAT_REPEATER_OPEN_LEN = FORMAT_REPEATER_OPEN.length(); 
    private static final int FORMAT_REPEATER_SEP_LEN = FORMAT_REPEATER_SEP.length(); 
    private static final int FORMAT_REPEATER_CLOSE_LEN = FORMAT_REPEATER_CLOSE.length();
    protected Parameter param=null;
    
    public RecognizerResult recognize(String template, int startPos, FormatProcessor topProcessor)
        throws ToolException
    {
        RecognizerResult result = new RecognizerResult();

        if(!template.startsWith(FORMAT_REPEATER_OPEN, startPos))
            return result;
            
        startPos += FORMAT_REPEATER_OPEN_LEN;
        
        int sepPos = template.indexOf(FORMAT_REPEATER_SEP, startPos);
        int closePos = template.indexOf(FORMAT_REPEATER_CLOSE, startPos);
        
        if(closePos < 0)
            return result;
        
        String repBody;
        String sepBody;

        if(sepPos >= 0 && sepPos < closePos) {
            repBody = template.substring(startPos, sepPos);
            sepBody = template.substring(sepPos + FORMAT_REPEATER_SEP_LEN, closePos);
        } else {
            repBody = template.substring(startPos, closePos);
            sepBody = "";
        }
        for (int startName=0;startName<repBody.length();){
        	int genNamePos = repBody.indexOf(FORMAT_REPEATER_NAME_MARK,startName);

        	if(genNamePos < 0)
        		throw new ToolException("Generator name (i.e. sequence '" + FORMAT_REPEATER_NAME_MARK + 
        				"NAME') not found");

        	int genNameEnd = Utils.findBoundary(repBody, genNamePos + FORMAT_REPEATER_NAME_MARK_LEN);

        	if(genNameEnd == genNamePos)
        		throw new ToolException("Generator name after '" + FORMAT_REPEATER_NAME_MARK +
        				"' mark is absent");
        	startName=genNameEnd;
        	String repPrefix = repBody.substring(0, genNamePos);
        	String genName = repBody.substring(genNamePos + FORMAT_REPEATER_NAME_MARK_LEN, genNameEnd);        
        	String repSuffix = repBody.substring(genNameEnd);

        	//       System.out.println("Gen name: '" + genName + 
        	//                          "', repPrefix: '" + repPrefix + 
        	//                          "', repSuffix: '" + repSuffix + 
        	//                          "', sepBody: '" + sepBody + 
        	//                          "'");

        	result.set(findGenerator(genName, repPrefix, repSuffix, sepBody, topProcessor),     /* Why did it miss FileListGenerator here? */   
        			closePos + FORMAT_REPEATER_CLOSE_LEN, topProcessor);

        	if(result.getGenerator() == null) { // wrong generator - should be processed separately
        		continue;
//        		throw new ToolException("Unknown generator '" + genName + "'");
        	}
        	// if prefix or suffix contains %% - evaluate them and re-run result.set(findGenerator(... 
        	if (repPrefix.contains(FORMAT_REPEATER_NAME_MARK) ||
        			repSuffix.contains(FORMAT_REPEATER_NAME_MARK) ||
        			sepBody.contains(FORMAT_REPEATER_NAME_MARK)) {
            	result.set(findGenerator(genName,
            			resolveString(repPrefix,topProcessor),
            			resolveString(repSuffix,topProcessor),
            			resolveString(sepBody,topProcessor),
            			topProcessor),     /* Why did it miss FileListGenerator here? */   
            			closePos + FORMAT_REPEATER_CLOSE_LEN, topProcessor);
        		
        	}
        	return result;
        }
        throw new ToolException("Generator name (i.e. sequence '" + FORMAT_REPEATER_NAME_MARK + "NAME') not found");
    }
    private String resolveString(String template, FormatProcessor topProcessor){
    	if ((template==null) || !template.contains(FORMAT_REPEATER_NAME_MARK)) return template;
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                new ParamFormatRecognizer(param), 
                new SimpleGeneratorRecognizer(topProcessor)
            }, false,topProcessor);
        String result = null;
        if(template != null) {
            try {
                result = processor.process(template).get(0);
            } catch(ToolException e) {
                MessageUI.error(e);
                return template;
            }
        }
        return result;
    }
    
    
    protected AbstractGenerator findGenerator(String genName, 
                                              String repPrefix, 
                                              String repSuffix,
                                              String separator,
                                              FormatProcessor topProcessor) 
    {
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
    		System.out.println("Ever get here? RepeaterRecognizer.java:findGenerator("+genName+","+repPrefix+
    				","+repSuffix+","+separator+","+((topProcessor==null)?"NULL":((topProcessor.getCurrentTool()==null)?
    						"Null":topProcessor.getCurrentTool().getName()))+")"); // yes, sure
    		// repeater in the output lines - see grep.xml
    	}
    	AbstractGenerator gen=new FilteredSourceListGenerator(repPrefix, repSuffix, separator,topProcessor);
    	if (genName.equals(gen.getName())) return gen;
    	gen=new SourceListGenerator(repPrefix, repSuffix, separator);
    	if (genName.equals(gen.getName())) return gen;
    	gen=new FileListGenerator(repPrefix, repSuffix, separator);
    	if (genName.equals(gen.getName())) return gen;
    	gen=new TopModulesNameGenerator(repPrefix, repSuffix, separator);
    	if (genName.equals(gen.getName())) return gen;
    	 /* ID, not name! */
/*    	
        if(genName.equals(SourceListGenerator.NAME))
            return new SourceListGenerator(repPrefix, repSuffix, separator);
        else if(genName.equals(FileListGenerator.NAME))
            return new FileListGenerator(repPrefix, repSuffix, separator);
        else if(genName.equals(TopModulesNameGenerator.NAME))
            return new TopModulesNameGenerator(repPrefix, repSuffix, separator);
*/        
        return null;
    }
}
