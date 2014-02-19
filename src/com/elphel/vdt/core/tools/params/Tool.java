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
import org.eclipse.swt.widgets.Display;
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
import com.elphel.vdt.ui.VDTPluginImages;
import com.elphel.vdt.ui.views.DesignFlowView;
import com.elphel.vdt.ui.variables.SelectedResourceManager;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;


public class Tool extends Context implements Cloneable, Inheritable {
    private static final String ICON_ID_PREFIX = VDT.ID_VDT + ".Tool.Image.";
    private static final String ICON_ID_ACTION = ".action.";
    private static final String TAG_TOOL_PINNED = ".toolstate.pinned";
    private static final String TAG_TOOL_STATE =  ".toolstate.state";
    private static final String TAG_TOOL_TIMESTAMP =  ".toolstate.timeStamp";


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
    
    private List<String> depends=null;       // list of tools this one depends on -> list of files (states) and strings (name of sessions)
    private String logDirString=null;              // directory to store this tool log files
    private String stateDirString=null;            // directory to store this tool log files
    private Parameter logDir=null;              // directory to store this tool log files
    private Parameter stateDir=null;            // directory to store this tool log files
    private String disabledString=null;          // to disable tools from automatic running
    private String resultString=null;              // parameter name of kind of file that represents state after running this tool
    private String restoreString=null;             // name of tool that restores the state of this tool ran (has own dependencies)
    private String saveString=null;             // name of tool that saves the state of this tool run 
    private String autoSaveString=null;         // name of boolean that turns on/off auto-save after the tool run
    private boolean abstractTool;               // abstract tools can only be used for inheritance, not directly (so they will not be
                                                // considered when looking for solution to run
 
    private Parameter disabled;
    private Tool restoreTool;
    private Parameter result;
    
    // TODO: Compare dependFiles with literary result of other tools, if match - these are states, not files
    private List<Parameter> dependSessions;
    private List<Parameter> dependFiles;
//    private boolean toolIsRestore;           // this tool is referenced by other as restore="this-tool"
    private boolean dirty=false;             // tool ran before its sources (runtime value)
    private boolean pinned=false;             // tool ran before its sources (runtime value)
    
    private long runStamp=0;                 // timestamp of the tool last ran (0 - never)
    private TOOL_STATE state=TOOL_STATE.NEW; // tool state (succ, fail,new, running)
//    private boolean running=false;
//    private long finishTimeStamp=0;
    private String timeStamp=null;
    private DesignFlowView designFlowView;
    
    private String resultFile;               // used to overwrite name of the default result file that normally
                                             // is calculated from result and timestamp;
    
    private String openState=null;           // (only for open sessions) - last successful result ran in this session
    private Tool openTool=null;              // (only for open sessions) - tool last successful result ran in this session (null if none/failed)
    private Tool restoreMaster;              // Tool, for which this one is restore (or null if for none). Same restore for
    										 // multiple masters is not allowed
    private Tool saveMaster;                 // Tool, for which one this is used to save state (should have only one master)
    									     // with inheritance "disabled" will be taken from master
    private Tool saveTool;
    private Parameter autoSave;                // automatically run saveTool after successful completion of this one
    
    private TOOL_MODE runMode;
    private TOOL_MODE lastRunMode;           // last running (not STOP) mode
    

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
                List<String> depends,
                String logDirString,
                String stateDirString,
                String disabledString,          // to disable tools from automatic running
                String resultString,            // parameter name of kind of file that represents state after running this tool
                String restoreString,           // name of tool that restores the state of this tool ran (has own dependencies)
                String saveString,              // name of tool that saves the state of this tool run 
                String autoSaveString,          // name of boolean that turns on/off auto-save after the tool run
                boolean abstractTool,
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
        this.depends=       depends;
        this.logDirString=        logDirString;
        this.stateDirString=      stateDirString;
        
