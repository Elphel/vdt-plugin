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
package com.elphel.vdt.core.launching;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IStreamMonitor;
//import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.console.IConsole;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;


/**
 * Holder for various arguments passed to a Verilog development tool runner.
 * Mandatory parameters are passed in the constructor; optional arguments, 
 * via setters.
 * 
 * Created: 22.12.2005
 * @author  Lvov Konstantin
 *
 * @see org.eclipse.jdt.launching.VMRunnerConfiguration
 */

public class VDTRunnerConfiguration {
	public class MonListener{
		private IStreamMonitor monitor;
		private IStreamListener listener;
		public IStreamMonitor getMonitor() {
			return monitor;
		}
		public IStreamListener getListener() {
			return listener;
		}
		public MonListener(IStreamMonitor monitor, IStreamListener listener) {
			super();
			this.monitor = monitor;
			this.listener = listener;
		}
		public void finalize() throws Throwable{
			if ((monitor!=null) && (listener!=null)){
				monitor.removeListener(listener);
			}
			monitor=null;
			listener=null;
			super.finalize();
		}
	}

	private String   toolToLaunch;
	private String   toolProjectPath;
	private String[] toolArgs;
	private String[] environment;
	private String   workingDirectory;
	private String   projectPath;
    private String[] controlFiles;
    private boolean  isShell=false;
    private String toolName;
    private String toolErrors;
    private String toolWarnings;
    private String toolInfo;
    private String toolLogDir;
    
//    private int    buildStep;
    private AtomicInteger nextBuildStep;
    private int prevBuildStep=0;
    private ILaunchConfiguration configuration;
    private  ILaunch launch;
    private  IProgressMonitor monitor;
    private BuildParamsItem[] argumentsItemsArray; // calculate once for the launch of the sequence
    
    private String consoleFinish; // double prompt? - string to look for in consoleBuffer to finish
    private String consoleGood;   // output console sequence meaning success of the tool 
    private String consoleBad;    //  output console sequence meaning failure of the tool
    private boolean hasGood;      // at least one "good" sequence was received
    private boolean hasBad;       // at least one "bad" sequence was received
    
    private String consoleBuffer; // accumulates stdout & stderr, looking for consoleFinish (endsWith() )
//    private int extraChars=100; // Allow these chars to appear in the output after consoleFinish (user pressed smth.?)
    private int extraChars=1000; // Allow these chars to appear in the output after consoleFinish (user pressed smth.?)
    private String originalConsoleName=null; // will replace
    private String buildDateTime=null;
    private int maxLength=extraChars;
    private Pattern patternGood=null;
    private Pattern patternBad=null;
    private AtomicBoolean gotFinish;
    private boolean keptOpen;
    
    private String playBackStamp=null; // timestamp of the logs to play back ("" for latest), or
                                       // null for normal running tools
    
    
	private VDTConsoleRunner consoleRunner=null;
	private VDTProgramRunner programRunner=null;
	private VDTConsolePlayback consolePlayback=null;

    
	private IConsole iConsole;
	
	
    public IConsole getIConsole() {
		return iConsole;
	}

	public void setIConsole(IConsole iConsole) {
		this.iConsole = iConsole;
	}

	public BuildParamsItem[] getArgumentsItemsArray(){
    	return argumentsItemsArray;
    }
    
    public void canceTimers(){
//    	if (argumentsItemsArray!=null) for (int i=0;i<buildStep;i++){
       	if (argumentsItemsArray!=null) for (int i=0;i<getPrevBuildStep();i++){
    		argumentsItemsArray[i].cancelTimer();
    	}
    }
    
    public void setArgumentsItemsArray(BuildParamsItem[] argumentsItemsArray){
    	this.argumentsItemsArray=argumentsItemsArray;
    }
    
