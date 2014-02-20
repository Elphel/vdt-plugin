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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.Display;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
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
    public static final QualifiedName OPTION_TOOL_HASHCODE  = new QualifiedName(VDT.ID_VDT, "OPTION_TOOL_HASHCODE");
    public static final QualifiedName OPTION_TOOL_TIMESTAMP  = new QualifiedName(VDT.ID_VDT, "OPTION_TOOL_TIMESTAMP");
    
	private boolean shiftPressed=false;
	private DesignFlowView designFlowView;
	private boolean stopOn; // Stop button is pressed
	private boolean saveOn; // save button is on
	
	
	public boolean okToRun(){
		if (isAnyToolRunnig()){
			MessageUI.error("Some tool(s) are running, can not start another one. Press 'stop' button while holding"+
		" 'Shift' key if it is an error. This is a debug feature - the tools will not be stopped (just marked as if stopped)");
			return false;
		}
// Do other things: turn off SAVE, STOP, ...		
		setStop(false);
		setSave(false);
		designFlowView.setToggleSaveTools(false);
		designFlowView.setToggleStopTools(false);
		return true;
	}
	
	public void setShiftPressed(boolean pressed){
		shiftPressed=pressed;
		System.out.println("setShiftPressed("+shiftPressed+")");
	}
	public boolean isShiftPressed(){
		return shiftPressed;
	}
	
	public ToolSequence(DesignFlowView designFlowView){
		this.designFlowView=designFlowView;
	}
	public void setStop(boolean pressed){
		this.stopOn=pressed;
		if (pressed && shiftPressed) {
			System.out.println("Marking all running tools as if they are stopped");
			stopAllRunnig();
		}
	}
	public void setSave(boolean pressed){
		this.saveOn=pressed;
	}
	public boolean isStop(){
		return stopOn;
	}
	public boolean isSave(){
		return saveOn;
	}
	public boolean isSaveEnabled(){
		return !getToolsToSave().isEmpty();
	}
	
	public List<Tool> getOpenSessions(){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		List<Tool> sessionList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			System.out.println("Looking for open console: "+tool.getName()+
					" state="+tool.getState());
			if (
					(tool.getState()==TOOL_STATE.KEPT_OPEN) &&
					(tool.getOpenTool()!=null) &&
					(tool.getOpenState()!=null)){
				// See if state file is not saved
				Tool ranTool=tool.getOpenTool();
				String stateDirString=ranTool.getStateDir();
				if (stateDirString==null){
					System.out.println("getOpenSessions(): stateDirString==null");
					continue;
				}
				String linkString=ranTool.getStateLink();
				if (linkString==null){
					System.out.println("getOpenSessions(): linkString==null");
					continue;
				}
				IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
				IFile link=   stateDir.getFile(linkString); // null
				IFile target= stateDir.getFile(tool.getOpenState());
				System.out.println("****link.getRawLocation()=  "+link.getRawLocation().toString());
				System.out.println("****target.getLocation()=  "+target.getLocation().toString());
				sessionList.add(tool);
			}
		}
		return sessionList;
	}

	// TODO: "save" - finds the first unsaved state and launches save.
	// If none available - turns off save and updates view.
	// when tool is finished - check "save" button, and if "on" - repeat again
	// save is turned off by any tool launch and finish with error
	// Decide how to find out any tool is running (search all and ) and overrun block - block will just set all tools to not-running
	
	
	/**
	 * Create list of tools (just one with a single open session) that have unsaved state 
	 * @return never null, may be empty list
	 */
	public List<Tool> getToolsToSave(){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		List<Tool> saveToolsList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
//			System.out.println("Looking for open console: "+tool.getName()+" state="+tool.getState());
			if (
					(tool.getState()==TOOL_STATE.KEPT_OPEN) &&
					(tool.getOpenTool()!=null) &&
					(tool.getOpenState()!=null)){
				// See if state file is not saved
				Tool ranTool=tool.getOpenTool();
				String stateDirString=ranTool.getStateDir();
				if (stateDirString==null){
					System.out.println("getOpenSessions(): stateDirString==null");
					continue;
				}
				String linkString=ranTool.getStateLink();
				if (linkString==null){
					System.out.println("getOpenSessions(): linkString==null");
					continue;
				}
				IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
				IFile link=   stateDir.getFile(linkString); // null
				IFile target= stateDir.getFile(tool.getOpenState());
				System.out.println("****link.getRawLocation()=  "+link.getRawLocation().toString());
				System.out.println("****target.getLocation()=  "+target.getLocation().toString());
				if (!link.getRawLocation().toString().equals(target.getLocation().toString())){
					saveToolsList.add(ranTool);
					System.out.println("****Adding=  "+ranTool.getName());
				}
			}
		}
		return saveToolsList;
	}
	
	//TODO: make possible to run multiple tools async if they do not share common session
	public boolean isAnyToolRunnig(){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		List<Tool> saveToolsList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (tool.isRunning()) return true;
		}
		return false;
	}

