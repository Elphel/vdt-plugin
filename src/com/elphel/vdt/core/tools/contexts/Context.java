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
package com.elphel.vdt.core.tools.contexts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.params.*;
import com.elphel.vdt.core.tools.params.recognizers.*;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.conditions.StringConditionParser;
import com.elphel.vdt.util.StringPair;


public abstract class Context {
    protected String name;
    protected String label;
    protected String iconName;
    protected String controlInterfaceName;
    protected String inputDialogLabel;
    protected ParameterContainer paramContainer; 
    protected ControlInterface controlInterface;
    protected List<ParamGroup> paramGroups;
    protected List<CommandLinesBlock> commandLinesBlocks;
    protected List<ParamGroup> visibleParamGroups = new ArrayList<ParamGroup>();
    protected Config config;

    private StringConditionParser conditionParser = new StringConditionParser(this); 
    private List<String> createdControlFiles = new ArrayList<String>();    
    private boolean initialized = false; 
    private String workingDirectory;
    private String version;
    private Context context=null;
    protected Context(String name,
                      String controlInterfaceName,
                      String label,
                      String iconName, 
                      String inputDialogLabel,
                      List<Parameter> params,
                      List<ParamGroup> paramGroups,
                      List<CommandLinesBlock> commandLinesBlocks) 
    {     
        this.name = name;
        this.controlInterfaceName = controlInterfaceName;
        this.label = label;
        this.iconName = iconName; 
        this.commandLinesBlocks = commandLinesBlocks;
        this.inputDialogLabel = inputDialogLabel;
        this.paramGroups = paramGroups;        
        this.paramContainer = new ParameterContainer(params);
    }
    
