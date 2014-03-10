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
package com.elphel.vdt.core.tools.params;

import java.util.*;
import java.io.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.ui.IMemento;

import com.elphel.vdt.VDT;
import com.elphel.vdt.core.launching.ToolLogFile;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.*;
import com.elphel.vdt.core.tools.contexts.*;
import com.elphel.vdt.core.tools.config.*;
import com.elphel.vdt.core.tools.params.conditions.ConditionUtils;
import com.elphel.vdt.core.tools.params.recognizers.*;
import com.elphel.vdt.core.tools.params.types.ParamTypeBool;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.core.tools.params.types.ParamTypeString.KIND;
import com.elphel.vdt.core.tools.params.types.RunFor;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.VDTPluginImages;
import com.elphel.vdt.ui.views.DesignFlowView;
import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;


public class Tool extends Context implements Cloneable, Inheritable {
    private static final String ICON_ID_PREFIX = VDT.ID_VDT + ".Tool.Image.";
    private static final String ICON_ID_ACTION = ".action.";
//    private static final String TAG_TOOL_PINNED = ".toolstate.pinned";
//    private static final String TAG_TOOL_STATE =  ".toolstate.state";
//    private static final String TAG_TOOL_TIMESTAMP =  ".toolstate.timeStamp";
//    private static final String TAG_TOOL_LASTRUNHASH =  ".toolstate.lastRunHash";
//    private static final String TAG_TOOL_FILEDEPSTAMP =  ".toolstate.fileDependency.";
//    private static final String TAG_TOOL_STATEDEPSTAMP =  ".toolstate.stateDependency.";
    private static final String MEMENTO_TOOL_TYPE =  VDT.ID_VDT+".tool";
    private static final String MEMENTO_TOOL_PINNED = "pinned";
    private static final String MEMENTO_TOOL_STATE =  "state";
    private static final String MEMENTO_TOOL_TIMESTAMP =  "timeStamp";
    private static final String MEMENTO_TOOL_LASTRUNHASH =  "lastRunHash";
    private static final String MEMENTO_TOOL_DEPNAME =       "name";
    private static final String MEMENTO_TOOL_FILEDEPSTAMP =  "fileDependency";
    private static final String MEMENTO_TOOL_STATEDEPSTAMP = "stateDependency";
    
    private static final String PROJECT_TOOL_NAME =          "tool.";
    private static final String PROJECT_TOOL_PINNED =        ".pinned";
    private static final String PROJECT_TOOL_STATE =         ".state";
    private static final String PROJECT_TOOL_TIMESTAMP =     ".timestamp";
    private static final String PROJECT_TOOL_LASTRUNHASH =   ".lastrunhash";
    private static final String PROJECT_TOOL_DEPSTATE =      ".depstate.";
    private static final String PROJECT_TOOL_DEPFILE =       ".depfile.";


    private String baseToolName;
    private String parentPackageName;
    private String parentProjectName;
    private String exeName;
    
    private String toolErrors;
    private String toolWarnings;
    private String toolInfo;

    private List<String> extensions;
    private List<RunFor> runfor;
    private int choice; // selected variant for runfor
    private String ignoreFilter;
    
    private Tool baseTool;
    private PackageContext parentPackage;
    private ProjectContext parentProject;

    private String absoluteExeName = null;
    private boolean locationSet = false;
    private boolean isShell = false; /* Tool is a shell, preserve first argument, merge all others */
    private String projectPath=null;
    private boolean initialized = false;
    private String [] imageKeysActions = null;
    
    private List<String> dependStateNames=null;    // list of tools this one depends on -> list of files (source files) and strings (name state files)
    private List<String> dependFileNames=null;      // list of tools this one depends on -> list of files (source files) and strings (name state files)
    
    private String logDirString=null;        // directory to store this tool log files
    private String stateDirString=null;      // directory to store this tool log files
    private Parameter logDir=null;           // directory to store this tool log files
    private Parameter stateDir=null;         // directory to store this tool log files
    private String disabledString=null;      // to disable tools from automatic running
    private String resultString=null;        // parameter name of kind of file that represents state after running this tool
    private String restoreString=null;       // name of tool that restores the state of this tool ran (has own dependencies)
    private String saveString=null;          // name of tool that saves the state of this tool run 
    private String autoSaveString=null;      // name of boolean that turns on/off auto-save after the tool run
    private boolean abstractTool;            // abstract tools can only be used for inheritance, not directly (so they will not be
                                             // considered when looking for solution to run
 
    private Parameter disabled;
    private Tool restoreTool;
    private Parameter result;
    
    // TODO: Compare dependFiles with literary result of other tools, if match - these are states, not files
    private List<Parameter> dependStates; // snapshot names
    private List<Parameter> dependFiles;
    
    private Map <String,String> dependStatesTimestamps;
    private Map <String,String> dependFilesTimestamps;
//    private boolean toolIsRestore;           // this tool is referenced by other as restore="this-tool"
    private boolean dirty=false;             // tool ran before its sources (runtime value)
    private boolean pinned=false;             // tool ran before its sources (runtime value)
    
//    private long runStamp=0;                 // timestamp of the tool last ran (0 - never)
    private TOOL_STATE state=TOOL_STATE.NEW; // tool state (succ, fail,new, running)
//    private boolean running=false;
//    private long finishTimeStamp=0;
    private String timeStamp=null;
    private String timeStampRan=null;
    private String restoreTimeStamp=null;
    private DesignFlowView designFlowView;
    
    private String resultFile;               // used to overwrite name of the default result file that normally
                                             // is calculated from result and timestamp;
    
    private String [] openState=null;        // (only for open sessions) - last successful [name,result] ran in this session
    private Tool openTool=null;              // (only for open sessions) - tool last successful result ran in this session (null if none/failed)
    private Tool restoreMaster;              // Tool, for which this one is restore (or null if for none). Same restore for
    										 // multiple masters is not allowed
    private Tool saveMaster;                 // Tool, for which one this is used to save state (should have only one master)
    									     // with inheritance "disabled" will be taken from master
    private Tool saveTool;
    private Parameter autoSave;                // automatically run saveTool after successful completion of this one
    
