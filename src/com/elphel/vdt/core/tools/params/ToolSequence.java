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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
import com.elphel.vdt.VerilogUtils;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ToolSequence {
    public static final QualifiedName OPTION_TOOL_HASHCODE  =  new QualifiedName(VDT.ID_VDT, "OPTION_TOOL_HASHCODE");
    public static final QualifiedName OPTION_TOOL_TIMESTAMP  = new QualifiedName(VDT.ID_VDT, "OPTION_TOOL_TIMESTAMP");

    // same state may be after different tools
    public static final QualifiedName OPTION_TOOL_NAME  =           new QualifiedName(VDT.ID_VDT, "OPTION_TOOL_NAME");
    public static final String TOOL_FILEDEPSTAMP  =   "OPTION_TOOL_FILEDEPSTAMP_";
    public static final String TOOL_STATEDEPSTAMP  =  "OPTION_TOOL_STATEDEPSTAMP_";
    
    private static final String TAG_CURRENTSTATE_TOOLNAME =  ".currentState.toolName.";
    private static final String TAG_CURRENTSTATE_STATEFILE =  ".currentState.stateFile.";
    private static final String TAG_CURRENTSTATE_TOOLSTAMP =  ".currentState.toolStamp.";
    
   
	private boolean shiftPressed=false;
	private DesignFlowView designFlowView;
	private boolean stopOn; // Stop button is pressed
	private boolean saveOn; // save button is on
	private Map<String,Tool> stateProvides;
	private Map<String,ToolStateStamp> currentStates;
	private IMemento unfinishedMemento=null;
	private String menuName=null;
//	private IProgressMonitor monitor;
	
	public ToolSequence(DesignFlowView designFlowView){
		this.designFlowView=designFlowView;
		this.currentStates=new Hashtable<String,ToolStateStamp>();
	}

	public void setUnfinishedBoot(IMemento memento){
		unfinishedMemento=memento;
		if (memento!=null){
			// Does not seem to work:
			IActionBars bars = designFlowView.getViewSite().getActionBars();
			bars.getStatusLineManager().setMessage("Waiting for VEditor database to be built...");
			menuName=designFlowView.getPartName();
			System.out.println("Menu name:"+menuName);
			designFlowView.changeMenuTitle("Waiting for VEditor database...");
		}
	}
	public void finalizeBootAfterVEditor(){
		if (unfinishedMemento!=null) {
			// Does not seem to work:
			IActionBars bars = designFlowView.getViewSite().getActionBars();
			bars.getStatusLineManager().setMessage("");
//			designFlowView.changeMenuTitle(menuName);
			designFlowView.finalizeAfterVEditorDB(unfinishedMemento);
		}
	}

	
	
	public void toolFinished(Tool tool){
		doToolFinished(tool);
		if (designFlowView!=null){
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					designFlowView.updateLaunchAction(); // Run from Display thread to prevent "invalid thread access" when called from Runner
				}
			});
		}
	}	
	public void doToolFinished(Tool tool){
		if (tool.isRunning()) {
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
				System.out.println("\nTool "+tool.getName()+" is still running");
			return;
		}
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
			System.out.println("\nTool "+tool.getName()+" FINISHED , state="+tool.getState()+", mode="+tool.getLastMode());
		if (tool.getState()==TOOL_STATE.SUCCESS){
			// Update state of the session(s) - should be done after run or restore
			if ((tool.getLastMode()==TOOL_MODE.RUN) ||	(tool.getLastMode()==TOOL_MODE.RESTORE)){
				boolean sessionUpdated=updateSessionTools(tool); // Update state 
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
					System.out.println("updateSessionTools("+tool.getName()+"tool)-> "+sessionUpdated);
			}
			if (tool.getLastMode()==TOOL_MODE.RESTORE){
				restoreToolProperties(tool);// set last run hashcode and timestamp for the tool just restored
				if (tool.getRestoreMaster()!=null) tool.setPinned(true);
				else {
					System.out.println("Pribably a bug - tool.getRestoreMaster()==null for "+tool.getName()+", while state is "+tool.getState());
					tool.setPinned(true); // restored state should be pinned?
				}
			}
			// add/update current state produced by the finished tool
			putCurrentState(tool);
			// Set tool timestamps for states and source files
			if (tool.getLastMode()==TOOL_MODE.RUN) {
				setDependState(tool);
			}
			setToolsDirtyFlag(false); // no need to recalculate all parameters here 
			
			// Check for stop here
			if ((tool.getLastMode()==TOOL_MODE.RUN) || (tool.getLastMode()==TOOL_MODE.SAVE)){
				updateLinkLatest(tool); // Do not update link if the session was just restored. Or should it be updated
			}			
			getToolsToSave(); // find if there are any sessions in unsaved state - returns list (not yet processed)
			if (tryAutoSave(tool)) return;  // started autoSave that will trigger "toolFinished" again
		} else if (tool.getState()==TOOL_STATE.KEPT_OPEN){ // Got here after launching a session
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
				System.out.println("\nTool "+tool.getName()+" kept open , state="+tool.getState()+", mode="+tool.getLastMode());
		} else { // Process failures here

		}
	}

	/**
	 * Find which tool to run to satisfy dependency of the specified tool (may be recursive - make sure no loops)
	 * @param tool tool to satisfy dependency for
	 * @return tool to run or null - failed to find any
	 */
	private Tool findToolToLaunch(Tool tool, boolean launchSessions){ 
		List<Tool> consoleTools= getUsedConsoles(tool);
		if (launchSessions) for (Tool consoleTool:consoleTools){ // or maybe do it after other dependencies?
			if (consoleTool.getState()!=TOOL_STATE.KEPT_OPEN) {
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
					System.out.println("Need to launch tool"+consoleTool.getName()+" to satisfy dependencies of tool "+tool.getName());
				}
				return consoleTool;
			}
		}
		// All session tools are running
		return null;

	}
	
	/**
	 * Setup what tools can provide needed states
	 */
	public void setStateProvides(){
		stateProvides=new ConcurrentHashMap<String,Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			System.out.println("Looking for all states defined, tool= "+tool.getName());
			if (!tool.isDisabled()){
				String state=tool.getStateLink(); // some tools (like reports) do not change states 
				if (state!=null) stateProvides.put(state,tool);
			}
		}
		// Verify that each dependent state has a provider
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
//			System.out.println("Looking for all states defined, tool= "+tool.getName());
			if (!tool.isDisabled()){
				List<String> dependStates=tool.getDependStates();
				if ((dependStates!=null) && (dependStates.size()>0)){
					for (String state:dependStates){
						if (!stateProvides.containsKey(state)){
							MessageUI.error("No tool provide output state '"+state+"' needed to satisfy dependency of the tool "+tool.getName());
							System.out.println("No tool provide output state '"+state+"' needed to satisfy dependency of the tool "+tool.getName());
						}
					}
				}
			}
		}
		
		//    List<String> list = new ArrayList<String>(hashset);
		System.out.println("Got "+stateProvides.keySet().size()+" different states, number of mappings="+stateProvides.keySet().size());
		// For each state - find latest tool that made it
		
		
	}
	
	public void launchToolSequence(
			Tool tool,
			TOOL_MODE mode,
			int choice,
			String fullPath,
			String ignoreFilter) throws CoreException {
		if (!okToRun()) return;
		
		setStateProvides(); // just testing
		
		//    		tool.setDesignFlowView(designFlowView);
		tool.setDesignFlowView(designFlowView); // maybe will not be needed with ToolSequencing class
		tool.setMode(mode) ; //TOOL_MODE.RUN);
		tool.toolFinished();
		tool.setChoice(0);
		SelectedResourceManager.getDefault().updateActionChoice(fullPath, choice, ignoreFilter); // Andrey
		SelectedResourceManager.getDefault().setBuildStamp(); // Andrey
		// apply designFlowView to the tool itself
		LaunchCore.launch( tool,
				SelectedResourceManager.getDefault().getSelectedProject(),
				fullPath,
				null); // run, not playback 
	} // launchTool()

	// TODO: restore "working copy" functionality here to be able to play back logs while tools are running
	// It just should not touch the actual tool state
	public void playLogs(// does immediately, no need of console (wait for console) - need the tool itself. So
			Tool tool,
			String fullPath,
			String logBuildStamp) throws CoreException {
		if (logBuildStamp==null) return; // cancelled selection
		if (!okToRun()) return;
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
			System.out.println("logBuildStamp="+logBuildStamp);
		}
		tool.setDesignFlowView(designFlowView);
		//            tool.setRunning(true);
		tool.setMode(TOOL_MODE.PLAYBACK);
		tool.toolFinished();
		tool.setChoice(0);
		SelectedResourceManager.getDefault().updateActionChoice(fullPath, 0, null); // Andrey
		SelectedResourceManager.getDefault().setBuildStamp(); // OK - even for log? Or use old/selected one?
		// apply designFlowView to the tool itself
		LaunchCore.launch(tool,
				SelectedResourceManager.getDefault().getSelectedProject(),
				fullPath,
				logBuildStamp); 
	} // launchTool()

    
    
    
	
	
	public boolean okToRun(){
		if (isAnyToolRunnigOrWaiting()){
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
	public boolean isAnyToolRunnigOrWaiting(){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		List<Tool> saveToolsList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (tool.isRunning() || tool.isWaiting()) return true;
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
		// restore dependencies
		// 1. See if it was saved by the same tool
		String stateToolName=null;
		try {
			stateToolName=target.getPersistentProperty(OPTION_TOOL_NAME);
			System.out.println("Got stateToolName="+stateToolName+" in "+target.getLocation().toOSString());
		} catch (CoreException e) {
			System.out.println("No stateToolName in "+target.getLocation().toOSString());
			return false;
		}
		if ((stateToolName==null) || !tool.getName().equals(stateToolName)){
			System.out.println("State file "+target.getLocation().toOSString()+" was saved for tool "+stateToolName+
					", while restoring tool is "+tool.getName()+" - ivalidating all dependencies timestamps.");
			tool.clearDependStamps();
			return false;
		}
		// 2. Read timestmaps for state file(s) and source files
		tool.clearDependStamps(); // clear old dependency timestamps
		Map<QualifiedName, String> properties=null;
		try {
			properties = target.getPersistentProperties();
		} catch (CoreException e) {
			System.out.println("Failed to get persisten properties from "+target.getLocation().toOSString());
			return false;
		}

		for (QualifiedName qName: properties.keySet()){
			if (qName.getLocalName().startsWith(TOOL_FILEDEPSTAMP)){
    			String value=properties.get(qName);
    			tool.setFileTimeStamp(qName.getLocalName().substring(TOOL_FILEDEPSTAMP.length()), value);
			} else if (qName.getLocalName().startsWith(TOOL_STATEDEPSTAMP)){
    			String value=properties.get(qName);
    			tool.setStateTimeStamp(qName.getLocalName().substring(TOOL_STATEDEPSTAMP.length()), value);
			} 
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
		if ((stateDirString==null) || (targetString==null) || (linkString==null)) return;
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
		// Create file for target and see if it actually exists
		IFile target=  stateDir.getFile(targetString); // null pointer after error in copying files
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
		// Save dependencies:
		// Set toll name (same state may be result of running different tool (not yet used)
		try {
			target.setPersistentProperty(OPTION_TOOL_NAME, tool.getName());
			System.out.println("setPersistentProperty("+OPTION_TOOL_NAME+","+tool.getName()+
					" on "+target.getLocation().toOSString());			
		} catch (CoreException e) {
			System.out.println("Failed to setPersistentProperty("+OPTION_TOOL_NAME+","+tool.getName()+
					" on "+target.getLocation().toOSString());
		}
		// Save dependencies on states (snapshot file names)
        for(String state:tool.getDependStatesTimestamps().keySet()){
        	String stamp=tool.getStateTimeStamp(state);
        	QualifiedName qn=new QualifiedName(VDT.ID_VDT,TOOL_STATEDEPSTAMP+state);
    		try {
    			target.setPersistentProperty(qn, stamp);
    			System.out.println("setPersistentProperty("+qn+","+tool.getName()+
    					" on "+target.getLocation().toOSString());			
    		} catch (CoreException e) {
    			System.out.println("Failed to setPersistentProperty("+qn+","+tool.getName()+
    					" on "+target.getLocation().toOSString());
    		}
        }
		// Save dependencies on files (source file names)
        for(String depfile:tool.getDependFilesTimestamps().keySet()){
        	String stamp=tool.getFileTimeStamp(depfile);
        	QualifiedName qn=new QualifiedName(VDT.ID_VDT,TOOL_FILEDEPSTAMP+depfile);
    		try {
    			target.setPersistentProperty(qn, stamp);
    			System.out.println("setPersistentProperty("+qn+","+stamp+
    					" on "+target.getLocation().toOSString());			
    		} catch (CoreException e) {
    			System.out.println("Failed to setPersistentProperty("+qn+","+stamp+
    					" on "+target.getLocation().toOSString());
    		}
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
		List<Tool> sessionList=getUsedConsoles(tool); // never null
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
	List<Tool> getUsedConsoles(Tool tool){
		List<Tool> sessionList=new ArrayList<Tool>();
		List<String> consoleNames=tool.getSessionConsoles(); // which consoles are used by this tool
		if (consoleNames!=null){
			for(Iterator<String> iter = consoleNames.iterator(); iter.hasNext();) {
				String consoleName=iter.next();
				if (consoleName!=null) {
					Tool consoleTool=ToolsCore.getContextManager().findTool(consoleName);
					if (tool!=null) sessionList.add(consoleTool);

				}
			}
		}
		return sessionList;
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
	
	public class ToolStateStamp{
    	private String toolName;
    	private String toolStateFile; // 
    	private String toolStamp; // after restore differs from stamp in stateFile
    	public ToolStateStamp(
    			String toolName,
    			String stateFile,
    			String toolStamp
    			){
    		this.toolName=toolName;
    		this.toolStateFile=stateFile;
    		this.toolStamp=toolStamp;
    	}
    	public ToolStateStamp(Tool tool){
    		this.toolName=  tool.getName();
    		this.toolStateFile= tool.getStateFile();
    		this.toolStamp= tool.getRestoreTimeStamp();
    	}
    	public String getToolName(){
    		return toolName;
    	}
    	public String getToolStamp(){
    		return toolStamp;
    	}
    	public String getToolStateFile(){
    		return toolStateFile;
    	}
    	public boolean after(ToolStateStamp after, ToolStateStamp before){
    		return SelectedResourceManager.afterStamp(after.getToolStamp(), before.getToolStamp());
    	}
    }
	
    public void saveCurrentStates(IMemento memento) {
    	for (String state:currentStates.keySet()){
    		ToolStateStamp tss=currentStates.get(state);
            memento.putString(state+TAG_CURRENTSTATE_TOOLNAME, tss.getToolName());
            memento.putString(state+TAG_CURRENTSTATE_STATEFILE, tss.getToolStateFile());
            memento.putString(state+TAG_CURRENTSTATE_TOOLSTAMP, tss.getToolStamp());
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
        		System.out.println("Saving state  "+state+
        				", toolName="+tss.getToolName()+
        				", toolStateFile="+tss.getToolStateFile()+
        				", toolStamp="+tss.getToolStamp());
        	}

    	}
    }

    /**
     * Restore states (snaphot files status) from persistent storage
     * Should be called after tools are restored
     * @param memento
     */
	public void restoreCurrentStates(IMemento memento) {
    	currentStates.clear();
    	String[] mementoKeys=memento.getAttributeKeys();
    	Map<String,String> mementoKeysMap=new Hashtable<String,String>();
    	for (String key:mementoKeys){
    		if (
    				key.contains(TAG_CURRENTSTATE_TOOLNAME)||
    				key.contains(TAG_CURRENTSTATE_STATEFILE)||
    				key.contains(TAG_CURRENTSTATE_TOOLSTAMP)
    				) mementoKeysMap.put(key,memento.getString(key));

    	}
   	    setStateProvides(); // Can be called just once - during initialization?
   	    for (String state:stateProvides.keySet()){
   	    	if ( mementoKeysMap.containsKey(state+TAG_CURRENTSTATE_TOOLNAME)) {
   	    		currentStates.put(state,new ToolStateStamp(
   	    	    		memento.getString(state+TAG_CURRENTSTATE_TOOLNAME),
   	    	    		memento.getString(state+TAG_CURRENTSTATE_STATEFILE),
   	    	    		memento.getString(state+TAG_CURRENTSTATE_TOOLSTAMP)
   	    				));
   	        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
   	        		System.out.println("Restoring state  "+state+
   	        				", toolName="+memento.getString(state+TAG_CURRENTSTATE_TOOLNAME)+
   	        				", toolStateFile="+memento.getString(state+TAG_CURRENTSTATE_STATEFILE)+
   	        				", toolStamp="+memento.getString(state+TAG_CURRENTSTATE_TOOLSTAMP));
   	        	}
   	    	}
   	    }
   	    // Set all tool dirty flags according to restored states and tools dendencies
   	    //        updateContextOptions(project); // Fill in parameters - it parses here too - at least some parameters? (not in menu mode)
   	    // setToolsDirtyFlag(true) initiates Verilog database rebuild, let's trigger it intentionally
   	    //VerilogUtils.getTopModuleNames((IFile)resource);
   	    setToolsDirtyFlag(true); // recalculate each successful tool's parameters
	}
	
    public void putCurrentState(Tool tool){
		String linkString=tool.getStateLink(); // name of the state file w/o timestamp
		if (linkString!=null) currentStates.put(linkString, new ToolStateStamp(tool));
    }


    /**
     * Scan all succeeded tools and set "dirty" flag if their dependencies do not match stored ones 
     * 
     */
    public void setToolsDirtyFlag(boolean update){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (tool.getState()==TOOL_STATE.SUCCESS){
		        if (update){
		        	// tool.updateContextOptions(project) recalculates parameters, but not the hashcodes
		        	tool.updateContextOptions(project); // Fill in parameters - it parses here too - at least some parameters? (not in menu mode)
		        	try {
						tool.buildParams(true); // dryRun
					} catch (ToolException e) {
						System.out.println("setToolsDirtyFlag(): failed to buildParams() for tool "+tool.getName());
					}
		        }
				tool.setDirty(!matchDependState(tool));
			}
		}
    }
    
    /**
     * Set state files (full names with timestamps) and source files timestamps for the tool
     * so they can be compared later to determine if the tool state is current or "dirty" (will
     * use parameters-defined hashcodes too
     * @param tool Reference to a tool to process
     */
    public void setDependState(Tool tool){
    	tool.clearDependStamps(); // is it needed?
    	Map <String,String> depStates=makeDependStates(tool);
    	for (String state:depStates.keySet()){
			tool.setStateTimeStamp(state,depStates.get(state)); // name of the state file including timestamp
    	}
    	Map <String,String> depFiles=makeDependFiles(tool);
    	for (String file:depFiles.keySet()){
			tool.setFileTimeStamp(file,depFiles.get(file)); 
    	}
    }
    
    /**
     * Compare current timestamps of the source files and state(s) filenames (that include timestamps)
     * against ones stored for the tool (when it ran or was restored) 
     * @param tool tool to process
     * @return true if all timestamps matched, false otherwise
     */
    public boolean matchDependState(Tool tool){
    	Map <String,String> depStates=makeDependStates(tool);
    	Map <String,String> depFiles=makeDependFiles(tool);
        Map <String,String> storedDepStates = tool.getDependStatesTimestamps();
        Map <String,String> storedDepFiles =  tool.getDependFilesTimestamps();
        if (depStates.size()!=storedDepStates.size()) {
        	System.out.println("matchDependState("+tool.getName()+") :"+
            " depStates.size()!=storedDepStates.size() - "+depStates.size()+"!="+storedDepStates.size());
        	return false;  
        }
        if (depFiles.size()!=storedDepFiles.size()) {
        	System.out.println("matchDependState("+tool.getName()+") :"+
                    " depFiles.size()!=storedDepFiles.size() - "+depFiles.size()+"!="+storedDepFiles.size());

        	return false; 
        }
        for (String state:depStates.keySet()){
        	if (!storedDepStates.containsKey(state) || !depStates.get(state).equals(storedDepStates.get(state))){
            	System.out.println("matchDependState("+tool.getName()+") :"+
            			state+ ": "+depStates.get(state)+" <-> "+storedDepStates.get(state));
        		return false;
        	}
        }
        for (String file:depFiles.keySet()){
        	if (!storedDepFiles.containsKey(file) || !depFiles.get(file).equals(storedDepFiles.get(file))){
            	System.out.println("matchDependState("+tool.getName()+") :"+
            			file+ ": "+depFiles.get(file)+" <-> "+storedDepFiles.get(file));
        		
        		return false;
        	}
        }
    	System.out.println("matchDependState("+tool.getName()+") : full match!");
    	return true;
    }
    
    
    private  Map <String,String> makeDependStates(Tool tool){
    	Map <String,String> depStates=new Hashtable<String,String>();    	
    	List<String> dependStates=tool.getDependStates();
    	if (dependStates!=null) for (String state: dependStates){
    		if (currentStates.containsKey(state)){
    			ToolStateStamp tss=currentStates.get(state);
    			depStates.put(state,tss.getToolStateFile()); // name of the state file including timestamp
    		} else {
    			System.out.println("Seems a BUG: no information for state "+state+" on which tool "+tool.getName()+" depends");
    		}
    	}
    	return depStates;
    }
    private  Map <String,String> makeDependFiles(Tool tool){
    	Map <String,String> depFiles=new Hashtable<String,String>();    	
    	List<String> dependFileNames=tool.getDependFiles();
    	if (dependFileNames!=null) {
    		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
    		for (String depFile: dependFileNames){
    			if (depFile.length()==0){
    				System.out.println("makeDependFiles(): depFile is empty");
    				continue;
    			}
    			IFile sourceFile=project.getFile(depFile); //Path must include project and resource name: /npmtest
    			if (sourceFile.exists()) {
    				depFiles.put(depFile, String.format("%d",sourceFile.getModificationStamp()));
    			} else {
    				System.out.println("Seems a BUG:  source file "+sourceFile.getLocation()+" on which tool "+
    						tool.getName()+" depends does not exist");
    				depFiles.put(depFile, ""); // empty stamp for non-existent files?
    			}
    		}
    	}
    	return depFiles;
    }
}