    public void init(Config config) throws ConfigException {
        checkNotInitialized();

        this.config = config;
        
        initControlInterface();
        initParams();
        initCommandLines();

        initialized = true;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setParams(List<Parameter> params) throws ConfigException {
        checkNotInitialized();
        
        this.paramContainer = new ParameterContainer(params);
    }

    public void setParamGroups(List<ParamGroup> paramGroups) throws ConfigException {
        checkNotInitialized();
        
        this.paramGroups = paramGroups; 
    }
    
    public void setInputDialogLabel(String inputDialogLabel) throws ConfigException {
        checkNotInitialized();

        this.inputDialogLabel = inputDialogLabel;
    }
    
    public void setCommandLinesBlocks(List<CommandLinesBlock> commandLinesBlocks) throws ConfigException {
        checkNotInitialized();

        this.commandLinesBlocks = commandLinesBlocks;
    }
    
    public void setWorkingDirectory(String location) {
        this.workingDirectory = location;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
    
    public String getEscapedLabel() {
        return label.replace(" ", "_");
    }

    public String getNiceLabel() {
        return label.replace("_", " ");
    }
    
    
    public String getIconName() {
        return iconName;
    }
    
    public List<Parameter> getParams() {
        return paramContainer.getParams();
    }
    
    public String getInputDialogLabel() {
        return inputDialogLabel;
    }

    public ControlInterface getControlInterface() {
        return controlInterface;
    }

    public List<ParamGroup> getParamGroups() {
        return paramGroups;
    }
    
    public List<ParamGroup> getVisibleParamGroups() {
//        try {
//            initParamGroups();
//        } catch(ConfigException e) {
//            MessageUI.error(e); 
//        }

        return visibleParamGroups; // project foriverilog: [com.elphel.vdt.core.tools.params.ParamGroup@775b1885, null, null, null, null, null, null, null, null, null]
        // ... label "Project properties", name="General"
    }
    
    public boolean isVisible() {
        return !getVisibleParamGroups().isEmpty();
    }
    
    public Parameter findParam(String paramID) {
        return paramContainer.findParam(paramID);
    }
    
    public StringConditionParser getConditionParser() {
        return conditionParser;
    }
    
    public List<String> getCreatedControlFiles() {
        return createdControlFiles;
    }

    // scans all the parameters that belong to the tool, checks those of them
    // which need to be put into a control file, and puts them to that file
    // all other needed params are built into command line array
    // that array is then returned
//    public String[] buildParams() throws ToolException {
      public BuildParamsItem[] buildParams() throws ToolException {
    	List<BuildParamsItem> buildParamItems = new ArrayList<BuildParamsItem>();
    	
 //       List<String> commandLineParams = new ArrayList<String>();
        Iterator<CommandLinesBlock> commandLinesBlockIter = commandLinesBlocks.iterator(); // command lines block is empty (yes, there is nothing in project output)
        
        createdControlFiles.clear();
        
        while(commandLinesBlockIter.hasNext()) {
            CommandLinesBlock commandLinesBlock = (CommandLinesBlock)commandLinesBlockIter.next();
            
            if(!commandLinesBlock.isEnabled())
                continue;
            String destName = commandLinesBlock.getDestination(); 
            boolean isConsoleName=commandLinesBlock.isConsoleKind();
            String sep = commandLinesBlock.getSeparator();
            
            String name=commandLinesBlock.getName();    
        	String mark=commandLinesBlock.getMark();
        	String toolErrors=commandLinesBlock.getErrors();
        	String toolWarnings=commandLinesBlock.getWarnings();
        	String toolInfo=commandLinesBlock.getInfo();
        	String stderr=commandLinesBlock.getStderr();
        	String stdout=commandLinesBlock.getStdout();
        	String prompt=buildSimpleString(commandLinesBlock.getPrompt()); // evaluate string
        	prompt=commandLinesBlock.parseCntrl(prompt); // replace control character codes (\n,\t,\x)
        	prompt=commandLinesBlock.applyMark(prompt); // remove mark sequence
        	String interrupt=commandLinesBlock.getInterrupt();
            List<String> lines = commandLinesBlock.getLines();  // [%Param_Shell_Options, echo BuildDir=%BuildDir ;, echo SimulationTopFile=%SimulationTopFile ;, echo SimulationTopModule=%SimulationTopModule ;, echo BuildDir=%BuildDir;, %Param_PreExe, %Param_Exe, %Param_TopModule, %TopModulesOther, %ModuleLibrary, %LegacyModel, %NoSpecify, %v, %SourceList, %ExtraFiles, %Filter_String]          
            List<List<String>> commandSequence = new ArrayList<List<String>>();
            for(Iterator<String> lineIter = lines.iterator(); lineIter.hasNext();) {
            	String line = (String)lineIter.next();
            	commandSequence.add(buildCommandString(line));  // TODO: parses them here? VERIFY               
            }
            
            // Here - already resolved to empty            
            List<String> commandLineParams = new ArrayList<String>();		
            if(destName != null) {
                Parameter parName = findParam(destName);  // command file or console name
                String controlFileName = parName != null? 
                		parName.getValue().get(0).trim() : null;
            	if (isConsoleName) {
 //           		System.out.println("TODO: Enable console command generation here");
            		printStringsToConsoleLine(commandLineParams, commandSequence,mark);
            		buildParamItems.add(
            				new BuildParamsItem (
            						(String[])commandLineParams.toArray(new String[commandLineParams.size()]),
            						controlFileName, // find console beginning with this name, send commands there
            					    name, //nameAsParser
//            					    mark,
            					    toolErrors,
            					    toolWarnings,
            					    toolInfo,
            					    prompt,
            					    interrupt,
            					    stderr,
            					    stdout)
            				);
            	} else { // processing command file
            		if(workingDirectory != null)
            			controlFileName = workingDirectory + File.separator + controlFileName;

            		// check param type first
            		if(!(parName.getType() instanceof ParamTypeString))
            			throw new ToolException("Parameter '" + parName.getID() + 
            					"' specified in the description of context '" + parName +
            					"' must be of type '" + ParamTypeString.NAME + "'");

            		// write strings to control file
            		boolean controlFileExists = controlFileExists(controlFileName);

            		printStringsToFile(controlFileName, controlFileExists, commandSequence, sep, mark);

            		if(!controlFileExists)
            			createdControlFiles.add(controlFileName);

            	}
            } else { // processing command line
            	printStringsToCommandLine(commandLineParams, commandSequence, mark);
        		buildParamItems.add(
        				new BuildParamsItem (
        						(String[])commandLineParams.toArray(new String[commandLineParams.size()]),
        						null, // external tool in a new console
        					    name, //nameAsParser
//        					    mark,
        					    toolErrors,
        					    toolWarnings,
        					    toolInfo,
        					    prompt,
        					    interrupt,
        					    stderr,
        					    stdout)
        				);
            	
            }
        }
        
        // keep names only for commands that are referenced in console scripts, others make null
        Iterator<BuildParamsItem> buildParamItemsIter = buildParamItems.iterator(); // command lines block is empty (yes, there is nothing in project output)
        while(buildParamItemsIter.hasNext()) {
        	BuildParamsItem buildParamsItem = (BuildParamsItem)buildParamItemsIter.next();
        	buildParamsItem.removeNonParser(buildParamItems);
        }
        return (BuildParamsItem[])buildParamItems.toArray(new BuildParamsItem[buildParamItems.size()]);
    }
    
    protected List<String> buildCommandString(String paramStringTemplate)
        throws ToolException
    {
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                            new SimpleGeneratorRecognizer(),
                                                            new RepeaterRecognizer()
                                                            // new ContextParamRecognizer(this),
                                                            // new ContextParamRepeaterRecognizer(this)
                                                        });
                            
        return processor.process(paramStringTemplate);
    }
    
