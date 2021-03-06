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
package com.elphel.vdt;

import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
//import java.util.Map;


import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.document.HdlDocument;
import com.elphel.vdt.veditor.document.VerilogDocument;
import com.elphel.vdt.veditor.parser.OutlineContainer;
import com.elphel.vdt.veditor.parser.OutlineDatabase;
//import com.elphel.vdt.veditor.parser.vhdl.VhdlOutlineElementFactory.PackageDeclElement;


import com.elphel.vdt.veditor.parser.OutlineElement;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Verilog file utilities.
 * 
 * Created: 04.02.2006
 * @author  Lvov Konstantin
 */
public class VerilogUtils {
	public static boolean  existsVeditorOutlineDatabase(IProject project){
		try {
			if (project.getSessionProperty(VerilogPlugin.getOutlineDatabaseId()) !=null) return true;
		} catch (CoreException e) {
			System.out.println("Probably project is closed: "+e);
		}
		return false;
	}
	
	public static OutlineDatabase getVeditorOutlineDatabase(IProject project){
		OutlineDatabase database=null;
		HdlDocument hdlDocument=null;
		try {
			database = (OutlineDatabase)project.getSessionProperty(VerilogPlugin.getOutlineDatabaseId());			
		} catch (CoreException e) {
			System.out.println("Probably project is closed: "+e);
		}
		if (database !=null) return database;
   		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
   			System.out.println("database is null, looking for the HdlDocument");
		try {
			hdlDocument=(HdlDocument)project.getSessionProperty(VerilogPlugin.getHdlDocumentId());
			if ((hdlDocument!=null) && (hdlDocument.getFile()!=null)) {
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
					System.out.println("database is null, looking for the HdlDocument="+hdlDocument.getFile().toString());
			} else {
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
					System.out.println("database is null, HdlDocument="+((hdlDocument==null)?"NULL":" not null, but getFile() is NULL"));
			}
			
		} catch (CoreException e) {			
			e.printStackTrace();
		}
		if (hdlDocument!=null) return hdlDocument.getOutlineDatabase(); /* will create a new one if does not exist */
		// Create HdlDocument from selected/restored HDLfile
		if (SelectedResourceManager.getDefault().getChosenVerilogFile()==null) {
			System.out.println("database is null, and no Verilog file is selected to create one.");
			return null;
		}
		// getChosenVerilogFile() sometimes returns project (after switching projects)
		hdlDocument=new VerilogDocument(project, (IFile) SelectedResourceManager.getDefault().getChosenVerilogFile());
		return hdlDocument.getOutlineDatabase(); /* will create a new one if does not exist */
	}
	
	public static String [] getExtList(String str){
		return str.split("[\\, ]+");
	}
	public static boolean isVerilogFile(IFile file){
		String name=file.getName();
		String [] patterns=getExtList(VerilogPlugin.getPreferenceString(PreferenceStrings.VERILOG_EXT));
		for (int i=0;i<patterns.length;i++) if (name.endsWith(patterns[i])) return true;
		return false;
	}
	public static boolean isVhdlFile(IFile file){
		String name=file.getName();
		String [] patterns=getExtList(VerilogPlugin.getPreferenceString(PreferenceStrings.VHDL_EXT));
		for (int i=0;i<patterns.length;i++) if (name.endsWith(patterns[i])) return true;
		return false;
	}
	
	public static boolean isHhdlFile(IFile file){
		return isVerilogFile(file) || isVhdlFile(file); 
	}
	
	
	
	
	
	
	
    /**
     * Returns the top module(s) for given verilog file.
     */

	public static String[] getTopModuleNames(IFile file) { //L/x353/data/vdt/workspace_11-runtime/x353/x353_1.tf
    	OutlineElement[] outlineElements= getTopModulesVeditor(file); // empty inside
    	if (outlineElements==null) return null;
    	String [] list = new String[outlineElements.length];
    	for (int i=0;i<list.length;i++) {
    		list[i] = outlineElements[i].getName();
    	}
    	return list;
    }
    /**
     * Returns the top module for given verilog file.
     */
    public static OutlineElement getTopModuleVeditor(IFile file) {
    	OutlineElement[] outlineElements= getTopModulesVeditor(file);
    	if ((outlineElements!=null) && (outlineElements.length>0)) return outlineElements[0];
        else
            return null;
    } // getTopModuleVeditor()
    
