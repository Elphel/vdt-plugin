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
package com.elphel.vdt.core.tools.params.types;

import org.eclipse.core.resources.IProject;

import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.ControlInterface;
import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;


public class ParamTypeString extends ParamType {
    public static final String NAME = "string";

    public static final int    DEFAULT_LENGTH = 256;
    
    public static final String MAX_LENGTH_ID = "maxlength";
    
    public static final String CASE_SENSITIVITY_ID = "sensitivity";
    public static final String CASE_SENSITIVE_ID   = "sensitive";
    public static final String CASE_INSENSITIVE_ID = "insensitive";
    public static final String CASE_LOWERCASE_ID   = "lowercase";
    public static final String CASE_UPPERCASE_ID   = "uppercase";
    
    public static final String KIND_ID = "textkind";
    public static final String KIND_TEXT_ID = "text";
    public static final String KIND_FILE_ID = "file";
    public static final String KIND_DIR_ID = "dir";
    public static final String FILEMASK_ID = "filemask";
    
    public enum CASE {
        SENSITIVE,
        INSENSITIVE,
        LOWERCASE,
        UPPERCASE
    }

    public enum KIND {
        TEXT,
        FILE,
        DIR
    }
    
    private static final String DEFAULT_FILEMASK = "*";
    
    private final KIND kind;
    private final CASE caseSensitive;
    private final int maxLength;
    private final String filemask;
    private final boolean is_list;
    
    public ParamTypeString(boolean is_list, 
                           int maxLength, 
                           CASE caseSensitive) 
    {
        this(is_list, maxLength, caseSensitive, KIND.TEXT);
    }
    
    public ParamTypeString(boolean is_list, 
                           int maxLength, 
                           CASE caseSensitive, 
                           KIND kind) 
    {
        this(is_list, maxLength, caseSensitive, kind, DEFAULT_FILEMASK);
    }
    
    public ParamTypeString(boolean is_list,
                           int maxLength, 
                           CASE caseSensitive, 
                           KIND kind, 
                           String filemask) 
    {
        this.maxLength = maxLength;
        this.caseSensitive = caseSensitive;
        this.filemask = filemask;
        this.is_list = is_list;
        this.kind = kind;
    }
    
    public void init(ControlInterface controlInterface, String typedefName)
        throws ConfigException 
    {
        if(maxLength <= 0)
            throw new ConfigException("Maximum length (" + maxLength + 
                                      ") in '" + NAME + 
                                      "' type '" + typedefName +
                                      "' is bad: must be positive");
    }    

    public String getName() {
        return NAME;
    }
    
    public boolean isList() {
        return is_list;
    }

    public KIND getKind() {
        return kind;
    }

    public String getFilemask() {
        return filemask;
    }

    public CASE getCaseSensitive() {
        return caseSensitive;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public String toExternalForm(String paramValue) {
        return paramValue;
    }
    
    public boolean equal(String value1, String value2) {
        switch(caseSensitive) {
            case SENSITIVE: 
                return value1.equals(value2);
            
            default:
                return value1.equalsIgnoreCase(value2);
        }
    }
    
    public String tryProjectRelativePath(String path){
    	if (path==null)
    		return null;
        IProject project = SelectedResourceManager.getDefault().getSelectedProject();
    	if (project==null) return path;
        String projectPath=project.getLocation().toString();
    	if (path.startsWith(projectPath)) {
        	if (path.equals(projectPath)){
        		System.out.println("Path equals to project path = \""+path+"\", returning \".\"");
        		return ".";
        	}
    		return path.substring(projectPath.length()+1);
    	}
    	return path;
    }

    public String canonicalizeValue(String value) {
// Try to convert file/dir parameters to project-relative
    	if ((kind == KIND.FILE) || (kind == KIND.FILE)) {
//    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) System.out.print("Converting \""+value+"\"to ");
    		value=tryProjectRelativePath(value);
//    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) System.out.println("\""+value+"\"");
    	}
    	
        switch(caseSensitive) {
            case UPPERCASE: 
                return value.toUpperCase();

            case LOWERCASE: 
                return value.toLowerCase();
                
            default:
                return value;
        }        
        
    }
    
    public void checkValue(String value) throws ConfigException {
        if(value.length() > maxLength)
            throw new ConfigException("Bad string '" + value +
                                      "' value length " + value.length() +
                                      ": must be less or equal to " + maxLength); 
            
    }
}
