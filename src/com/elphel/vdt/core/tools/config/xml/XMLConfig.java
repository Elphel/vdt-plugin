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
package com.elphel.vdt.core.tools.config.xml;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

import com.elphel.vdt.core.tools.BooleanUtils;
import com.elphel.vdt.core.tools.params.*;
import com.elphel.vdt.core.tools.params.conditions.*;
import com.elphel.vdt.core.tools.params.types.*;
import com.elphel.vdt.core.tools.config.*;
import com.elphel.vdt.core.tools.contexts.*;
import com.elphel.vdt.core.tools.menu.*;
import com.elphel.vdt.ui.MessageUI;
import com.elphel.vdt.ui.VDTPluginImages;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;


public class XMLConfig extends Config {
    static final String CONFIG_EXTENSION = ".xml";
    
    static final String ROOT_NODE_TAG = "vdt-project";
    static final String ROOT_VERSION_ATTR = "version";
    
    static final String INTERFACE_TAG = "interface";
    static final String INTERFACE_NAME_ATTR = "name";
    static final String INTERFACE_EXTENDS_ATTR = "extends";
    
    static final String TYPEDEF_TAG = "typedef";
    static final String TYPEDEF_NAME_ATTR = "name";
    static final String TYPEDEF_LIST_ATTR = "list";
    static final String TYPEDEF_PARAMTYPE_TAG = "paramtype";
    static final String TYPEDEF_PARAMTYPE_KIND_ATTR = "kind";
    
    static final String ENUM_ELEMENT_TAG = "item";

    static final String SYNTAX_TAG = "syntax";
    static final String SYNTAX_NAME_ATTR = "name";
    static final String SYNTAX_FORMAT_ATTR = "format";
    static final String SYNTAX_TYPE_ATTR = "type";

    static final String PARAMGROUP_TAG = "group";
    static final String PARAMGROUP_NAME_ATTR = "name";
    static final String PARAMGROUP_LABEL_ATTR = "label";
    static final String PARAMGROUP_VISIBLE_ATTR = "visible";
    static final String PARAMGROUP_WEIGHT_ATTR = "weight";
    public static final String PARAMGROUP_SEPARATOR = "---";
    
    static final String PARAMETER_TAG = "parameter";

    static final String PARAMETER_ID_ATTR = "id";
    static final String PARAMETER_OUTID_ATTR = "outid";
    static final String PARAMETER_TYPE_NAME_ATTR = "type";
    static final String PARAMETER_FORMAT_NAME_ATTR = "format";
    static final String PARAMETER_DEFAULT_VALUE_ATTR = "default";
    static final String PARAMETER_OMIT_VALUE_ATTR = "omit";
    static final String PARAMETER_LABEL_ATTR = "label";
    static final String PARAMETER_TOOLTIP_ATTR = "tooltip";
    static final String PARAMETER_READONLY_ATTR = "readonly";
    static final String PARAMETER_VISIBLE_ATTR = "visible";
    
    static final String CONTEXT_INSTALLATION_TAG = "installation";
    static final String CONTEXT_PROJECT_TAG = "project";
    static final String CONTEXT_PACKAGE_TAG = "package";
    static final String CONTEXT_TOOL_TAG = "tool";
    
    static final String CONTEXT_OUTPUT_SECTION_TAG = "output";

    static final String CONTEXT_INPUT_SECTION_TAG = "input";
    static final String CONTEXT_INPUT_SECTION_LABEL_ATTR = "label";

    static final String CONTEXT_INTERFACE_ATTR = "interface";
    static final String CONTEXT_LABEL_ATTR = "label";    
    static final String CONTEXT_ICON_ATTR = "icon";
    static final String CONTEXT_NAME_ATTR = "name";
    static final String CONTEXT_PROJECT_PACKAGE_ATTR = "package";
    static final String CONTEXT_PROJECT_DESIGNMENU_ATTR = "menu";
    static final String CONTEXT_TOOL_BASE_ATTR = "inherits";
    static final String CONTEXT_TOOL_PACKAGE_ATTR = "package";
    static final String CONTEXT_TOOL_PROJECT_ATTR = "project";
    static final String CONTEXT_TOOL_EXE_ATTR = "exe";
    static final String CONTEXT_TOOL_SHELL_ATTR = "shell";
    static final String CONTEXT_TOOL_EXTENSIONS_LIST_TAG = "extensions-list";
    static final String CONTEXT_TOOL_EXTENSION_TAG = "extension";
    static final String CONTEXT_TOOL_EXTENSION_MASK_ATTR = "mask";
    static final String CONTEXT_TOOL_ACTION_LIST_TAG = "action-menu";
    static final String CONTEXT_TOOL_ACTION_TAG = "action";
    static final String CONTEXT_TOOL_ACTION_LABEL = "label";
    static final String CONTEXT_TOOL_ACTION_RESOURCE = "resource";
    static final String CONTEXT_TOOL_ACTION_CHECK_EXTENSION = "check-extension";
    static final String CONTEXT_TOOL_ACTION_CHECK_EXISTENCE = "check-existence";
    static final String CONTEXT_TOOL_ACTION_ICON = "icon";
    static final String CONTEXT_TOOL_DEPENDS_LIST_TAG =  "depends-list";
    static final String CONTEXT_TOOL_DEPENDS_TAG =       "depends";
    static final String CONTEXT_TOOL_DEPENDS_STATE_TAG = "state";
    static final String CONTEXT_TOOL_DEPENDS_FILES_TAG = "files";
// TODO: May add other types of dependencies 
    
    static final String CONTEXT_TOOL_DFLT_ACTION_LABEL = "Run for";
    static final String CONTEXT_TOOL_DFLT_ACTION_RESOURCE = "%%CurrentFile";
    static final String CONTEXT_TOOL_DFLT_ACTION_CHECK_EXTENSION = "true";
    static final String CONTEXT_TOOL_DFLT_ACTION_CHECK_EXISTENCE = "false";
    
    static final String CONTEXT_TOOL_SYNTAX_ERRORS =  "errors";
    static final String CONTEXT_TOOL_SYNTAX_WARNINGS= "warnings";
    static final String CONTEXT_TOOL_SYNTAX_INFO =    "info";
    static final String CONTEXT_TOOL_IGNORE_FILTER =  "ignore"; // file path regular expression to remove libraries from source list
    static final String CONTEXT_TOOL_LOG_DIRECTORY =  "log-dir"; // folder to store the tool log files
    static final String CONTEXT_TOOL_STATE_DIRECTORY = "state-dir"; // folder to store the tool state (snapshot) files
    
