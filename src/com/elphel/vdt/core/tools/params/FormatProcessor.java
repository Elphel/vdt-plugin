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
package com.elphel.vdt.core.tools.params;

import java.util.*;

import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.params.recognizers.Recognizer;
import com.elphel.vdt.core.tools.params.recognizers.RecognizerResult;
import com.elphel.vdt.core.Utils;

public class FormatProcessor {
    private static final String CONTROL_SEQ = "%"; 
    private static final int CONTROL_SEQ_LEN = CONTROL_SEQ.length(); 
    
    private static List<String> expandedGenerators = new ArrayList<String>();
    private static int callCount = 0;
    
    private final boolean recursive = true;
    private final boolean multiline;
    private final Recognizer[] recognizers;
    
    private String initialTemplate;
    
    public FormatProcessor(Recognizer[] recognizers, boolean multiline) {
        this.recognizers = recognizers;
        this.multiline = multiline;
    }
    
    public FormatProcessor(Recognizer[] recognizers) {
        this(recognizers, true);
    }
    
    public List<String> process(String template) throws ToolException {
        if(callCount++ == 0)
            expandedGenerators.clear();
        
        initialTemplate = template;
        
        List<String> outputLines = null;
        
        try {
            outputLines = processTemplate(template); // echo %SimulationTopFile %%SelectedFile ; -> null
        } finally {
            callCount--;
        }
        
        if(!multiline) {
            String str = Utils.listToString(outputLines);
        
            outputLines.clear();
            outputLines.add(str);
        }

        return outputLines;
    }

    private List<String> processTemplate(String template)
        throws ToolException 
    {
        List<String> outputLines = new ArrayList<String>();
        int posAfterLastGen = 0, pos = 0;

        while(pos < template.length()) {
            if(template.startsWith(CONTROL_SEQ, pos)) {
                pos += CONTROL_SEQ_LEN;
                
                RecognizerResult result = recognize(template, pos);
                
                if(result != null && result.getGenerator() != null) {
                    assert result.getNewPos() > pos;
                    
                    String partBetweenGenerators = 
                        template.substring(posAfterLastGen, pos - CONTROL_SEQ_LEN); 
                    
                    generateAndAdd(outputLines, 
                                   partBetweenGenerators, 
                                   result.getGenerator());
                    
                    posAfterLastGen = result.getNewPos();
                    pos = posAfterLastGen;
                } else {
//                    throw new ToolException("Cannot recognize control sequence '" + 
//                                            template.substring(pos - CONTROL_SEQ_LEN) + "'. " + 
//                                            "The full template string is '" + template + "'");
                }                
            } else {
                pos++;
            }
        }
        
        String tail = template.substring(posAfterLastGen);
        
        addLine(outputLines, tail);
        
        return outputLines;
    }
    
    private RecognizerResult recognize(String template, int pos) throws ToolException {
        for(int i = 0; i < recognizers.length; i++) {
            RecognizerResult result = recognizers[i].recognize(template, pos);
            
            assert result != null;
            
            if(result.getGenerator() != null)
                return result;
        }            
        
        return null;
    }   
    
    private void generateAndAdd(List<String> lines,
                                String firstLineToAdd,
                                AbstractGenerator generator)
        throws ToolException
    {
        checkCyclic(generator);
        
        // first, we have to add the explicitly given line  
        addLine(lines, firstLineToAdd);

        pushGen(generator.getName());
       
        // use generator
        String[] generatedLines = generator.generate();

        popGen();
        
        int addFrom = 0;
        
        if(generatedLines.length == 0)
            return;
        
        List<String> processedLines = null;
        
        if(recursive) {
            pushGen(generator.getName());
            
            processedLines = processTemplate(generatedLines[0]);
            
            for(int i = 1; i < generatedLines.length; i++)
                processedLines.addAll(processTemplate(generatedLines[i]));
            
            popGen();
        } else {
            processedLines = new ArrayList<String>(generatedLines.length);
        
            for(int i = 0; i < generatedLines.length; i++)
                processedLines.add(generatedLines[i]);
        }
        
        // we need to check if 'firstLineToAdd' ends with a blank space
        // in such a case we just add all the generated lines in the list
        // otherwise, we glue the line with first of additional ones
        if(!Utils.stringEndsWithSpace(firstLineToAdd)) {
            glueToLastLine(lines, processedLines.get(0));
            addFrom = 1;            
        }
            
        for(int i = addFrom; i < processedLines.size(); i++) {
            String line = processedLines.get(i);
            
            if(!line.equals(""))
                lines.add(line);        
        }
    }
    
    private void addLine(List<String> lines, String additionalLine) {
        if(lines.isEmpty()) {
            lines.add(additionalLine);
        } else if(multiline && Utils.stringStartsWithSpace(additionalLine)) {
            String trimLine = additionalLine.trim();
            
            if(!trimLine.equals(""))
                lines.add(trimLine);
        } else {
            // in this case we need to glue last line and this line, as there is
            // no blank space between the corresponding parts in the initial template
            glueToLastLine(lines, additionalLine);
        }
    }
    
    private void glueToLastLine(List<String> lines, String lineToGlue) {
        int last = lines.size()-1;
        
        String lastLine = lines.get(last);
        
        lines.set(last, lastLine + lineToGlue);
    }
    
    private void checkCyclic(AbstractGenerator generator)
        throws ToolException
    {
        if(Utils.containsStr(expandedGenerators, generator.getName()))
            throw new ToolException(
                    "Pattern '" + generator.getName() + 
                    "' expanded to a string containing this pattern itself. " +
                    "The initial string is '" + initialTemplate + "'.\n\n" +
                    "To understand and fix this error, check all uses of this parameter " +
                    "and try to detect the cyclic dependency by successively removing them.");
    }

    private static void pushGen(String genName) {
        expandedGenerators.add(genName);
    }

    private static void popGen() {
        expandedGenerators.remove(expandedGenerators.size()-1);
    }
}
