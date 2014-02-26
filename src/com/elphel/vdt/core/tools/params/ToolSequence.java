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
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.core.launching.ToolLogFile;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.params.Tool.TOOL_MODE;
import com.elphel.vdt.core.tools.params.Tool.TOOL_STATE;
import com.elphel.vdt.core.tools.params.Tool.ToolWaitingArguments;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.options.FilteredFileSelector;
import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.ui.views.DesignFlowView;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
//    private static final int MAX_TOOLS_TO_RUN=100;
   
	private boolean shiftPressed=false;
	private DesignFlowView designFlowView;
	private boolean stopOn; // Stop button is pressed
	private boolean saveOn; // save button is on
	private Map<String,Set<Tool>> stateProviders;
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
		if ((tool.getState()==TOOL_STATE.SUCCESS) || (tool.getState()==TOOL_STATE.KEPT_OPEN)){
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
					System.out.println("Probably a bug - tool.getRestoreMaster()==null for "+tool.getName()+", while state is "+tool.getState());
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
//			if (SelectedResourceManager.getDefault().isToolsLinked()) {
//				System.out.println("===Propagating 'dirty' flags to dependent non-pinned tools");
//				propagateDirty();
//			}
			if (isStop()){
				releaseStop();
				System.out.println("Stop was ctivated");
				return; // do nothing more
			}
			
			// Check for stop here
			if ((tool.getLastMode()==TOOL_MODE.RUN) || (tool.getLastMode()==TOOL_MODE.SAVE)){
				updateLinkLatest(tool); // Do not update link if the session was just restored. Or should it be updated
			}			
			getToolsToSave(); // find if there are any sessions in unsaved state - returns list (not yet processed)
			if (tryAutoSave(tool)) return;  // started autoSave that will trigger "toolFinished" again
			if (continueRunningTools()) return; // started next needed tool
			// will get here if no more tools to run or failed to find one
			Tool waitingTool=findWaitingTool();
			if (waitingTool==null){
				System.out.println("No tool is waiting to run, all done.");
				return;
			} else {
				waitingTool.setState(TOOL_STATE.FAILURE); // should state be set to FAILURE?
				waitingTool.setMode(TOOL_MODE.STOP);
				String msg="Failed to find a tool to run for "+waitingTool.getName();
				System.out.println(msg);
				MessageUI.error(msg);
			}
			
//		} else if (tool.getState()==TOOL_STATE.KEPT_OPEN){ // Got here after launching a session
//			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE))
//				System.out.println("\nTool "+tool.getName()+" kept open , state="+tool.getState()+", mode="+tool.getLastMode());
		} else { // Process failures here
			//Turn off any Waiting tool
			Tool waitingTool=findWaitingTool();
			if (waitingTool!=null){
				waitingTool.setState(TOOL_STATE.FAILURE); // should state be set to FAILURE?
				waitingTool.setMode(TOOL_MODE.STOP);
			}
		}
	}
	
	private boolean continueRunningTools(){
		if (SelectedResourceManager.getDefault().isToolsLinked()){
			Tool reportTool=findReportTool();
			if (reportTool!=null){
				// Launch report tool that can be ran from the current state
				try {
					launchNextTool(reportTool);
				} catch (CoreException e) {
					String msg="Failed to launch reportTool "+reportTool.getName()+" e="+e;
					System.out.println(msg);
					MessageUI.error(msg);
					return false;
				}
				return true; // launched report tool
			}
			Tool waitingTool=findWaitingTool();
			if (waitingTool!=null){
				// prevent recursion - limit number of steps to the total number of tools (each should run once at most ?) 
				Tool nextTool=findToolToLaunch(waitingTool,ToolsCore.getConfig().getContextManager().getToolList().size());
				if (nextTool!=null){
					try {
						launchNextTool(nextTool);
					} catch (CoreException e) {
						String msg="Failed to launch next tool "+nextTool.getName()+" e="+e;
						System.out.println(msg);
						MessageUI.error(msg);
						return false;
					}
					return true; // launched next tool
				}
				return false; // failed to find tool to launch
			}
			return false; // nothing to do - no tool was waiting
		} else {
			return false; // tools are not linked
		}
	}

	public void launchNextTool(Tool tool) throws CoreException {
//		if (!okToRun()) return;
//		setStateProvides(); // just testing
		tool.setDesignFlowView(designFlowView); // maybe will not be needed with ToolSequencing class
		if (tool.isWaiting()){
			ToolWaitingArguments twa=tool.getToolWaitingArguments();
			if (twa!=null){
				SelectedResourceManager.getDefault().updateActionChoice(
						twa.getFullPath(), //fullPath,
						twa.getChoice(), // choice,
						twa.getIgnoreFilter()); //ignoreFilter);
				tool.setMode(twa.getMode()) ; //TOOL_MODE.RUN);
				tool.setChoice(twa.getChoice());

				LaunchCore.launch( tool,
						SelectedResourceManager.getDefault().getSelectedProject(),
						twa.getFullPath(),
						null); // run, not playback 
				return;
			}
		}
		tool.setMode(TOOL_MODE.RUN) ; //TOOL_MODE.RUN);
//		tool.toolFinished();
		tool.setChoice(0);
//		SelectedResourceManager.getDefault().updateActionChoice(fullPath, choice, ignoreFilter); // Andrey
//		SelectedResourceManager.getDefault().setBuildStamp(); // Andrey
		// apply designFlowView to the tool itself
		LaunchCore.launch( tool,
				SelectedResourceManager.getDefault().getSelectedProject(),
				SelectedResourceManager.getDefault().getChosenTarget(),
				null); // run, not playback 
	} // launchTool()

	
	
	
	/**
	 * Find which tool to run to satisfy dependency of the specified tool (may be recursive - make sure no loops)
	 * @param tool tool to satisfy dependency for
	 * @param numSteps Maximal number of tools to run automatically (to prevent recursion)
	 * @return tool to run or null - failed to find any
	 */
	// TODO: support session-less states
	private Tool findToolToLaunch(Tool tool, int numSteps){
		if (numSteps<0){
			MessageUI.error("Maximal number of tool steps exceeded");
			return null;
		}
		// get list of states of the open sessions
		List<Tool> sessions=getOpenSessions();
		Map<String,String> openStates=new ConcurrentHashMap<String,String>();
		for (Tool session:sessions){
			if (session.getOpenStateName() != null) openStates.put(session.getOpenStateName(),session.getOpenStateFile());
		}
		
		// First see if all the state dependencies are met
		List<String> depStates=tool.getDependStates();
		
		// restore tools should not have any dependencies but console, check it here just in case
		if (!tool.isPinned() && (depStates!=null) && (tool.getRestoreMaster()==null)) for (String state:depStates){
			// see if the state is available, good and not dirty
			System.out.println("findToolToLaunch("+tool.getName()+" state= "+state);
			ToolStateStamp tss=currentStates.get(state);
			// Check that this state provider
			if (tss!=null){
				System.out.println("State "+state+" tss.toolName="+tss.toolName+" tss.toolStateFile="+tss.toolStateFile+" tss.toolStamp="+tss.toolStamp );
				if ((tss.getToolName()!=null) ){
					Tool depTool=ToolsCore.getContextManager().findTool(tss.getToolName());
					if ((depTool.getState()==TOOL_STATE.SUCCESS) &&
							(depTool.isPinned() || !depTool.isDirtyOrChanged())){
						System.out.println("depTool= "+depTool.getName());
						// dependency satisfied, but is this state current for some session?
						for (String osn:openStates.keySet()){
							System.out.println("-- openStateName="+osn+" state="+state); // openState - with ts, state - link
						}
						if (openStates.keySet().contains(state)){
							continue; // dependency satisfied and the state is open
						} else {
							// The state was achieved, but it is not current. Try restore tool if possible
							System.out.println("State "+state+" was previously achieved, but it is not current - will need to restore/re-run");
							// just do nothing here and fall through to tss be set to null ?
						}
					}
				}
				tss=null; // missing or dirty tool - will need to re-run
			}
			if (tss==null){
				System.out.println("State "+state+" is not available");
				if (stateProviders.containsKey(state)){
					Set<Tool> providersSet=stateProviders.get(state);
					for (Tool provider:providersSet){
						if (!provider.isDisabled()){
							System.out.println("For wanted tool "+tool.getName()+
									" using tool "+provider.getName()+
									" to get needed state "+state);
							return findToolToLaunch(provider, numSteps-1);
						}
					}
					MessageUI.error("Could not find enabled tool to provide state "+state);
					System.out.println("Could not find enabled tool to provide state "+state);
					return null;
				} else {
					MessageUI.error("No tool provide state "+state);
					System.out.println("No tool provide state "+state);
					return null;
				}
			}
		}
		// Got here if all state dependencies are met (or there are none)
		// Does the tool need a console that needs to be started?
		List<Tool> consoleTools= getUsedConsoles(tool);
		for (Tool consoleTool:consoleTools){ // or maybe do it after other dependencies?
			if (consoleTool.getState()!=TOOL_STATE.KEPT_OPEN) {
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
					System.out.println("Need to launch tool"+consoleTool.getName()+" to satisfy dependencies of tool "+tool.getName());
				}
				return consoleTool; // start the console session
			}
		}
		// everything is met to run this tool. Can we just restore it instead (only if it is not in WAIT state)
		if (tool.isWaiting()){
			System.out.println("OK to run waiting tool "+tool.getName());
			return tool;
		}
		if ((tool.getRestore()!=null) &&
			(tool.isPinned() || !tool.isDirtyOrChanged()) &&
			(tool.getState() == TOOL_STATE.SUCCESS)){
			System.out.println("Try to restore tool "+tool.getName()+" using "+tool.getRestore());
			return tool.getRestore();
		}
		System.out.println("All cleared to run "+tool.getName());
		return tool;
	}
	
	
	/**
	 * Find enabled tool that can be ran from the current state without changing it (such as various reports)
	 * and is "dirty" or never ran
	 * @return found tool to launch or null
	 */
	private Tool findReportTool(){
		// get open session(s)
		List<Tool> sessions=getOpenSessions();
		if (sessions.size()==0) return null;
		Map<String,String> openStates=new ConcurrentHashMap<String,String>();
		for (Tool session:sessions){
			if (session.getOpenStateName() != null) openStates.put(session.getOpenStateName(),session.getOpenStateFile());
		}
		if (openStates.size()==0) return null;
		for (Tool tool:ToolsCore.getConfig().getContextManager().getToolList()){
			if (
				tool.isDisabled() ||                // disabled tool (or abstract)
				(tool.getSaveMaster()!=null) ||     // Is Save tool
				(tool.getRestoreMaster()!=null) ||  // or is Restore tool
				(tool.getStateLink()!=null)) {      // or changes output state(s)
				continue;
			}
			// already good and nothing changed?
			if (
					(tool.getState()==TOOL_STATE.SUCCESS) && !tool.isDirtyOrChanged()){
				continue;
			}
			// dependencies met (and do exist)?
			List<String> depStates=tool.getDependStates();
			if ((depStates==null) || (depStates.size()==0)){
				continue; // no dependencies at all;
			}
			boolean met=true;
			for (String state:depStates){
				if (!openStates.keySet().contains(state)){
					met=false;
					break;
				}
			}
			if (!met) {
				continue; // dependencies not among open session states (TODO: what about non-session states?
			}
			return tool; // All checks passed, return this tool
		}
		return null; // Nothing found
	}
	
	/**
	 * Setup what tools can provide needed states
	 */
	public void setStateProvides(){
		stateProviders=new ConcurrentHashMap<String,Set<Tool>>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			System.out.println("Looking for all states defined, tool= "+tool.getName());
			if (!tool.isDisabled()){
				String state=tool.getStateLink(); // some tools (like reports) do not change states 
				if (state!=null) {
//					stateProviders.putIfAbsent(state,(Set<Tool>) new HashSet<Tool>());
					if (!stateProviders.containsKey(state)){
						stateProviders.put(state,(Set<Tool>) new HashSet<Tool>());
					}
					stateProviders.get(state).add(tool);
				}
			}
		}
		// Verify that each dependent state has a provider
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
//			System.out.println("Looking for all states defined, tool= "+tool.getName());
			if (!tool.isDisabled()){
				List<String> dependStates=tool.getDependStates();
				if ((dependStates!=null) && (dependStates.size()>0)){
					for (String state:dependStates){
						if (!stateProviders.containsKey(state)){
							MessageUI.error("No tool provide output state '"+state+"' needed to satisfy dependency of the tool "+tool.getName());
							System.out.println("No tool provide output state '"+state+"' needed to satisfy dependency of the tool "+tool.getName());
						}
					}
				}
			}
		}
		
		//    List<String> list = new ArrayList<String>(hashset);
		System.out.println("Got "+stateProviders.keySet().size()+" different states, number of mappings="+stateProviders.keySet().size());
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
		if (((mode==TOOL_MODE.RUN) || (mode==TOOL_MODE.RESTORE)) && SelectedResourceManager.getDefault().isToolsLinked()){
			tool.setModeWait(
	    			mode,
	        		choice,
	        		fullPath,
	        		ignoreFilter);
			if (!continueRunningTools()){
				System.out.println("Failed to initiate continueRunningTools() for tool="+tool.getName());
			}
		} else {
			LaunchCore.launch( tool,
					SelectedResourceManager.getDefault().getSelectedProject(),
					fullPath,
					null); // run, not playback
		}
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
	public void releaseStop(){
		this.stopOn=false;
		designFlowView.setToggleStopTools(false);
	}
	
	
	public void setSave(boolean pressed){
		this.saveOn=pressed;
		if (saveOn){
			List<Tool> toolsToSave=getToolsToSave(); // find if there are any sessions in unsaved state - returns list (not yet processed)
			if ((toolsToSave==null) || (toolsToSave.size()==0)){
				System.out.println("Nothing to save - how was the button pressed?");
				releaseSave();
				return;
			}
			tryAutoSave(toolsToSave.get(0)); // launch autosave and trigger toolFinished() when done
		}
	}
	public void releaseSave(){
		this.saveOn=false;
		designFlowView.setToggleSaveTools(false);
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
//			System.out.println("Looking for open console: "+tool.getName()+	" state="+tool.getState());
			if (
					(tool.getState()==TOOL_STATE.KEPT_OPEN) &&
					(tool.getOpenTool()!=null) &&
					(tool.getOpenStateFile()!=null)){
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
				IFile target= stateDir.getFile(tool.getOpenStateFile());
				System.out.println("*****link.getRawLocation()=  "+link.getRawLocation().toString());
				System.out.println("******target.getLocation()=  "+target.getLocation().toString());
				// Debug
				try {
					Map<QualifiedName,String> allPP=	target.getPersistentProperties();
					System.out.println("1. Getting persistent properties from properties from "+target.getLocation().toOSString()+" / "+tool.getOpenStateFile());
					for (QualifiedName qn:allPP.keySet()){
						System.out.println("... "+qn.toString()+": "+allPP.get(qn));
					}
				} catch (CoreException e2) {
					System.out.println("Failed to get persistent properties from "+target.getLocation().toOSString());
				} 

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
					(tool.getOpenStateFile()!=null)){
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
				IFile target= stateDir.getFile(tool.getOpenStateFile());
				System.out.println("**link.getRawLocation()=  "+link.getRawLocation().toString());
				System.out.println("***target.getLocation()=  "+target.getLocation().toString());
				if (!link.getRawLocation().toString().equals(target.getLocation().toString())){
					saveToolsList.add(ranTool);
					System.out.println("****Adding=  "+ranTool.getName());
				}
				// Debug
				try {
					Map<QualifiedName,String> allPP=	target.getPersistentProperties();
					System.out.println("2. Getting persistent properties from properties from "+target.getLocation().toOSString()+" / "+tool.getOpenStateFile());
					for (QualifiedName qn:allPP.keySet()){
						System.out.println("... "+qn.toString()+": "+allPP.get(qn));
					}
				} catch (CoreException e2) {
					System.out.println("Failed to get persistent properties from "+target.getLocation().toOSString());
				} 

			}
		}
		return saveToolsList;
	}
	
	//TODO: make possible to run multiple tools async if they do not share common session
	public boolean isAnyToolRunnigOrWaiting(){
//		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
//		List<Tool> saveToolsList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (tool.isRunning() || tool.isWaiting()) return true;
		}
		return false;
	}
	
	private Tool findWaitingTool(){
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (tool.isWaiting()) return tool;
		}
		return null;
		
	}
	