// call when double-click on stop?	
	public void stopAllRunnig(){ // does not actually stop - just marks as if stopped for debug purposes
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		List<Tool> saveToolsList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (tool.isRunning()) {
				tool.setState(TOOL_STATE.FAILURE);
				tool.setMode(TOOL_MODE.STOP);
			}
		}
	}

	
	public void toolFinished(Tool tool){
		if (tool.isRunning()) {
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
				System.out.println("\nTool "+tool.getName()+" is still running");
			return;
		}
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
			System.out.println("\nTool "+tool.getName()+" FINISHED , state="+tool.getState()+", mode="+tool.getLastMode());
		if (tool.getState()==TOOL_STATE.SUCCESS){
			// Update state of the session(s) - should be done after run or restore
			if (
					(tool.getLastMode()==TOOL_MODE.RUN) ||
					(tool.getLastMode()==TOOL_MODE.RESTORE)){
				boolean sessionUpdated=updateSessionTools(tool); // Update state 
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
					System.out.println("updateSessionTools("+tool.getName()+"tool)-> "+sessionUpdated);
			}
			if (tool.getLastMode()==TOOL_MODE.RESTORE){
				restoreToolProperties(tool);// set last run hashcode and timestamp for the tool just restored 
			}
			// Check for stop here
			if (
					(tool.getLastMode()==TOOL_MODE.RUN) || // not needed, but won't harm. Update will be after save
					(tool.getLastMode()==TOOL_MODE.SAVE)){
				updateLinkLatest(tool); // Do not update link if the session was just restored. Or should it be updated
//Currently hashcode/timestamp are set by the restore tool (at least when (by mistake) it was trying to save - it used it's own				
			}			

			getToolsToSave();
			if (tryAutoSave(tool)) return;  // started autoSave that will trigger "toolFinished" again
		} else if (tool.getState()==TOOL_STATE.KEPT_OPEN){
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
				System.out.println("\nTool "+tool.getName()+" kept open , state="+tool.getState()+", mode="+tool.getLastMode());

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
	public boolean restoreToolProperties(Tool tool){
		if (tool.getLastMode()!=TOOL_MODE.RESTORE) return false;
		if (tool.getRestoreMaster()!=null) tool=tool.getRestoreMaster();
		else {
			System.out.println("Tool "+tool.getName()+" does not have restoreMaster, but it came with getLastMode()!=TOOL_MODE.RESTORE");
		}
		String stateDirString=tool.getStateDir();
//		String linkString=tool.getStateLink();
		String targetString=tool.getStateFile(); // With timestamp or specifically set through selection
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
		// Create file for target and see if it actually exists
		IFile target=  stateDir.getFile(targetString);
		if (!target.exists()){
			System.out.println("BUG: file that was just restored does not exist: "+
					target.getLocation().toOSString());
			return false;
		}
		String timestamp=null;
		String sHashCode=null;
		int hashCode=0;
		try {
			timestamp=target.getPersistentProperty(OPTION_TOOL_TIMESTAMP);
			System.out.println("Got timestamp="+timestamp+" in "+target.getLocation().toOSString());
		} catch (CoreException e) {
			System.out.println("No timestamp in "+target.getLocation().toOSString());
		}
		try {
			sHashCode=target.getPersistentProperty(OPTION_TOOL_HASHCODE);
			hashCode=Integer.parseInt(sHashCode);
			System.out.println("Got hashcode="+hashCode+" ("+sHashCode+") in "+target.getLocation().toOSString());
		} catch (CoreException e) {
			System.out.println("No hashcode in "+target.getLocation().toOSString());
		}
		if (timestamp!=null) {
			tool.setTimeStamp(timestamp);
			System.out.println("Restored timestamp="+timestamp+" for tool"+tool.getName());
		}
		if (hashCode!=0) {
			tool.setLastRunHash(hashCode);
			System.out.println("Restored lastRunHashCode="+hashCode+" for tool"+tool.getName());
		}
		return (timestamp!=null) && (hashCode!=0);
	}
	
	
	
	private boolean tryAutoSave(Tool tool){
		if  ((tool.getSave()!=null) && // save tool exists
				tool.getAutoSave() &&  // autosave enabled
				(designFlowView!=null) && // not needed anymore?
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
		} else if (tool.getSaveMaster()!=null){ // that was save?
    		tool=tool.getSaveMaster();
    		tool.setMode(TOOL_MODE.STOP);
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
				System.out.println("Finished autosave tool "+tool.getName()+" for "+tool.getName());
			}
		}
		return false;
	}
	
	// Result file may be skipped, in that case link should not be updated, but the console state should be
	/**
	 * Update "latest" link to the last generated
	 * Executed after =TOOL_MODE.SAVE
	 * @param tool tool that just ran
	 */
	private void updateLinkLatest(Tool tool){
		if (tool.getLastMode()==TOOL_MODE.PLAYBACK) return; // do nothing here
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
			System.out.println("updateLinkLatest("+tool.getName()+"), getLastMode()= "+tool.getLastMode());
		}
		String stateDirString=tool.getStateDir();
		String linkString=tool.getStateLink();
		String targetString=tool.getStateFile(); // With timestamp or specifically set through selection
		
		System.out.println("Tool:"+tool.getName()+
				" stateDirString="+stateDirString+
				" linkString ="+linkString+
				" targetString="+targetString);
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
			System.out.println("Will not link "+linkString+" to nonexistent resource:"+targetString+
					" in "+stateDirString+": "+target.getLocation());
			return; // No link created as there was no snapshot, but the console state is valid.
		}
		if (linkString==null){
			System.out.println("No link name available for "+tool.getName());
			return;
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
		if (tool.getSaveMaster()!=null){
			tool=tool.getSaveMaster();
		}
		tool.getTimeStamp();
//		tool.getLastRunHash()+"";
		String sHash= new Integer(tool.getLastRunHash()).toString();
		System.out.println("tool.getLastRunHash()="+tool.getLastRunHash()+", sHash="+sHash);
		try {
			target.setPersistentProperty(OPTION_TOOL_TIMESTAMP, tool.getTimeStamp());
			System.out.println("setPersistentProperty("+OPTION_TOOL_TIMESTAMP+","+tool.getTimeStamp()+
					" on "+target.getLocation().toOSString());
		} catch (CoreException e) {
			System.out.println("Failed to setPersistentProperty("+OPTION_TOOL_TIMESTAMP+","+tool.getTimeStamp()+
					" on "+target.getLocation().toOSString());
		}
		try {
			target.setPersistentProperty(OPTION_TOOL_HASHCODE, sHash);
			System.out.println("setPersistentProperty("+OPTION_TOOL_HASHCODE+","+sHash+
					" on "+target.getLocation().toOSString());			
		} catch (CoreException e) {
			System.out.println("Failed to setPersistentProperty("+OPTION_TOOL_HASHCODE+","+sHash+
					" on "+target.getLocation().toOSString());
		}
	}

	/**
	 * Update open session(s) state
	 * @param tool - tool just finished 
	 * @return true if update happened
	 */
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
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
					System.out.println("Set openState of "+consoleTool.getName()+" to "+targetString);
				if (tool.getRestoreMaster()!=null) { // after restore save master tool
					consoleTool.setOpenTool(tool.getRestoreMaster());
					if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
						System.out.println("Set setOpenTool of "+consoleTool.getName()+" to "+tool.getRestoreMaster().getName());
				} else {
					consoleTool.setOpenTool(tool);
					if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
						System.out.println("Set setOpenTool of "+consoleTool.getName()+" to "+tool.getName());
				}
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

