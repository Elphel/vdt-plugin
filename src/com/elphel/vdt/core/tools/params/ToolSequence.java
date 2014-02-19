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
package com.elphel.vdt.core.tools.params;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Display;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.core.launching.ToolLogFile;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.params.Tool.TOOL_MODE;
import com.elphel.vdt.core.tools.params.Tool.TOOL_STATE;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.options.FilteredFileSelector;
import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.ui.views.DesignFlowView;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class ToolSequence {
	private DesignFlowView designFlowView;
	public ToolSequence(DesignFlowView designFlowView){
		this.designFlowView=designFlowView;
	}
	public void toolFinished(Tool tool){
		if (tool.isRunning()) {
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
				System.out.println("\nTool "+tool.getName()+" is still running");
			return;
		}
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
			System.out.println("\nTool "+tool.getName()+" FINISHED - add more stuff here");
		if (tool.getState()==TOOL_STATE.SUCCESS){
			if (tryAutoSave(tool)) return;  // started autoSave that will trigger "toolFinished" again
			updateLinkLatest(tool); // TODO - maybe swap and do updateLinkLatest before tryAutoSave
		} else if (tool.getState()==TOOL_STATE.KEPT_OPEN){
			
		} else {
			
		}
		if (designFlowView!=null){
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					designFlowView.updateLaunchAction(); // Run from Display thread to prevent "invalid thread access" when called from Runner
				}
			});
		}
	}
	private boolean tryAutoSave(Tool tool){
		if  (
				(tool.getSave()!=null) &&
				tool.getAutoSave() &&
				(designFlowView!=null) &&
				(tool.getLastMode()==TOOL_MODE.RUN)) { // it was not playback of logs
			final Tool fTool=tool.getSave();
			final IProject fProject = SelectedResourceManager.getDefault().getSelectedProject();
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
				System.out.println("Launching autosave tool "+fTool.getName()+" for "+tool.getName());
			}
			fTool.setDesignFlowView(designFlowView); // maybe will not be needed with ToolSequencing class
//    		fTool.setRunning(true);
			fTool.setMode(TOOL_MODE.SAVE);
//    		fTool.toolFinished();
			fTool.setChoice(0);
//   		SelectedResourceManager.getDefault().updateActionChoice(fullPath, choice, ignoreFilter); // A
//   		SelectedResourceManager.getDefault().setBuildStamp(); // Use the same from Master
    		// apply designFlowView to the tool itself
			Display.getDefault().asyncExec(new Runnable() { 
				public void run() {
		    		try {
						LaunchCore.launch( fTool,
								fProject,
								null, //fullPath,
								null);  // run, not playback 
					} catch (CoreException e1) {
                        MessageUI.error( Txt.s("Action.ToolLaunch.Error", 
                                new String[] {"Autosave by "+fTool.getName(), e1.getMessage()})
                         , e1);
					}
				}
			});
			return true;
		} else if (tool.getSaveMaster()!=null){
//			tool.getSaveMaster().setRunning(false); // turn off the master tool that invoked this save one (may be called directly too)
    		tool.getSaveMaster().setMode(TOOL_MODE.STOP);

			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
				System.out.println("Finished autosave tool "+tool.getName()+" for "+tool.getSaveMaster().getName());
			}
		}
		return false;
	}
	
	// Result file may be skipped, in that case link should not be updated, but the console state should be
	private void updateLinkLatest(Tool tool){
		if (tool.getLastMode()==TOOL_MODE.PLAYBACK) return; // do nothing here
		String stateDirString=tool.getStateDir();
		String linkString=tool.getResultName();
		String targetString=tool.getStateFile(); // With timestamp or specifically set through selection 
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
		// Create file for target and see if it actually exists
		IFile target=  stateDir.getFile(targetString);
		// Eclipse does not know IFile exists until it refreshes. It is also possible to test File existence, not the IFile
		try {
			target.refreshLocal(0, null); // long-running
		} catch (CoreException e1) {
			System.out.println("Failed to refreshLocal "+target.getLocation().toOSString()+" e="+e1.toString());
		}
		File file=new File (target.getLocation().toOSString());
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
			System.out.println("File "+file.getAbsolutePath()+" exists="+file.exists());
			System.out.println("IFile "+target.getLocation().toOSString()+" exists="+target.exists());
		}
