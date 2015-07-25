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
package com.elphel.vdt.core.tools.contexts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.IValueVariable;
import org.eclipse.core.variables.VariablesPlugin;

import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.config.xml.XMLConfig;
import com.elphel.vdt.core.tools.params.*;
import com.elphel.vdt.core.tools.params.recognizers.*;
import com.elphel.vdt.core.tools.params.types.ParamTypeString;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.conditions.StringConditionParser;
import com.elphel.vdt.util.StringPair;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;


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
    private int currentHash; // calculated during buildparam from non-parser command blocks and command files.
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
    
    /**
     * Generated hashcode for the last run of buildParams() - includes command files and non-parser command lines 
     * @return generated hash code
     */
    public int getCurrentHash(){
    	return currentHash; 
    }
    public void setCurrentHash(int hash){
    	currentHash=hash; 
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
    
    public String subsitutePattern(String pattern){
    	if ((pattern==null) || (pattern.length()==0)) return pattern;
    	Parameter param=findParam(pattern);
    	if (param==null) return pattern;
//    	List<String> lv=param.getCurrentValue();
    	List<String> lv=param.getValue(null); // null - topFormatProcessor
    	if ((lv==null) || (lv.size()==0)) return null;
    	return lv.get(0);
    }

    
    // scans all the parameters that belong to the tool, checks those of them
    // which need to be put into a control file, and puts them to that file
    // all other needed params are built into command line array
    // that array is then returned
//    public String[] buildParams() throws ToolException {
    public List<String> getSessionConsoles(){
    	List<String> consoleList=new ArrayList<String>();
    	Iterator<CommandLinesBlock> commandLinesBlockIter = commandLinesBlocks.iterator();
    	while(commandLinesBlockIter.hasNext()) {
    		CommandLinesBlock commandLinesBlock = (CommandLinesBlock)commandLinesBlockIter.next();
    		if (commandLinesBlock.isConsoleKind()){
    			Parameter parName = findParam(commandLinesBlock.getDestination());  // command file or console name
    			String consoleName = (parName != null)?	parName.getValue(null).get(0).trim() : null;
    			if (consoleName!=null) consoleList.add(consoleName);
    		}
    	}
    	return consoleList;
    }
    // currently - for all tools, skip generation of control files, ignore errors     
    public void recalcHashCodes(){
    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
    		System.out.println("Context.java(): RECALC HASH CODES");
    	}
    	// called from ContextOptionsDialog.okPressed() line: 89
    	// all context parameters are already recalculated (buildParams() ), so now we just go through all tool contexts,
    	// calling them with dryRun=true;
    	for (Tool tool : ToolsCore.getConfig().getContextManager().getToolList()){
    		try {
    			tool.buildParams(true); // was false
    	    	if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
    			if (tool.hashMatch()) System.out.println("recalcHashCodes(): "+tool.getName()+
    					" hashMatch()="+tool.hashMatch()+
    					" getCurrentHash()="+tool.getCurrentHash()+
    					" getLastRunHash()="+tool.getLastRunHash());
    	    	}
    		} catch (ToolException e) {
    			System.out.println("failed buildParams(true) on tool="+tool.getName()+", e="+e.toString());
    		}
    	}
    }
    
      public BuildParamsItem[] buildParams() throws ToolException {
    	  return buildParams(false);
      }
      public BuildParamsItem[] buildParams(boolean dryRun) throws ToolException {
    	List<BuildParamsItem> buildParamItems = new ArrayList<BuildParamsItem>();
    	
 //       List<String> commandLineParams = new ArrayList<String>();
        Iterator<CommandLinesBlock> commandLinesBlockIter = commandLinesBlocks.iterator(); // command lines block is empty (yes, there is nothing in project output)
        
        createdControlFiles.clear();
        currentHash=0;
//		if (name.equals("VivadoSynthesis")){
//			System.out.println("1. Check here: VivadoSynthesis");
//		}
        
        while(commandLinesBlockIter.hasNext()) {
            CommandLinesBlock commandLinesBlock = (CommandLinesBlock)commandLinesBlockIter.next();
            
            if(!commandLinesBlock.isEnabled())
                continue;
            String destName = commandLinesBlock.getDestination(); 
            boolean isConsoleName=commandLinesBlock.isConsoleKind();
            String sep = commandLinesBlock.getSeparator();
            
            String name=commandLinesBlock.getName();    
        	String mark=commandLinesBlock.getMark();
        	String toolErrors=  subsitutePattern(commandLinesBlock.getErrors());
        	String toolWarnings=subsitutePattern(commandLinesBlock.getWarnings());
        	String toolInfo=    subsitutePattern(commandLinesBlock.getInfo());
        	
        	String instCapture=   subsitutePattern(commandLinesBlock.getinstCapture());
        	String instSeparator= subsitutePattern(commandLinesBlock.getInstSeparator());
        	String instSuffix=    subsitutePattern(commandLinesBlock.getInstSuffix());
        	
        	
        	String stderr=commandLinesBlock.getStderr();
        	String stdout=commandLinesBlock.getStdout();
        	// the result will not be used as some other parameter value, so topProcessor is null in the next 2 lines /Andrey
        	String prompt=  buildSimpleString(commandLinesBlock.getPrompt(), null); // evaluate string
        	String sTimeout=buildSimpleString(commandLinesBlock.getTimeout(),null);
        	int timeout=0;
        	try{
        		timeout=Integer.parseInt(sTimeout);
        	} catch(Exception e){
        	}
        	String successString=  subsitutePattern(commandLinesBlock.getSuccessString());
        	String failureString=  subsitutePattern(commandLinesBlock.getFailureString());
        	boolean keepOpen=      commandLinesBlock.isKeepOpen();
        	String logFile=        subsitutePattern(commandLinesBlock.getLogPath());


        	prompt=CommandLinesBlock.parseCntrl(prompt); // replace control character codes (\n,\t,\x)
        	prompt=commandLinesBlock.applyMark(prompt); // remove mark sequence
        	String interrupt=commandLinesBlock.getInterrupt();
            List<String> lines = commandLinesBlock.getLines();  // [%Param_Shell_Options, echo BuildDir=%BuildDir ;, echo SimulationTopFile=%SimulationTopFile ;, echo SimulationTopModule=%SimulationTopModule ;, echo BuildDir=%BuildDir;, %Param_PreExe, %Param_Exe, %Param_TopModule, %TopModulesOther, %ModuleLibrary, %LegacyModel, %NoSpecify, %v, %SourceList, %ExtraFiles, %Filter_String]
            if ((lines.size()==0) && commandLinesBlock.hadStrings()){
                if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING))
                	System.out.println("Removing command lines block by false condition");
            	continue; // to enable conditionals for the command line blocks, still making possible to use empty blocks for no-arguments programs
            }
            List<List<String>> commandSequence = new ArrayList<List<String>>();
            for(Iterator<String> lineIter = lines.iterator(); lineIter.hasNext();) {
            	String line = (String)lineIter.next();
            	// the result will not be used as some other parameter value, so topProcessor is null in the next line /Andrey
            	commandSequence.add(resolveStrings(buildCommandString(line,null)));  // TODO: parses them here? VERIFY               
            }
            
            // Here - already resolved to empty            
            List<String> commandLineParams = new ArrayList<String>();		
            if(destName != null) {
                Parameter parName = findParam(destName);  // command file or console name
                String controlFileName = parName != null? 
                		parName.getValue(null).get(0).trim() : null;
            	if (isConsoleName) {
 //           		System.out.println("TODO: Enable console command generation here");
            		printStringsToConsoleLine(commandLineParams, commandSequence,sep,mark);
            		//if (name!=null) - it is a parser, do not include in hashcode generation
            		buildParamItems.add(
            				new BuildParamsItem (
            						(String[])commandLineParams.toArray(new String[commandLineParams.size()]),
            						controlFileName, // find console beginning with this name, send commands there
            					    name, //nameAsParser
//            					    mark,
            					    toolErrors,
            					    toolWarnings,
            					    toolInfo,
            						instCapture,
            						instSeparator,
            						instSuffix,
            					    prompt,
            					    interrupt,
            					    stderr,
            					    stdout,
            					    timeout,
            			        	successString,
            			        	failureString,
            			        	keepOpen,
            			        	logFile
            					    )
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
            		if (!dryRun) {
            			printStringsToFile(controlFileName, controlFileExists, commandSequence, sep, mark);
            			if(!controlFileExists)
            				createdControlFiles.add(controlFileName);
            		}
            		// include hash codes for each segment of the command file content
            		for (List<String> lStr:commandSequence){
            			if (lStr!=null) for (String str:lStr){
            				if (str!=null) currentHash += str.hashCode();
            			}
            		}
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
        						instCapture,
        						instSeparator,
        						instSuffix,
        					    prompt,
        					    interrupt,
        					    stderr,
        					    stdout,
        					    timeout,
        			        	successString,
        			        	failureString,
        			        	keepOpen,
        			        	logFile
        					    )
        				);
            }
        }
        
        // keep names only for commands that are referenced in console scripts, others make null
        Iterator<BuildParamsItem> buildParamItemsIter = buildParamItems.iterator(); // command lines block is empty (yes, there is nothing in project output)
        while(buildParamItemsIter.hasNext()) {
        	BuildParamsItem buildParamsItem = (BuildParamsItem)buildParamItemsIter.next();
        	buildParamsItem.removeNonParser(buildParamItems);
        }
		// include hash codes for each line in the command sequence if it is not a parser
		for (BuildParamsItem item:buildParamItems){
			if (!item.isParser()){
				String [] params=item.getParams();
				if (params!=null) for (int i=0;i<params.length;i++){
					currentHash += params[i].hashCode();
//			if (name.equals("VivadoBitstream")){
//				System.out.println(params[i]+": "+currentHash);
//			}

				}
			}
		}