	private static final String[] empty= new String[0];
	
	
	/**
	 * Creates a new configuration for launching a tool to run 
	 * the given tool name.
	 *
	 * @param toolToLaunch The fully qualified name of the tool to launch. May not be null.
	 */
	public VDTRunnerConfiguration(String toolToLaunch) {
		if (toolToLaunch == null) { 
			throw new IllegalArgumentException(Txt.s("Launch.Error.ToolNotNull"));
		}	
		this.toolToLaunch = toolToLaunch;
		this.consoleFinish=null;
		this.consoleGood=null;
		this.consoleBad=null;
		this.playBackStamp=null;
		
		this.consoleBuffer="";
		this.gotFinish=new AtomicBoolean(false);
		this.consoleRunner= new VDTConsoleRunner(this); // arguments here?
		this.programRunner=new VDTProgramRunner(); // arguments here?
		this.consolePlayback=new VDTConsolePlayback(this);
		this.keptOpen=false;
		this.iConsole=null;
		this.nextBuildStep=new AtomicInteger(0);
		prevBuildStep=0;
	}
	public int getPrevBuildStep(){
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
			System.out.println("getPrevBuildStep(): prevBuildStep=" +prevBuildStep+ ", nextBuildStep=" +
					((nextBuildStep==null)?null:nextBuildStep.get()));
		return prevBuildStep;
	}

	
	public int getAndIncBuildStep(){
		prevBuildStep=nextBuildStep.getAndIncrement();
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
			System.out.println("getAndIncBuildStep(): prevBuildStep=" +prevBuildStep+ ", nextBuildStep=" +
					((nextBuildStep==null)?null:nextBuildStep.get()));
		return prevBuildStep;
	}

	public void resetBuildStep(){
		nextBuildStep=new AtomicInteger(0);
		prevBuildStep=0;
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
			System.out.println("resetBuildStep(): prevBuildStep=" +prevBuildStep+ ", nextBuildStep=" +
					((nextBuildStep==null)?null:nextBuildStep.get()));
//		updateBuildStep();
	}
	
	public void invalidateBuildStep(){
		nextBuildStep=null;
	}
	
	public boolean acquireBuildStep (int expected){
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
			System.out.println("acquireBuildStep(): prevBuildStep=" +prevBuildStep+ ", nextBuildStep=" +
					((nextBuildStep==null)?null:nextBuildStep.get())+
					" expected="+expected);
		if ((nextBuildStep!=null) && nextBuildStep.compareAndSet(expected, expected+1)){
			prevBuildStep=expected;
			return true;
		}
		return false;
	}
	
	public boolean isStepValid(){
		return (nextBuildStep!=null);
	}
	
	/**
	 * Verify to have a lock on the runner
	 * @return true if got a lock, false (normally should not happen) - duplicate (i.e. timeout+listener)
	 */
//	public boolean gotFirst(){
//		return (nextBuildStep.compareAndSet(buildStep, buildStep+1));
//	}
	
	/**
	 *  Called before exiting - enables next step
	 */
//	public void updateBuildStep(){
//		buildStep=nextBuildStep.get();
//	}
	public void setKeptOpen(boolean keepOpen){
		this.keptOpen=keepOpen;
	}
	public boolean isKeptOpen(){
		return this.keptOpen;
	}
	
	public void setPlayBackStamp(String str){
		playBackStamp=str;
	}
	public String getPlayBackStamp(){
		return playBackStamp;
	}
	
	public VDTConsoleRunner getConsoleRunner(){
		return this.consoleRunner;
	}

	public VDTConsolePlayback getConsolePlayback(){
		return this.consolePlayback;
	}
	
	public VDTProgramRunner getProgramRunner(){
		return this.programRunner;
	}

	public void setConsoleFinish(String consoleFinish){
		this.consoleFinish=consoleFinish;
		if ((this.consoleFinish!=null) && ((this.consoleFinish.length()+extraChars) > maxLength)){
			maxLength=this.consoleFinish.length()+extraChars;
		}
	}

	public void setConsoleBad(String consoleBad){
		this.consoleBad=consoleBad;
		this.hasBad=false;
		if ((consoleBad!=null) && (consoleBad.length()==0)) this.consoleBad=null; 
		if ((this.consoleBad!=null) && ((this.consoleBad.length()+extraChars) > maxLength)){
			maxLength=this.consoleBad.length()+extraChars;
		}
		// Patterns that start with "!" are always treated as not being regexp, "!" removed
		// "\" in the first position escapes "!" (only in the first and on;y "!")
		patternBad=null;
		if (this.consoleBad!=null) {
			if ((this.consoleBad.length()>=2) && (this.consoleBad.substring(0,2).equals("\\!"))){
				this.consoleBad=this.consoleBad.substring(1);
			} else if ((this.consoleBad.length()>=1) && (this.consoleBad.substring(0,1).equals("!"))){
				this.consoleBad=this.consoleBad.substring(1);
			} else {
				try {
					patternBad=Pattern.compile(this.consoleBad);
				} catch (PatternSyntaxException e){
					MessageUI.error("Invalid regular expression: \""+this.consoleBad+"\" - using is as a literal string");
					// just use string, not pattern
				}
			}
		}
	}

