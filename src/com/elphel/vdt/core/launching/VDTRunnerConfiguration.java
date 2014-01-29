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
package com.elphel.vdt.core.launching;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
//import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.ui.console.IConsole;

import com.elphel.vdt.Txt;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;


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
    private int    buildStep;
    private ILaunchConfiguration configuration;
    private  ILaunch launch;
    private  IProgressMonitor monitor;
    private BuildParamsItem[] argumentsItemsArray; // calculate once for the launch of the sequence
    
    private String consoleFinish; // double prompt? - string to look for in consoleBuffer to finish
    private String consoleBuffer; // accumulates stdout & stderr, looking for consoleFinish (endsWith() )
    private int extraChars=100; // Allow these chars to appear in the output after consoleFinish (user pressed smth.?)
    private String originalConsoleName=null;
    private Set<IConsole> consoles=null;  // parser consoles opened for this console
	private VDTConsoleRunner consoleRunner=null;

    
    public BuildParamsItem[] getArgumentsItemsArray(){
    	return argumentsItemsArray;
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
		this.consoleBuffer="";
		this.consoles= new HashSet<IConsole>();
		this.consoleRunner= new VDTConsoleRunner(this); // arguments here?
	}
	
	public VDTConsoleRunner getConsoleRunner(){
		return this.consoleRunner;
	}
	
	public void addConsole(IConsole console){
		consoles.add(console);
	}
	public void removeConsole(IConsole console){
		consoles.remove(console);
	}
	public boolean noConsoles(){
		return consoles.isEmpty();
	}
	public boolean hasConsole(IConsole console){
		return consoles.contains(console);
	}

	public void setConsoleFinish(String consoleFinish){
		this.consoleFinish=consoleFinish;
	}
	public boolean addConsoleText(String text){
		if (consoleFinish==null){
			System.out.println("Dinish console sequence is not defined");
			return false;
		}
		consoleBuffer=consoleBuffer+text;
		int maxLength=consoleFinish.length()+extraChars;
		if (consoleBuffer.length()>(maxLength)){
			consoleBuffer=consoleBuffer.substring(consoleBuffer.length()-maxLength);
		}
		if (consoleBuffer.indexOf(consoleFinish)>=0){
			resetConsoleText();
			return true;
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
	
	public void setBuildStep(int buildStep){
		this.buildStep=buildStep;
	}

	public int getBuildStep(){
		return buildStep;
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
    	this.originalConsoleName=VDTRunner.renderProcessLabel(this.toolName); //
    }
    public String getOriginalConsoleName() {
    	return originalConsoleName;
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