    public static OutlineElement[] getTopModulesVeditor(IFile file) {
        IProject project = file.getProject();
        if (project==null){
        	System.out.println("getTopModulesVeditor(): Project is null for file="+file.getFullPath());
        	return null;
        }
        OutlineDatabase outlineDatabase=getVeditorOutlineDatabase(project);
        if (outlineDatabase==null){
        	System.out.println("getTopModulesVeditor(): outlineDatabase is null for project: "+project+" file="+file.getFullPath());
        	return null;
        }
    	OutlineContainer outlineContainer=outlineDatabase.getOutlineContainer(file);
        if (outlineContainer != null) {
        	OutlineElement[] allTopElements=outlineContainer.getTopLevelElements();
        	ArrayList<OutlineElement> list = new ArrayList<OutlineElement>();
    		for (int i=0;i<allTopElements.length;i++){
    			if (allTopElements[i].getParent()==null) list.add(allTopElements[i]);
    		}
    		return list.toArray(new OutlineElement[0]);
        } else
            return null;
    } // getTopModulesVeditor()
    
    /**
     * Returns dependency closure for given verilog file.
     */

    public static IFile[] getDependencies(IFile [] topFiles) {
    	if (topFiles==null) return null;
        IProject project = topFiles[0].getProject();
        OutlineDatabase outlineDatabase=getVeditorOutlineDatabase(project);
    	return outlineDatabase.getClosureSorted(topFiles);
    } // getDependencies()
    
    public static IFile[] getDependencies(IFile topFile, String toolDefine) {
//		System.out.println("===VerilogUtils.getDependencies("+topFile+")");
        IProject project = topFile.getProject();
        if (toolDefine != null) {
            OutlineDatabase outlineDatabase=new OutlineDatabase(project); // new OutlineDatabase just for this scan
            outlineDatabase.scanFilesWithDefine(topFile,toolDefine);
       		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_CLOSURE)) {
	    		System.out.println("-->>>VerilogUtils.getDependencies number of files="+outlineDatabase.getDatabaseFileList().length);
	    		for (int i=0; i<outlineDatabase.getDatabaseFileList().length; i++){
	    			System.out.println(i+": "+outlineDatabase.getDatabaseFileList()[i]);
	    		}
       		}
    		IFile[] topFiles = {topFile}; 
    		return outlineDatabase.getClosureSorted(topFiles);
        } else {
       		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_CLOSURE)) {
       			System.out.println("===---VerilogUtils.getDependencies("+topFile+") - using editor depends");
       		}
        	return getDependencies(new IFile [] {topFile});	
        }
    }    

    /**
     * Returns included files dependency closure for given verilog file.
     */

    public static IFile[] getIncludedDependencies(IFile topFile, String toolDefine) {
//		System.out.println("===VerilogUtils.getIncludedDependencies("+topFile+", "+toolDefine+")");
        IProject project = topFile.getProject();
        if (toolDefine != null) {
            OutlineDatabase outlineDatabase=new OutlineDatabase(project); // new OutlineDatabase just for this scan
            outlineDatabase.scanFilesWithDefine(topFile,toolDefine);
//    		System.out.println("-->>>VerilogUtils.getDependencies number of files="+outlineDatabase.getDatabaseFileList().length);
//    		for (int i=0; i<outlineDatabase.getDatabaseFileList().length; i++){
//    			System.out.println(i+": "+outlineDatabase.getDatabaseFileList()[i]);
//    		}
    		IFile[] topFiles = {topFile}; 
    		return outlineDatabase.getClosureIncludes(topFiles);
        } else {
        	return getIncludedDependencies(new IFile [] {topFile});
        }
    }    
    
    public static IFile[] getIncludedDependencies(IFile [] topFiles) {
    	if (topFiles==null) return null;
        IProject project = topFiles[0].getProject();
        OutlineDatabase outlineDatabase=getVeditorOutlineDatabase(project);
    	return outlineDatabase.getClosureIncludes(topFiles);
    } // getDependencies()
    

    /* for now all modules, including library ones */
    
    public static OutlineElement[] getModuleListVeditor(IProject project) {
   		OutlineDatabase database=getVeditorOutlineDatabase(project);
   		return database.findTopLevelElements(""); // get all top level elements
    }
    
    /**
     * Returns all verilog modules from given file.
     */
    public static OutlineElement[] getModuleListVeditor(IFile file) {
    	
        IProject project = file.getProject();
    	OutlineContainer outlineContainer=getVeditorOutlineDatabase(project).getOutlineContainer(file);
        if (outlineContainer != null) {
            return outlineContainer.getTopLevelElements();
        } else
            return null;
    }
    
    
    
    
    /**
     * Returns true if module with such name elready exists in given file.
     */