    protected String buildSimpleString(String stringTemplate)
            throws ToolException
        {
    	    if (stringTemplate==null) return null;
            FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                                new SimpleGeneratorRecognizer(),
                                                                // new RepeaterRecognizer()
                                                                // new ContextParamRecognizer(this),
                                                                // new ContextParamRepeaterRecognizer(this)
                                                            });
            
            List<String> result= processor.process(stringTemplate);                    
            if (result.size()==0) return "";
            return result.get(0);
        }
        
    

    protected void initControlInterface() throws ConfigException {
        if(controlInterfaceName != null) {
            controlInterface = config.findControlInterface(controlInterfaceName);
        
            if(controlInterface == null)
                throw new ConfigException("No such control interface '" + controlInterfaceName +
                                          "' (used in context '" + name + 
                                          "')");
        }
    }   

    protected void initCommandLines() throws ConfigException {
        for(Iterator<CommandLinesBlock> i = commandLinesBlocks.iterator(); i.hasNext();)
            ((CommandLinesBlock)i.next()).init(config);
    }

    protected void initParams() throws ConfigException {
        paramContainer.init(config, this); //??? label="iverilog project label", name="IVerilogProject", config.currentConfigFileName="/data/vdt/elphel-EclipseVDT/vdt/tools/XDS/XDS.xml"
// config.currentConfigFileName is just the last file processed for this context, OK
        // do the first init to detect most errors
        // This time got labelname="ModelSIMProject" (empty)
        initParamGroups();
    }
    
    protected void initParamGroups() throws ConfigException {
        visibleParamGroups.clear();
        
        List<StringPair> foundParams = 
            new ArrayList<StringPair>(paramContainer.getParams().size());  // [null, null, null, null, null, null, null, null, null, null]
        
        for(ParamGroup group : paramGroups) {
            List<String> paramIDs = group.getParams();

            // just ignore irrelevant param groups
            if(!group.isRelevant())
                continue;
            
            for(String id : paramIDs) {
                if(findParam(id) == null)
                    throw new ConfigException("Parameter '" + id + 
                                              "' in context '" + name + 
                                              "' referenced from parameter group '" + group.getName() + 
                                              "' is absent");
                
                // check the presence of the given parameter
                // in other param groups
                for(StringPair pair : foundParams) {
                    String otherParamId = pair.getFirst();
                    String otherGroupName = pair.getSecond();
                    
                    if(otherParamId.equals(id)) {
                        if(group.getName().equals(otherGroupName))
                            throw new ConfigException("Parameter '" + id + 
                                                      "' in context '" + name + 
                                                      "' is referenced from parameter group '" +
                                                      group.getName() + 
                                                      "' more than once");
                        else
                            throw new ConfigException("Parameter '" + id + 
                                                      "' in context '" + name + 
                                                      "' is referenced from two parameter groups: '" +
                                                      group.getName() + 
                                                      "' and '" +
                                                      otherGroupName +
                                                      "'");
                    }
                }
                
                foundParams.add(new StringPair(id, group.getName()));
            }

            if(group.isVisible())           
                visibleParamGroups.add(group);
        }
    }

    private boolean controlFileExists(String controlFileName) {
        for(Iterator<String> i = createdControlFiles.iterator(); i.hasNext();) {
            if(controlFileName.equals(i.next()))
                return true;
        }
        
        return false;
    }
    
    private void printStringsToFile(String controlFileName, 
                                    boolean append, 
                                    List<List<String>> commandString,
                                    String separator,
                                    String mark)
        throws ToolException
    {
    	boolean useMark=(mark!=null) && (mark.length()>0);
        FileOutputStream outputStream = null;
        
        try {
            File file = new File(controlFileName);
            outputStream = new FileOutputStream(file, append);
        } catch(FileNotFoundException e) {
            throw new ToolException("Cannot open file '" + controlFileName + "' for writing");
        }
        
        String sep = (separator != null? separator : " ");
        PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream));
        int written = 0;
        
        for(Iterator<List<String>> li = commandString.iterator(); li.hasNext();) {
            List<String> strList = (List<String>)li.next();

            if(strList.size() > 0) {
                int writtenNow = 0;
                
                for(Iterator<String> si = strList.iterator(); si.hasNext();) {
                    String s = (String)si.next();

                    if(s.length() == 0)
                        continue;
                    out.print(useMark?(s.replace(mark,"")):s);
                    
                    written += s.length();
                    writtenNow += s.length();
                    
                    // try to avoid needless spaces  
                    if(writtenNow > 0 && si.hasNext()) {
                        String stripped = s.
                                          replace('\n', ' ').
                                          replace('\t', ' ').
                                          trim();
                        
                        if(stripped.length() > 0)
                            out.print(" ");
                    }
                }

                if(writtenNow > 0 && li.hasNext())
                    out.print(sep);
            }
        }
        
        if(written > 0)
            out.println();
        
        out.close();
    }

    private void printStringsToCommandLine(List<String> commandLineParams, 
                                           List<List<String>> commandSequence,
                                           String mark) 
        throws ToolException
    {
    	boolean useMark=(mark!=null) && (mark.length()>0);
        for(Iterator<List<String>> li = commandSequence.iterator(); li.hasNext();) {
            List<String> strList = (List<String>)li.next();

            if(strList.size() > 0) {
                for(Iterator<String> si = strList.iterator(); si.hasNext();) {
                    String s = ((String)si.next()).trim();
                    if(!s.equals(""))
                        commandLineParams.add(useMark?(s.replace(mark,"")):s);
                }
            }
        }
    }
// Andrey: now is the same as command line, but will change to allow last element be prompt
    
    private void printStringsToConsoleLine(
    		List<String> commandLineParams, 
    		List<List<String>> commandSequence,
    		String mark) throws ToolException
    		{
    	boolean useMark=(mark!=null) && (mark.length()>0);
    	for(Iterator<List<String>> li = commandSequence.iterator(); li.hasNext();) {
    		List<String> strList = (List<String>)li.next();

    		if(strList.size() > 0) {
    			for(Iterator<String> si = strList.iterator(); si.hasNext();) {
    				String s = ((String)si.next()).trim();

    				if(!s.equals(""))
    					commandLineParams.add(useMark?(s.replace(mark,"")):s);
    			}
    		}
    	}
	}
    
    private void checkNotInitialized() throws ConfigException {
        if(initialized)
            throw new ConfigException("Context cannot be re-initialized");
    }
}
