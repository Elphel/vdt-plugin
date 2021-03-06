/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
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
package com.elphel.vdt.core.tools.params;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
import com.elphel.vdt.core.launching.LaunchCore;
import com.elphel.vdt.core.launching.ToolLogFile;
//import com.elphel.vdt.core.launching.VDTLaunchUtil;
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

    private static final String PROJECT_CURRENTSTATE_TOOLNAME =  "currentState.toolName.";
    private static final String PROJECT_CURRENTSTATE_STATEFILE = "currentState.stateFile.";
    private static final String PROJECT_CURRENTSTATE_TOOLSTAMP = "currentState.toolStamp.";

//    private static final int MAX_TOOLS_TO_RUN=100;
   
	private boolean shiftPressed=false;
	private DesignFlowView designFlowView;
	private boolean stopOn; // Stop button is pressed
	private boolean saveOn; // save button is on
	private Map<String,Set<Tool>> stateProviders;
	private Map<String,ToolStateStamp> currentStates;
	private IMemento unfinishedMemento=null;
	private String menuName=null;
	
	private DependChangeListener dependChangeListener;
	
//	private IProgressMonitor monitor;
	
	private static void DEBUG_PRINT(String msg){
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
			System.out.println(msg);
		}
	}

	public ToolSequence(DesignFlowView designFlowView){
		this.designFlowView=designFlowView;
		this.currentStates=new Hashtable<String,ToolStateStamp>();
		this.dependChangeListener = new DependChangeListener();
	}

	public void clearToolStates(){
		this.currentStates=new Hashtable<String,ToolStateStamp>();
		for (Tool tool:ToolsCore.getConfig().getContextManager().getToolList()){
			// TODO: Actually find and close all running sessions
			if (tool.getState()== TOOL_STATE.KEPT_OPEN) {
				MessageUI.error("Tool "+tool.getName()+" is running session, you need to close it to stop");
			} else {
				tool.setStateJustThis(TOOL_STATE.NEW);
				tool.clearDependStamps();
			}
			tool.setOpenTool(null); // reset tool state (only needed for console tools) 
		}
		setToolsDirtyFlag(true); // update may be needed ?
		toolFinished(null);
	}

	public void clearProjectStates(){
		IProject project= SelectedResourceManager.getDefault().getSelectedProject();
		if (project==null){
			System.out.println("Can not clear persistent properties for a non-existent project");
			return;
		}
    	Map<QualifiedName,String> pp;    
    	try {
    		pp=project.getPersistentProperties();
    	} catch (CoreException e){
    		System.out.println(project+": Failed getPersistentProperties(), e="+e);
    		return;
    	}
    	for(QualifiedName qn:pp.keySet()) {
    		DEBUG_PRINT("Clearing persistent property "+qn.toString()+" for project "+project.toString());
    		try {
				project.setPersistentProperty(qn, null);
			} catch (CoreException e) {
	    		System.out.println(project+": Failed setPersistentProperties("+qn.toString()+",null), e="+e);
			}
    	}
	}
	
	
	public Set<IFile> getOldFiles(Set<String> dirs){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		Set<String> linkFilesTargets=new HashSet<String>();
		Set<IFile> nonLinkFiles=new HashSet<IFile>();
		for (String dir:dirs){
			IFolder stateDir= project.getFolder(dir);
			if (stateDir.exists()){
				IResource [] files;
				try {
					files=stateDir.members();
				} catch (CoreException e) {
					System.out.println("Error getting member resources in directory "+stateDir.getLocation().toOSString());
					continue;
				}
				for (IResource file:files){
					if (file.getType()==IResource.FILE) {
						if (file.isLinked()){
//							System.out.println("Got linked file: "+file.getLocation().toOSString());
							linkFilesTargets.add(file.getRawLocation().toString());
						} else {
//							System.out.println("Got non=link file: "+file.getLocation().toOSString());
							nonLinkFiles.add((IFile) file);
						}
					}
				}
			}
		}
		DEBUG_PRINT("Got "+linkFilesTargets.size()+" links, "+nonLinkFiles.size()+" regular files");
		Set<IFile> fileToRemove=new HashSet<IFile>(nonLinkFiles);
		for (IFile file:nonLinkFiles){
			String fileString=file.getLocation().toString();
			for (String linkTarget:linkFilesTargets){
				if (linkTarget.equals(fileString)){
					fileToRemove.remove(file);
					break;
				}
			}
		}
		DEBUG_PRINT("Left "+fileToRemove.size()+" to remove:");
		for (IFile file:fileToRemove){
			DEBUG_PRINT("---- "+file.getLocation().toOSString());
		}
		return fileToRemove;
	}
	
	
	public Set<String> getStateDirs(){
		Set <String> dirs = new HashSet<String>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			String dir=tool.getStateDir();
			if (dir!=null){
				dirs.add(dir);
			}
		}
		return dirs;
	}	
	
	public Set<String> getLogDirs(){
		Set <String> dirs = new HashSet<String>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			String dir=tool.getLogDir();
			if (dir!=null){
				dirs.add(dir);
			}
		}
		return dirs;
	}	
	
	
	
	public void setUnfinishedBoot(IMemento memento, boolean updateDB){
		unfinishedMemento=memento;
//		if (memento!=null){
		if (updateDB) {
			// Does not seem to work:
			IActionBars bars = designFlowView.getViewSite().getActionBars();
			bars.getStatusLineManager().setMessage("Waiting for VEditor database to be built...");
			menuName=designFlowView.getPartName();
			DEBUG_PRINT("Menu name:"+menuName);
			designFlowView.changeMenuTitle("Waiting for VEditor database...");
		}
	}
	public void finalizeBootAfterVEditor(){
//		if (unfinishedMemento!=null) {
			// Does not seem to work:
			IActionBars bars = designFlowView.getViewSite().getActionBars();
			bars.getStatusLineManager().setMessage("");
//			designFlowView.changeMenuTitle(menuName);
			designFlowView.finalizeAfterVEditorDB(unfinishedMemento);
			DEBUG_PRINT("==============finalizeBootAfterVEditor()");
			addResourceChangeListener();
//		}
	}
	
	public void addResourceChangeListener(){
		VerilogPlugin.getWorkspace().addResourceChangeListener(
				dependChangeListener, IResourceChangeEvent.POST_CHANGE);
		DEBUG_PRINT("==============addResourceChangeListener()");
		
	}
	
	
	
	public void toolFinished(Tool tool){
		if (tool!=null) doToolFinished(tool);
		if (designFlowView!=null){
//			System.out.print("1.designFlowView.updateLaunchAction() threadID="+Thread.currentThread().getId());
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					designFlowView.updateLaunchAction(); // Run from Display thread to prevent "invalid thread access" when called from Runner
//					System.out.println("  2.designFlowView.updateLaunchAction() threadID="+Thread.currentThread().getId());
				}
			});
		}
	}	
	public void doToolFinished(Tool tool){
		if (tool.isRunning()) { // PLAYBACK is also isRunning()
			DEBUG_PRINT("\nTool "+tool.getName()+" is (still) running");
			return;
		}
		DEBUG_PRINT("\n-----> Tool "+tool.getName()+" FINISHED , state="+tool.getState()+", mode="+tool.getLastMode()+" threadID="+Thread.currentThread().getId());
		if (tool.getLastMode()==TOOL_MODE.PLAYBACK) { // Stopped after finishing playback
			DEBUG_PRINT("\nTool "+tool.getName()+" finished playback");
			return;
		}
// Restore tool in Run mode - same as RESTORE_MODE ? Change it to tool.setMode?		
		// RESTORE - only manual command "restore", automatic runs with RUN
		if ((tool.getState()==TOOL_STATE.SUCCESS) || (tool.getState()==TOOL_STATE.KEPT_OPEN)){
			if (tool.getRestoreMaster()!=null){ // RUN with restoreMaster will be converted to RESTORE in tool.setMode
				// copy success state to restoreMaster?
				
				restoreToolProperties(tool);// set last run hashcode and timestamp for the tool just restored
				// Only manual restore - will get pinned
				if (tool.getLastMode()==TOOL_MODE.RESTORE) {
					if (tool.getRestoreMaster()!=null) {
						tool.getRestoreMaster().setPinned(true);
					} else {
						System.out.println("Probably a bug - tool.getRestoreMaster()==null for "+tool.getName()+", while state is "+tool.getState());
						tool.setPinned(true); // restored state should be pinned?
					}
				}
			}
			// Update state of the session(s) - should be done after run or restore
			if ((tool.getLastMode()==TOOL_MODE.RUN) ||	(tool.getLastMode()==TOOL_MODE.RESTORE)){
				boolean sessionUpdated=updateSessionTools(tool); // Update state 
				DEBUG_PRINT("updateSessionTools("+tool.getName()+"tool)-> "+sessionUpdated);
			}
			// add/update current state produced by the finished tool
			putCurrentState(tool); // delegates to *Master
			// Set tool timestamps for states and source files
			if ((tool.getLastMode()==TOOL_MODE.RUN) || (tool.getLastMode()==TOOL_MODE.RESTORE)) {
		    	DEBUG_PRINT("doToolFinished(), will run setDependState("+tool.getName()+")");
				setDependState(tool); 
			}
			setToolsDirtyFlag(false); // no need to recalculate all parameters here
			if (isStop()){
				Tool waitingTool=findWaitingTool();
				if (waitingTool!=null){
//					waitingTool.setState(TOOL_STATE.FAILURE);
					DEBUG_PRINT("doToolFinished("+tool.getName()+") "+tool.toString()+"  state="+tool.getState()+" threadID="+Thread.currentThread().getId());
					waitingTool.setMode(TOOL_MODE.STOP);
				}
				releaseStop();
				DEBUG_PRINT("Stop was activated");
				return; // do nothing more
			}
			
			// Check for stop here
			if ((tool.getLastMode()==TOOL_MODE.RUN) || (tool.getLastMode()==TOOL_MODE.SAVE) || (tool.getLastMode()==TOOL_MODE.RESTORE)){
				updateLinkLatest(tool); // Do not update link if the session was just restored. Or should it be updated? - should
				                        // as it might have different timestamp
			}			
			getToolsToSave(); // find if there are any sessions in unsaved state - returns list (not yet processed)
			if (tryAutoSave(tool)) return;  // started autoSave that will trigger "toolFinished" again
			if (continueRunningTools()) return; // started next needed tool
			// will get here if no more tools to run or failed to find one
			Tool waitingTool=findWaitingTool();
			if (waitingTool==null){
				DEBUG_PRINT("No tool is waiting to run, all done.");
				return;
			} else {
				waitingTool.setState(TOOL_STATE.FAILURE); // should state be set to FAILURE?
				waitingTool.setMode(TOOL_MODE.STOP);
				String msg="Failed to find a tool to run for "+waitingTool.getName();
				DEBUG_PRINT(msg);
				MessageUI.error(msg);
			}
		} else { // Process failures here
			//Turn off any Waiting tool
			Tool waitingTool=findWaitingTool();
			if (waitingTool!=null){
				waitingTool.setState(TOOL_STATE.FAILURE); // should state be set to FAILURE?
				DEBUG_PRINT("failure:doToolFinished("+tool.getName()+") "+tool.toString()+"  state="+tool.getState()+" threadID="+Thread.currentThread().getId());
				waitingTool.setMode(TOOL_MODE.STOP);
			}
		}
	}
	
	private boolean continueRunningTools(){
		if (SelectedResourceManager.getDefault().isToolsLinked()){
			Tool reportTool=null;
			Tool waitingTool=findWaitingTool();
			if ((waitingTool==null) || (waitingTool.getRestoreMaster()==null)){
				reportTool=findReportTool();
			} else {
				DEBUG_PRINT("No report tools are considered when waiting tool ("+waitingTool.getName()+") is a restore one");
			}
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
	public void launchNextTool(final Tool tool) throws CoreException {
		if (Thread.currentThread().getId()>1){
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_THREAD_CONFICT))
				System.out.println(">>>>>>>> launching 2A: "+tool.getName()+" threadID="+Thread.currentThread().getId()+
						" ("+Thread.currentThread().getName()+")");
			Display.getDefault().asyncExec(new Runnable() { 
				public void run() {
		    		try {
		    			do_launchNextTool(tool);		    			
					} catch (CoreException e1) {
                        MessageUI.error( "Error launchNextTool("+tool.getName()+" e="+ e1.getMessage());
					}
				}
			});
			
		} else {
			do_launchNextTool(tool);
		}
	}
	public void do_launchNextTool(Tool tool) throws CoreException {
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
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_THREAD_CONFICT))
					System.out.println(">>>>>>>> launching: "+tool.getName()+" threadID="+Thread.currentThread().getId()+
							" ("+Thread.currentThread().getName()+")");
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
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_THREAD_CONFICT))
			System.out.println(">>>>>>>> launching 2: "+tool.getName()+" threadID="+Thread.currentThread().getId());
		if (Thread.currentThread().getId()>1){
			System.out.println("High thread number");
		}
		// here were problems with the thread id >1, waiting at breakpoint fixed
		LaunchCore.launch( tool,
				SelectedResourceManager.getDefault().getSelectedProject(),
				SelectedResourceManager.getDefault().getChosenTarget(),
				null); // run, not playback 
