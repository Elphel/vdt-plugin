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

import java.util.*;

import com.elphel.vdt.core.tools.params.FormatProcessor;

public class Condition {
    private Condition left, right;
    private BOOL_OP op;
    
    public enum BOOL_OP {
        AND, OR;
        
        public boolean isTrue(boolean left, boolean right) {
            switch(this) {
                case AND: return left && right;
                case OR:  return left || right;
            }

            assert false;            
            return false;
        }
    }
    
    protected Condition() {        
    }
    
    public Condition(BOOL_OP op, Condition left, Condition right) {
        this.op = op;
        this.left = left;
        this.right = right;         
    }
    
    public boolean equals(Object other) {
        if(this == other)
            return true;

        if(!(other instanceof Condition))
            return false;
            
        Condition otherCondition = (Condition)other;
        
        return op.equals(otherCondition.op) &&
               left.equals(otherCondition.left) && 
               right.equals(otherCondition.right);
    }
    
    public boolean isTrue(FormatProcessor topProcessor) {
        return op.isTrue(left.isTrue(topProcessor), right.isTrue(topProcessor));
    }
    
    public List<String> getDependencies() {
        List<String> deps = left.getDependencies();
        
        deps.addAll(right.getDependencies());
        
        return deps; 
    }
}
