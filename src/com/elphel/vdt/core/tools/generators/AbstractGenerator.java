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
package com.elphel.vdt.core.tools.generators;

import com.elphel.vdt.core.Utils;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.MessageUI;

/**
 * Common engine for strings generation. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public abstract class AbstractGenerator {
    protected final String prefix, suffix;
    protected String separator;
    private final boolean forcedMultiline;
    private boolean menuMode=false; // managing menu items, not running tool. Ignore Generator errors
    protected Tool tool0; // "tool" was already used in ToolParamRecognizer / Andrey
    protected FormatProcessor topProcessor; // to protect from cycles in recursion, replacing static in FormatProcessor / Andrey

    public AbstractGenerator(FormatProcessor processor) {
        this(false, processor);
    }
    
    public AbstractGenerator(String prefix, 
                             String suffix, 
                             String separator,
                             FormatProcessor processor) 
    {
        this(prefix, suffix, separator, false,processor);
    }
    
    protected AbstractGenerator(boolean forcedMultiline, FormatProcessor processor) {
        this("", "", "", forcedMultiline, processor);
    }

    protected AbstractGenerator(String prefix, 
                                String suffix, 
                                String sep,
                                boolean forcedMultiline,
                                FormatProcessor processor) 
    {
        this.prefix = prefix;
        this.suffix = suffix;
        this.forcedMultiline = forcedMultiline;
        this.separator = sep;
        if (separator!=null) {
        	separator = separator.replace("\\n", "\n");
        	separator = separator.replace("\\t", "\t");
        }
        topProcessor=processor;
    }
    public void setMenuMode(boolean menuMode){
    	this.menuMode=menuMode;
    }
    public boolean getMenuMode(){
    	return menuMode;
    }
    public void setTool(Tool tool){
    	this.tool0=tool;
    }
    protected FormatProcessor getTopProcessor(){return topProcessor;}

    
    public abstract String getName();
    
    public String[] generate() {
    	// Andrey: added separator==null option to enable copying list parameter to another list parameter as default
    	// TODO: Make sure nothing else is broken because of that
    	if (separator==null) return getStringValues();
        boolean multiline = Utils.stringContainsSpace(separator);
        String[] output;
        
        if(multiline || forcedMultiline)
            output = generateMultipleLine(prefix, suffix, separator);
        else
            output = generateSingleLine(prefix, suffix, separator);
        
        return output;
    }
    
    protected String[] generateSingleLine(String prefix, String suffix, String separator) {
        String[] values = getStringValues();
        String output = "";

        if((values != null) && (values.length > 0)) {
            for(int i = 0; i < values.length; i++) {
//                output += prefix + values[i] + suffix + separator;
                output += prefix + values[i] + suffix; /*TODO: Check I did not break anything, but for a "repetitor" separator was duplicate*/
                
                if(i < values.length-1)
                    output += separator;
            }
        }
        
        return new String[]{output};
    }

    protected String[] generateMultipleLine(String prefix, String suffix, String separator) {
        String[] values = getStringValues();

        if((values == null) || (values.length == 0))
            return new String[]{""};
        
        final int outputLen = values.length*2 - 1;            
        String[] output = new String[outputLen];
        
        for(int i = 0; i < values.length; i++) {
            output[i*2] = prefix + values[i] + suffix;
            
            if(i < values.length-1)
                output[i*2+1] = separator;
        }
        
        return output;
    }
    
    protected String fault(String message) {
    	if (menuMode)
    		return "";
        MessageUI.error("Generator '" + getName() + "' fault: " + message);
        return null;
    }
    
    protected abstract String[] getStringValues();
}
