/*******************************************************************************
 * Copyright (c) 2015 Elphel, Inc.
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

public class FilteredIncludesListGenerator extends AbstractGenerator {
//    public static final String NAME = VDT.GENERATOR_ID_FILTEREDSOURCE_LIST;
    public static final String NAME = "FilteredIncludesList";

    
    public FilteredIncludesListGenerator(String prefix, 
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
        String topFile =    null;
        String toolDefine = null;
    	if (topProcessor!=null){
    		Tool tool=topProcessor.getCurrentTool();
    		if (tool != null) {
    			ignoreFilter=tool.getIgnoreFilter();
                topFile = tool.getTopFile();
                toolDefine = tool.getDefine();
    		} else {
    			System.out.println("FilteredIncludesListGenerator():  topProcessor.getCurrentTool() is null");
    		}
            if ((toolDefine == null) || (toolDefine == "")) {
                if ((topFile == null ) || (topFile == "")) toolDefine = null; // no need to reparse tree for this tool
                else toolDefine = ""; // reparse for this tool - top file may have different defines
            }
            if (!tool.needsTreeReparse()) toolDefine = null;
    	} else {
    		System.out.println("FilteredIncludesListGenerator():  topProcessor is null");
    	}
        String[] file_names = null;
        IResource resource = SelectedResourceManager.getDefault().getChosenVerilogFile();
        if ((topFile != null) && (topFile !="") && (resource !=null)) {
            IResource resource1 = resource.getProject().getFile(topFile);
            if ((resource1 != null) && (resource1.getType() == IResource.FILE)){
                resource = resource1;
//                System.out.println("resource1="+resource1);
            }
        }
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
        	IFile[] files = VerilogUtils.getIncludedDependencies((IFile)resource, toolDefine); // returned just the same x353_1.tf
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

} // class FilteredIncludesListGenerator
