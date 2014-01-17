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
    
    public boolean isTrue() {
        String actualLeft = left;
        String actualRight = right;

        if(context != null) {
            FormatProcessor processor = 
                new FormatProcessor(new Recognizer[] {
                    new ContextParamRecognizer(context),
                    new SimpleGeneratorRecognizer()
                });
                    
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