//		System.out.println("BildParam("+dryRun+"), name="+name+" currentHash="+currentHash);
// Seems that during build it worked on a working copy of the tool, so calculated parameter did not get back
		Tool proto=ToolsCore.getConfig().getContextManager().findTool(name);
//		System.out.println("Calculated currentHash for "+name+"="+currentHash);
//		if (name.equals("VivadoBitstream")){
//			System.out.println("Check here: VivadoBitstream");
//		}
		if (proto!=null){
			if (proto!=this){
				if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_TOOL_SEQUENCE)) {
					System.out.println("++++ Updating tool's currentHash from working copy, name="+name);
				}
				proto.setCurrentHash(currentHash);
			}
			
		}
		
		
        return (BuildParamsItem[])buildParamItems.toArray(new BuildParamsItem[buildParamItems.size()]);
    }
    
    protected List<String> buildCommandString(String paramStringTemplate, FormatProcessor topProcessor)
        throws ToolException
    {
        if (topProcessor==null) topProcessor=new FormatProcessor(this); // or use "context" 
        else topProcessor.setCurrentTool(this); // or use "context"
        FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                            new SimpleGeneratorRecognizer(topProcessor),
                                                            new RepeaterRecognizer()
                                                            // new ContextParamRecognizer(this),
                                                            // new ContextParamRepeaterRecognizer(this)
                                                        },topProcessor);
                            
        return processor.process(paramStringTemplate);
    }
    
    // recognizes parameter name (just %name), or simple generators
    protected String buildSimpleString(String stringTemplate, FormatProcessor topProcessor)
            throws ToolException
        {
    	    if (stringTemplate==null) return null;
    	    
            if (topProcessor==null) topProcessor=new FormatProcessor(this); // or use "context" 
            else topProcessor.setCurrentTool(this); // or use "context"

    	    Parameter parName=findParam(stringTemplate);
    	    if (parName!=null){
    	    	return parName.getValue(topProcessor).get(0).trim(); // get parameter
    	    }
            FormatProcessor processor = new FormatProcessor(new Recognizer[] {
                                                                new SimpleGeneratorRecognizer(topProcessor),
                                                                new RepeaterRecognizer()
                                                                // new ContextParamRecognizer(this),
                                                                // new ContextParamRepeaterRecognizer(this)
                                                            },topProcessor);
            
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
            	if (id.equals(XMLConfig.PARAMGROUP_SEPARATOR)) // Will try to add separator to the dialog
            		continue;
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
                    out.print(useMark?(CommandLinesBlock.parseCntrl(s).replace(mark,"")):CommandLinesBlock.parseCntrl(s));
                    
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

    private void printStringsToConsoleLine(
    		List<String> commandLineParams, 
    		List<List<String>> commandSequence,
    		String separator,
    		String mark) throws ToolException
    		{
        String sep = (separator != null? separator : " ");
    	boolean useMark=(mark!=null) && (mark.length()>0);
        int written = 0;
		StringBuilder builder = new StringBuilder();
    	for(Iterator<List<String>> li = commandSequence.iterator(); li.hasNext();) {
    		List<String> strList = (List<String>)li.next();
    		if(strList.size() > 0) {
                int writtenNow = 0;
                for(Iterator<String> si = strList.iterator(); si.hasNext();) { // "words" in each line
                    String s = (String)si.next();
                    if(s.length() == 0)
                        continue;
                    builder.append(useMark?(CommandLinesBlock.parseCntrl(s).replace(mark,"")):CommandLinesBlock.parseCntrl(s));
                    written += s.length();
                    writtenNow += s.length();
                    // try to avoid needless spaces  
                    if(writtenNow > 0 && si.hasNext()) { // adding spaces between generator "words" ?
                        String stripped = s.
                                          replace('\n', ' ').
                                          replace('\t', ' ').
                                          trim();
                        
                        if(stripped.length() > 0)
                        	builder.append(" ");
                    }
                }
                if(writtenNow > 0 && li.hasNext())
                	builder.append(sep);
    		}
    	}
        if(written > 0)
        	builder.append("\n");
        commandLineParams.add(builder.toString()); // just a single line
	}
/*
 *  previous version
    private void printStringsToConsoleLine(
    		List<String> commandLineParams, 
    		List<List<String>> commandSequence,
    		String separator,
    		String mark) throws ToolException
    		{
        String sep = (separator != null? separator : " ");
    	boolean useMark=(mark!=null) && (mark.length()>0);
    	for(Iterator<List<String>> li = commandSequence.iterator(); li.hasNext();) {
    		List<String> strList = (List<String>)li.next();

    		if(strList.size() > 0) {
    			for(Iterator<String> si = strList.iterator(); si.hasNext();) {
    				String s = ((String)si.next()).trim();

    				if(!s.equals(""))
    					commandLineParams.add(useMark?
    							(CommandLinesBlock.parseCntrl(s).replace(mark,"")):
    								CommandLinesBlock.parseCntrl(s));
    			}
    		}
    	}
	}

 */
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
                        commandLineParams.add(useMark?
                        		(CommandLinesBlock.parseCntrl(s).replace(mark,"")):
                        			CommandLinesBlock.parseCntrl(s));
                }
            }
        }
    }