	public void setConsoleGood(String consoleGood){
		if ((consoleGood!=null) && (consoleGood.length()==0)){ // empty but not null  - no need to check, considered always to happen
			this.hasGood=true;
			this.consoleGood=null;
		} else {
			this.consoleGood=consoleGood;
			this.hasGood=false;
		}
		if ((this.consoleGood!=null) && ((this.consoleGood.length()+extraChars) > maxLength)){
			maxLength=this.consoleGood.length()+extraChars;
		}
		patternGood=null;
		// Patterns that start with "!" are always treated as not being regexp, "!" removed
		// "\" in the first position escapes "!" (only in the first and on;y "!")
		if (this.consoleGood!=null) {
			if ((this.consoleGood.length()>=2) && (this.consoleGood.substring(0,2).equals("\\!"))){
				this.consoleGood=this.consoleGood.substring(1);
			} else if ((this.consoleGood.length()>=1) && (this.consoleGood.substring(0,1).equals("!"))){
				this.consoleGood=this.consoleGood.substring(1);
			} else {
				try {
					patternGood=Pattern.compile(this.consoleGood);
				} catch (PatternSyntaxException e){
					MessageUI.error("Invalid regular expression: \""+this.consoleGood+"\" - using is as a literal string");
					patternGood=null;
					// just use string, not pattern
				}
			}
		}
	}

	public boolean gotBad(){
		return hasBad;
	}
	public boolean gotGood(){
		return hasGood;
	}
	public boolean isSetGood(){
		return consoleGood != null;
		
	}
	public boolean isSetBad(){
		return consoleBad != null;
		
	}
	
	public String getConsoluBuffer(){
		return consoleBuffer; // just for debugging
	}
	
	public boolean addConsoleText(String text){
//		if (consoleFinish==null){
//			System.out.println("Finish console sequence is not defined");
//			return false;
//		}
		consoleBuffer=consoleBuffer+text;
		if (consoleBuffer.length()>(maxLength)){
			consoleBuffer=consoleBuffer.substring(consoleBuffer.length()-maxLength);
		}
		if ((patternBad!=null) && patternBad.matcher(consoleBuffer).find()){
			hasBad=true;
			return !gotFinish.getAndSet(true); // return true;
		} else {
			if ((consoleBad!=null) && (consoleBuffer.indexOf(consoleBad)>=0)) {
				hasBad=true;
				// Should we return true immediately or still wait for consoleFinish?
				// Or only return true if (consoleFinish==null) ??
				//			resetConsoleText();
				return !gotFinish.getAndSet(true); //return true; // return as soon as got failure - anyway there will be no next tools running 
			}
		}
		if ((patternGood!=null) && patternGood.matcher(consoleBuffer).find()){
			hasGood=true;
		} else {
			if ((consoleGood!=null) && (consoleBuffer.indexOf(consoleGood)>=0)) {
				hasGood=true;
			}
		}
		
		if (consoleFinish==null){
			return false; 
		}
		if (consoleBuffer.indexOf(consoleFinish)>=0){
//			resetConsoleText();
			return !gotFinish.getAndSet(true); //return true;
		}
		return false;
	}
	
	public void resetConsoleText(){
		consoleBuffer="";
	}
	public void setConfiguration(ILaunchConfiguration configuration){
		this.configuration=configuration;
	}
	public ILaunchConfiguration getConfiguration(){
		 return configuration;
	}
	public void setLaunch(ILaunch launch){
		this.launch=launch;
	}
	public ILaunch getLaunch(){
		 return launch;
	}
	public void setMonitor(IProgressMonitor monitor){
		this.monitor=monitor;
	}
	public IProgressMonitor getMonitor(){
		 return monitor;
	}
	
	
	/**
	 * Returns the name of the class to launch.
	 *
	 * @return The fully qualified name of the class to launch. Will not be <code>null</code>.
	 */
	public String getToolToLaunch() {
		return toolToLaunch;
	}
	
