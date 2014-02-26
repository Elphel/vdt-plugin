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
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.generators.FileListGenerator;
import com.elphel.vdt.core.tools.generators.FilteredSourceListGenerator;
import com.elphel.vdt.core.tools.generators.TopModulesNameGenerator;
import com.elphel.vdt.core.tools.generators.SourceListGenerator;


public class RepeaterRecognizer implements Recognizer {
    private static final String FORMAT_REPEATER_NAME_MARK = "%%"; 
    
    private static int FORMAT_REPEATER_NAME_MARK_LEN = FORMAT_REPEATER_NAME_MARK.length();

    private static final String FORMAT_REPEATER_OPEN  = "("; 
    private static final String FORMAT_REPEATER_SEP   = "%|"; 
    private static final String FORMAT_REPEATER_CLOSE = "%)"; 

    private static final int FORMAT_REPEATER_OPEN_LEN = FORMAT_REPEATER_OPEN.length(); 
    private static final int FORMAT_REPEATER_SEP_LEN = FORMAT_REPEATER_SEP.length(); 
    private static final int FORMAT_REPEATER_CLOSE_LEN = FORMAT_REPEATER_CLOSE.length(); 
    
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
                       
        int genNamePos = repBody.indexOf(FORMAT_REPEATER_NAME_MARK);
        
        if(genNamePos < 0)
            throw new ToolException("Generator name (i.e. sequence '" + FORMAT_REPEATER_NAME_MARK + 
                                    "NAME') not found");
        
        int genNameEnd = Utils.findBoundary(repBody, genNamePos + FORMAT_REPEATER_NAME_MARK_LEN);

        if(genNameEnd == genNamePos)
            throw new ToolException("Generator name after '" + FORMAT_REPEATER_NAME_MARK +
                                    "' mark is absent");
        
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
        
        if(result.getGenerator() == null)
            throw new ToolException("Unknown generator '" + genName + "'");
        
        return result;
    }
    
    protected AbstractGenerator findGenerator(String genName, 
                                              String repPrefix, 
                                              String repSuffix,
                                              String separator,
                                              FormatProcessor topProcessor) 
    {
    	System.out.println("Ever get here? RepeaterRecognizer.java:findGenerator()"); // yes, sure
    	AbstractGenerator gen=new FilteredSourceListGenerator(repPrefix, repSuffix, separator);
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
