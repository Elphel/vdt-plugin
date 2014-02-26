/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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

 *******************************************************************************/
package com.elphel.vdt.core.tools.generators;

import com.elphel.vdt.VDT;
import com.elphel.vdt.ui.variables.SelectedResourceManager;


/**
 * Generates name of the sate file without extension
 *
 */
public class StateBaseGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_STATE_FILE;
    public StateBaseGenerator()
    {
    	super(null); // null for topFormatProcessor - this generator can not reference other parameters
    }

    public String getName() {
        return NAME;
    }
    
    protected String[] getStringValues() {
    	String base=(tool0!=null)?tool0.getStateFile(): "";
    	if (base.lastIndexOf('.')>=0) base=base.substring(0,base.lastIndexOf('.'));
    	return new String[] {base};
    }
}
