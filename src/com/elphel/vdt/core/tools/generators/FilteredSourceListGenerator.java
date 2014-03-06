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


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;








//import com.elphel.vdt.VDT;
import com.elphel.vdt.VerilogUtils;
import com.elphel.vdt.core.tools.params.FormatProcessor;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.MessageUI;
//import com.elphel.vdt.core.verilog.VerilogUtils;
import com.elphel.vdt.ui.variables.SelectedResourceManager;

/**
 * Generate the file name list of dependency closure for last selected 
 * verilog source file. 
 * 
 * Created: 21.02.2006
 * @author  Lvov Konstantin
 */

public class FilteredSourceListGenerator extends AbstractGenerator {
//    public static final String NAME = VDT.GENERATOR_ID_FILTEREDSOURCE_LIST;
    public static final String NAME = "FilteredSourceList";

    
    public FilteredSourceListGenerator(String prefix, 
                               String suffix, 
                               String separator,
                               FormatProcessor topProcessor) 
    {
        super(prefix, suffix, separator, topProcessor ); 
    }
    
    public String getName() {
        return NAME;
    }

    protected String[] getStringValues() {
        String ignoreFilter= SelectedResourceManager.getDefault().getFilter(); // old version
    	
//    	System.out.println("FilteredSourceListGenerator(), tool0="+((tool0==null)?null:(tool0.getName()+tool0.getIgnoreFilter())));
//    	System.out.print("FilteredSourceListGenerator(): ");
    	if (topProcessor!=null){
    		Tool tool=topProcessor.getCurrentTool();
//    		System.out.println(", tool="+tool+" tool name="+((tool!=null)?tool.getName():null));
    		if (tool != null) {
    			ignoreFilter=tool.getIgnoreFilter();
//        		System.out.println(" tool="+tool.getName()+", ignoreFilter="+ignoreFilter);

    		} else {
    			System.out.println("FilteredSourceListGenerator():  topProcessor.getCurrentTool() is null");
    		}
    	} else {
    		System.out.println("FilteredSourceListGenerator():  topProcessor is null");
    	}
        String[] file_names = null;
        IResource resource = SelectedResourceManager.getDefault().getChosenVerilogFile();
        Pattern ignorePattern = null;
        if (ignoreFilter!=null){
        	try {
        		ignorePattern=Pattern.compile(ignoreFilter);
        	} catch (PatternSyntaxException e){
        		System.out.println("Error in regular expression for ignore filter: \""+ignoreFilter+"\" - ignoring");
        		MessageUI.error("Error in regular expression for ignore filter: \""+ignoreFilter+"\" - ignoring");
        	}
        }
        
        if (resource != null && resource.getType() == IResource.FILE) {
        	IFile[] files = VerilogUtils.getDependencies((IFile)resource); // returned just the same x353_1.tf
        	List<String> fileList=new ArrayList<String>();
            for (int i=0; i < files.length; i++) {
            	String fileName=files[i].getProjectRelativePath().toOSString(); //.getName();
            	if ((ignorePattern!=null) &&ignorePattern.matcher(fileName).matches()) {
            			continue;
            	}
            	fileList.add(fileName);            		
            }
            file_names=fileList.toArray(new String[0]);
        } else {
//            fault("There is no selected project");
            System.out.println(getName()+": no project selected");
            return new String[] {""};
        }
        return file_names;
    }

} // class FilteredSourceListGenerator