    static final String CONTEXT_TOOL_DISABLED =       "disable"; // Parameter name that disables the tool if true
    static final String CONTEXT_TOOL_RESULT =         "result";   // Parameter name keeps the filename representing result (snapshot)
    static final String CONTEXT_TOOL_RESTORE =        "restore";  // tool name that restores the state from result (shapshot)
    static final String CONTEXT_TOOL_SAVE =           "save";     // tool name that saves the state to result file (snapshot)
    static final String CONTEXT_TOOL_AUTOSAVE =       "autosave"; // Parameter name of boolean type that controls automatic save after success
    static final String CONTEXT_TOOL_ABSTRACT =       "abstract"; // true for the prototype tools used only for inheritance by others
    static final String CONTEXT_TOOL_PRIORITY =       "priority"; // lower the value, first to run among otherwise equivalent report tools (taht do not change state)
    

    static final String CONTEXT_LINEBLOCK_TAG =           "line";
    static final String CONTEXT_LINEBLOCK_NAME_ATTR =     "name";
    static final String CONTEXT_LINEBLOCK_DEST_ATTR =     "dest";
    static final String CONTEXT_LINEBLOCK_SEP_ATTR =      "sep";
    
    static final String CONTEXT_LINEBLOCK_MARK_ATTR =     "mark";
    static final String CONTEXT_LINEBLOCK_ERRORS_ATTR =   "errors";
    static final String CONTEXT_LINEBLOCK_WARNINGS_ATTR = "warnings";
    static final String CONTEXT_LINEBLOCK_INFO_ATTR =     "info";
//Regular expressions for capturing hierarchical names    
    static final String CONTEXT_LINEBLOCK_INSTANCE_CAPTURE =  "instance-capture";
    static final String CONTEXT_LINEBLOCK_INSTANCE_SEPARATOR ="instance-separator";
    static final String CONTEXT_LINEBLOCK_INSTANCE_SUFFIX =   "instance-suffix";
    
    static final String CONTEXT_LINEBLOCK_PROMPT_ATTR =   "prompt";
    static final String CONTEXT_LINEBLOCK_INTERRUPT_ATTR ="interrupt";
    static final String CONTEXT_LINEBLOCK_STDERR_ATTR =   "stderr";
    static final String CONTEXT_LINEBLOCK_STDOUT_ATTR =   "stdout";
    static final String CONTEXT_LINEBLOCK_TIMEOUT_ATTR =  "timeout";
 
    static final String CONTEXT_LINEBLOCK_SUCCESS_ATTR =  "success";
    static final String CONTEXT_LINEBLOCK_FAILURE_ATTR =  "failure";
    static final String CONTEXT_LINEBLOCK_KEEP_OPEN_ATTR ="keep-open";
    static final String CONTEXT_LINEBLOCK_LOGPATH_ATTR =  "log";
    
    static final String CONTEXT_STRINGS_DELETE_TAG = "delete";
    static final String CONTEXT_STRINGS_INSERT_TAG = "insert";
    static final String CONTEXT_STRINGS_INSERT_AFTER_ATTR = "after";
    
    static final String MENU_TAG = "menu";
    static final String MENU_INHERITS_ATTR = "inherits";
    static final String MENU_AFTER_ATTR = "after";
    static final String MENU_NAME_ATTR = "name";
    static final String MENU_LABEL_ATTR = "label";
    static final String MENU_ICON_ATTR = "icon";
    static final String MENU_DESCRIPTION_ATTR = "tip";
    static final String MENU_VISIBLE_ATTR = "visible";

    static final String MENUITEM_TAG = "menuitem";
    static final String MENUITEM_CALL_ATTR = "call";
    static final String MENUITEM_INSTANCE_ATTR = "tool-instance";
    static final String MENUITEM_NAME_ATTR = MENU_NAME_ATTR;
    static final String MENUITEM_LABEL_ATTR = MENU_LABEL_ATTR;
    static final String MENUITEM_ICON_ATTR = MENU_ICON_ATTR;
    static final String MENUITEM_VISIBLE_ATTR = MENU_VISIBLE_ATTR;
    static final String MENUITEM_AFTER_ATTR = MENU_AFTER_ATTR;

    static final String CONDITION_IF_TAG = "if";
    static final String CONDITION_AND_TAG = "if-and";
    static final String CONDITION_NOT_TAG = "if-not";

    private static final int MAX_ERRORS_TO_SHOW = 4;
    
    private int errorCount = 0;

    private Node rootNode;
    private String currentConfigFileName;
    private String currentFileVersion;
        
    //
    // publics
    //

    public XMLConfig(String configDirectoryName) {
        try {
            readConfig(configDirectoryName);
            initConfig();
        } catch(Exception e) {
            MessageUI.error(e);
        }
    }
    
    public String getConfigFileName(){
    	return currentConfigFileName;
    }
    
    public void logError(Exception e) throws ConfigException {
        if(errorCount++ < MAX_ERRORS_TO_SHOW) {
            MessageUI.error("Error reading config file '" + currentConfigFileName + ". \n" +
                            "Reason: " + e.getMessage(), e);
        }
    }

    //
    // interface available in the package
    //

    ConditionalStringsList readConditionalStringsNode(Node node, 
                                                      Context context,
                                                      Condition initialCondition)
        throws ConfigException
    {
        ConditionalStringsNodeReader linesReader = new ConditionalStringsNodeReader(this, context);
    
        new ConditionNodeReader(this, context, linesReader, initialCondition).readNode(node);
                   
        return linesReader.getConditionalStrings();
    }
    
    ConditionalStringsList readDeleteStringsNode(Node node, 
                                                 Context context,
                                                 Condition initialCondition)
        throws ConfigException 
    {
        List<Node> deleteNodes = XMLConfig.findChildNodes(node, CONTEXT_STRINGS_DELETE_TAG);

        if(deleteNodes.isEmpty())
            return null;
        else if(deleteNodes.size() > 1)
            throw new ConfigException("More that one '" + CONTEXT_STRINGS_DELETE_TAG +
                                      "' section in context '" + context.getName() + 
                                      "' definition");

        return readConditionalStringsNode(deleteNodes.get(0), context, initialCondition);
    }
    
    List<NamedConditionalStringsList> readInsertStringsNode(Node node, 
                                                            Context context,
                                                            Condition initialCondition)
        throws ConfigException 
    {
        List<Node> insertNodes = XMLConfig.findChildNodes(node, CONTEXT_STRINGS_INSERT_TAG);
        
        if(insertNodes.isEmpty())
            return null;
        
        List<NamedConditionalStringsList> insertLinesList = new ArrayList<NamedConditionalStringsList>();
        
        for(int i = 0; i < insertNodes.size(); i++) {
            Node insertNode = insertNodes.get(i);
            
            String after = 
                XMLConfig.getAttributeValue(insertNode, CONTEXT_STRINGS_INSERT_AFTER_ATTR);

            if(after == null)
                throw new ConfigException("Attribute '" + CONTEXT_STRINGS_INSERT_AFTER_ATTR +
                                          "' in '" + CONTEXT_STRINGS_INSERT_TAG +
                                          "' section in lines block definition of context '" +
                                          context.getName() + "' definition is absent");
            
            ConditionalStringsList insertLines = 
                readConditionalStringsNode(insertNode, context, initialCondition);
         
            insertLinesList.add(new NamedConditionalStringsList(insertLines, after));
        }
        
        return insertLinesList;
    }
    