// call when double-click on stop?	
	public void stopAllRunnig(){ // does not actually stop - just marks as if stopped for debug purposes
//		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
//		List<Tool> saveToolsList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (tool.isRunning() || tool.isWaiting()) {
				tool.setState(TOOL_STATE.FAILURE);
				tool.setMode(TOOL_MODE.STOP);
			}
		}
	}

	
	public boolean restoreToolProperties(Tool tool){
		if (tool.getLastMode()!=TOOL_MODE.RESTORE) return false;
		String targetString=tool.getStateFile(); // With timestamp or specifically set through selection

		if (tool.getRestoreMaster()!=null) {
//			System.out.println("restoreToolProperties("+tool.getName()+") tool.getRestoreMaster()="+tool.getRestoreMaster().getName()+" tool.getStateFile()="+tool.getStateFile());
//			System.out.println("tool.getRestoreMaster().getStateFile()="+tool.getRestoreMaster().getStateFile());
//			targetString=tool.getResultName(); // after restore this is the actual file name
			targetString=tool.getStateFile();
			System.out.println("targetString="+targetString+" tool.getName()="+tool.getName()+"tool.getRestoreMaster().getName()="+tool.getRestoreMaster().getName());
			tool=tool.getRestoreMaster();
		}
		else {
			System.out.println("Tool "+tool.getName()+" does not have restoreMaster, but it came with getLastMode()!=TOOL_MODE.RESTORE");
		}
		String stateDirString=tool.getStateDir();
		System.out.println("Tool "+tool.getName()+" getStateDir()="+tool.getStateDir());
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
		// Create file for target and see if it actually exists
		IFile target=  stateDir.getFile(targetString); //null po
		if (!target.exists()){
			System.out.println("BUG: file that was just restored does not exist: "+
					target.getLocation().toOSString());
			return false;
		}
		// Debug
		try {
			Map<QualifiedName,String> allPP=	target.getPersistentProperties();
			System.out.println("3. Getting persistent properties from properties from "+target.getLocation().toOSString()+" / "+targetString);
			for (QualifiedName qn:allPP.keySet()){
				System.out.println("... "+qn.toString()+": "+allPP.get(qn));
			}
		} catch (CoreException e2) {
			System.out.println("Failed to get persistent properties from "+target.getLocation().toOSString());
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
		} catch (NumberFormatException e1){
			System.out.println("Bad hashcode "+sHashCode+" in "+target.getLocation().toOSString());
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
					", while restoring tool is "+tool.getName()+" - invalidating all dependencies timestamps.");
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
				(tool.getAutoSave() || isSave())&&  // autosave enabled or save button pressed
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
				System.out.println("Finished (auto)save tool "+tool.getName()+" for "+tool.getName());
			}
			if (isSave()) { // more to save?
				List<Tool> toolsToSave=getToolsToSave(); // find if there are any sessions in unsaved state - returns list (not yet processed)
				if ((toolsToSave!=null) && (toolsToSave.size()>=0)){
					if (toolsToSave.get(0).getSaveMaster()!=null){
						System.out.println("Seems to be a BUG that might cause infinite attempts to save while processing tool "+
								tool.getName()+", first save tool "+toolsToSave.get(0).getName()+
								", saveMaster()="+toolsToSave.get(0).getSaveMaster().getName());
						return false;
					}
					if (tryAutoSave(toolsToSave.get(0))) return true;
				}
			}
			releaseSave();
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
		
		System.out.println("updateLinkLatest("+tool.getName()+")"+
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
		// Debug
		try {
			Map<QualifiedName,String> allPP=	target.getPersistentProperties();
			System.out.println("4. Getting persistent properties from properties from "+target.getLocation().toOSString());
			for (QualifiedName qn:allPP.keySet()){
				System.out.println("... "+qn.toString()+": "+allPP.get(qn));
			}
		} catch (CoreException e2) {
			System.out.println("Failed to get persistent properties from "+target.getLocation().toOSString());
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
        // debug - readback:
		try {
			target.refreshLocal(0, null); // long-running
		} catch (CoreException e1) {
			System.out.println("Failed to refreshLocal "+target.getLocation().toOSString()+" e="+e1.toString());
		}

		try {
			Map<QualifiedName,String> allPP=	target.getPersistentProperties();
			System.out.println("5. Getting persistent properties from properties from "+target.getLocation().toOSString());
			for (QualifiedName qn:allPP.keySet()){
				System.out.println("... "+qn.toString()+": "+allPP.get(qn));
			}
		} catch (CoreException e2) {
			System.out.println("Failed to get persistent properties from "+target.getLocation().toOSString());
		} 
        
        
	}

	/**
	 * Update open session(s) state
	 * @param tool - tool just finished 
	 * @return true if update happened
	 */
	private boolean updateSessionTools(Tool tool){
		String targetString=tool.getStateFile();
		String linkString=  tool.getStateLink();

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
				consoleTool.setOpenState(linkString,targetString);
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
   	    for (String state:stateProviders.keySet()){
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
    	if (tool.getRestoreMaster()!=null) tool.getRestoreMaster();
    	else if (tool.getSaveMaster()!=null) tool.getSaveMaster();
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
		// why did it fail?
		if (SelectedResourceManager.getDefault().isToolsLinked()) {
			System.out.println("====Propagating 'dirty' flags to dependent non-pinned tools");
			propagateDirty();
		}
    }
    
    private void propagateDirty(){
    	boolean newDirty=false;
    	//	private Map<String,ToolStateStamp> currentStates;

		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (
					tool.isDisabled() ||                // disabled tool (or abstract)
					(tool.getSaveMaster()!=null) ||     // Is Save tool
					(tool.getRestoreMaster()!=null)){   // or is Restore tool
					continue;
				}

			if (!tool.isDirty()){
				List<String> depStates=tool.getDependStates();
				if ((depStates==null) || (depStates.size()==0)){
					continue; // no dependencies at all;
				}
				boolean met=true;
				if (currentStates!=null) for (String state:depStates){
					Tool provider=(currentStates.get(state)==null)?null: ToolsCore.getTool(currentStates.get(state).getToolName());
					if (provider==null){
						met=false;
						System.out.println("No providers for depend state "+state);
						break;
					}
					if (provider.getRestoreMaster()!=null){
						System.out.println("propagateDirty(): should not happen: tool "+
								provider.getName()+".getRestoreMaster()="+
								provider.getRestoreMaster().getName());
						provider=provider.getRestoreMaster();
					} else if (provider.getSaveMaster()!=null){
						System.out.println("propagateDirty(): should not happen: tool "+
								provider.getName()+".getSaveMaster()="+
								provider.getSaveMaster().getName());
						provider=provider.getSaveMaster();
					}
					if (provider.isDirty() && !provider.isPinned()){
						met=false;
						System.out.println("Provider for depend state "+state+" - "+provider.getName()+" : isDirty()="+
								provider.isDirty()+", provider.isPinned()="+provider.isPinned());
						break;
					}
				}
				if (!met) {
					tool.setDirty(true);
					newDirty=true;
				}
			}
		}
		if (newDirty) propagateDirty(); // recursive call until no more new tools are marked dirty 
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

