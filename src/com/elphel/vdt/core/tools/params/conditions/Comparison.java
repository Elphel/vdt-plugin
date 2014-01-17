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

import java.util.List;

public abstract class Comparison extends Condition {
    protected COMPARE_OP op;
    
    public enum COMPARE_OP {
        EQ, NEQ;
        
        public boolean isTrue(String s1, String s2) {
            switch(this) {
                case EQ:  return  s1.equals(s2);
                case NEQ: return !s1.equals(s2);
            }

            assert false;            
            return false;
        }        
    }
    
    public Comparison(COMPARE_OP op) {
        this.op = op;
    }

    public abstract boolean equals(Object other);
    public abstract boolean isTrue();
    public abstract List<String> getDependencies();
}