    static List<String> readStringsNode(Node node) {
        List<String> stringList = new ArrayList<String>(); 

        if(isTextNode(node)) {
            // we need the raw text in this node
            String text = node.getNodeValue();
            
            String[] lines = text.split("\"");
            
            // we need only lines enclosed in quotes
            // so in the 'lines' array we need only odd elems (1, 3, 5, ...)
            /*
            for(int k = 1; k < lines.length; k += 2) {
                stringList.add(lines[k]);
                if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
                	System.out.println ("got quoted string: \""+lines[k]+"\"");
            }
            */
            //Andrey: modified to accept \"
            String line=null;
            for (int k=1;k<lines.length;k++){
            	if (line ==null){
            		line=lines[k];
            	} else if (line.endsWith("\\")){ // So it was \"
            		line=line.substring(0,line.length()-1)+"\""+lines[k];
            	} else {
            		stringList.add(line);
            		line=null;
            	}
            }
        } else {    
            NodeList childNodes = node.getChildNodes();
            
            for(int i = 0; i < childNodes.getLength(); i++) {
                Node child = childNodes.item(i);
                
                if(isTextNode(child))
                    stringList.addAll(readStringsNode(child));
            }
        }
        
        return stringList;
    }
    
    static boolean isElemNode(Node node) {
        return node.getNodeType() == Node.ELEMENT_NODE;
    }
    
    static boolean isElemNode(Node node, String name) {
        return isElemNode(node) && (node.getNodeName().equals(name));
    }
    
    static boolean isTextNode(Node node) {
        return node.getNodeType() == Node.TEXT_NODE;
    }
    
    static List<Node> findChildNodes(Node node, String childNodeName) {
        return findChildNodes(node.getChildNodes(), childNodeName);
    }

    static List<Node> findChildNodes(NodeList nodes, String childNodeName) {
        List<Node> found = new ArrayList<Node>();
        
        for(int i = 0; i < nodes.getLength(); i++) {
            Node child = nodes.item(i);
            
            if(isElemNode(child, childNodeName))
                found.add(child);
        }            

        return found;
    }
    
    static String getAttributeValue(Node node, String attr) {
        NamedNodeMap attributes = node.getAttributes();
        
        if(attributes == null)
            return null;
        
        Node paramNode = attributes.getNamedItem(attr);
        return paramNode != null? paramNode.getNodeValue() : null;        
    }
    
    static void checkBoolAttr(String attr, String attrName) throws ConfigException {
        if(!BooleanUtils.isBoolean(attr))
            throw new ConfigException("Unknown value of the attribute '" + 
                                      attrName + 
                                      "': " + 
                                      attr);
    }
    
    static boolean getBoolAttrValue(String attr) {
        return BooleanUtils.isTrue(attr);
    }

    //
    // private stuff
    //

    private void readConfig(String directory) throws ConfigException,
                                                     IOException  
    {
        File cfgDir = new File(directory);
        
        if(!cfgDir.isDirectory())
            throw new IOException("Cannot read config directory '" + directory + "'");
        
        File[] configFiles = cfgDir.listFiles();
        
        for(int i = 0; i < configFiles.length; i++) {
            File configFile = configFiles[i];
            
            if(configFile.isFile()) {
                if(configFile.getName().toLowerCase().endsWith(CONFIG_EXTENSION.toLowerCase())) {
                    try {
                        readConfigFile(configFile);
                    } catch(Exception e) {
                        logError(e);
                    }
                }
            } else {
                readConfig(configFile.getAbsolutePath());
            }
        }
    }
    
    private void initConfig() throws ConfigException  {
        try {
            initControlInterfaces();
            contextManager.init(this);
            checkConfig();
            designMenuManager.init(this);
        } catch(ConfigException e) {
            throw new ConfigException("Error initializing config: " + e.getMessage(), e);
        }
        
        if(errorCount > 0) {
            throw new ConfigException("Some errors occured during config initialization.");
        }
    }

    private void initControlInterfaces() throws ConfigException {
        for(Iterator ci = controlInterfaceList.iterator(); ci.hasNext();)
            ((ControlInterface)ci.next()).init();
//Andrey: had to split in two separate passes, otherwise failed to initialize types
//when parent control interface was not yet set up (only name was known)
//Alternatively it was possible to check baseInterfaceName if baseInterface was null,
//Replace null name with BasicInterface, ...        
        for(Iterator ci = controlInterfaceList.iterator(); ci.hasNext();)
            ((ControlInterface)ci.next()).initTypes();
        
        
    }
    
    private void checkConfig() throws ConfigException {
        for(Iterator ci = controlInterfaceList.iterator(); ci.hasNext();) {
            ControlInterface interf = (ControlInterface)ci.next();
            
            interf.check();
        }
    }
   