//		if (!file.exists()){
		if (!target.exists()){
			System.out.println("Will not link "+linkString+" to nonexistent resource:"+targetString+" in "+stateDirString+": "+target.getLocation());
			return; // No link created as there was no snapshot, but the console state is valid.
		}
		IFile link=   stateDir.getFile(linkString);
		
		try {
			link.createLink(
					target.getLocation(),
					IResource.ALLOW_MISSING_LOCAL |  IResource.REPLACE,
					null);
		} catch (CoreException e) {
        	MessageUI.error("Failed to create link "+link.toString()+" to the target "+
        			target.toString()+" e="+e.toString());
        	return;
		}
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
			System.out.println("Created link "+link.toString()+" to the target "+
					target.toString());
			
			System.out.println("link.getRawLocation()=  "+link.getRawLocation().toString());
			System.out.println("link.getModificationStamp()=    "+link.getModificationStamp());
			System.out.println("target.getModificationStamp()=  "+target.getModificationStamp());

		}
	}

	private boolean updateSessionTools(Tool tool){
		String targetString=tool.getStateFile();
		// after restore this may be a non-timestamp file - use current timestamp instead of the restored?
		if (targetString==null){
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
				System.out.println("No result state specified for tool "+tool.getName()+
						", no session will be updated");
			return false;
		}
		List<Tool> sessionList=new ArrayList<Tool>();
		List<String> consoleNames=tool.getSessionConsoles();
		if (consoleNames!=null){
			for(Iterator<String> iter = consoleNames.iterator(); iter.hasNext();) {
				String consoleName=iter.next();
				if (consoleName!=null) {
					Tool consoleTool=ToolsCore.getContextManager().findTool(consoleName);
					if (tool!=null) sessionList.add(consoleTool);

				}
			}
		}
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
			System.out.println("Found "+sessionList.size()+" console sessions for this tool "+tool.getName());
		if (sessionList.size()>0){
			for(Iterator<Tool> iter = sessionList.iterator(); iter.hasNext();) {
				Tool consoleTool=iter.next();
				consoleTool.setOpenState(targetString);
				consoleTool.setOpenTool(tool);
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
					System.out.println("Set openState of "+consoleTool.getName()+" to "+targetString);
			}
		}
		return true;
	}
	public  String getSelectedStateFile(Tool tool, boolean select){
		String [] filter=splitResultName(tool);
		String linkString=tool.getResultName();
		if (filter==null) return null;
		String stateDirString=tool.getStateDir();
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
		if (!select){
			if (linkString==null) {
				MessageUI.error("Saved data for tool "+tool.getName()+" is not specified");
				return null;
			}
			IFile link=   stateDir.getFile(linkString);
			if (!link.exists()) {
				MessageUI.error("Saved data for tool "+tool.getName()+": "+ link.getLocation().toOSString()+" does not exist.");
				return null;
			}
			String targetString=link.getRawLocation().makeRelativeTo(stateDir.getRawLocation()).toString();
			IFile target=stateDir.getFile(targetString);
			if (!target.exists()){
				MessageUI.error("Saved data for tool "+tool.getName()+": "+targetString+", pointed by link "+
						link.getLocation().toOSString()+" does not exist.");
				return null;
			}
			return targetString;
		}
    	FilteredFileSelector selector= new FilteredFileSelector(
    			stateDir.getLocation().toFile() , //File dir,
    			"Select snapshot file for "+tool.getName(), //String title,
    			 null, // Component parent, or convert from SHell VerilogPlugin.getActiveWorkbenchShell()
    			"Select", //String approveText,
    			"Select snapshot file to restore", //String approveToolTip,
    			filter[0]+ToolLogFile.BUILD_STAMP_SEPARATOR,
    			filter[1],
    			false, // allow empty middle
    			"Matchig log files for "+tool.getName(), //String filterDescription,
    			false //boolean allowDirs
    			);
    	File result=selector.openDialog();
    	if (result == null) {
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
    			System.out.println("Selection canceled");
    		return null;
    	}
    	return result.getName();
	}
	
	public static String[] splitResultName(Tool tool){
		String linkString=tool.getResultName();
		if (linkString==null) return null;
		int index=linkString.lastIndexOf(".");
		String [] result={
				((index>=0)?linkString.substring(0,index):linkString),
				((index>=0)?linkString.substring(index):"")
		};
		return result;
	}
	
	
}

