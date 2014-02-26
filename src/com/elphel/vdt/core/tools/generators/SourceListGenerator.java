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


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

//import com.elphel.vdt.VDT;
import com.elphel.vdt.VerilogUtils;
//import com.elphel.vdt.core.verilog.VerilogUtils;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

/**
 * Generate the file name list of dependency closure for last selected 
 * verilog source file. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public class SourceListGenerator extends AbstractGenerator {
//    public static final String NAME = VDT.GENERATOR_ID_SOURCE_LIST;
    public static final String NAME = "SourceList";

    
    public SourceListGenerator(String prefix, 
                               String suffix, 
                               String separator) 
    {
        super(prefix, suffix, separator,null);  // null for topFormatProcessor - this generator can not reference other parameters
    }
    
    public String getName() {
        return NAME;
    }

    protected String[] getStringValues() {
        String[] file_names = null;
//        IResource resource = SelectedResourceManager.getDefault().getSelectedVerilogFile();
        IResource resource = SelectedResourceManager.getDefault().getChosenVerilogFile();
        if (resource != null && resource.getType() == IResource.FILE) {
        	IFile[] files = VerilogUtils.getDependencies((IFile)resource); // returned just the same x353_1.tf
            file_names = new String[files.length];
            for (int i=0; i < files.length; i++)
                file_names[i] = files[i].getProjectRelativePath().toOSString(); //.getName();
        } else {
            fault("There is no selected project");
        }
        return file_names;
    }

} // class SourceListGenerator