    private TOOL_MODE runMode;
    private TOOL_MODE lastRunMode;           // last running (not STOP) mode
    private int lastRunHash;                 // hash code of the last run
    private ToolWaitingArguments toolWaitingArguments; // save here launch parameters when tool need to wait for other tools to run/get restored
    private double priority; // the lower the earlier tool will run among those with the same dependencies
	private static void DEBUG_PRINT(String msg){
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
			System.out.println(msg);
		}
	}
    public Tool(String name,
                String controlInterfaceName,
                String label,
                String iconName, 
                String baseToolName,
                String inputDialogLabel,
                String parentPackageName,
                String parentProjectName,
                String exeName, 
                boolean isShell, 
                List<String> extensions,
                String toolErrors,
                String toolWarnings,
                String toolInfo,
                List<RunFor> runfor,
                String ignoreFilter,
                List<String> toolDependsStates,
                List<String> toolDependsFiles,
                String logDirString,
                String stateDirString,
                String disabledString,          // to disable tools from automatic running
                String resultString,            // parameter name of kind of file that represents state after running this tool
                String restoreString,           // name of tool that restores the state of this tool ran (has own dependencies)
                String saveString,              // name of tool that saves the state of this tool run 
                String autoSaveString,          // name of boolean that turns on/off auto-save after the tool run
                boolean abstractTool,
                double priority,
                /* never used ??? */
                List<Parameter> params,
                List<ParamGroup> paramGroups,
                List<CommandLinesBlock> commandLinesBlocks)
    {
        super(name,
              controlInterfaceName, 
              label,
              iconName,
              inputDialogLabel,
              params, 
              paramGroups, 
              commandLinesBlocks);
        this.runfor=runfor; // should it be cloned?
        this.ignoreFilter= ignoreFilter;
        this.baseToolName = baseToolName;
        this.label = label;
        this.parentPackageName = parentPackageName;
        this.parentProjectName = parentProjectName;
        this.exeName = exeName;
        this.isShell = isShell;
        this.extensions = extensions;
        this.toolErrors   = toolErrors;
        this.toolWarnings = toolWarnings;
        this.toolInfo     = toolInfo;
        
        this.dependStateNames=toolDependsStates;    // list of tools this one depends on -> list of files (source files) and strings (name state files)
        this.dependFileNames= toolDependsFiles;      // list of tools this one depends on -> list of files (source files) and strings (name state files)
        
//        this.depends=       depends;
        this.logDirString=        logDirString;
        this.stateDirString=      stateDirString;
        
        this.disabledString=      disabledString;          // to disable tools from automatic running
        this.resultString=        resultString;            // parameter name of kind of file that represents state after running this tool
        this.restoreString=       restoreString;           // name of tool that restores the state of this tool ran (has own dependencies)
        this.saveString=          saveString;             // name of tool that saves the state of this tool run 
        this.autoSaveString=      autoSaveString;         // name of boolean that turns on/off auto-save after the tool run
        this.abstractTool=        abstractTool;
        this.priority =           priority;

        disabled=null;
        restoreTool=null;
        result=null;
        dependStates=null;
        dependFiles=null;

        pinned=false;
        openState=null;
        openTool=null;
        choice=0;
        designFlowView =null;
        timeStamp=null;
        timeStampRan=null;
        restoreTimeStamp=null;
        logDir=null;
        stateDir=null;
        resultFile=null;
        restoreMaster=null;
        saveMaster=null;
        saveTool=null;
        autoSave=null;
        runMode=TOOL_MODE.STOP;
        lastRunMode=TOOL_MODE.STOP;
        dependStatesTimestamps=new Hashtable<String,String>();
        dependFilesTimestamps= new Hashtable<String,String>();

    }
    public enum TOOL_STATE {
        NEW,
        UNKNOWN,
        FAILURE,
        SUCCESS,
        KEPT_OPEN//,
//        RUNNING
    }
    public enum TOOL_MODE {
    	STOP,
    	WAIT,
        RUN,
        RESTORE,
        SAVE,
        PLAYBACK
    }
    
    public class ToolWaitingArguments{
		private TOOL_MODE mode;
		private int choice;
		private String fullPath;
		private String ignoreFilter;
    	public ToolWaitingArguments(
    			TOOL_MODE mode,
        		int choice,
        		String fullPath,
        		String ignoreFilter){
    		this.mode=mode;
    		this.choice=choice;
    		this.fullPath=fullPath;
    		this.ignoreFilter=ignoreFilter;
    	}
    	public TOOL_MODE getMode() { return mode;}
    	public int getChoice()     { return choice;}
    	public String getFullPath(){ return fullPath;}
    	public String getIgnoreFilter(){ return ignoreFilter;}
    }
   

    public void clearDependStamps(){
    	dependStatesTimestamps.clear();
    	dependFilesTimestamps.clear();
    }
    
    public void setStateTimeStamp(String state, String stamp){
    	dependStatesTimestamps.put(state,stamp);
    }
    public void setFileTimeStamp(String file, String stamp){
    	DEBUG_PRINT("setFileTimeStamp("+file+","+stamp+") for tool "+name);
    	dependFilesTimestamps.put(file,stamp);
    }
    
    public String getStateTimeStamp(String state){
    	return dependStatesTimestamps.get(state);
    }
    public String getFileTimeStamp(String state){
    	return dependFilesTimestamps.get(state);
    }
    
    public Map <String,String> getDependStatesTimestamps(){
    	return	dependStatesTimestamps;
    }
    
    public Map <String,String> getDependFilesTimestamps(){
        return	dependFilesTimestamps;
    }

    
    
    public List<String> getDependStateNames()    { return dependStateNames; }
    public List<String> getDependFileNames()     { return dependFileNames; }
    public boolean isDirty()               { return dirty; }
    public boolean isDirtyOrChanged()      {
    	return isDirty() || !hashMatch();
    }
    public boolean isRunning()             { return (runMode!=TOOL_MODE.STOP) && ((runMode!=TOOL_MODE.WAIT)); }
    public boolean isWaiting()             { return runMode==TOOL_MODE.WAIT; }
    
    public boolean isAlmostDone(){
    	if (runMode!=TOOL_MODE.STOP) return false; 
    	if ((getRestore()!=null) && (getRestore().isRunning())) return true;
    	if ((getSave()!=null) && (getSave().isRunning())) return true;
    	return false;
    }
    
    public TOOL_MODE getMode()             { return runMode; }
    public TOOL_MODE getLastMode()         { return lastRunMode; }

    public TOOL_STATE getState()           { return state; }
    public boolean isPinned()              { return pinned; }
    public String getOpenStateName()           { return (openState!=null)?openState[0]:null; }
    public String getOpenStateFile()           { return (openState!=null)?openState[1]:null; }
    public void setOpenState(String stateName, String stateFile) {
    	if ((stateName!=null) && (stateFile!=null)) {
    		String [] pair={stateName, stateFile};
    		openState=pair;
    	}
    }

    public Tool getOpenTool()              { return openTool; }
    public void setOpenTool(Tool openTool) { this.openTool=openTool;}
    
    
    public void setDirty(boolean dirty) {
    	this.dirty=dirty;
    	DEBUG_PRINT("SetDirty("+dirty+") tool:"+getName());
    }
    public void setPinned(boolean pinned) {
    	this.pinned=pinned;
    	DEBUG_PRINT("SetPinned("+pinned+")");
    }
    
    public void setModeWait(
    		TOOL_MODE mode,
    		int choice,
    		String fullPath,
    		String ignoreFilter) {
    	setModeWait(new ToolWaitingArguments(
    			mode,
        		choice,
        		fullPath,
        		ignoreFilter));
    }

    public void setModeWait(ToolWaitingArguments toolWaitingArguments) {
    	this.toolWaitingArguments = toolWaitingArguments;
    	setMode(TOOL_MODE.WAIT);
    }

    
    public ToolWaitingArguments getToolWaitingArguments(){
    	return toolWaitingArguments;
    }
    
    
    public void setMode(TOOL_MODE mode) {
    	DEBUG_PRINT(">>--- "+name+": setMode("+mode+"), runMode="+runMode+", lastRunMode="+lastRunMode+" "+this.toString()+" threadID="+Thread.currentThread().getId());
    	if (mode !=TOOL_MODE.WAIT) this.toolWaitingArguments = null;
    	if ((runMode!=TOOL_MODE.PLAYBACK) && (runMode!=TOOL_MODE.STOP) && (mode==TOOL_MODE.STOP)){ // just stopped, but not from PLAYBACK
    		timeStampRan=timeStamp; // to determine that the tool is tried for the second time in a launch (loop) 
    		lastRunHash=getCurrentHash();
    		DEBUG_PRINT(":::: Tool "+name+": lastRunHash="+lastRunHash);
    	}
    	runMode=mode;
    	if (mode!=TOOL_MODE.STOP)  lastRunMode=mode;
    	if (mode == TOOL_MODE.RUN) { // Only RUN
    		setTimeStamp(); // copy current time to tool timestamp
    		restoreTimeStamp=null; 
        	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
        		DEBUG_PRINT("::::: Tool "+name+": setTimeStamp="+timeStamp);
        		if (name.equals("VivadoTimingReportSynthesis")){
        			DEBUG_PRINT("Tool.setMode()");
        		}
        	}
    	} else if (mode == TOOL_MODE.RESTORE){
    		if (restoreMaster!=null){
    			restoreMaster.setRestoreTimeStamp(); // copy current time to tool restore timestamp will be used when calculating dependencies
    		} else {
        		System.out.println("Restore mode, but no restoreMaster for "+name);
    		}
    	}
    	DEBUG_PRINT("--->> "+name+": setMode("+mode+"), lastRunMode="+lastRunMode+" "+this.toString()+" threadID="+Thread.currentThread().getId());
    }
    
    public boolean hashMatch(){
    	return lastRunHash == getCurrentHash();
    }
    public int getLastRunHash(){
    	return lastRunHash;
    }
    public void setLastRunHash(int hash){ // to restore from file
    	lastRunHash=hash;
    }
    public void setStateJustThis(TOOL_STATE state) {
    	this.state=state;
    }
    
    public void setState(TOOL_STATE state) {
    	setStateJustThis(state);
    	DEBUG_PRINT("SetState("+state+") for tool "+getName()+" threadID="+Thread.currentThread().getId());
    	if ((getRestoreMaster()!=null) && (state != TOOL_STATE.NEW)){
    		getRestoreMaster().setState(state); // TODO: Should it be always or just for SUCCESS ?
    	}
    }

    public void setDesignFlowView (DesignFlowView designFlowView){
    	this.designFlowView =  designFlowView; // to be able to update actions and so the state icons
    }

    public void toolFinished(){
    	designFlowView.getToolSequence().toolFinished(this);
    }
    
    
    public void initIcons(boolean force) {
    	if (!force && (imageKeysActions!=null)) return;
        if (runfor!=null){
        	String image; 
        	imageKeysActions=new String [runfor.size()];
        	for (int i=0;i<imageKeysActions.length;i++){
        		imageKeysActions[i]=null;
        		image = runfor.get(i).getIconName();
        		if (image != null) {
//        			imageKeysActions[i] = ICON_ID_PREFIX + (new File(getExeName())).getName()+ICON_ID_ACTION+i;
        			imageKeysActions[i] = ICON_ID_PREFIX + name+ICON_ID_ACTION+i;
        			VDTPluginImages.addImage(image, imageKeysActions[i], null/*tool.getLaunchType()*/);
        		}
        	}
        }
       
    } // ToolUI()

    public String getImageKey(int actionIndex) {
    	if (imageKeysActions==null) return null;
        return imageKeysActions[actionIndex];
    }
    
    public double getPriority(){
    	return  (Double.isNaN(priority))? 1.0: priority;
    }
   
    
    public List<RunFor> getRunFor(){
    	return runfor;
    }
    
    public int getChoice(){
    	return choice;
    }
    
     public void setChoice(int choice){
    	this.choice=choice;
    }

    public void init(Config config) throws ConfigException {
        if(initialized)
            return;
        
        this.config = config;
        
        initControlInterface();
        initBaseTool();

        Checks.checkCyclicInheritance(this, "tool");

        if(baseTool != null) {
            baseTool.init(config);
            abstractTool=false;
            copyBaseAttributes();
        }
        
        checkBaseTool();
        
        initParentPackage();
        initParentProject();
        initParams(); // *Inherits and sets up contexts? Also Error with copying context to items
        
        initOtherAttributes();
        
        initDisabled();
        initDepends();
        initResult();
        initRestore();
        initSave();
        initAutoSave();
        initStateDir();
        initLogDir();
        
        initCommandLines();
        initialized = true;
    }

    // Should be called before strings are processed
    public void copyBaseAttributes(){
    	if (label==null) label=baseTool.label;
    	if (exeName==null) {
    		exeName=baseTool.exeName;
    		isShell=baseTool.isShell;
    	}
    	if (extensions==     null) extensions =     baseTool.extensions;
    	if (toolErrors==     null) toolErrors =     baseTool.toolErrors;
    	if (toolWarnings==   null) toolWarnings =   baseTool.toolWarnings;
    	if (toolInfo==       null) toolInfo =       baseTool.toolInfo;
    	if (runfor==         null) runfor =         baseTool.runfor;
    	if (ignoreFilter==   null) ignoreFilter =   baseTool.ignoreFilter;
    	
    	if (dependStateNames== null) dependStateNames = baseTool.dependStateNames;
    	if (dependStateNames== null) dependStateNames = baseTool.dependStateNames;
    	
    	if (logDirString==   null) logDirString =   baseTool.logDirString;
    	if (stateDirString== null) stateDirString = baseTool.stateDirString;
    	if (disabledString== null) disabledString = baseTool.disabledString;
    	if (resultString==   null) resultString =   baseTool.resultString;
    	if (restoreString==  null) restoreString =  baseTool.restoreString;
    	if (saveString==     null) saveString =     baseTool.saveString;
    	if (autoSaveString== null) autoSaveString = baseTool.autoSaveString;
//    	System.out.println("copyBaseAttributes(), tool="+getName());
    	if (Double.isNaN(priority)) priority=       baseTool.priority;
//  What about output lines attributes?  	

    }
    
    public void initDisabled() throws ConfigException{
    	if (disabledString==null) return;
    	disabled=findParam(disabledString);
        if(disabled == null) {
            throw new ConfigException("Parameter disabled='" + disabledString + 
                                      "' used for tool '" + name + 
                                      "' is absent");
        } else if(!(disabled.getType() instanceof ParamTypeBool)) {
            throw new ConfigException("Parameter disabled='" + disabledString + 
                    "' used for tool '" + name + 
                                      "' must be of type '" + ParamTypeBool.NAME + 
                                      "'");
        }
    }
    
    public boolean isDisabled(){
    	if (abstractTool) return true; // abstract are always disabled
    	if (disabled==null) return false;
    	List<String> values=disabled.getValue(null); // null for topFormatProcessor
    	if ((values==null) || (values.size()==0)) return false;
    	return (!values.get(0).equals("true"));
    }
    
    public void initDepends() throws ConfigException{
    	// Verify definitions for state dependency
    	if (dependStateNames!=null) {
    		dependStates=new ArrayList<Parameter>();
    		for(Iterator<String> iter = dependStateNames.iterator(); iter.hasNext();) {
    			String paramID=iter.next();

    			Parameter param=findParam(paramID);
    			if(param == null) {
    				throw new ConfigException("Parameter dependStateNames='" + paramID + 
    						"' used for tool '" + name + 
    						"' is absent");
    			} else if(!(param.getType() instanceof ParamTypeString)) {
    				throw new ConfigException("Parameter dependStateNames='" + paramID + 
    						"' defined in "+param.getSourceXML()+" used for tool '" + name + 
    						"' must be of type '" + ParamTypeString.NAME + 
    						"'");                    
    			} else {
    				KIND kind=((ParamTypeString)param.getType()).getKind();
    				if (kind == ParamTypeString.KIND.TEXT) {
    					dependStates.add(param);
    				} else {
    					throw new ConfigException("Parameter dependStateNames='" + paramID + 
    							"' of type '" + ParamTypeString.NAME +
    							"' defined in "+param.getSourceXML()+" used for tool '" + name + 
    							"' must be of kind '" + ParamTypeString.KIND_TEXT_ID + "', it is '"+kind+"'");                   
    				}
    			}
    		}
    	}
    	// Verify definitions for source file dependency

    	if (dependFileNames!=null) {
    		dependFiles= new ArrayList<Parameter>();
    		for(Iterator<String> iter = dependFileNames.iterator(); iter.hasNext();) {
    			String paramID=iter.next();

    			Parameter param=findParam(paramID);
    			if(param == null) {
    				throw new ConfigException("Parameter dependFileNames='" + paramID + 
    						"' used for tool '" + name + 
    						"' is absent");
    			} else if(!(param.getType() instanceof ParamTypeString)) {
    				throw new ConfigException("Parameter dependFileNames='" + paramID + 
    						"' defined in "+param.getSourceXML()+" used for tool '" + name + 
    						"' must be of type '" + ParamTypeString.NAME + 
    						"'");                    
    			} else {
    				KIND kind=((ParamTypeString)param.getType()).getKind();
    				if (kind == ParamTypeString.KIND.FILE) {
    					dependFiles.add(param);
    				} else {
    					throw new ConfigException("Parameter depends='" + paramID + 
    							"' of type '" + ParamTypeString.NAME +
    							"' defined in "+param.getSourceXML()+" used for tool '" + name + 
    							"' must be of kind '" + ParamTypeString.KIND_FILE_ID + "', it is '"+kind+"'");                   
    				}
    			}
    		}
    	}
    }
    public List<String> getDependFiles(){
    	if ((dependFiles == null) || (dependFiles.size()==0)) return null;
    	List<String> list = new ArrayList<String>();
    	for (Iterator<Parameter> iter= dependFiles.iterator(); iter.hasNext();) {
    		List<String> vList=iter.next().getValue(null); // null for topFormatProcessor
    		if (vList!=null) {
    			for (String item:vList){
    				if ((item!=null) && (item.trim().length()>0)){
    					list.add(item.trim());
    				}
    			}
//    			list.addAll(vList);
    		}
    	}
    	return list;
    }

    public List<String> getDependStates(){
    	if ((dependStates == null) || (dependStates.size()==0)) return null;
    	List<String> list = new ArrayList<String>();
    	for (Iterator<Parameter> iter= dependStates.iterator(); iter.hasNext();) {
    		List<String> vList=iter.next().getValue(null); // null for topFormatProcessor
			for (String item:vList){
				if ((item!=null) && (item.trim().length()>0)){
					list.add(item.trim());
				}
			}
//    		if (vList!=null) list.addAll(vList);
    	}
    	return list;
    }
    
    
    public void initResult() throws ConfigException{
    	if (resultString==null) return;
    	result=findParam(resultString);
        if(result == null) {
            throw new ConfigException("Parameter result='" + resultString + 
                                      "' used for tool '" + name + 
                                      "' is absent");
        } else if(!(result.getType() instanceof ParamTypeString)) {
            throw new ConfigException("Parameter result='" + resultString +
            		"' defined in "+result.getSourceXML()+" used for tool '" + name + 
                                      "' must be of type '" + ParamTypeString.NAME + 
                                      "'");
        } else {
        	KIND kind=((ParamTypeString)result.getType()).getKind();
//        	if(kind != ParamTypeString.KIND.FILE) {
           	if(kind != ParamTypeString.KIND.TEXT) {
        		throw new ConfigException("Parameter result='" + resultString + 
        				"' of type '" + ParamTypeString.NAME +
        				"' defined in "+result.getSourceXML()+" used for tool '" + name + 
//        				"' must be of kind '" + ParamTypeString.KIND_FILE_ID + "'"+
        				"' must be of kind '" + ParamTypeString.KIND_TEXT_ID + "'"+
        				" (it is '"+kind+"')");                    
        	}
        }
    }
