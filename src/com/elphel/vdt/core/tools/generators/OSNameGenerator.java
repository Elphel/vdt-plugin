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

import com.elphel.vdt.VDT;
import com.elphel.vdt.ui.MessageUI;


public class OSNameGenerator extends AbstractGenerator {
    public static final String NAME = VDT.GENERATOR_ID_OS_NAME;

    private static final String OS_WINDOWS = "Windows";
    private static final String OS_LINUX = "Linux";
    
    public String getName() {
        return NAME;
    }
    public OSNameGenerator()
    {
    	super(null); // null for topFormatProcessor - this generator can not reference other parameters
    }

    protected String[] getStringValues() {
    	String osName = System.getProperty("os.name");

        if(osName.indexOf(OS_WINDOWS) >= 0) {
            return new String[] { OS_WINDOWS };
        } else if (osName.indexOf("Linux") >= 0) {
            return new String[] { OS_LINUX };
        } else {
            MessageUI.error("Generator '" + getName() + "' failure: OS '" + osName + "' is unknown");
            return null;
        }
    }
}
