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

    public AbstractGenerator() {
        this(false);
    }
    
    public AbstractGenerator(String prefix, 
                             String suffix, 
                             String separator) 
    {
        this(prefix, suffix, separator, false);
    }
    
    protected AbstractGenerator(boolean forcedMultiline) {
        this("", "", "", forcedMultiline);
    }

    protected AbstractGenerator(String prefix, 
                                String suffix, 
                                String sep,
                                boolean forcedMultiline) 
    {
        this.prefix = prefix;
        this.suffix = suffix;
        this.forcedMultiline = forcedMultiline;
        this.separator = sep;

        separator = separator.replace("\\n", "\n");
        separator = separator.replace("\\t", "\t");
    }
    public void setMenuMode(boolean menuMode){
    	this.menuMode=menuMode;
    }
    public boolean getMenuMode(){
    	return menuMode;
    }

    
    public abstract String getName();
    
    public String[] generate() {
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