//        this.autoSaveString=autoSaveString;         // name of boolean that turns on/off auto-save after the tool run

    public void initAutoSave() throws ConfigException{
    	if (autoSaveString==null) return;
    	autoSave=findParam(autoSaveString);
        if(autoSave == null) {
            throw new ConfigException("Parameter autoSave='" + autoSaveString + 
                                      "' used for tool '" + name + 
                                      "' is absent");
        } else if(!(autoSave.getType() instanceof ParamTypeBool)) {
            throw new ConfigException("Parameter autoSave='" + autoSaveString +
            		"' defined in "+autoSave.getSourceXML()+" used for tool '" + name + 
                                      "' must be of type '" + ParamTypeBool.NAME + 
                                      "'");
        }
    }

    public boolean getAutoSave(){
    	if (autoSave==null) return false;
    	List<String>result=autoSave.getValue(null); // null for topFormatProcessor
    	if (!result.isEmpty()) return result.get(0).equals("true");
    	return false;
    }
    
    public List<String> getResultNames(){
    	if (result==null) return null;
    	return result.getValue(null); // null for topFormatProcessor
    }
    
    public void initRestore() throws ConfigException{
    	if (restoreString==null) return;
    	restoreTool=config.getContextManager().findTool(restoreString);
    	if (restoreTool == null) {
    		throw new ConfigException("Restore tool '" + restoreString +
    				"' of tool '" + name + 
    				"' is absent");
    	}
    	if (restoreTool.restoreMaster!=null){ // verify they have the same result
    		throw new ConfigException("Same restore tool ("+restoreTool.getName()+") for multiple master tools: " +
    				restoreTool.restoreMaster.getName() + " and "+this.getName()+" - restore tools should differ.");
    	}
    	if (resultString==null){
    		throw new ConfigException("Tool "+getName()+" has restore='"+restoreTool.getName()+
    				"' defined, but does not have the result attribute.");
    	}
    	//restoreMaster
    	restoreTool.restoreMaster=this;
    }
    
    public Tool getRestore(){
    	return restoreTool;
    }
    public Tool getRestoreMaster(){
    	return restoreMaster;
    }

    public void initSave() throws ConfigException{
    	if (saveString==null) return;
    	saveTool=config.getContextManager().findTool(saveString);
    	if (saveTool == null) {
    		throw new ConfigException("Save tool '" + saveString +
    				"' of tool '" + name + 
    				"' is absent");
    	}
    	if (saveTool.saveMaster!=null){ // verify they have the same result
    		throw new ConfigException("Same save tool ("+saveTool.getName()+") for multiple master tools: " +
    				saveTool.saveMaster.getName() + " and "+this.getName()+" - save tools should differ.");
    	}
    	if (resultString==null){
    		throw new ConfigException("Tool "+getName()+" has save='"+saveTool.getName()+
    				"' defined, but does not have the result attribute.");
    	}
    	saveTool.saveMaster=this;
    }

    public Tool getSave(){
    	return saveTool;
    }
    public Tool getSaveMaster(){
    	return saveMaster;
    }
    
    
    
    public void initLogDir() throws ConfigException{
    	if (logDirString==null) return;
    	logDir=findParam(logDirString);
        if(logDir == null) {
            throw new ConfigException("Parameter log-dir='" + logDirString + 
                                      "' used for tool '" + name + 
                                      "' is absent");
        } else if(!(logDir.getType() instanceof ParamTypeString)) {
            throw new ConfigException("Parameter log-dir='" + logDirString + 
            		"' defined in "+logDir.getSourceXML()+" used for tool '" + name + 
                                      "' must be of type '" + ParamTypeString.NAME + 
                                      "'");
        } else {
        	KIND kind=((ParamTypeString) logDir.getType()).getKind();
        	if(kind != ParamTypeString.KIND.DIR) {
        		throw new ConfigException("Parameter log-dir='" + logDirString + 
        				"' of type '" + ParamTypeString.NAME +
        				"' defined in "+logDir.getSourceXML()+" used for tool '" + name + 
        				"' used for tool '" + name + 
        				"' must be of kind '" + ParamTypeString.KIND_DIR_ID + "'"+
        				" (it is '"+kind+"')");                   
        	}
        }
    }

    public void initStateDir() throws ConfigException{
    	if (stateDirString==null) return;
    	stateDir=findParam(stateDirString);
        if(stateDir == null) {
            throw new ConfigException("Parameter log-dir='" + stateDirString + 
                                      "' used for tool '" + name + 
                                      "' is absent");
        } else if(!(stateDir.getType() instanceof ParamTypeString)) {
            throw new ConfigException("Parameter state-dir='" + stateDirString + 
            				"' defined in "+stateDir.getSourceXML()+" used for tool '" + name + 
                                      "' must be of type '" + ParamTypeString.NAME + 
                                      "'");
        } else {
        	KIND kind=((ParamTypeString) stateDir.getType()).getKind();
        	if(kind != ParamTypeString.KIND.DIR) {
        		throw new ConfigException("Parameter state-dir='" + stateDirString + 
        				"' of type '" + ParamTypeString.NAME +
           				"' defined in "+stateDir.getSourceXML()+" used for tool '" + name + 
        				"' must be of kind '" + ParamTypeString.KIND_DIR_ID + "'"+
        				" (it is '"+kind+"')");                    
        	}
        }
    }
    public String getLogDir() {return getLogDir(true); } 
    public String getLogDir(boolean first) {
    	if (logDir!=null) { // has logDir specified, but may be empty
        	List<String> value=logDir.getValue(null); // null for topFormatProcessor
        	if (value.size()==0) return null; // overwrites with empty
        	return value.get(0);
    	}
    	if (!first) return null ; // prevent loops
    	if (restoreMaster != null) return restoreMaster.getLogDir(false);
    	if (saveMaster != null)    return saveMaster.getLogDir(false);
    	return null;
    }
    
    public String getStateDir() {return getStateDir(true); } 
    public String getStateDir(boolean first) {
    	if (stateDir!=null) { // has stateDir specified, but may be empty
        	List<String> value=stateDir.getValue(null); // null for topFormatProcessor
        	if (value.size()==0) return null; // overwrites with empty
        	return value.get(0);
    	}
    	if (!first) return null ; // prevent loops
    	if (restoreMaster != null) return restoreMaster.getStateDir(false);
    	if (saveMaster != null)    return saveMaster.getStateDir(false);
    	return null;
    }

    public String getStateFile() {return getStateFile(true); } // With timestamp or as specified in resultFile
    public String getStateFile(boolean first) {
    	if (resultFile!=null) return resultFile;
    	List<String> names=	getResultNames();
    	if (names!=null) {
        	if (names.size()==0) return null; 
        	String stamp=getTimeStamp();
        	if (stamp==null){
        		return null;
        	}
    		return ToolLogFile.insertTimeStamp(names.get(0),getTimeStamp());
    	}
    	if (!first) return null ; // prevent loops
    	if (restoreMaster != null) return restoreMaster.getStateFile(false);
    	if (saveMaster != null)    return saveMaster.getStateFile(false);
    	return null;
    }
    
	/**
	 * Set tool timestamp from the command timestamp (set when user launches the sequence)
	 */
	public void setTimeStamp(){
    	DEBUG_PRINT(getName()+".setTimeStamp()");
		setTimeStamp(SelectedResourceManager.getDefault().getBuildStamp()); // sometimes (in the beginning) it is null???
    }
	
	public boolean alreadyRan(){
		return (timeStampRan!=null) && (timeStampRan.equals(SelectedResourceManager.getDefault().getBuildStamp()));
	}
	
	public void setRestoreTimeStamp(){
    	restoreTimeStamp=SelectedResourceManager.getDefault().getBuildStamp();
    }
	
    public void setTimeStamp(String timeStamp){
    	DEBUG_PRINT(getName()+".setTimeStamp("+timeStamp+")");
    	this.timeStamp=timeStamp;
    }
    
    public String getTimeStamp() {return getTimeStamp(true); } // With timestamp or as specified in resultFile
    public String getTimeStamp(boolean first){
    	if (timeStamp!=null) return timeStamp;
    	if (!first) return null ; // prevent loops
    	if (restoreMaster != null) return restoreMaster.getTimeStamp(false);
    	if (saveMaster != null)    return saveMaster.getTimeStamp(false);
    	return null;
    }

    
    /**
     * Use to compare dependencies, when the snapshot is restored, it will have have restore time (latest),
     * and the timeStamp (and getStateFile) will still use original one (for hash calculation)
     * @return restore time stamp (if available), otherwise just a timestamp. Will look at restoreMaster.
     */
    public String getRestoreTimeStamp() {return getRestoreTimeStamp(true); } // With timestamp or as specified in resultFile
    public String getRestoreTimeStamp(boolean first){
    	if (first && (restoreMaster != null)) return restoreMaster.getRestoreTimeStamp(false);
    	if (restoreTimeStamp!=null) return restoreTimeStamp;
    	if (timeStamp!=null) return timeStamp;
    	return null;
    }

    
    public String getStateLink() {return getStateLink(true); } // No timestamp, link name (or null)
    public String getStateLink(boolean first) {
    	List<String> names=	getResultNames();
    	if (names!=null) {
        	if (names.size()==0) return null; 
    		return names.get(0);
    	}
    	if (!first) return null ; // prevent loops
    	if (restoreMaster != null) return restoreMaster.getStateLink(false);
    	if (saveMaster != null)    return saveMaster.getStateLink(false);
    	return null;
    }
    
    
    public void setResultFile(String filename){
    	resultFile=filename;
    }

    public String getResultName(){ // does not look at masters
    	List<String> names=	getResultNames();
    	if ((names==null) || (names.size()==0)) return null;
    	return names.get(0);
    }
    
    /**
     * Save tool state as project persistent properties
     * @param project where to attach properties
     */
    public void saveState(IProject project) {
    	QualifiedName qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_PINNED);
    	try {project.setPersistentProperty(qn, new Boolean(isPinned()).toString());}
    	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+isPinned()+", e="+e);}
    	qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_STATE);
    	try {project.setPersistentProperty(qn, getState().toString());}
    	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+getState()+", e="+e);}
        if (getTimeStamp()!=null) {
        	qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_TIMESTAMP);
        	try {project.setPersistentProperty(qn, getTimeStamp());}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+getTimeStamp()+", e="+e);}

        }
        if (getLastRunHash()!=0)  {
        	qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_LASTRUNHASH);
        	try {project.setPersistentProperty(qn, new Integer(getLastRunHash()).toString());}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+getLastRunHash()+", e="+e);}
        }
        for(String state:dependStatesTimestamps.keySet()){
        	qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_DEPSTATE+state);
        	try {project.setPersistentProperty(qn, dependStatesTimestamps.get(state));}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+dependStatesTimestamps.get(state)+", e="+e);}
        }
        for(String file:dependFilesTimestamps.keySet()){
        	qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_DEPFILE+file);
        	try {project.setPersistentProperty(qn, dependFilesTimestamps.get(file));}
        	catch (CoreException e)  {System.out.println(project+"Failed setPersistentProperty("+qn+", "+dependFilesTimestamps.get(file)+", e="+e);}
        }
        DEBUG_PRINT("*** Updated persistent properties for tool "+getName()+ "  in the project "+project.toString());
    }

    public void restoreState(IProject project) {
    	Map<QualifiedName,String> pp;    
    	try {
    	pp=project.getPersistentProperties();
    	} catch (CoreException e){
    		System.out.println(project+": Failed getPersistentProperties(), e="+e);
    		return;
    	}
    	DEBUG_PRINT("restoring "+getName()+" state for project "+project);
    	if (getState()!=TOOL_STATE.KEPT_OPEN) { // do not 
    		QualifiedName qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_PINNED);
    		String str=	pp.get(qn);
    		if (str!=null)	try {
    			setPinned(Boolean.parseBoolean(str));
    		} catch (Exception e){
    			System.out.println(project+"Failed setPinned(), e="+e);
    		}
    		qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_STATE);
    		str=	pp.get(qn);
    		if (str!=null) {
    			try {
    				setStateJustThis(TOOL_STATE.valueOf(str));
    			} catch (IllegalArgumentException e){
    				System.out.println("Invalid tool state: "+str+" for tool "+getName()+" in memento");
    			}
    			if (getState()==TOOL_STATE.KEPT_OPEN)
    				setStateJustThis(TOOL_STATE.NEW);
    			// See if console with this name is open    		
    		}
    		qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_TIMESTAMP);
    		str=	pp.get(qn);
    		if (str!=null)	setTimeStamp(str);

    		qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_LASTRUNHASH);
    		str=	pp.get(qn);
    		if (str!=null)	{
    			try {
    				Integer hc=Integer.parseInt(str);
    				setLastRunHash(hc);
    			} catch (Exception e){
    				System.out.println("Invalid hashCode: "+str+" for tool "+getName()+" for project "+project);
    			}
    		}
    		clearDependStamps();
    		String statePrefix=PROJECT_TOOL_NAME+name+PROJECT_TOOL_DEPSTATE;
    		String filePrefix= PROJECT_TOOL_NAME+name+PROJECT_TOOL_DEPFILE;
    		for (QualifiedName qName: pp.keySet()){
    			//        	qn= new QualifiedName(VDT.ID_VDT, PROJECT_TOOL_NAME+name+PROJECT_TOOL_DEPSTATE+state);
    			if (qName.getLocalName().startsWith(statePrefix)){
    				String value=pp.get(qName);
    				setStateTimeStamp(qName.getLocalName().substring(statePrefix.length()), value);
    			}
    			if (qName.getLocalName().startsWith(filePrefix)){
    				String value=pp.get(qName);
    				setFileTimeStamp(qName.getLocalName().substring(filePrefix.length()), value);
    			}
    		}
    	} else {
        	DEBUG_PRINT("Do not update state of the open session  "+getName()+" for project "+project);
    	}
    }
    
    
    public void saveState(IMemento memento) {
    	IMemento toolMemento= memento.createChild(MEMENTO_TOOL_TYPE,name);
    	toolMemento.putBoolean(MEMENTO_TOOL_PINNED, new Boolean(pinned));
    	toolMemento.putString(MEMENTO_TOOL_STATE,  this.state.toString());
        if (getTimeStamp()!=null) toolMemento.putString(MEMENTO_TOOL_TIMESTAMP, getTimeStamp());
        if (getLastRunHash()!=0)  toolMemento.putInteger(MEMENTO_TOOL_LASTRUNHASH, getLastRunHash());
        IMemento depMemento;
        for(String state:dependStatesTimestamps.keySet()){
        	depMemento=toolMemento.createChild(MEMENTO_TOOL_STATEDEPSTAMP);
        	depMemento.putString(MEMENTO_TOOL_DEPNAME, state);
        	depMemento.putString(MEMENTO_TOOL_TIMESTAMP, dependStatesTimestamps.get(state));
//        	DEBUG_PRINT("state="+state+" depMemento="+ depMemento.toString());
        }
        for(String file:dependFilesTimestamps.keySet()){
        	depMemento=toolMemento.createChild(MEMENTO_TOOL_FILEDEPSTAMP);
        	depMemento.putString(MEMENTO_TOOL_DEPNAME, file);
        	depMemento.putString(MEMENTO_TOOL_TIMESTAMP, dependFilesTimestamps.get(file));
//        	DEBUG_PRINT("file="+file+" depMemento="+ depMemento.toString());
        }
        DEBUG_PRINT("*** Updated memento for tool "+name+ "  memento="+toolMemento.toString());
        
    }

    public void restoreState(IMemento memento) {
    	IMemento[] toolMementos=memento.getChildren(MEMENTO_TOOL_TYPE);
    	IMemento toolMemento=null;
    	for (IMemento tm:toolMementos){
    		if (tm.getID().equals(name)){
    			toolMemento=tm;
    			break;
    		}
    	}
    	if (toolMemento==null){
    		System.out.println("No memento data for tool "+name);
    		return;
    	}
//    	System.out.println("Got memento for "+name+": "+toolMemento.toString());
    	Boolean pinned=toolMemento.getBoolean(MEMENTO_TOOL_PINNED);
    	if (pinned!=null) this.pinned=pinned;
    	String state=toolMemento.getString(MEMENTO_TOOL_STATE);
    	if (state!=null){
    		try {
    			setStateJustThis(TOOL_STATE.valueOf(state));
    		} catch (IllegalArgumentException e){
    			System.out.println("Invalid tool state: "+state+" for tool "+name+" in memento");
    		}
    		if (getState()==TOOL_STATE.KEPT_OPEN)
    			setStateJustThis(TOOL_STATE.NEW);
    	}
    	String timestamp=toolMemento.getString(MEMENTO_TOOL_TIMESTAMP);
    	if (timestamp!=null) setTimeStamp(timestamp);
    	Integer hc=toolMemento.getInteger(MEMENTO_TOOL_LASTRUNHASH);
    	if (hc!=null) lastRunHash=hc;
    	clearDependStamps();
    	IMemento[] mementoStates=toolMemento.getChildren(MEMENTO_TOOL_STATEDEPSTAMP);
    	for (IMemento ms:mementoStates){
    		String depName=ms.getString(MEMENTO_TOOL_DEPNAME);
    		String value=ms.getString(MEMENTO_TOOL_TIMESTAMP);
    		if ((depName==null) || (value==null)){
    			System.out.println("problem reading memento data for "+name+":" +
    					MEMENTO_TOOL_STATEDEPSTAMP+": got name="+depName+" stamp="+value);
    		} else {
    			setStateTimeStamp(depName, value);
  //  			DEBUG_PRINT("Got memento data for "+name+":" +	MEMENTO_TOOL_STATEDEPSTAMP+": name="+depName+" stamp="+value);
    		}
    	}
    	
    	 mementoStates=toolMemento.getChildren(MEMENTO_TOOL_FILEDEPSTAMP);
     	for (IMemento ms:mementoStates){
     		String depName=ms.getString(MEMENTO_TOOL_DEPNAME);
     		String value=ms.getString(MEMENTO_TOOL_TIMESTAMP);
     		if ((depName==null) || (value==null)){
     			System.out.println("problem reading memento data for "+name+":" +
     					MEMENTO_TOOL_FILEDEPSTAMP+": got name="+depName+" stamp="+value);
     		} else {
     			setFileTimeStamp(depName, value);
//    			DEBUG_PRINT("Got memento data for "+name+":" + MEMENTO_TOOL_FILEDEPSTAMP+": name="+depName+" stamp="+value);
     		}
     	}
    }
    
    public void checkBaseTool() throws ConfigException {
        if(baseToolName != null) {
            ControlInterface baseToolInterface = baseTool.getControlInterface();
            String baseToolInterfaceName = baseTool.getControlInterface().getName();
            
            if(!controlInterface.isInheritedFrom(baseToolInterface))
                throw new ConfigException(
                        "Tool '" + name + 
                        "' inherited from tool '" + baseToolName + 
                        "' has control interface '" + controlInterfaceName +
                        "' that is not inherited from control interface '" + baseToolInterfaceName +
                        "' of the base tool");
        }
    }
    
    public Object clone() {
        Tool newTool = null;

        try {
            newTool = (Tool)super.clone();
        } catch (CloneNotSupportedException e) {
            assert false;
        }
        
        return newTool; 
    }

    public String getExeName() { // seems to be running in 2 parallel processes!!
    	if ((absoluteExeName !=null) && !absoluteExeName.substring(0,1).equals("/")){
    		String path=System.getenv("PATH");
    		if (path!=null){ // Linux
//    			System.out.println("PATH=\""+path+"\"");
    			String [] dirs = path.split(":");
    			for (int i=0;i<dirs.length;i++){
    				File file=new File(dirs[i]+"/"+absoluteExeName);
    				if (file.isFile() && file.exists()){
    					absoluteExeName=dirs[i]+"/"+absoluteExeName;
    					break;
    				}
    			}
    		}
    	}
    	
        if(locationSet) // true
            return absoluteExeName; // bash
        
        return getResolvedExeName();
    }

    public boolean getIsShell() {
    	return isShell;
    }

    public String getToolProjectPath() {
    	return projectPath;
    }
    public void setToolProjectPath(String projectPath) {
    	this.projectPath=projectPath;
    }
    
    public void setLocation(String path) {        
        String fileName = (new File(getResolvedExeName())).getName();
        
        if ((path != null) && (path.length() > 0))
            absoluteExeName = path + File.separator + fileName;
        else
            absoluteExeName = fileName;
        
        locationSet = true;
    }

    /* Patterns for Error Parser */
    
    public String getPatternErrors() {
    	return this.toolErrors;
    }

    public String getPatternWarnings() {
    	return this.toolWarnings;
    }

    public String getPatternInfo() {
    	return this.toolInfo;
    }
    
    public String[] getExtensions() {
        if(extensions == null)
            return null;
    	FormatProcessor topProcessor=new FormatProcessor(this);
        FormatProcessor processor = new FormatProcessor(
                                            new Recognizer[] {
                                            		new ContextParamRecognizer(this,topProcessor),
//                                                    new SimpleGeneratorRecognizer() // Andrey: Trying
                                            		},topProcessor); // null for topFormatProcessor - this generator can not reference other parameters

        String[] actualExtensions = new String[extensions.size()];
        
        for(int i = 0; i < extensions.size(); i++) {
            List<String> ext = null;
            
            try {
                ext = processor.process(extensions.get(i));
            } catch(ToolException e) {
                assert false;
            }
            
            assert ext.size() == 1;            
            
            actualExtensions[i] = ext.get(0);
        }
        
        return actualExtensions;
    }
    
    public RunFor[] getMenuActions(IProject project) {
        if(runfor == null)
            return null;
        updateContextOptions(project); // Fill in parameters - it parses here too - at least some parameters? (not in menu mode)
// Can be two different processors for labels and resources 
        
//SimpleGeneratorRecognizer(true) may be not needed, as current file is already set here
    	FormatProcessor topProcessor=new FormatProcessor(this);
        FormatProcessor processor = new FormatProcessor(
                                            new Recognizer[] {
                                            		new ContextParamRecognizer(this,topProcessor),
                                                    new SimpleGeneratorRecognizer(true,topProcessor) // in menuMode
//                                                    new SimpleGeneratorRecognizer(false) // in menuMode
                                            		},topProcessor); // null for topFormatProcessor - this generator can not reference other parameters

        RunFor[] actualActions = new RunFor[runfor.size()];
        
        for(int i = 0; i < runfor.size(); i++) {
            List<String> labels = null;
            try {
                labels = processor.process(runfor.get(i).getLabel());
            } catch(ToolException e) {
                assert false;
            }
            assert labels.size() == 1;            
            List<String> resources = null;
            String resource=null;
            try {
            	resources = processor.process(runfor.get(i).getResource());
            } catch(ToolException e) {
// OK to be null;
            }
            if (resources!=null) {
            	assert ((resources==null) || (resources.size() == 1));
            	resource = resources.get(0);
            }
            actualActions[i] = new RunFor(
            		labels.get(0),
            		resource,
            		runfor.get(i).getCheckExtension(),
            		runfor.get(i).getCheckExistence(),
            		runfor.get(i).getIconName());
        }
        return actualActions;
    }
    // Should be called after getMenuActions to have updateContextOptions() already ran
    public String getIgnoreFilter(){ // calculate and get
    	if (ignoreFilter==null) return null;
    	FormatProcessor topProcessor=new FormatProcessor(this);
        FormatProcessor processor = new FormatProcessor(
                new Recognizer[] {
                		new ContextParamRecognizer(this,topProcessor),
                        new SimpleGeneratorRecognizer(true,topProcessor) // in menuMode
//                        new SimpleGeneratorRecognizer(false) // in menuMode
                		},topProcessor); // null for topFormatProcessor - this generator can not reference other parameters
        List<String> results=null;
        try {
			results=processor.process(ignoreFilter);
        } catch (ToolException e) {
        	return null;
        }
    	if ((results == null) || (results.size()==0)) return null;
    	return results.get(0);
    }
    