    private void readConfigFile(File configFile) throws ConfigException,
                                                        ParserConfigurationException, 
                                                        SAXException, 
                                                        IOException  
    {
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
            System.out.println("Reading file '" + configFile + "'");
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(configFile);
        currentConfigFileName = configFile.getAbsolutePath();
        findRootNode(document);
        
        // read and tie control interface list
        List<ControlInterface> controlInterfaces = readInterfaceList(document);
        
        try {
            addControlInterfaceList(controlInterfaces);
        } catch(ConfigException e) {
            logError(e);
        }
        
        // try to find & read context layers
        List<InstallationContext> installationContexts = readContextList(document, ContextKind.INSTALLATION);
        List<ProjectContext> projectContexts = readContextList(document, ContextKind.PROJECT);
        List<PackageContext> packageContexts = readContextList(document, ContextKind.PACKAGE);
        List<Tool> toolContexts = readContextList(document, ContextKind.TOOL);
        
        try {
            contextManager.addInstallationContexts(installationContexts);
        } catch(ConfigException e) {
            logError(e);
        }

        try {
            contextManager.addProjectContexts(projectContexts);
        } catch(ConfigException e) {
            logError(e);
        }

        try {
            contextManager.addPackageContexts(packageContexts);
        } catch(ConfigException e) {
            logError(e);
        }

        try {
            contextManager.addToolContexts(toolContexts);
        } catch(ConfigException e) {
            logError(e);
        }
        
        List<DesignMenu> menuComponents = readDesignMenu(document);

        designMenuManager.addDesignMenuComponents(menuComponents);
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER))
        	System.out.println("Done reading file '" + configFile + "'");

    }

    private void addControlInterfaceList(List<ControlInterface> controlInterfaces)
        throws ConfigException
    {
        for(Iterator<ControlInterface> i = controlInterfaces.iterator(); i.hasNext();) {
            ControlInterface c = (ControlInterface)i.next();
            
            if(findControlInterface(c.getName()) != null)
                throw new ConfigException("Duplicating control interface '" + c.getName() + "'");
                
            this.controlInterfaceList.add(c);
        }
    }
    
    private String contextKindToTag(ContextKind contextKind) {
        switch(contextKind) {
            case INSTALLATION: return CONTEXT_INSTALLATION_TAG;
            case PROJECT:      return CONTEXT_PROJECT_TAG;
            case PACKAGE:      return CONTEXT_PACKAGE_TAG;
            case TOOL:         return CONTEXT_TOOL_TAG;

            default:
                assert false;
        }
        
        return null;
    }
    
    private <T extends Context> List<T> readContextList(Document document, ContextKind contextKind)
        throws ConfigException 
    {
        final String tag = contextKindToTag(contextKind);
        
        List<T> contextList = new ArrayList<T>();
        NodeList contextNodeList = document.getElementsByTagName(tag);

        for(int i = 0; i < contextNodeList.getLength(); i++) {
            Node contextNode = contextNodeList.item(i);

            try {
                contextList.add((T)readContextNode(contextNode, contextKind));
            } catch(ConfigException e) {
                logError(e);
            }
        }
        
        return contextList;
    }

    private Context readContextNode(Node contextNode, ContextKind contextKind)
        throws ConfigException
    {
        final String tag = contextKindToTag(contextKind);
        final String contextName = getAttributeValue(contextNode, CONTEXT_NAME_ATTR);

        if(contextName == null)
            throw new ConfigException("Context of type '" + tag + "' definition is bad: " +
                                      "attribute '" + CONTEXT_NAME_ATTR + "' is absent");

        NoAttrError err = new NoAttrError() {
            public String msg(String attr) {
                return "Context '" + contextName + 
                       "' (of type '" + tag + 
                       "') definition is bad: attribute '" + attr +
                       "' is absent";
            }
        };
        
        String contextIcon = getAttributeValue(contextNode, CONTEXT_ICON_ATTR);
        String contextInterfaceName = getAttributeValue(contextNode, CONTEXT_INTERFACE_ATTR);        
        String contextLabel = getAttributeValue(contextNode, CONTEXT_LABEL_ATTR);
        
        if(contextLabel == null)
        	contextLabel=contextName; // Use name as label /Andrey
//            throw new ConfigException(err.msg(CONTEXT_LABEL_ATTR));
        
        Context context = null;
        
        switch(contextKind) {
            case INSTALLATION:
                context = new InstallationContext(contextName, 
                                                  contextInterfaceName, 
                                                  contextLabel, 
                                                  contextIcon,
                                                  null,
                                                  null, 
                                                  null,
                                                  null);
                
                break;
                
            case PROJECT:      
                String packageName = getAttributeValue(contextNode, CONTEXT_PROJECT_PACKAGE_ATTR);
                String projectMenu = getAttributeValue(contextNode, CONTEXT_PROJECT_DESIGNMENU_ATTR);
                
                context = new ProjectContext(contextName, 
                                             contextInterfaceName,
                                             contextLabel,
                                             contextIcon,
                                             null,
                                             packageName,
                                             null, 
                                             null,
                                             null,
                                             projectMenu);
                
                break;
                
            case PACKAGE:      
                context = new PackageContext(contextName, 
                                             contextInterfaceName, 
                                             contextLabel, 
                                             contextIcon,
                                             null,
                                             null, 
                                             null,
                                             null);
                
                break;
    
            case TOOL:
                String toolBase    = getAttributeValue(contextNode, CONTEXT_TOOL_BASE_ATTR);
                String toolPackage = getAttributeValue(contextNode, CONTEXT_TOOL_PACKAGE_ATTR);
                String toolProject = getAttributeValue(contextNode, CONTEXT_TOOL_PROJECT_ATTR);                
                String toolExe     = getAttributeValue(contextNode, CONTEXT_TOOL_EXE_ATTR);
                String toolShell   = getAttributeValue(contextNode, CONTEXT_TOOL_SHELL_ATTR);
                
                String toolErrors   = getAttributeValue(contextNode, CONTEXT_TOOL_SYNTAX_ERRORS);
                String toolWarnings = getAttributeValue(contextNode, CONTEXT_TOOL_SYNTAX_WARNINGS);
                String toolInfo     = getAttributeValue(contextNode, CONTEXT_TOOL_SYNTAX_INFO);
                String ignoreFilter = getAttributeValue(contextNode, CONTEXT_TOOL_IGNORE_FILTER);
                String logDir =       getAttributeValue(contextNode, CONTEXT_TOOL_LOG_DIRECTORY);
                String stateDir =     getAttributeValue(contextNode, CONTEXT_TOOL_STATE_DIRECTORY);
                
                String disabled =     getAttributeValue(contextNode, CONTEXT_TOOL_DISABLED);
                String result =       getAttributeValue(contextNode, CONTEXT_TOOL_RESULT);
                String restore =      getAttributeValue(contextNode, CONTEXT_TOOL_RESTORE);
  
                String saveString =      getAttributeValue(contextNode, CONTEXT_TOOL_SAVE); 
                String autoSaveString =  getAttributeValue(contextNode, CONTEXT_TOOL_AUTOSAVE);

                String isAbstractAttr = getAttributeValue(contextNode, CONTEXT_TOOL_ABSTRACT);
                
                String priorityString = getAttributeValue(contextNode, CONTEXT_TOOL_PRIORITY);


                double priority=Double.NaN;;
                if (priorityString!=null){
                	try {
                		priority=Double.parseDouble(priorityString);
                	} catch (Exception e){
                        throw new ConfigException("Tool priority '" + contextName + 
                                "' has invalid priority string '"+priorityString+"' - floating point value is expected.");        
                	}
                }
                
                boolean isAbstract;
                if(isAbstractAttr != null) {
                    checkBoolAttr(isAbstractAttr, CONTEXT_TOOL_ABSTRACT);
                    isAbstract = getBoolAttrValue(isAbstractAttr);
                } else {
                	isAbstract = false;
                }
                
                boolean isShell=false;
                if (toolShell != null){
                	toolExe=toolShell;
                	isShell=true;
                }
                if((toolExe == null) && (toolBase == null)) // when tool inherits, it's exe will be inherited too - check!
                    throw new ConfigException(err.msg(CONTEXT_TOOL_EXE_ATTR));
//                if(toolShell == null) toolShell="";
                    
                List<String> toolExtensionsList = readToolExtensionsList(contextNode, contextName);
                List<RunFor> toolRunfor=  readToolRunForList(contextNode, contextName);
                List<String> toolDependsStates= readToolDependsList(contextNode, contextName,false);
                List<String> toolDependsFiles=  readToolDependsList(contextNode, contextName,true);
                
                if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
                	System.out.println("contextNode.getNodeValue()="+contextNode.getNodeValue());
                	System.out.println("toolPackage="+toolPackage);
                	System.out.println("toolProject="+toolProject);
                	System.out.println("toolExe="+toolExe);
                	System.out.println("toolShell="+toolShell);
                	if (toolRunfor!=null){
                		System.out.println("got toolRunfor.size()="+toolRunfor.size());
                	}
                }
              
                context = new Tool(contextName,
                                   contextInterfaceName, 
                                   contextLabel,
                                   contextIcon,
                                   toolBase,
                                   null,
                                   toolPackage,
                                   toolProject,
                                   toolExe,
                                   isShell,
                                   toolExtensionsList,
                                   toolErrors,
                                   toolWarnings,
                                   toolInfo,
                                   toolRunfor,
                                   ignoreFilter,
                                   toolDependsStates,
                                   toolDependsFiles,
                                   logDir,
                                   stateDir,
                                   disabled,
                                   result,
                                   restore,
                                   saveString, 
                                   autoSaveString,
                                   isAbstract,
                                   priority,
                                   null,
                                   null,
                                   null);
                break;
            default:
                throw new ConfigException("Internal error: unknown context kind '" + contextKind + "'");
        }

        //
        // we must read some stuff after the context is created, because the 
        // creation routines really DO need to have a reference to the context
        //
        List<Parameter> contextParams = readParameters(contextNode, context);
        List<CommandLinesBlock> contextCommandLinesBlocks = readCommandLinesBlocks(contextNode, context);  // Correct? for "project_parameters" in /vdt/tools/Verilog/IVerilog.xml
        // Each of contextParams (3 total, correct) has null context. Probably OK, same for the tool context
        ContextInputDefinition contextInputDefinition = readContextInputDefinition(contextNode, context);
        if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_OTHER)) {
        	if (contextKind==ContextKind.PROJECT){
        		System.out.println("processing project context");
        	}
        }
        context.setParams(contextParams);
        context.setParamGroups(contextInputDefinition.getParamGroups()); // "Project Properties", "General"
        context.setInputDialogLabel(contextInputDefinition.getLabel());
        context.setCommandLinesBlocks(contextCommandLinesBlocks);
        
        context.setVersion(currentFileVersion);
        
        return context;
    }
    
    private List<ControlInterface> readInterfaceList(Document document) throws ConfigException {
        NodeList interfaceNodeList = document.getElementsByTagName(INTERFACE_TAG);
        List<ControlInterface> controlInterfaceList = new ArrayList<ControlInterface>();

        // now read interface definitions
        for(int i = 0; i < interfaceNodeList.getLength(); i++) {
            Node interfaceNode = interfaceNodeList.item(i);

            if(interfaceNode.getNodeType() != Node.ELEMENT_NODE)
                continue;

            // extract interface name
            String interfaceName =
                getAttributeValue(interfaceNode, INTERFACE_NAME_ATTR);

            if(interfaceName == null)
                throw new ConfigException("Interface name is absent");
            
            String baseInterface = 
                getAttributeValue(interfaceNode, INTERFACE_EXTENDS_ATTR);

            // get and verify interface contents (it must be typedef and syntax lists)
            NodeList interfaceContents = interfaceNode.getChildNodes();

            try {
                List<TypeDef> typeDefList = readTypeDefList(interfaceContents, interfaceName);
                List<Syntax> syntaxList = readSyntaxList(interfaceContents, interfaceName);
    
                controlInterfaceList.add(
                        new ControlInterface(this,
                                             interfaceName, 
                                             baseInterface,
                                             typeDefList, 
                                             syntaxList));
            } catch(ConfigException e) {
                logError(e);
            }
        }
        
        return controlInterfaceList;
    }
    
    private List<TypeDef> readTypeDefList(NodeList interfaceContents, String interfaceName)
        throws ConfigException 
    {
        String interfaceInfo = "Interface '" + interfaceName + "'";
        
        List<TypeDef> typeDefList = new ArrayList<TypeDef>();
        List<Node> typedefNodes = findChildNodes(interfaceContents, TYPEDEF_TAG);
        
        for(Iterator<Node> ni = typedefNodes.iterator(); ni.hasNext();) {
            // extract and verify one type definition section
            Node node = (Node)ni.next();
        
            String nodeInfo = interfaceInfo + ", node '" + node.getNodeName() + "'";
            
            String name = getAttributeValue(node, TYPEDEF_NAME_ATTR);
            String isListAttr = getAttributeValue(node, TYPEDEF_LIST_ATTR);

            boolean isList;

            if(isListAttr != null) {
                checkBoolAttr(isListAttr, TYPEDEF_LIST_ATTR);
                isList = getBoolAttrValue(isListAttr);
            } else {
                isList = false;
            }
            
            if(name == null)
                throw new ConfigException(nodeInfo + ": Type name is absent");
            else if(name.length() == 0)
                throw new ConfigException(nodeInfo + ": Empty type name");

            // extract and verify the corresponding parameter type
            List<Node> paramTypeNodes = findChildNodes(node, TYPEDEF_PARAMTYPE_TAG);
            
            if(paramTypeNodes.isEmpty())
                throw new ConfigException(nodeInfo + ": Parameter type is absent");
            else if(paramTypeNodes.size() > 1)
                throw new ConfigException(nodeInfo + ": There must be only one parameter type");

            Node paramTypeNode = paramTypeNodes.get(0);

            // now read the parameter type attributes from this definition
            // and put it into the list
            typeDefList.add(
                    new TypeDef(name, readParamTypeNode(paramTypeNode, isList, name)));
        }
        
        return typeDefList;
    }
    
    private ParamType readParamTypeNode(Node paramTypeNode, boolean isList, String typeName)
        throws ConfigException 
    {
        String kind = getAttributeValue(paramTypeNode, TYPEDEF_PARAMTYPE_KIND_ATTR);
    
        if(kind == null)
            throw new ConfigException("Kind of type '" + typeName + "' is absent");

        ParamType paramType = null; 
        
        if(kind.equals(ParamTypeBool.NAME))
            paramType = createParamTypeBool(paramTypeNode, typeName);
        else if(kind.equals(ParamTypeEnum.NAME))
            paramType = createParamTypeEnum(paramTypeNode, typeName);
        else if(kind.equals(ParamTypeNumber.NAME))
            paramType = createParamTypeNumber(paramTypeNode, typeName);
        else if(kind.equals(ParamTypeString.NAME))
            paramType = createParamTypeString(paramTypeNode, isList, typeName);
        else
            throw new ConfigException("Unknown parameter type kind: " + kind + 
                                      "' for type '" + typeName + "'");

        return paramType;
    }
    
    private ParamType createParamTypeBool(Node paramTypeNode, String typeName)
        throws ConfigException 
    {
        String formatTrue  = getAttributeValue(paramTypeNode, ParamTypeBool.FORMAT_TRUE_ID);
        String formatFalse = getAttributeValue(paramTypeNode, ParamTypeBool.FORMAT_FALSE_ID);

        if(formatTrue == null || formatFalse == null)
            throw new ConfigException("Bad bool type '" + typeName + "' definition");
        
        return new ParamTypeBool(formatTrue, formatFalse);
    }
    
    private ParamType createParamTypeEnum(Node paramTypeNode, String typeName) 
        throws ConfigException 
    {
        String baseTypeName = getAttributeValue(paramTypeNode, ParamTypeEnum.BASETYPE_ID);

        if(baseTypeName == null)
            throw new ConfigException("Enum base type name is absent for type '" + typeName + "'");
        
        NodeList enumDefNodes = paramTypeNode.getChildNodes();
        
        List<String> enumElemLabels = new ArrayList<String>();
        List<String> enumElemValues = new ArrayList<String>();
        
        // read the enum definition block
        for(int i = 0; i < enumDefNodes.getLength(); i++) {
            Node node = enumDefNodes.item(i);
            
            if(isElemNode(node, ENUM_ELEMENT_TAG)) {
                String label = getAttributeValue(node, ParamTypeEnum.ELEM_LABEL_ID);
                String value = getAttributeValue(node, ParamTypeEnum.ELEM_VALUE_ID);
    
                if(value == null)
                    throw new ConfigException("Element value for enum type '" + typeName + "' is absent");
                else if(enumElemLabels.contains(label))
                    throw new ConfigException("Element label for enum type '" + typeName + "' cannot be specified more than once");
    
                // if label is not specified, use the read value instead
                if(label == null)
                    label = new String(value); 
                        
                enumElemLabels.add(label);
                enumElemValues.add(value);
            }
        }
        
        if(enumElemLabels.isEmpty())
            throw new ConfigException("Enum label/value list is empty");            
    
        return new ParamTypeEnum(baseTypeName,
                                 (String[])enumElemLabels.toArray(new String[enumElemLabels.size()]),
                                 (String[])enumElemValues.toArray(new String[enumElemValues.size()]));
    }
    
    private ParamType createParamTypeNumber(Node paramTypeNode, String typeName) 
        throws ConfigException 
    {
        String lo     = getAttributeValue(paramTypeNode, ParamTypeNumber.LO_ID);
        String hi     = getAttributeValue(paramTypeNode, ParamTypeNumber.HI_ID);
        String format = getAttributeValue(paramTypeNode, ParamTypeNumber.FORMAT_ID);

        if(hi == null || lo == null || format == null)
            throw new ConfigException("Bad number parameter type '" + typeName + 
                                      "' definition: one or more of attributes '" + 
                                      ParamTypeNumber.LO_ID + "', '" + 
                                      ParamTypeNumber.HI_ID + "', '" + 
                                      ParamTypeNumber.FORMAT_ID + "' are absent");
        
        return new ParamTypeNumber(Integer.parseInt(lo),
                                   Integer.parseInt(hi),
                                   format);
    }

    private ParamType createParamTypeString(Node paramTypeNode, boolean isList, String typeName) 
        throws ConfigException 
    {
        String maxLength     = getAttributeValue(paramTypeNode, ParamTypeString.MAX_LENGTH_ID);
        String caseSensitive = getAttributeValue(paramTypeNode, ParamTypeString.CASE_SENSITIVITY_ID);
        String textKind      = getAttributeValue(paramTypeNode, ParamTypeString.KIND_ID);
        String filemask      = getAttributeValue(paramTypeNode, ParamTypeString.FILEMASK_ID);

        if(caseSensitive == null)
            throw new ConfigException("Attribute '" + ParamTypeString.CASE_SENSITIVITY_ID + 
                                      "' of string type '" + typeName + 
                                      "' is absent");

        ParamTypeString.CASE caseSensitiveFlag;
        
        if(caseSensitive.equals(ParamTypeString.CASE_SENSITIVE_ID))
            caseSensitiveFlag = ParamTypeString.CASE.SENSITIVE;
        else if(caseSensitive.equals(ParamTypeString.CASE_INSENSITIVE_ID))
            caseSensitiveFlag = ParamTypeString.CASE.INSENSITIVE;
        else if(caseSensitive.equals(ParamTypeString.CASE_UPPERCASE_ID))
            caseSensitiveFlag = ParamTypeString.CASE.UPPERCASE;
        else if(caseSensitive.equals(ParamTypeString.CASE_LOWERCASE_ID))
            caseSensitiveFlag = ParamTypeString.CASE.LOWERCASE;
        else
            throw new ConfigException("Unknown value of " + 
                                      ParamTypeString.CASE_SENSITIVE_ID + 
                                      " attribute in the type '" + typeName + 
                                      "' definition");

        int maxLengthValue = maxLength != null? Integer.parseInt(maxLength) : ParamTypeString.DEFAULT_LENGTH;
        
        if(textKind == null) {
            return new ParamTypeString(isList, maxLengthValue, caseSensitiveFlag);
        } else {
            ParamTypeString.KIND textKindValue;
            
            if(textKind.equals(ParamTypeString.KIND_FILE_ID))
                textKindValue = ParamTypeString.KIND.FILE;
            else if(textKind.equals(ParamTypeString.KIND_DIR_ID))
                textKindValue = ParamTypeString.KIND.DIR;
            else if(textKind.equals(ParamTypeString.KIND_TEXT_ID))
                textKindValue = ParamTypeString.KIND.TEXT;
            else
                throw new ConfigException("Unknown value of " + ParamTypeString.KIND_ID + 
                                          " attribute in the type '" + typeName + 
                                          "' definition");
            
            return new ParamTypeString(isList,
                                       maxLengthValue,
                                       caseSensitiveFlag,
                                       textKindValue,
                                       filemask);
        }
    }
    
    private List<Syntax> readSyntaxList(NodeList interfaceContents, String interfaceName) 
        throws ConfigException 
    {
        List<Syntax> syntaxList = new ArrayList<Syntax>();
        List<Node> syntaxNodes = findChildNodes(interfaceContents, SYNTAX_TAG);

        for(Iterator<Node> n = syntaxNodes.iterator(); n.hasNext();) {
            try {
                syntaxList.add(readSyntaxNode((Node)n.next(), interfaceName));
            } catch(ConfigException e) {
                logError(e);
            }
        }
        
        return syntaxList;
    }
    
    private Syntax readSyntaxNode(Node syntaxNode, String interfaceName)
        throws ConfigException 
    {
        String nodeInfo = "Interface '" + interfaceName + 
                          "', node '" + syntaxNode.getNodeName() + "'";
                    
        String name   = getAttributeValue(syntaxNode, SYNTAX_NAME_ATTR);
        String format = getAttributeValue(syntaxNode, SYNTAX_FORMAT_ATTR);
        String type   = getAttributeValue(syntaxNode, SYNTAX_TYPE_ATTR);

        if(name == null)
            throw new ConfigException(nodeInfo + ": Syntax name is absent");
        else if(format == null)
            throw new ConfigException(nodeInfo + ": Syntax format is absent");

        return new Syntax(name, format, type);
    }
    
    private List<String> readToolExtensionsList(Node toolNode, String toolName)
        throws ConfigException 
    {
        String toolInfo = "Tool '" + toolName + "'";
        
        List<String> extList = new ArrayList<String>();
        List<Node> extListNodes = findChildNodes(toolNode, CONTEXT_TOOL_EXTENSIONS_LIST_TAG);
    
        if(extListNodes.isEmpty())
            return null;
           
        if(extListNodes.size() > 1)
            throw new ConfigException(toolInfo + 
                                      " definition cannot contain several '" + 
                                      CONTEXT_TOOL_EXTENSIONS_LIST_TAG + 
                                      "' nodes");
                
        Node extListNode = extListNodes.get(0);
        List<Node> extNodes = findChildNodes(extListNode, CONTEXT_TOOL_EXTENSION_TAG);
        
        for(Iterator<Node> n = extNodes.iterator(); n.hasNext();) {
            Node node = (Node)n.next();
            String ext = getAttributeValue(node, CONTEXT_TOOL_EXTENSION_MASK_ATTR);
            
            if(ext == null)
                throw new ConfigException(toolInfo + ": Attribute '" + CONTEXT_TOOL_EXTENSION_MASK_ATTR + "' is absent");

            extList.add(ext);
        }
        return extList;
    }
    
    private List<String> readToolDependsList(Node toolNode, String toolName, boolean filesNotStates)
            throws ConfigException 
        {
    	    String filesStateTag=   filesNotStates?CONTEXT_TOOL_DEPENDS_FILES_TAG:CONTEXT_TOOL_DEPENDS_STATE_TAG;
    	    String otherTag=     (!filesNotStates)?CONTEXT_TOOL_DEPENDS_FILES_TAG:CONTEXT_TOOL_DEPENDS_STATE_TAG;
    	
            String toolInfo = "Tool '" + toolName + "'";
            
            List<String> depList = new ArrayList<String>();
            List<Node> depListNodes = findChildNodes(toolNode, CONTEXT_TOOL_DEPENDS_LIST_TAG);
        
            if(depListNodes.isEmpty())
                return null;
               
            if(depListNodes.size() > 1)
                throw new ConfigException(toolInfo + 
                                          " definition cannot contain several '" + 
                                          CONTEXT_TOOL_DEPENDS_LIST_TAG + 
                                          "' nodes");
            Node depListNode = depListNodes.get(0);
            List<Node> depNodes = findChildNodes(depListNode, CONTEXT_TOOL_DEPENDS_TAG);
// TODO: allow here other types of dependencies (conditionals(source files)            
            for(Iterator<Node> n = depNodes.iterator(); n.hasNext();) {
                Node node = (Node)n.next();
                String dep = getAttributeValue(node, filesStateTag);
                if (dep != null) {
                	depList.add(dep);
                } else if (getAttributeValue(node, otherTag)==null){
                    throw new ConfigException(toolInfo + ": Both alternative attributes '" + CONTEXT_TOOL_DEPENDS_FILES_TAG +
                    		" and '"+CONTEXT_TOOL_DEPENDS_STATE_TAG +"' are absent");
                	
                }
            }
            return depList;
        }

    private  List<RunFor> readToolRunForList(Node toolNode, String toolName) throws ConfigException {
    	String toolInfo = "Tool '" + toolName + "'";

    	List<RunFor> runForList = new ArrayList<RunFor>();
    	List<Node> runForNodesList = findChildNodes(toolNode, CONTEXT_TOOL_ACTION_LIST_TAG);

    	if(runForNodesList.isEmpty()) {
    		runForList.add(new RunFor(
    				CONTEXT_TOOL_DFLT_ACTION_LABEL,
    				CONTEXT_TOOL_DFLT_ACTION_RESOURCE,
    				getBoolAttrValue(CONTEXT_TOOL_DFLT_ACTION_CHECK_EXTENSION),
    				getBoolAttrValue(CONTEXT_TOOL_DFLT_ACTION_CHECK_EXISTENCE),
    				null));

    	} else {            
    		if(runForNodesList.size() > 1)
    			throw new ConfigException(toolInfo + 
    					" definition cannot contain several '" + 
    					CONTEXT_TOOL_ACTION_LIST_TAG + 
    					"' nodes");

    		Node runForNode = runForNodesList.get(0);
    		List<Node> runForNodes = findChildNodes(runForNode, CONTEXT_TOOL_ACTION_TAG);

    		for(Iterator<Node> n = runForNodes.iterator(); n.hasNext();) {
    			Node node = (Node)n.next();
    			boolean checkExtension=false; // for empty resource field - do not check file or extensions
    			boolean checkExistence=false;
    			String label = getAttributeValue(node, CONTEXT_TOOL_ACTION_LABEL);
    			if (label == null)
    				label=CONTEXT_TOOL_DFLT_ACTION_LABEL;
    			String icon = getAttributeValue(node, CONTEXT_TOOL_ACTION_ICON);
    			String resource = getAttributeValue(node, CONTEXT_TOOL_ACTION_RESOURCE);
    			if ((resource == null) || (resource.length()==0)) {
    				//    				resource= CONTEXT_TOOL_DFLT_ACTION_RESOURCE;
    				resource = "";
    			} else {
    				String checkExtensionAttr=getAttributeValue(node, CONTEXT_TOOL_ACTION_CHECK_EXTENSION);
    				if (checkExtensionAttr==null){
    					checkExtensionAttr=CONTEXT_TOOL_DFLT_ACTION_CHECK_EXTENSION;
    				}
    				checkBoolAttr(checkExtensionAttr, CONTEXT_TOOL_ACTION_CHECK_EXTENSION);
    				checkExtension=getBoolAttrValue(checkExtensionAttr);
    				String checkExistenceAttr=getAttributeValue(node, CONTEXT_TOOL_ACTION_CHECK_EXISTENCE);
    				if (checkExistenceAttr==null){
    					checkExistenceAttr=CONTEXT_TOOL_DFLT_ACTION_CHECK_EXISTENCE;
    				}
    				checkBoolAttr(checkExistenceAttr, CONTEXT_TOOL_ACTION_CHECK_EXISTENCE);
    				checkExistence=getBoolAttrValue(checkExistenceAttr);
    			}
    			runForList.add(new RunFor(label, resource, checkExtension, checkExistence, icon));
    		}
    	}
    	return runForList;
    }

    private List<Parameter> readParameters(Node node, Context context)
        throws ConfigException 
    {
        ParamNodeReader paramNodeReader = new ParamNodeReader(this, context);
        
        new ConditionNodeReader(this, context, paramNodeReader).readNode(node);
                       
        return paramNodeReader.getParamList();
    }

    /////
    
    private List<DesignMenu> readDesignMenu(Document document) throws ConfigException {
        List<Node> menuNodes = findChildNodes(rootNode, MENU_TAG);
        List<DesignMenu> menuList = new ArrayList<DesignMenu>();
        
        for(Iterator<Node> node = menuNodes.iterator(); node.hasNext();) {
            try {
                menuList.add(readDesignMenuNode((Node)node.next(), null));
            } catch(ConfigException e) {
                logError(e);
            }
        }
        
        return menuList;
    }
    
    private DesignMenu readDesignMenuNode(Node menuNode, DesignMenu parentMenu)
        throws ConfigException 
    {
        final String menuName = getAttributeValue(menuNode, MENU_NAME_ATTR);

        if(menuName == null)
            throw new ConfigException("Menu must have '" + MENU_NAME_ATTR + "' attribute");
        
        String menuInherits = getAttributeValue(menuNode, MENU_INHERITS_ATTR);
        String menuAfter    = getAttributeValue(menuNode, MENU_AFTER_ATTR);
        String menuLabel    = getAttributeValue(menuNode, MENU_LABEL_ATTR);
        String menuIcon     = getAttributeValue(menuNode, MENU_ICON_ATTR);
        String menuDescr    = getAttributeValue(menuNode, MENU_DESCRIPTION_ATTR);
        String menuVisible  = getAttributeValue(menuNode, MENU_VISIBLE_ATTR);
        
        Boolean isVisible = null;

        if(menuVisible != null) {
            checkBoolAttr(menuVisible, MENU_VISIBLE_ATTR);
            isVisible = getBoolAttrValue(menuVisible);
        }
        
        List<DesignMenuItem> menuItems = new ArrayList<DesignMenuItem>();
        
        DesignMenu designMenu = new DesignMenu(this,
                                               parentMenu,
                                               menuInherits,
                                               menuAfter,
                                               menuName,                        
                                               menuLabel,
                                               menuIcon,
                                               menuDescr,
                                               isVisible,
                                               menuItems);

        for(int i = 0; i < menuNode.getChildNodes().getLength(); i++) {
            Node child = menuNode.getChildNodes().item(i);
            
            try {
                if(isElemNode(child, MENUITEM_TAG)) {
                    menuItems.add(readDesignMenuItemNode(child, designMenu));
                } else if(isElemNode(child, MENU_TAG)) {
                    menuItems.add(readDesignMenuNode(child, designMenu));
                }
            } catch(ConfigException e) {
                logError(e);
            }
        }            
        
        return designMenu;
    }

    private DesignMenuItem readDesignMenuItemNode(Node menuItemNode, final DesignMenu parentMenu) 
        throws ConfigException 
    {
        final String itemName = getAttributeValue(menuItemNode, MENUITEM_NAME_ATTR);
        
        if(itemName == null)
            throw new ConfigException("Menu '" + parentMenu.getName() + 
                                      "' item must have '" + MENUITEM_NAME_ATTR +
                                      "' attribute");

        String itemLabel    = getAttributeValue(menuItemNode, MENUITEM_LABEL_ATTR);
        String itemIcon     = getAttributeValue(menuItemNode, MENUITEM_ICON_ATTR);
        String itemCall     = getAttributeValue(menuItemNode, MENUITEM_CALL_ATTR);
        String itemInstance = getAttributeValue(menuItemNode, MENUITEM_INSTANCE_ATTR);
        
        String itemVisible  = getAttributeValue(menuItemNode, MENUITEM_VISIBLE_ATTR);
        String itemAfter    = getAttributeValue(menuItemNode, MENUITEM_AFTER_ATTR);
        
        Boolean isVisible = null;

        if(itemVisible != null) {
            checkBoolAttr(itemVisible, MENUITEM_VISIBLE_ATTR);
            isVisible = getBoolAttrValue(itemVisible);
        }
        
        return new DesignMenuToolItem(this,
                                      parentMenu,
                                      itemAfter,
                                      itemName,
                                      itemLabel,
                                      itemIcon,
                                      isVisible,
                                      itemCall,
                                      itemInstance);
    }

    private List<CommandLinesBlock> readCommandLinesBlocks(Node node, Context context)
        throws ConfigException 
    {
        List<CommandLinesBlock> commandLinesBlocks = new ArrayList<CommandLinesBlock>();
        List<Node> outputNodes = findChildNodes(node, CONTEXT_OUTPUT_SECTION_TAG);
        
        if(outputNodes.isEmpty())
            return commandLinesBlocks;
        else if(outputNodes.size() > 1)
            throw new ConfigException("Context '" + context.getName() + 
                                      "' definition cannot contain several '" + 
                                      CONTEXT_OUTPUT_SECTION_TAG + 
                                      "' nodes");
        
        Node outputNode = outputNodes.get(0);

        CommandLinesNodeReader commandLinesNodeReader = new CommandLinesNodeReader(this, context);
        
        new ConditionNodeReader(this, context, commandLinesNodeReader).readNode(outputNode);
                       
        return commandLinesNodeReader.getCommandLinesBlocks();
    }

    private ContextInputDefinition readContextInputDefinition(Node node, Context context)
        throws ConfigException 
    {
        List<ParamGroup> paramGroups = new ArrayList<ParamGroup>();
        List<Node> inputNodes = findChildNodes(node, CONTEXT_INPUT_SECTION_TAG);
        
        if(inputNodes.isEmpty())
            return new ContextInputDefinition(null, paramGroups);
        else if(inputNodes.size() > 1)
            throw new ConfigException("Context '" + context.getName() + 
                                      "' input definition cannot contain several '" + 
                                      CONTEXT_INPUT_SECTION_TAG + 
                                      "' nodes");
     
        Node inputNode = inputNodes.get(0); 
        String label = getAttributeValue(inputNode, CONTEXT_INPUT_SECTION_LABEL_ATTR);
        
        ParamGroupNodeReader paramGroupNodeReader = new ParamGroupNodeReader(this, context);
        
        new ConditionNodeReader(this, context, paramGroupNodeReader).readNode(inputNode);
                       
        return new ContextInputDefinition(label, paramGroupNodeReader.getParamGroups());
    }
    
    /////

    private void findRootNode(Document document) throws ConfigException {
        NodeList roots = document.getChildNodes();
        int rootsNum = 0;
        
        for(int i = 0; i < roots.getLength(); i++) {
            Node node = roots.item(i);
            
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                if((++rootsNum > 1) || !node.getNodeName().equals(ROOT_NODE_TAG))
                    throw new ConfigException("Config file must contain only one root node '" + 
                                              ROOT_NODE_TAG + "'");
                
                rootNode = node;
                
                currentFileVersion = getAttributeValue(node, ROOT_VERSION_ATTR);
            }
        }
        
        if(rootsNum == 0)
            throw new ConfigException("Config file must contain root node '" + 
                                      ROOT_NODE_TAG + "'");
    }
}


abstract class NoAttrError {
    abstract String msg(String attr); 
}