// Andrey: now is the same as command line, but will change to allow last element be prompt
    
    
    private void checkNotInitialized() throws ConfigException {
        if(initialized)
            throw new ConfigException("Context cannot be re-initialized");
    }
    //Substitute Eclipse and VDT ("verilog_") strings ${} here, after all VDT parameter processing
    // recursively resolve variables
    private List<String> resolveStrings(List<String> preResolved){
        List<String> resolved = new ArrayList<String>();
        for (String s:preResolved){
        	resolved.add(resolveStrings(s));
        }
        return resolved;
    }
    
    
    private String resolveStrings(String s){
    	int start = s.indexOf("${");
    	if (start < 0) return s;
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) System.out.println("Before substitution\""+s+"\"");
    	int end = s.indexOf("}", start);
    	if (end < 0) return s;
    	String dynVar = s.substring(start+2,end).trim();
    	String dynValue = resolveEclipseVariables(dynVar);
    	if (dynValue == null) {
    		System.out.println("getEclipseSubstitutedString("+s+") - undefined variable: "+dynVar);
    		dynValue = s.substring(start,end+1); // just no substitution
    	}
    	String result = s.substring(0,start)+dynValue;
    	if (end < s.length()) result += resolveStrings(s.substring(end+1));
		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) System.out.println("After substitution\""+result+"\"");
    	return result;
    	
    }
    // from http://www.programcreek.com/java-api-examples/index.php?api=org.eclipse.core.variables.IDynamicVariable
    private String resolveEclipseVariables(String key){
    	if (key == null)   return null;
    	IStringVariableManager variableManager=VariablesPlugin.getDefault().getStringVariableManager();
    	
    	//debug
/*    	
       	IValueVariable [] variables = variableManager.getValueVariables();
    	IDynamicVariable [] dVariables = variableManager.getDynamicVariables();
		System.out.println("resolveEclipseVariables: key=" + key);
	
    	for (IValueVariable v : variables){
    		System.out.println("resolveEclipseVariables: variables=" + v.getValue());
    	}
    	for (IDynamicVariable dv : dVariables){
    		if (!dv.getName().contains("prompt")) {
    			try {
    				System.out.print("resolveEclipseVariables: dVariables=" + dv.getName());
    				System.out.println(" -- "+dv.getValue(null));
    			} catch (CoreException e) {
    				// TODO Auto-generated catch block
    				System.out.println(" -- null?");
    			}
    		}
    	}
*/  	
    	int index=key.indexOf(':');
    	if (index > 1) {
    		String varName=key.substring(0,index);
    		IDynamicVariable variable=variableManager.getDynamicVariable(varName);
    		if (variable == null)     return null;
    		try {
    			if (key.length() > index + 1)       return variable.getValue(key.substring(index + 1));
    			return variable.getValue(null);
    		}
    		catch (    CoreException e) {
    			return null;
    		}
    	}
    	IValueVariable variable=variableManager.getValueVariable(key);
    	
    	
    	if (variable == null) {
    		IDynamicVariable dynamicVariable=variableManager.getDynamicVariable(key);
    		if (dynamicVariable == null)     return null;
    		try {
    			return dynamicVariable.getValue(null);
    		}
    		catch (    CoreException e) {
    			return null;
    		}
    	}
    	return variable.getValue();
    }
    
    
}