//    private void updateContextOptions (IProject project){
    public void updateContextOptions (IProject project){ // public to be able to update parameters before setting "dirty" flags
    	DEBUG_PRINT("~~~updateContextOptions(project) for tool "+getName());
        PackageContext packageContext = getParentPackage();
        if (packageContext != null) {
            OptionsCore.doLoadContextOptions(packageContext);
/*            
            try {
            	packageContext.buildParams();
            } catch (ToolException e) { // Do nothing here
            	System.out.println("updateContextOptions ToolException for Package Context="+e.getMessage());
            } catch (NullPointerException e) { // Do nothing here Or make it "finally" to ignore all parameters parsing here?
            	System.out.println("updateContextOptions NullPointerException for Package Context="+e.getMessage());
            } finally {
            	System.out.println("updateContextOptions for Package Context - other error");
            }
*/            
        }
        Context context = getParentProject();
        if (context != null) {
            OptionsCore.doLoadContextOptions(context, project);
/*            
            try {
            	context.buildParams();
            } catch (ToolException e) { // Do nothing here Or make it "finally" to ignore all parameters parsing here?
            	System.out.println("updateContextOptions ToolException for Project Context="+e.getMessage());
            } catch (NullPointerException e) { // Do nothing here Or make it "finally" to ignore all parameters parsing here?
            	System.out.println("updateContextOptions NullPointerException for Project Context="+e.getMessage());
            } finally {
            	System.out.println("updateContextOptions for Project Context - other error");
            }
*/            
        }
        //NullPointerException
        OptionsCore.doLoadContextOptions(this, project);
/*        
        try {
        	buildParams();
        } catch (ToolException e) { // Do nothing here
        	System.out.println("updateContextOptions ToolException for Tool Context="+e.getMessage());
        } catch (NullPointerException e) { // Do nothing here Or make it "finally" to ignore all parameters parsing here?
        	System.out.println("updateContextOptions NullPointerException for Tool Context="+e.getMessage());
        } finally {
        	System.out.println("updateContextOptions for Tool Context - other error");
        }
*/        
    }
    
    
    
    public List<Parameter> getParams() {
        return paramContainer.getParams();
    }
    
    public PackageContext getParentPackage() {
        return parentPackage;
    }

    public ProjectContext getParentProject() {
        return parentProject;
    }
    
    public Parameter findParam(String paramID) {
//    	System.out.println("findParam("+paramID+")");
//    	if (paramID==null){
//    		System.out.println("findParam(null!!!)");
//    		return null;
//    	}
    	
        Parameter param = super.findParam(paramID); //Andrey: happily finds ProjectContext parameter, thinks it is tool context
/*
 * Andrey: Added isChild property to the Property, and still left static inheritance at XML parsing time. Then, during parameter
 * processing that inheritance is ignored
 */
//        if(param != null) // Was before the change described above 
/*        
        if ((param != null) &&(param.getID().equals("SimulationTopFile"))){ // Andrey
        	System.out.println("Initializing parameter SimulationTopFile, isChild="+param.getIsChild());
        }
 */       
        
        if ((param != null) && !param.getIsChild())  
            return param;
        
        if(baseTool != null) {
            param = baseTool.findParam(paramID);
        } else {
            if(parentProject != null) {
                param = parentProject.findParam(paramID);
            } else if(parentPackage != null) {
                param = parentPackage.findParam(paramID);
            } else {
                InstallationContext installation = config.getContextManager().getInstallationContext();
                    
                if(installation != null)
                    param = installation.findParam(paramID);
            }
        }
        
        return param; 
    }

    public Inheritable getBase() {
        return baseTool;
    }
    