/*
 * Reused old VDTRunner()
RunningBuilds#isAlreadyOpen(VivadoOpt), no match threadID=42
Unfinished build: Vivado (Apr 1, 2014 11:23:25 AM MDT) tool Vivado
Internal Error
java.lang.NullPointerException
	at org.eclipse.debug.internal.ui.DebugUIPlugin.launchInBackground(DebugUIPlugin.java:1257)
	at org.eclipse.debug.ui.DebugUITools.launch(DebugUITools.java:757)
	at com.elphel.vdt.core.launching.LaunchCore.launch(LaunchCore.java:255)
	at com.elphel.vdt.core.tools.params.ToolSequence.launchNextTool(ToolSequence.java:403)
	at com.elphel.vdt.core.tools.params.ToolSequence.continueRunningTools(ToolSequence.java:356)
	at com.elphel.vdt.core.tools.params.ToolSequence.doToolFinished(ToolSequence.java:306)
	at com.elphel.vdt.core.tools.params.ToolSequence.toolFinished(ToolSequence.java:236)
	at com.elphel.vdt.core.tools.params.Tool.toolFinished(Tool.java:455)

 */
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
		DEBUG_PRINT("+++ findToolToLaunch("+tool.getName()+","+numSteps+")");
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
			DEBUG_PRINT("findToolToLaunch("+tool.getName()+") state= "+state);
			ToolStateStamp tss=currentStates.get(state);
			// Check that this state provider
			if (tss!=null){
				DEBUG_PRINT("State "+state+
						" tss.toolName="+tss.toolName+
						" tss.toolStateFile="+tss.toolStateFile+
						" tss.toolStamp="+tss.toolStamp );
				if ((tss.getToolName()!=null) ){
					Tool depTool=ToolsCore.getContextManager().findTool(tss.getToolName());
					if ((depTool.getState()==TOOL_STATE.SUCCESS) &&
							(depTool.getSaveMaster()==null) &&     // somehow got here (maybe from older - memento)
							(depTool.getRestoreMaster()==null) &&
							(depTool.isPinned() || !depTool.isDirtyOrChanged())){
						DEBUG_PRINT("depTool= "+depTool.getName());
						// dependency satisfied, but is this state current for some session?
						for (String osn:openStates.keySet()){
							DEBUG_PRINT("-- openStateName="+osn+" state="+state); // openState - with ts, state - link
						}
						if (openStates.keySet().contains(state)){
							continue; // dependency satisfied and the state is open
						} else {
							// The state was achieved, but it is not current. Try restore tool if possible
							DEBUG_PRINT("State "+state+" was previously achieved, but it is not current - will need to restore/re-run");
							if (restoreHasFile(depTool)){ // restoreHasFile(tool)
								DEBUG_PRINT("Using "+depTool.getRestore().getName()+" to restore state="+state); // DEBUG_PRINT("Using "+tool.getRestore().getName()+" to restore state="+state);
								Tool neededConsole=findNeededConsole(depTool.getRestore());
								if (neededConsole!=null){
//									DEBUG_PRINT("But first open console "+neededConsole.getName());
									return neededConsole;
								}
								return depTool.getRestore(); // return tool.getRestore();
							}
							// just do nothing here and fall through to tss be set to null ?
						}
					}
				}
				tss=null; // missing or dirty tool - will need to re-run
			}
			if (tss==null){
				DEBUG_PRINT("State "+state+" is not available");
				if (stateProviders.containsKey(state)){
					Set<Tool> providersSet=stateProviders.get(state);
					for (Tool provider:providersSet){
						if (!provider.isDisabled() && (provider.getRestoreMaster()==null)){ // not considering restore tools here (later)
							DEBUG_PRINT("For wanted tool "+tool.getName()+
									" using tool "+provider.getName()+
									" to get needed state "+state);
							return findToolToLaunch(provider, numSteps-1);
						}
					}
					MessageUI.error("Could not find enabled tool to provide state "+state);
					DEBUG_PRINT("Could not find enabled tool to provide state "+state);
					return null;
				} else {
					MessageUI.error("No tool provides state "+state);
					DEBUG_PRINT("No tool provides state "+state);
					return null;
				}
			}
		}
		// Got here if all state dependencies are met (or there are none)
		// Does the tool need a console that needs to be started?
		Tool neededConsole=findNeededConsole(tool);
		if (neededConsole!=null) return neededConsole;
		// everything is met to run this tool. Can we just restore it instead (only if it is not in WAIT state)
		if (tool.isWaiting()){
			DEBUG_PRINT("OK to run waiting tool "+tool.getName());
			return tool;
		}
		if ((tool.getRestore()!=null) &&
			(tool.isPinned() || !tool.isDirtyOrChanged()) &&
			(tool.getState() == TOOL_STATE.SUCCESS)&&
			restoreHasFile(tool)){
			DEBUG_PRINT("Try to restore tool "+tool.getName()+" using "+tool.getRestore().getName());
			return tool.getRestore();
		}
		DEBUG_PRINT("All cleared to run "+tool.getName());
		if (tool.alreadyRan()){
			DEBUG_PRINT("But this tool was already tried in this launch.");
			return null;
		}
		return tool;
	}
	
	private Tool findNeededConsole(Tool tool){
		List<Tool> consoleTools= getUsedConsoles(tool);
		for (Tool consoleTool:consoleTools){ // or maybe do it after other dependencies?
			if (consoleTool.getState()!=TOOL_STATE.KEPT_OPEN) {
				DEBUG_PRINT("Need to launch tool"+consoleTool.getName()+" to satisfy dependencies of tool "+tool.getName());
				consoleTool.setOpenTool(null); // reset tool state 
				return consoleTool; // start the console session
			}
		}
		return null;
	}
	
	/**
	 * Find enabled tool that can be ran from the current state without changing it (such as various reports)
	 * and is "dirty" or never ran
	 * @return found tool to launch or null
	 */
	private Tool findReportTool(){
		// get open session(s)
		DEBUG_PRINT("Looking for report tools for current state");
		List<Tool> sessions=getOpenSessions();
		if (sessions.size()==0) return null;
		Map<String,String> openStates=new ConcurrentHashMap<String,String>();
		for (Tool session:sessions){
			if (session.getOpenStateName() != null) openStates.put(session.getOpenStateName(),session.getOpenStateFile());
		} // Got open state that is now dirty
		// Remove open states that correspond to dirty tools
		for (String state:openStates.keySet()){
			for (Tool tool:ToolsCore.getConfig().getContextManager().getToolList()){
//				if ((tool.getStateLink().equals(state)) && tool.isDirty()){
	      		if (state.equals(tool.getStateLink()) && tool.isDirty()){
					openStates.remove(state);
					DEBUG_PRINT("Removing state "+state+" as the tool "+tool.getName()+" is dirty.");
				}
			}
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
			if (tool.alreadyRan()){
				DEBUG_PRINT("Report tool "+tool.getName()+" already ran in this launch, skipping");
				continue;
			}
			if (tool.getState()==TOOL_STATE.FAILURE){
				// find latest of the current states (usually just one)
//				MessageUI.error("Enable breakpoints, debugging findReportTool()");
				ToolStateStamp stamp=null;
				for (String state:depStates){
					if (openStates.keySet().contains(state)){
						ToolStateStamp tss=currentStates.get(state);
						if (tss!=null){
							if ((stamp==null) || tss.after(stamp)){
								stamp=tss;
							}
						}
					}
				}
				if (stamp!=null) {
					DEBUG_PRINT("stamp="+stamp.getToolStamp()+"\ntool.getTimeStamp()="+tool.getTimeStamp());
				} else {
					DEBUG_PRINT("stamp=null\ntool.getTimeStamp()="+tool.getTimeStamp());
				}
				if (tool.getTimeStamp()==null){
					DEBUG_PRINT("tool.getTimeStamp()=NULL");
				}
				if ((stamp==null) ||(tool.getTimeStamp()==null) || stamp.after(tool.getTimeStamp()) || stamp.getToolStamp().equals(tool.getTimeStamp())){ 
//					tool.recalcHashCodes(); // it was 0 here
		    		try {
		    			//Not needed anymore, as these are ran on load (before it was only for good ones)? 
		    			tool.updateContextOptions(SelectedResourceManager.getDefault().getSelectedProject()); // restoring this too, as once missed tool
		    			                                                                                      // parameters when tool state was deleted 
		    			tool.buildParams(true);  // recalculates hashCode for this tool, this is needed
		    	    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
		    			System.out.println("recalcHashCodes(): "+tool.getName()+
		    					" hashMatch()="+tool.hashMatch()+
		    					" getCurrentHash()="+tool.getCurrentHash()+
		    					" getLastRunHash()="+tool.getLastRunHash());
		    	    	}
		    		} catch (ToolException e) {
		    			System.out.println("failed buildParams(true) on tool="+tool.getName()+", e="+e.toString());
		    		}
					
					if (tool.hashMatch()) {
						DEBUG_PRINT("Report tool "+tool.getName()+" failed after the current state was created, skipping it from automatic run.");
						continue;
					} else {
						DEBUG_PRINT("Report tool "+tool.getName()+" failed after the current state was created, but hash is different - retryting.");
						DEBUG_PRINT("getLastRunHash()="+tool.getLastRunHash()+" getCurrentHash()="+tool.getCurrentHash());
					}
				}
				// will retry again
			}
			DEBUG_PRINT("Report tool "+tool.getName()+" can be ran at the current state.");
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
			// Will not include save tool (definitely) and restore tool (will be dealt separately - as a substitute to the master)
			if ((!tool.isDisabled()) && (tool.getSaveMaster()==null) && (tool.getRestoreMaster()==null)){
				String state=tool.getStateLink(); // some tools (like reports) do not change states 
				if (state!=null) {
					if (!stateProviders.containsKey(state)){
						stateProviders.put(state,(Set<Tool>) new HashSet<Tool>());
					}
					stateProviders.get(state).add(tool);
				}
			}
		}
		// Verify that each dependent state has a provider
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (!tool.isDisabled()){
				List<String> dependStates=tool.getDependStates();
				if ((dependStates!=null) && (dependStates.size()>0)){
					for (String state:dependStates){
						if (!stateProviders.containsKey(state)){
							MessageUI.error("No tool provides output state '"+state+"' needed to satisfy dependency of the tool "+tool.getName());
							System.out.println("No tool provides output state '"+state+"' needed to satisfy dependency of the tool "+tool.getName());
						}
					}
				}
			}
		}
		DEBUG_PRINT("Got "+stateProviders.keySet().size()+" different states, number of mappings="+stateProviders.keySet().size());
	}
	
	public void launchToolSequence(
			Tool tool,
			TOOL_MODE mode,
			int choice,
			String fullPath,
			String ignoreFilter) throws CoreException {
		if (!okToRun()) return;
		// Just testing://
//		OutlineDatabase.getProjectsDatabase(SelectedResourceManager.getDefault().getSelectedProject()).invalidateToolCache();
		
		
		// Set this tool dirty (not to try reports on this tool before it ran)
		DEBUG_PRINT("launchToolSequence("+tool.getName()+", setting its state to \"Dirty\"");
		
		tool.setDirty(true);
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
			if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_THREAD_CONFICT))
				System.out.println(">>>>>>>> launching 3: "+tool.getName()+" threadID="+Thread.currentThread().getId());
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
		DEBUG_PRINT("logBuildStamp="+logBuildStamp);
		tool.setDesignFlowView(designFlowView);
		//            tool.setRunning(true);
		tool.setMode(TOOL_MODE.PLAYBACK);
		tool.toolFinished();
		tool.setChoice(0);
		SelectedResourceManager.getDefault().updateActionChoice(fullPath, 0, null); // Andrey
		SelectedResourceManager.getDefault().setBuildStamp(); // OK - even for log? Or use old/selected one?
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_THREAD_CONFICT))
			System.out.println(">>>>>>>> launching 4: "+tool.getName()+" threadID="+Thread.currentThread().getId());
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
		DEBUG_PRINT("setShiftPressed("+shiftPressed+")");
	}
	public boolean isShiftPressed(){
		return shiftPressed;
	}
	
	public void setStop(boolean pressed){
		this.stopOn=pressed;
		if (pressed && shiftPressed) {
			DEBUG_PRINT("Marking all running tools as if they are stopped");
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
			releaseSave();
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
//		System.out.println("isSaveEnabled(): "+!getToolsToSave().isEmpty());
		return !getToolsToSave().isEmpty();
	}
	
	public List<Tool> getOpenSessions(){
//		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		List<Tool> sessionList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (
					(tool.getState()==TOOL_STATE.KEPT_OPEN) &&
					(tool.getOpenTool()!=null) &&
					(tool.getOpenStateFile()!=null)){
				// See if state file is not saved
				Tool ranTool=tool.getOpenTool();
				String stateDirString=ranTool.getStateDir();
				if (stateDirString==null){
					DEBUG_PRINT("getOpenSessions(): stateDirString==null");
					continue;
				}
				String linkString=ranTool.getStateLink();
				if (linkString==null){
					DEBUG_PRINT("getOpenSessions(): linkString==null");
					continue;
				}
				/*
				IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
				IFile link=   stateDir.getFile(linkString); // null
				IFile target= stateDir.getFile(tool.getOpenStateFile());
				DEBUG_PRINT("*****link.getRawLocation()=  "+link.getRawLocation().toString());
				DEBUG_PRINT("******target.getLocation()=  "+target.getLocation().toString());
				*/
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
		DEBUG_PRINT("getToolsToSave():");
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		List<Tool> saveToolsList=new ArrayList<Tool>();
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
			if (
					(tool.getState()==TOOL_STATE.KEPT_OPEN) &&
					(tool.getOpenTool()!=null) &&
					(tool.getOpenStateFile()!=null)){
				// See if state file is not saved
				Tool ranTool=tool.getOpenTool();
				String stateDirString=ranTool.getStateDir();
				if (stateDirString==null){
					DEBUG_PRINT("getOpenSessions(): stateDirString==null");
					continue;
				}
				String linkString=ranTool.getStateLink();
				if (linkString==null){
					DEBUG_PRINT("getOpenSessions(): linkString==null");
					continue;
				}
				IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
				IFile link=   stateDir.getFile(linkString); // null
				IFile target= stateDir.getFile(tool.getOpenStateFile());
				DEBUG_PRINT("**link.getRawLocation()=  "+link.getRawLocation().toString());
				DEBUG_PRINT("***target.getLocation()=  "+target.getLocation().toString());
				if (!link.getRawLocation().toString().equals(target.getLocation().toString())){
					saveToolsList.add(ranTool);
					DEBUG_PRINT("****Adding=  "+ranTool.getName());
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
				DEBUG_PRINT("stopAllRunnig("+tool.getName()+") "+tool.toString()+"  state="+tool.getState()+" threadID="+Thread.currentThread().getId());
				tool.setMode(TOOL_MODE.STOP);
			}
		}
	}

	
	public boolean restoreHasFile(Tool tool){
		DEBUG_PRINT("restoreHasFile("+tool.getName()+")");
		if (tool.getRestore()==null) {
			return false; // No restore tool specified
		}
		String targetString=tool.getStateFile(); // With timestamp or specifically set through selection
		String stateDirString=tool.getStateDir();
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
		IFile target=  stateDir.getFile(targetString); //null po
		if (target==null){
			System.out.println("Restore file for tool "+tool.getName()+" - '"+targetString+"': got null");
			return false;
		}
		if (!target.exists()){
			System.out.println("Restore file for tool "+tool.getName()+": "+target.getLocation().toOSString()+" does not exist");
			return false;
		}
		return true;
	}
	
	
	public boolean restoreToolProperties(Tool tool){
		DEBUG_PRINT("restoreToolProperties("+tool.getName()+")");
		String targetString=tool.getStateFile(); // With timestamp or specifically set through selection
		if (tool.getRestoreMaster()!=null) {
			targetString=tool.getStateFile();
			tool=tool.getRestoreMaster();
		} else {
			System.out.println("Tool "+tool.getName()+" does not have restoreMaster, but it came with getLastMode()!=TOOL_MODE.RESTORE");
		}
		String stateDirString=tool.getStateDir();
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		IFolder stateDir= project.getFolder((stateDirString==null)?"":stateDirString);
		// Create file for target and see if it actually exists
		IFile target=  stateDir.getFile(targetString); //null po
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
			DEBUG_PRINT("Got timestamp="+timestamp+" in "+target.getLocation().toOSString());
		} catch (CoreException e) {
			System.out.println("No timestamp in "+target.getLocation().toOSString());
		}
		try {
			sHashCode=target.getPersistentProperty(OPTION_TOOL_HASHCODE);
			hashCode=Integer.parseInt(sHashCode);
			DEBUG_PRINT("Got hashcode="+hashCode+" ("+sHashCode+") in "+target.getLocation().toOSString());
		} catch (CoreException e) {
			System.out.println("No hashcode in "+target.getLocation().toOSString());
		} catch (NumberFormatException e1){
			System.out.println("Bad hashcode "+sHashCode+" in "+target.getLocation().toOSString());
		}
		if (timestamp!=null) {
			tool.setTimeStamp(timestamp);
			DEBUG_PRINT("Restored timestamp="+timestamp+" for tool"+tool.getName());
		}
		if (hashCode!=0) {
			tool.setLastRunHash(hashCode);
			DEBUG_PRINT("Restored lastRunHashCode="+hashCode+" for tool"+tool.getName());
		}
		// restore dependencies
		// 1. See if it was saved by the same tool
		String stateToolName=null;
		try {
			stateToolName=target.getPersistentProperty(OPTION_TOOL_NAME);
			DEBUG_PRINT("Got stateToolName="+stateToolName+" in "+target.getLocation().toOSString());
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
			DEBUG_PRINT("Launching autosave tool "+fTool.getName()+" for "+tool.getName());
			fTool.setDesignFlowView(designFlowView); // maybe will not be needed with ToolSequencing class
			fTool.setMode(TOOL_MODE.SAVE);
			fTool.setChoice(0);
    		// apply designFlowView to the tool itself
			Display.getDefault().asyncExec(new Runnable() { 
				public void run() {
					if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_THREAD_CONFICT))
						System.out.println(">>>>>>>> launching 5: "+fTool.getName()+" threadID="+Thread.currentThread().getId());
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
			DEBUG_PRINT("tryAutoSave("+tool.getName()+") "+tool.toString()+"  state="+tool.getState()+" threadID="+Thread.currentThread().getId());
    		tool.setMode(TOOL_MODE.STOP);
    		DEBUG_PRINT("Finished (auto)save tool "+tool.getName()+" for "+tool.getName());
			if (isSave()) { // more to save?
				List<Tool> toolsToSave=getToolsToSave(); // find if there are any sessions in unsaved state - returns list (not yet processed)
				if ((toolsToSave!=null) && !toolsToSave.isEmpty()){
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
		DEBUG_PRINT("updateLinkLatest("+tool.getName()+"), getLastMode()= "+tool.getLastMode());
		String stateDirString=tool.getStateDir();
		String linkString=tool.getStateLink();
		String targetString=tool.getStateFile(); // With timestamp or specifically set through selection
		
		DEBUG_PRINT("updateLinkLatest("+tool.getName()+")"+
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
		DEBUG_PRINT("File "+file.getAbsolutePath()+" exists="+file.exists());
		DEBUG_PRINT("IFile "+target.getLocation().toOSString()+" exists="+target.exists());
		if (!target.exists()){
			DEBUG_PRINT("Will not link "+linkString+" to nonexistent resource:"+targetString+
					" in "+stateDirString+": "+target.getLocation());
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
		DEBUG_PRINT("Created link "+link.toString()+" to the target "+	target.toString());
		DEBUG_PRINT("link.getRawLocation()=  "+link.getRawLocation().toString());
		DEBUG_PRINT("link.getModificationStamp()=    "+link.getModificationStamp());
		DEBUG_PRINT("target.getModificationStamp()=  "+target.getModificationStamp());
		if (tool.getSaveMaster()!=null){
			tool=tool.getSaveMaster();
		} else if (tool.getRestoreMaster()!=null){
			tool=tool.getRestoreMaster();
		}
		tool.getTimeStamp();
		String sHash= new Integer(tool.getLastRunHash()).toString();
		DEBUG_PRINT("tool.getLastRunHash()="+tool.getLastRunHash()+", sHash="+sHash);
		try {
			target.setPersistentProperty(OPTION_TOOL_TIMESTAMP, tool.getTimeStamp());
			DEBUG_PRINT("setPersistentProperty("+OPTION_TOOL_TIMESTAMP+","+tool.getTimeStamp()+
					" on "+target.getLocation().toOSString());
		} catch (CoreException e) {
			System.out.println("Failed to setPersistentProperty("+OPTION_TOOL_TIMESTAMP+","+tool.getTimeStamp()+
					" on "+target.getLocation().toOSString());
		}
		try {
			target.setPersistentProperty(OPTION_TOOL_HASHCODE, sHash);
			DEBUG_PRINT("setPersistentProperty("+OPTION_TOOL_HASHCODE+","+sHash+
					" on "+target.getLocation().toOSString());			
		} catch (CoreException e) {
			System.out.println("Failed to setPersistentProperty("+OPTION_TOOL_HASHCODE+","+sHash+
					" on "+target.getLocation().toOSString());
		}
		// Save dependencies:
		// Set toll name (same state may be result of running different tool (not yet used)
		try {
			target.setPersistentProperty(OPTION_TOOL_NAME, tool.getName());
			DEBUG_PRINT("setPersistentProperty("+OPTION_TOOL_NAME+","+tool.getName()+
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
    			DEBUG_PRINT("setPersistentProperty("+qn+","+tool.getName()+
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
    			DEBUG_PRINT("setPersistentProperty("+qn+","+stamp+
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
		String linkString=  tool.getStateLink();

		// after restore this may be a non-timestamp file - use current timestamp instead of the restored?
		if (targetString==null){
			DEBUG_PRINT("No result state specified for tool "+tool.getName()+", no session will be updated");
			return false;
		}
		List<Tool> sessionList=getUsedConsoles(tool); // never null
		DEBUG_PRINT("Found "+sessionList.size()+" console sessions for this tool "+tool.getName());
		if (sessionList.size()>0){
			for(Iterator<Tool> iter = sessionList.iterator(); iter.hasNext();) {
				Tool consoleTool=iter.next();
				consoleTool.setOpenState(linkString,targetString);
				DEBUG_PRINT("Set openState of "+consoleTool.getName()+" to "+targetString);
				if (tool.getRestoreMaster()!=null) { // after restore save master tool
					consoleTool.setOpenTool(tool.getRestoreMaster());
					DEBUG_PRINT("Set setOpenTool of "+consoleTool.getName()+" to "+tool.getRestoreMaster().getName());
				} else {
					consoleTool.setOpenTool(tool);
					DEBUG_PRINT("Set setOpenTool of "+consoleTool.getName()+" to "+tool.getName());
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
    			 null, // Component parent, or convert from Shell VerilogPlugin.getActiveWorkbenchShell()
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
    		DEBUG_PRINT("Selection canceled");
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
    	public boolean after(ToolStateStamp after){
    		return SelectedResourceManager.afterStamp(after.getToolStamp(), getToolStamp());
    	}
    	public boolean after(String after){
    		return SelectedResourceManager.afterStamp(after, getToolStamp());
    	}
    }
	
    /**
     * Save tool states to the project persistent properties
     * @param project - project to save properties to
     */
    public void saveCurrentStates(IProject project) {
    	if ((project==null) || !project.exists()) {
    		System.out.println("Can not set persistent properties of non-existent project "+project);
    		return;
    	}
    	for (String state:currentStates.keySet()){
    		ToolStateStamp tss=currentStates.get(state);
        	QualifiedName qn= new QualifiedName(VDT.ID_VDT, PROJECT_CURRENTSTATE_TOOLNAME+state);
        	try {project.setPersistentProperty(qn, tss.getToolName());}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+tss.getToolName()+", e="+e);}
        	
        	qn= new QualifiedName(VDT.ID_VDT, PROJECT_CURRENTSTATE_STATEFILE+state);
        	try {project.setPersistentProperty(qn, tss.getToolStateFile());}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+tss.getToolStateFile()+", e="+e);}
    		
        	qn= new QualifiedName(VDT.ID_VDT, PROJECT_CURRENTSTATE_TOOLSTAMP+state);
        	try {project.setPersistentProperty(qn, tss.getToolStamp());}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+tss.getToolStamp()+", e="+e);}
            DEBUG_PRINT("Saving state  "+state+
            		" to project:"+project.toString()+
            		", toolName="+tss.getToolName()+
            		", toolStateFile="+tss.getToolStateFile()+
            		", toolStamp="+tss.getToolStamp());
    	}
    }

	/**
	 * Restore tool states from the project persistent properites
	 * @param project - project to get properties from
	 */
	public void restoreCurrentStates(IProject project) {
    	if ((project==null) || !project.exists()) {
    		System.out.println("Can not read persistent properties from the non-existent project "+project);
    		return;
    	}
    	Map<QualifiedName,String> pp;    
    	try {
    	pp=project.getPersistentProperties();
    	} catch (CoreException e){
    		System.out.println(project+": Failed getPersistentProperties(), e="+e);
    		return;
    	}

    	currentStates.clear();
   	    setStateProvides(); // Can be called just once - during initialization?
   	    for (String state:stateProviders.keySet()){
        	QualifiedName qn_toolName=  new QualifiedName(VDT.ID_VDT, PROJECT_CURRENTSTATE_TOOLNAME+state);
        	if (pp.containsKey(qn_toolName)){
            	QualifiedName qn_stateFile= new QualifiedName(VDT.ID_VDT, PROJECT_CURRENTSTATE_STATEFILE+state);
            	QualifiedName qn_toolStamp= new QualifiedName(VDT.ID_VDT, PROJECT_CURRENTSTATE_TOOLSTAMP+state);
   	    		currentStates.put(state,new ToolStateStamp(
   	    				pp.get(qn_toolName),
   	    				pp.get(qn_stateFile),
   	    				pp.get(qn_toolStamp)
   	    				));
   	    		DEBUG_PRINT("Restoring state  "+state+
   	    				" from project "+project.toString()+
   	    				", toolName="+pp.get(qn_toolName)+
   	    				", toolStateFile="+pp.get(qn_stateFile)+
   	    				", toolStamp="+pp.get(qn_toolStamp));
        		
        	}
   	    }
   	    // Set all tool dirty flags according to restored states and tools dependencies
   	    //        updateContextOptions(project); // Fill in parameters - it parses here too - at least some parameters? (not in menu mode)
   	    // setToolsDirtyFlag(true) initiates Verilog database rebuild, let's trigger it intentionally
   	    //VerilogUtils.getTopModuleNames((IFile)resource);
   	    // stToolsDirtyFlag(true); // recalculate each successful tool's parameters - moved to caller
	}
	
	public void saveCurrentStates(IMemento memento) {
    	for (String state:currentStates.keySet()){
    		ToolStateStamp tss=currentStates.get(state);
            memento.putString(state+TAG_CURRENTSTATE_TOOLNAME, tss.getToolName());
            memento.putString(state+TAG_CURRENTSTATE_STATEFILE, tss.getToolStateFile());
            memento.putString(state+TAG_CURRENTSTATE_TOOLSTAMP, tss.getToolStamp());
            DEBUG_PRINT("Saving state  "+state+
            		", toolName="+tss.getToolName()+
            		", toolStateFile="+tss.getToolStateFile()+
            		", toolStamp="+tss.getToolStamp());

    	}
    }

    
    
    /**
     * Restore states (snapshot files status) from persistent storage
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
   	    		DEBUG_PRINT("Restoring state  "+state+
   	    				", toolName="+memento.getString(state+TAG_CURRENTSTATE_TOOLNAME)+
   	    				", toolStateFile="+memento.getString(state+TAG_CURRENTSTATE_STATEFILE)+
   	    				", toolStamp="+memento.getString(state+TAG_CURRENTSTATE_TOOLSTAMP));
   	    	}
   	    }
   	    // Set all tool dirty flags according to restored states and tools dependencies
   	    //        updateContextOptions(project); // Fill in parameters - it parses here too - at least some parameters? (not in menu mode)
   	    // setToolsDirtyFlag(true) initiates Verilog database rebuild, let's trigger it intentionally
   	    //VerilogUtils.getTopModuleNames((IFile)resource);
   	    // stToolsDirtyFlag(true); // recalculate each successful tool's parameters - moved to caller
	}
	
    public void putCurrentState(Tool tool){
    	if (tool.getRestoreMaster()!=null) tool=tool.getRestoreMaster();
    	else if (tool.getSaveMaster()!=null) tool=tool.getSaveMaster();
		String linkString=tool.getStateLink(); // name of the state file w/o timestamp
		if (linkString!=null) currentStates.put(linkString, new ToolStateStamp(tool));
    }


    /**
     * Scan all succeeded tools and set "dirty" flag if their dependencies do not match stored ones 
     * No, do for all - good or bad (bad needed to be updateContextOptions(project) to set current parameter values -> hashCodes for considering to be auto-rerun
     */
    public void setToolsDirtyFlag(boolean update){
		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
		for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
//			if (tool.getState()==TOOL_STATE.SUCCESS){
//			if (tool.getName().equals("ISExst")){
//				System.out.println("Debugging ISExst");
//			}
		        if (update){
		        	// tool.updateContextOptions(project) recalculates parameters, but not the hashcodes
		        	tool.updateContextOptions(project); // Fill in parameters - it parses here too - at least some parameters? (not in menu mode)
		        	try {
						tool.buildParams(true, true); // dryRun and re-parse for dependencies
					} catch (ToolException e) {
						System.out.println("setToolsDirtyFlag(): failed to buildParams() for tool "+tool.getName());
					}
		        }
				tool.setDirty(!matchDependState(tool));
//			}
		}
		// why did it fail?
		if (SelectedResourceManager.getDefault().isToolsLinked()) {
			DEBUG_PRINT("====Propagating 'dirty' flags to dependent non-pinned tools");
			propagateDirty();
		}
    }
    
    private void propagateDirty(){
    	boolean newDirty=false;
    	//	private Map<String,ToolStateStamp> currentStates;
		List<Tool> sessions=getOpenSessions();
		Map<String,String> openStates=new ConcurrentHashMap<String,String>();
		for (Tool session:sessions){
			if (session.getOpenStateName() != null) openStates.put(session.getOpenStateName(),session.getOpenStateFile());
		}
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
						// Maybe it is the current state even if it is unknown where it came from?
						
						//TODO: fix later, but first make sure report tools do not re-run
						if (!openStates.keySet().contains(state)){
							met=false;
							DEBUG_PRINT("No providers for depend state "+state+", and it is not current");
							break;
						} else {
							DEBUG_PRINT("No providers for depend state "+state+", but it is current for a session");
							continue;
						}
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
						DEBUG_PRINT("Provider for depend state "+state+" - "+provider.getName()+" : isDirty()="+
								provider.isDirty()+", provider.isPinned()="+provider.isPinned());
						break;
					}
				}
				if (!met) {
					tool.setDirty(true);
					newDirty=true;
					DEBUG_PRINT("propagateDirty(): Setting dirty flag for "+tool.getName());
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
    	DEBUG_PRINT("++++++++ setDependState("+tool.getName()+")");
    	tool.clearDependStamps(); // is it needed?
    	Map <String,String> depStates=makeDependStates(tool,false);
    	for (String state:depStates.keySet()){
			tool.setStateTimeStamp(state,depStates.get(state)); // name of the state file including timestamp
    	}
    	Map <String,String> depFiles=makeDependFiles(tool,false);
    	for (String file:depFiles.keySet()){
    		DEBUG_PRINT("setDependState("+tool.getName()+"), file="+file+" stamp="+depFiles.get(file));
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
    	Map <String,String> depStates=makeDependStates(tool, true);
    	Map <String,String> depFiles=makeDependFiles(tool,true);
        Map <String,String> storedDepStates = tool.getDependStatesTimestamps();
        Map <String,String> storedDepFiles =  tool.getDependFilesTimestamps();
        if (depStates == null) {
        	DEBUG_PRINT("matchDependState("+tool.getName()+") :"+
            " depStates == null");
        	return false;  
        }
        
        if (depStates.size()!=storedDepStates.size()) {
        	DEBUG_PRINT("matchDependState("+tool.getName()+") :"+
            " depStates.size()!=storedDepStates.size() - "+depStates.size()+"!="+storedDepStates.size());
        	return false;  
        }
        if (depFiles == null) {
        	DEBUG_PRINT("matchDependState("+tool.getName()+") :"+
                    " depFiles == null");
        	return false; 
        }
        if (depFiles.size()!=storedDepFiles.size()) {
        	DEBUG_PRINT("matchDependState("+tool.getName()+") :"+
                    " depFiles.size()!=storedDepFiles.size() - "+depFiles.size()+"!="+storedDepFiles.size());

        	return false; 
        }
        for (String state:depStates.keySet()){
        	if (!storedDepStates.containsKey(state) || !depStates.get(state).equals(storedDepStates.get(state))){
            	DEBUG_PRINT("matchDependState("+tool.getName()+") :"+
            			state+ ": "+depStates.get(state)+" <-> "+storedDepStates.get(state));
        		return false;
        	}
        }
        for (String file:depFiles.keySet()){
        	if (!storedDepFiles.containsKey(file) || !depFiles.get(file).equals(storedDepFiles.get(file))){
            	DEBUG_PRINT("matchDependState("+tool.getName()+") :"+
            			file+ ": "+depFiles.get(file)+" <-> "+storedDepFiles.get(file));
        		
        		return false;
        	}
        }
    	DEBUG_PRINT("matchDependState("+tool.getName()+") : full match!");
    	return true;
    }
    
    
    /**
     * Creating list of dependent states states after the tool was run, so each state should be available
     * When called from matchDependState() - it is OK to have missing states
     * @param tool tool just ran
     * @return map of states (link names) to states files (full with timestamps) 
     */
    private  Map <String,String> makeDependStates(Tool tool, boolean failOnMissing){
    	Map <String,String> depStates=new Hashtable<String,String>();    	
    	List<String> dependStates=tool.getDependStates();
    	if (dependStates!=null) for (String state: dependStates){
    		if (currentStates.containsKey(state)){
    			ToolStateStamp tss=currentStates.get(state);
    			depStates.put(state,tss.getToolStateFile()); // name of the state file including timestamp
    		} else {
				if (failOnMissing) {
	    			DEBUG_PRINT("makeDependStates: no information for state "+state+" on which tool "+tool.getName()+" depends, failing");
					return null;
				}
    			DEBUG_PRINT("Seems a BUG (OK when called matchDependState): no information for state "+state+" on which tool "+tool.getName()+" depends");
/*    			
    			DEBUG_PRINT("currentStates are:");
    			for (String cs:currentStates.keySet()){
    				DEBUG_PRINT(" ---- "+cs);
    			}
*/    			
    		}
    	}
    	return depStates;
    }
    private  Map <String,String> makeDependFiles(Tool tool, boolean failOnMissing){
    	DEBUG_PRINT("++++++ makeDependFiles("+tool.getName()+")");
    	Map <String,String> depFiles=new Hashtable<String,String>();
    	// Use depend files cache if available
    	List<String> dependFileNames=tool.getDependFiles(false); // files on which this tool depends - make cached version
    	if (dependFileNames!=null) {
    		IProject project = SelectedResourceManager.getDefault().getSelectedProject(); // should not be null when we got here
    		for (String depFile: dependFileNames){
    			if (depFile.length()==0){
    				DEBUG_PRINT("makeDependFiles(): depFile is empty");
    				continue;
    			}
    			IFile sourceFile=project.getFile(depFile); //Path must include project and resource name: /npmtest
    			if (sourceFile.exists()) {
    				depFiles.put(depFile, String.format("%d",sourceFile.getModificationStamp()));
    				DEBUG_PRINT("makeDependFiles(): file="+depFile+", stamp="+sourceFile.getModificationStamp()+
    						" ("+sourceFile.toString()+")");
    			} else {
    				if (failOnMissing) {
        				DEBUG_PRINT("makeDependFiles(): source file "+sourceFile.getLocation()+" on which tool "+
    						tool.getName()+" depends does not exist, failing");
        				return null;
    					
    				}
    				System.out.println("Seems a BUG:  source file "+sourceFile.getLocation()+" on which tool "+
    						tool.getName()+" depends does not exist");
    				depFiles.put(depFile, ""); // empty stamp for non-existent files?
    			}
    		}
    	}
    	return depFiles;
    }
    
	class DeltaPrinter implements IResourceDeltaVisitor {
		public boolean visit(IResourceDelta delta) {
			IResource res = delta.getResource();
			if ((res instanceof IFile) && (delta.getFlags() != IResourceDelta.MARKERS)) {
				IFile file = (IFile) res;
				switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					break;
				case IResourceDelta.REMOVED:
				    DEBUG_PRINT("=====>>> DependChangeListener: removed "+file);
			        setToolsDirtyFlag(false);
					break;
				case IResourceDelta.CHANGED:
					DEBUG_PRINT("=====>>> DependChangeListener: changed "+file+String.format("0x%x", delta.getFlags()));
		            setToolsDirtyFlag(false);
					break;
				}
			}
			return true; // visit the children
		}
	}
    
    // Check dependency on resource change event
    /**
     * Class used to keep track of workspace resources
     */
    class DependChangeListener implements IResourceChangeListener {
        /**
         * Called when a resource is changed
         */
        public void resourceChanged(IResourceChangeEvent event) {
//            	if (isAnyToolRunnigOrWaiting()) {
//    				DEBUG_PRINT("=====DependChangeListener.resourceChanged(): Tool is running");
//            		return;
//            	}
//				DEBUG_PRINT("=====DependChangeListener.resourceChanged() start: "+event.getType());
        	
            switch (event.getType()) {
            case IResourceChangeEvent.PRE_CLOSE:
                    break;
            case IResourceChangeEvent.PRE_DELETE:
                    break;
            case IResourceChangeEvent.POST_CHANGE:
    				try {
    					event.getDelta().accept(new DeltaPrinter());
    				} catch (CoreException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
                    break;
            case IResourceChangeEvent.PRE_BUILD:
                    break;
            case IResourceChangeEvent.POST_BUILD:
                    break;
            }
        }
    }
    
}