	public String getToolProjectPath(){
		return toolProjectPath;
	}
	
	public void setToolProjectPath(String toolProjectPath){
		this.toolProjectPath=toolProjectPath;
	}

	/**
	 * Sets the custom tool arguments. 
	 * These arguments will not be interpreted by a runner, the client is 
	 * responsible for passing arguments compatible with a particular tool.
	 *
	 * @param args the list of arguments	
	 */
	
	
	
	public void setToolArguments(String[] args) {
		toolArgs= args;
	}
	

	
	public void setIsShell(boolean isShell) {
		this.isShell= isShell;
	}
	public boolean getIsShell() {
		return isShell;
	}

    public String getPatternErrors() {
    	return this.toolErrors;
    }

    public String getPatternWarnings() {
    	return this.toolWarnings;
    }

    public String getPatternInfo() {
    	return this.toolInfo;
    }
    public void setToolName(String str) {
    	this.toolName=str;
    	this.buildDateTime=VDTRunner.renderProcessLabel("");
    	this.originalConsoleName=VDTRunner.renderProcessLabel(this.toolName); //
    }
    public String getOriginalConsoleName() {
    	return originalConsoleName;
    }

    public String getBuildDateTime() {
    	return this.buildDateTime;
    }

    public String getToolName() {
    	return toolName;
    }
	
    public void setPatternErrors(String str) {
    	this.toolErrors=str;
    }
    public void setPatternWarnings(String str) {
    	this.toolWarnings=str;
    }
    public void setPatternInfo(String str) {
    	this.toolInfo=str;
    }
    
    public void setToolLogDir(String str) {
    	this.toolLogDir=str;
    }
    
    public String getLogDir(){
    	return this.toolLogDir;
    }
	
	/**
	 * Returns the arguments to the tool.
	 *
	 * @return The toll arguments. Default is an empty array. Will not be <code>null</code>.
	 * @see #setToolArguments(String[])
	 */
	public String[] getToolArguments() {
		if (toolArgs == null) {
			return empty;
		}
		return toolArgs;
	}


	/**
	 * Sets the environment for the tool. The tool will be
	 * launched in the given environment.
	 * 
	 * @param environment the environment for the tool specified as an array
	 *  of strings, each element specifying an environment variable setting in the
	 *  format <i>name</i>=<i>value</i>
	 */
	public void setEnvironment(String[] environment) {
		this.environment = environment;
	}
	
	/**
	 * Returns the environment for tool or <code>null</code>
	 * 
	 * @return The tool environment. Default is <code>null</code>
	 * @see #setEnvironment(String[])
	 */
	public String[] getEnvironment() {
		return environment;
	}

	/* Currently ProjectPath is just to locate the process by it */
	public void setProjectPath(String path) {
		projectPath = path;
	}
	public String getProjectPath() {
		return projectPath;
	}	

	
	/**
	 * Sets the working directory for a launched tool.
	 * 
	 * @param path the absolute path to the working directory
	 *  to be used by a launched tool, or <code>null</code> if
	 *  the default working directory is to be inherited from the
	 *  current process
	 */
	public void setWorkingDirectory(String path) {
		workingDirectory = path;
	}
	
	/**
	 * Returns the working directory of a launched tool.
	 * 
	 * @return the absolute path to the working directory
	 *  of a launched tool, or <code>null</code> if the working
	 *  directory is inherited from the current process
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}	
	
    /**
     * Sets the list of autogenerated control files. 
     *
     * @controlFiles control files list    
     */
    public void setControlFiles(String[] controlFiles) {
        this.controlFiles = controlFiles;
    }
    
    /**
     * Returns the list of autogenerated control files. 
     *
     * @return The control files list. Will not return <code>null</code>
     * (an empty array will be returned instead).
     */
    public String[] getControlFiles() {
        if(controlFiles == null)
            return empty;
        
        return controlFiles;
    }
} // class VDTRunnerConfiguration