//    public String[] buildParams() throws ToolException {
    public BuildParamsItem[] buildParams() throws ToolException {
    	return buildParams(false);
    }
    
      public BuildParamsItem[] buildParams(boolean dryRun) throws ToolException {
    	  DEBUG_PRINT("buildParams("+dryRun+"): tool "+getName()+" state="+getState()+" dirty="+isDirty()+" hashMatch()="+hashMatch()+" pinned="+isPinned());
    	
        if(parentPackage != null)
            parentPackage.buildParams();

        if(parentProject != null)
            parentProject.buildParams();
        
        InstallationContext installation = config.getContextManager().getInstallationContext();
        
        if(installation != null)
            installation.buildParams();
        
        return super.buildParams(dryRun);
    }
    
    
    protected List<String> buildCommandString(String paramStringTemplate, FormatProcessor topProcessor)
        throws ToolException
    {
        if (topProcessor==null) topProcessor=new FormatProcessor(this);
        else topProcessor.setCurrentTool(this);
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                            new ToolParamRecognizer(this,topProcessor),
//                                                            new SimpleGeneratorRecognizer(),
//                                                            new SimpleGeneratorRecognizer(this),
                                                            new SimpleGeneratorRecognizer(topProcessor),
                                                            new RepeaterRecognizer(),
                                                            new ContextParamRecognizer(this,topProcessor),
                                                            new ContextParamRepeaterRecognizer(this)
                                                        }, topProcessor); // topFormatProcessor=null: this value will not be used as other parameter value
                            
        return processor.process(paramStringTemplate);
    }
    
    //
    // below comes various non-public initialization stuff
    //
    
    protected void initParams() throws ConfigException {
/*
 *  Seems that inheritance is implemented twice - here and when generating output lines, and the parameters are cloned, so when say
 *  project parameters are modified, the changes are not propagated to the tool parameters. I'll try to disable inheritance here,
 *  see what happens (this intermediate inheritance may be still needed somewhere - exe name, extensions?). Another option would be to reference instead of cloning
 *  and skip initialization of the "alien" context parameters.
 *  
 *  Yes,  exe name wants it        	
 */
        if(parentProject != null)
            inheritParams(parentProject);    // Here inheriting project parameters    

        if(parentPackage != null)
            inheritParams(parentPackage);
            
        //
        // should we inherit params from installation context?
        //
        
        if(baseTool != null) {
            assert baseTool.initialized;
            
            // inherit stuff from the base tool
            // note that the base tool is considered to be initialized
            // as well as its params

            inheritParams(baseTool);
            inheritParamGroups();
        }
        paramContainer.init(config, this); // Error inside here when context is cloned in inheritance. Parameter ('SimulationTopFile') cannot be re-initialized
        initParamGroups();
    }
    
    protected void initCommandLines() throws ConfigException {
        if(baseTool != null) {
            assert baseTool.initialized;
            
            inheritCommandLines();
        }
        
        super.initCommandLines();
    }    

    private void initParentPackage() throws ConfigException {
        if(parentPackageName != null) {
            parentPackage = 
                (PackageContext)config.getContextManager().findContext(parentPackageName);
            
            if(parentPackage == null) {
                throw new ConfigException("Parent package context '" + parentPackageName + 
                                          "' of tool '" + name + 
                                          "' is absent");
            } else if(!ControlInterface.isInheritedOrSame(parentPackage.getControlInterface(), controlInterface)) { 
                throw new ConfigException("Control interface of parent package context '" + parentPackageName + 
                                          "' of tool '" + name + 
                                          "' is neither equal to nor base of control interface of the tool");
            }
        }
    }

    private void initParentProject() throws ConfigException {
        if(parentProjectName == null)
            return;
        
        parentProject = 
            (ProjectContext)config.getContextManager().findContext(parentProjectName);
            
        if(parentProject == null) {
            throw new ConfigException("Parent project '" + parentProjectName + 
                                      "' of tool '" + name + 
                                      "' is absent");
        } else if(!ControlInterface.isInheritedOrSame(parentProject.getControlInterface(), controlInterface)) { 
            throw new ConfigException("Control interface of parent project '" + parentProjectName + 
                                      "' of tool '" + name + 
                                      "' is neither equal to nor base of control interface of the tool");
        } else {
            if(parentPackage == null) {
                if(parentProject.getParentPackage() != null)
                    throw new ConfigException(
                            "Parent project '" + parentProjectName + 
                            "' of tool '" + name + 
                            "' refers to package '" + parentProject.getParentPackage().getName() + 
                            "', but the tool doesn't refer to a package");
            } else if(parentProject != null) {
                if(parentProject.getParentPackage() == null)
                    throw new ConfigException(
                            "Parent project '" + parentProjectName + 
                            "' of tool '" + name + 
                            "' doesn't refer to any package, but the tool refers to package '" +
                            parentPackageName + "'");
                else if(!parentProject.getParentPackage().getName().equals(parentPackageName))
                    throw new ConfigException(
                            "Parent project '" + parentProjectName + 
                            "' of tool '" + name + 
                            "' refers to package '" + parentProject.getParentPackage().getName() + 
                            "', but the tool refers to package '" + parentPackageName +
                            "'");
            }
        }
    }
    
    private void initOtherAttributes() {
        if(inputDialogLabel == null) {
            if(baseTool != null && baseTool.inputDialogLabel != null)
                inputDialogLabel = baseTool.inputDialogLabel;
        }        
    }    
    
    private void inheritParams(Context context) throws ConfigException {
//      EntityUtils.update(context.getParams(), paramContainer.getParams());
        EntityUtilsMarkChildren.update(context.getParams(), paramContainer.getParams());
    }

    private void inheritParamGroups() throws ConfigException {
        EntityUtils.update(baseTool.paramGroups, paramGroups);
// Sort  paramGroups according to weight
        boolean inOrder=false;
        while (!inOrder){
        	inOrder=true;
        	for (int i=0;i<(paramGroups.size()-1);i++){
        		if (paramGroups.get(i).getWeight()>paramGroups.get(i+1).getWeight()){
        			paramGroups.add(i,paramGroups.remove(i+1));
        			inOrder=false;
        		}
        	}
        }
    }

    private void inheritCommandLines() throws ConfigException {
    	DEBUG_PRINT("inheritCommandLines(), baseTool="+baseTool.getName()+", this="+getName());
    	if (getName().equals("VivadoSynthesis")){
//    		MessageUI.error("inheritCommandLines() for "+getName());
    	}
        EntityUtils.update(baseTool.commandLinesBlocks, commandLinesBlocks);
    }
    
    private void initBaseTool() throws ConfigException {
        if(baseToolName != null) {
            baseTool = config.getContextManager().findTool(baseToolName);
        
            if(baseTool == null)
                throw new ConfigException("Base tool '" + baseToolName +
                                          "' of tool '" + name + 
                                          "' is absent");          
        }
    }
    
    //
    // other stuff
    //
    
    private String getResolvedExeName() {
        return ConditionUtils.resolveContextCondition(this, exeName, null); // null for topFormatProcessor 
    }
/*    
    private String getResolvedShellName() {
        return ConditionUtils.resolveContextCondition(this, shellName);
    }
*/    
}
