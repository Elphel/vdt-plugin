/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful,
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
package com.elphel.vdt.core.tools.params.conditions;

import java.util.ArrayList;
import java.util.List;

import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.ToolException;
import com.elphel.vdt.core.tools.params.recognizers.ContextParamRecognizer;
import com.elphel.vdt.core.tools.params.recognizers.Recognizer;
import com.elphel.vdt.core.tools.params.recognizers.SimpleGeneratorRecognizer;
import com.elphel.vdt.ui.MessageUI;

public class StringComparison extends Comparison {
    private Context context;
    private String left, right;
//    private FormatProcessor topProcessor;
    
    // constructor should not reference topProcessor (it is only used at startup)
    public StringComparison(COMPARE_OP op, String left, String right) {
        this(op, null, left, right);
    }
    
    public StringComparison(COMPARE_OP op, Context context, String left, String right) {
        super(op);
        
        this.context = context;
        this.left = left;
        this.right = right;
    }
    
    public boolean equals(Object other) {
        if(this == other)
            return true;

        if(!(other instanceof StringComparison))
            return false;
            
        StringComparison otherComparison = (StringComparison)other;
        
        return (context == otherComparison.context) &&
               op.equals(otherComparison.op) &&
               left.equals(otherComparison.left) && 
               right.equals(otherComparison.right);
    }
    
    public boolean isTrue(FormatProcessor topProcessor) {
        String actualLeft = left;
        String actualRight = right;

        if(context != null) {
            if (topProcessor==null) topProcessor=new FormatProcessor(context);
            else topProcessor.setCurrentTool(context);
            FormatProcessor processor = 
                new FormatProcessor(new Recognizer[] {
                    new ContextParamRecognizer(context,topProcessor),
                    new SimpleGeneratorRecognizer(topProcessor)
                },topProcessor);
                    
            try {
                actualLeft = processor.process(left).get(0);            
                actualRight = processor.process(right).get(0);            
            } catch(ToolException e) {
                MessageUI.error(e);
            }
        }
        
        return op.isTrue(actualLeft, actualRight);
    }

    public List<String> getDependencies() {
        List<String> deps = new ArrayList<String>();
        
        deps.add(left);
        deps.add(right);
        
        return deps; 
    }
}
