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

import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.generators.FileListGenerator;
import com.elphel.vdt.core.tools.generators.FilteredSourceListGenerator;
import com.elphel.vdt.core.tools.generators.SourceListGenerator;
import com.elphel.vdt.core.tools.generators.TopModulesNameGenerator;
import com.elphel.vdt.core.tools.generators.ValueGenerator;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Parameter;


public class ParamRepeaterRecognizer extends RepeaterRecognizer {
//    private final Parameter param; // moved to RepeaterRecognizer
    
    public ParamRepeaterRecognizer(Parameter param) {
        this.param = param;
    }

    protected AbstractGenerator findGenerator(String genName, 
                                              String repPrefix, 
                                              String repSuffix,
                                              String separator,
                                              FormatProcessor topProcessor) 
    {
    	// TODO make repetitor to accept several pattern generators with only one generating a list
    	/*
    	if(genName.equals(ParamFormatRecognizer.FORMAT_PARAM_NAME_MARK)) {
    		return new AbstractGenerator() {
    			public String getName() {
    				return "ParamName (parameter '" + param.getID() + 
    						"' of context '" + param.getContext().getName() +
    						"')";
    			}

    			protected String[] getStringValues() {
    				return new String[]{param.getOutID()};
    			}
    		};
    	}
    	*/
    	if(genName.equals(ParamFormatRecognizer.FORMAT_PARAM_VALUE_MARK))
    		return new ValueGenerator(param, repPrefix, repSuffix, separator, topProcessor);
    	/* Trying to put these here */        
        if(genName.equals(FilteredSourceListGenerator.NAME))
            return new FilteredSourceListGenerator(repPrefix, repSuffix, separator, topProcessor);
        else if(genName.equals(SourceListGenerator.NAME))
            return new SourceListGenerator(repPrefix, repSuffix, separator);
        else if(genName.equals(FileListGenerator.NAME))
            return new FileListGenerator(repPrefix, repSuffix, separator);
        else if(genName.equals(TopModulesNameGenerator.NAME))
            return new TopModulesNameGenerator(repPrefix, repSuffix, separator);
        return null;
    }
}