/*    
    public static boolean existsModule(IFile file, String moduleName) {
        IProject project = file.getProject();
        getModuleListManager().setCurrent(project);
        ModuleList projectModules = getModuleListManager().find(project);
        Set<String> modules = new HashSet<String>();
        for (Iterator<Module> i = projectModules.iterator(); i.hasNext(); ) {
            Module module = i.next();
            if (module.getSourceFile().getFile().getFullPath().equals(file.getFullPath()))
                modules.add(module.getName());
        }
        return modules.contains(moduleName);
    }
 */   
    /**
     * Returns all verilog files in specified container.
     */
    //Not used?
    public static IFile[] getVerilogFiles(IContainer parent) {
        List<IFile> files = new ArrayList<IFile>();
        getVerilogFiles(parent, files);
        return (IFile[])files.toArray(new IFile[files.size()]);
    } // getVerilogFiles()


    private static void getVerilogFiles(IContainer parent, List<IFile> list) {
        try {
            IResource[] members = parent.members();
            for (int i = 0; i < members.length; i++) {
                IResource resource = members[i];
                if (resource instanceof IContainer) {
                    getVerilogFiles((IContainer)resource, list);
                }
                if (resource instanceof IFile) {
                    IFile file = (IFile)resource;
                    if (file.getName().endsWith(".v"))
                        list.add((IFile)resource);
                }
            }
        } catch (CoreException e) {
        }
    } // getVerilogFiles()
    /**
     * Get the full text from one of the open editor windows or null (if there is none)
     * @param file IFile for which we are looking for text
     * @return full text of the current state of the file or null
     */
    public static String getEditorText(IFile file){
    	try {
    		IEditorPart editor=org.eclipse.ui.ide.ResourceUtil.findEditor(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage(), file);
    		if (editor instanceof TextEditor) {
    			IDocument doc =	((ITextEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput());
    			return doc.get();
    		}
    	} catch (Exception e) {
    		
    	}
    	return null;
    }
    
/*
 * 
 IDocument doc =
((ITextEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput());
String text = doc.get();

IWorkbench wb = PlatformUI.getWorkbench();
IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
IWorkbenchPage page = window.getActivePage();
IEditorPart editor = page.getActiveEditor();
IEditorInput input = editor.getEditorInput();
IPath path = ((FileEditorInput)input).getPath();

 */
/*
    
    private static SourceFile getSourceFile(IFile file) {
        IProject project = file.getProject();
        getModuleListManager().setCurrent(project);
        ModuleList projectModules = getModuleListManager().find(project);
        SourceFile sourceFile = projectModules.findSourceFile(file);
        return sourceFile;      
    } // getSourceFile()
    
    public static final ModuleListManager getModuleListManager() {
    	return ModuleListManager.getDefault();
    }
*/    
} // class VerilogUtils
