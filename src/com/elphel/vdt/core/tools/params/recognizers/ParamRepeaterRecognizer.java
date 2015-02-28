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

import com.elphel.vdt.core.tools.generators.AbstractGenerator;
import com.elphel.vdt.core.tools.generators.FileListGenerator;
import com.elphel.vdt.core.tools.generators.FilteredSourceListGenerator;
import com.elphel.vdt.core.tools.generators.SourceListGenerator;
import com.elphel.vdt.core.tools.generators.FilteredIncludesListGenerator;
import com.elphel.vdt.core.tools.generators.IncludesListGenerator;
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
        if(genName.equals(FilteredIncludesListGenerator.NAME))
            return new FilteredIncludesListGenerator(repPrefix, repSuffix, separator, topProcessor);
        else if(genName.equals(IncludesListGenerator.NAME))
            return new IncludesListGenerator(repPrefix, repSuffix, separator);
        else if(genName.equals(FileListGenerator.NAME))
            return new FileListGenerator(repPrefix, repSuffix, separator);
        else if(genName.equals(TopModulesNameGenerator.NAME))
            return new TopModulesNameGenerator(repPrefix, repSuffix, separator);
        return null;
    }
}