        this.disabledString=      disabledString;          // to disable tools from automatic running
        this.resultString=        resultString;            // parameter name of kind of file that represents state after running this tool
        this.restoreString=       restoreString;           // name of tool that restores the state of this tool ran (has own dependencies)
        this.saveString=          saveString;             // name of tool that saves the state of this tool run 
        this.autoSaveString=      autoSaveString;         // name of boolean that turns on/off auto-save after the tool run
        this.abstractTool=        abstractTool;

        disabled=null;
        restoreTool=null;
        result=null;
        dependSessions=null;
        dependFiles=null;

        pinned=false;
        openState=null;
        openTool=null;
        choice=0;
        designFlowView =null;
        timeStamp=null;
        logDir=null;
        stateDir=null;
        resultFile=null;
        restoreMaster=null;
        saveMaster=null;
        saveTool=null;
        autoSave=null;
        runMode=TOOL_MODE.STOP;
        lastRunMode=TOOL_MODE.STOP;
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
        RUN,
        RESTORE,
        SAVE,
        PLAYBACK
    }
   
    public void setRunStamp(long runStamp) { this.runStamp=runStamp; }
    public List<String> getDepends()       { return depends; }
    public boolean isDirty()               { return dirty; }
//    public boolean isRunning()             { return running; }
    public boolean isRunning()             { return runMode!=TOOL_MODE.STOP; }
    public TOOL_MODE getMode()             { return runMode; }
    public TOOL_MODE getLastMode()         { return lastRunMode; }
    

    public long getRunStamp()              { return runStamp; }
    public TOOL_STATE getState()           { return state; }
    public boolean isPinned()              { return pinned; }
    public String getOpenState()           { return openState; }
    public void setOpenState(String stateName) { openState=stateName;}

    public Tool getOpenTool()              { return openTool; }
    public void setOpenTool(Tool openTool) { this.openTool=openTool;}
    
    public void setTimeStamp(){
    	timeStamp=SelectedResourceManager.getDefault().getBuildStamp();
    }
    
    public String getFinishTimeStamp(){
    	return timeStamp;
    }
    
    public void setDirty(boolean dirty) {
    	this.dirty=dirty;
//    	toolFinished();
    	System.out.println("SetDirty("+dirty+")");
    }
    public void setPinned(boolean pinned) {
    	this.pinned=pinned;
//    	toolFinished();
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
    		System.out.println("SetPinned("+pinned+")");
    	}
    }
    
    public void setMode(TOOL_MODE mode) {
    	runMode=mode;
    	if (mode!=TOOL_MODE.STOP)  lastRunMode=mode;
    	//    	toolFinished();
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
    		System.out.println("--->> "+name+": setMode("+mode+"), lastRunMode="+lastRunMode);
    	}
    }
    
    public void setState(TOOL_STATE state) {
    	this.state=state;
//    	toolFinished();
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
    		System.out.println("SetState("+state+")");
    	}
    }
    
    public void setDesignFlowView (DesignFlowView designFlowView){
    	this.designFlowView =  designFlowView; // to be able to update actions and so the state icons
    }

    public void toolFinished(){
    	designFlowView.getToolSequence().toolFinished(this);
/*    	
System.out.println("Tool "+getName()+" FINISHED - add more stuff here");    	
    	
    	
    	if (designFlowView!=null){
        	Display.getDefault().syncExec(new Runnable() {
        		public void run() {
            		designFlowView.updateLaunchAction(); // Run from Display thread to prevent "invalid thread access" when called from Runner
        		}
        	});
    	}
*/    	
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
    	if (depends==        null) depends =        baseTool.depends;
    	if (logDirString==   null) logDirString =   baseTool.logDirString;
    	if (stateDirString== null) stateDirString = baseTool.stateDirString;
    	if (disabledString== null) disabledString = baseTool.disabledString;
    	if (resultString==   null) resultString =   baseTool.resultString;
    	if (restoreString==  null) restoreString =  baseTool.restoreString;
    	if (saveString==     null) saveString =     baseTool.saveString;
    	if (autoSaveString== null) autoSaveString = baseTool.autoSaveString;
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
    	List<String> values=disabled.getValue();
    	if ((values==null) || (values.size()==0)) return false;
    	return (!values.get(0).equals("true"));
    }
    
    public void initDepends() throws ConfigException{
    	if (depends==null) return;
        dependSessions=new ArrayList<Parameter>();
        dependFiles=   new ArrayList<Parameter>();
    	for(Iterator<String> iter = depends.iterator(); iter.hasNext();) {
    		String paramID=iter.next();

    		Parameter param=findParam(paramID);
    		if(param == null) {
    			throw new ConfigException("Parameter depends='" + paramID + 
    					"' used for tool '" + name + 
    					"' is absent");
    		} else if(!(param.getType() instanceof ParamTypeString)) {
    			throw new ConfigException("Parameter depends='" + paramID + 
        				"' defined in "+param.getSourceXML()+" used for tool '" + name + 
    					"' must be of type '" + ParamTypeString.NAME + 
    					"'");                    
    		} else {
    			KIND kind=((ParamTypeString)param.getType()).getKind();
    			if (kind == ParamTypeString.KIND.FILE) {
    				dependFiles.add(param);
    			} else if (kind == ParamTypeString.KIND.TEXT) {
    				dependSessions.add(param);
    			} else {
    				throw new ConfigException("Parameter depends='" + paramID + 
    						"' of type '" + ParamTypeString.NAME +
    						"' defined in "+param.getSourceXML()+" used for tool '" + name + 
    						"' must be of kind '" + ParamTypeString.KIND_FILE_ID + "' (for snapshot fiels) "+
    						" or '" + ParamTypeString.KIND_TEXT_ID + "' (for tool name of the open session)"+
        				", it is '"+kind+"'");                   
    			}
    		}
    	}
    }
    public List<String> getDependFiles(){
    	if ((dependFiles == null) || (dependFiles.size()==0)) return null;
    	List<String> list = new ArrayList<String>();
    	for (Iterator<Parameter> iter= dependFiles.iterator(); iter.hasNext();) {
    		List<String> vList=iter.next().getValue();
    		if (vList!=null) list.addAll(vList);
    	}
    	return list;
    }

    public List<String> getDependSessions(){
    	if ((dependSessions == null) || (dependSessions.size()==0)) return null;
    	List<String> list = new ArrayList<String>();
    	for (Iterator<Parameter> iter= dependSessions.iterator(); iter.hasNext();) {
    		List<String> vList=iter.next().getValue();
    		if (vList!=null) list.addAll(vList);
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
        	if(kind != ParamTypeString.KIND.FILE) {
        		throw new ConfigException("Parameter result='" + resultString + 
        				"' of type '" + ParamTypeString.NAME +
        				"' defined in "+result.getSourceXML()+" used for tool '" + name + 
        				"' must be of kind '" + ParamTypeString.KIND_FILE_ID + "'"+
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
            		"' defined in "+result.getSourceXML()+" used for tool '" + name + 
                                      "' must be of type '" + ParamTypeBool.NAME + 
                                      "'");
        }
    }

    public boolean getAutoSave(){
    	if (autoSave==null) return false;
    	List<String>result=autoSave.getValue();
    	if (!result.isEmpty()) return result.get(0).equals("true");
    	return false;
    }
    
    public List<String> getResultNames(){
    	if (result==null) return null;
    	return result.getValue();
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
    	//saveMaster
    	saveTool.saveMaster=this;
    }

    public Tool getSave(){
    	return saveTool;
    }
    public Tool getSaveMaster(){
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
			System.out.println("getSaveMaster("+name+")-> "+((saveMaster==null)?"null":"NOT null"));
		}
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
        	List<String> value=logDir.getValue();
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
        	List<String> value=stateDir.getValue();
        	if (value.size()==0) return null; // overwrites with empty
        	return value.get(0);
    	}
    	if (!first) return null ; // prevent loops
    	if (restoreMaster != null) return restoreMaster.getStateDir(false);
    	if (saveMaster != null)    return saveMaster.getStateDir(false);
    	return null;
    }

    public String getStateFile() {return getStateFile(true); } 
    public String getStateFile(boolean first) {
    	if (resultFile!=null) return resultFile;
    	List<String> names=	getResultNames();
    	if (names!=null) {
        	if (names.size()==0) return null; 
    		return ToolLogFile.insertTimeStamp(names.get(0),SelectedResourceManager.getDefault().getBuildStamp());
    	}
    	if (!first) return null ; // prevent loops
    	if (restoreMaster != null) return restoreMaster.getStateFile(false);
    	if (saveMaster != null)    return saveMaster.getStateFile(false);
    	return null;
    }
    
    
    
    public void setResultFile(String filename){
    	resultFile=filename;
    }

    public String getResultName(){
    	List<String> names=	getResultNames();
    	if ((names==null) || (names.size()==0)) return null;
    	return names.get(0);
    }
    
    
    public void saveState(IMemento memento) {
        memento.putBoolean(name+TAG_TOOL_PINNED, new Boolean(pinned));
        memento.putString(name+TAG_TOOL_STATE, this.state.toString());
        if (timeStamp!=null) memento.putString(name+TAG_TOOL_TIMESTAMP, timeStamp);
    }
    
    public void restoreState(IMemento memento) {
    	Boolean pinned=memento.getBoolean(name+TAG_TOOL_PINNED);
    	if (pinned!=null) this.pinned=pinned;
    	String state=memento.getString(name+TAG_TOOL_STATE);
    	if (state!=null){
    		try {
    			this.state=TOOL_STATE.valueOf(state);
    		} catch (IllegalArgumentException e){
    			System.out.println("Invalid tool state: "+state+" for tool "+name+" in memento");
    		}
    		if (this.state==TOOL_STATE.KEPT_OPEN)
    			this.state=TOOL_STATE.NEW;
    	}
    	String timestamp=memento.getString(name+TAG_TOOL_TIMESTAMP);
    	if (timestamp!=null) this.timeStamp=timestamp;
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
/*    
    public void setIsShell(boolean isShell) {
    	this.isShell=isShell;
    }
*/    
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
        
        FormatProcessor processor = new FormatProcessor(
                                            new Recognizer[] {
                                            		new ContextParamRecognizer(this),
//                                                    new SimpleGeneratorRecognizer() // Andrey: Trying
                                            		});

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
        
        FormatProcessor processor = new FormatProcessor(
                                            new Recognizer[] {
                                            		new ContextParamRecognizer(this),
                                                    new SimpleGeneratorRecognizer(true) // in menuMode
//                                                    new SimpleGeneratorRecognizer(false) // in menuMode
                                            		});

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
        FormatProcessor processor = new FormatProcessor(
                new Recognizer[] {
                		new ContextParamRecognizer(this),
                        new SimpleGeneratorRecognizer(true) // in menuMode
//                        new SimpleGeneratorRecognizer(false) // in menuMode
                		});
        List<String> results=null;
        try {
			results=processor.process(ignoreFilter);
        } catch (ToolException e) {
        	return null;
        }
    	if ((results == null) || (results.size()==0)) return null;
    	return results.get(0);
    }
    
    private void updateContextOptions (IProject project){
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
        if(parentPackage != null)
            parentPackage.buildParams();

        if(parentProject != null)
            parentProject.buildParams();
        
        InstallationContext installation = config.getContextManager().getInstallationContext();
        
        if(installation != null)
            installation.buildParams();
        
        return super.buildParams();
    }
    
    protected List<String> buildCommandString(String paramStringTemplate)
        throws ToolException
    {
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                            new ToolParamRecognizer(this),
//                                                            new SimpleGeneratorRecognizer(),
                                                            new SimpleGeneratorRecognizer(this),
                                                            new RepeaterRecognizer(),
                                                            new ContextParamRecognizer(this),
                                                            new ContextParamRepeaterRecognizer(this)
                                                        });
                            
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
    }

    private void inheritCommandLines() throws ConfigException {
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
        return ConditionUtils.resolveContextCondition(this, exeName);
    }
/*    
    private String getResolvedShellName() {
        return ConditionUtils.resolveContextCondition(this, shellName);
    }
*/    
}
