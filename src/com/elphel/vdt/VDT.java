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
package com.elphel.vdt;

import org.eclipse.core.runtime.QualifiedName;

import com.elphel.vdt.veditor.HdlNature;

/**
 * Central access point for the VDT plug-in (id <code>"com.elphel.vdt.ui"</code>).
 *
 * Created: 02.12.2005
 * @author  Lvov Konstantin
 */
public class VDT {
        
    private VDT() {
        // prevent instantiation of JavaUI.
    }
        
    //-------------------------------------------------------------------------
    //                        Plug-in Constant
    //-------------------------------------------------------------------------
        
    /** The id of the VDT plug-in */    
    public static final String ID_VDT = "com.elphel.vdt";

    /** The id of the VDT perspective */
//    public static final String ID_PERSPECTIVE = ID_VDT + ".ui.Perspective";
        
    /** The identifier for the Verilog nature */
    
//??????????????????????????????    
//    public static final String VERILOG_NATURE_ID  = ID_VDT + ".VerilogNature";
    public static final String VERILOG_NATURE_ID  = HdlNature.NATURE_ID; // TODO
    public static final String PREFERENCE_PAGE_ID = ID_VDT + ".ui.preferences.PreferencePage";
        
    //-------------------------------------------------------------------------
    //                          View Identificators
    //-------------------------------------------------------------------------

    /** The id of the Design Flow view */
    public static final String ID_DESINGFLOW_VIEW = ID_VDT + ".ui.views.DesignFlowView"; 
        
    /** The id of the Verilog Navigator view */
//    public static final String ID_NAVIGATOR_VIEW = ID_VDT + ".ui.views.VerilogModuleView"; 

    //-------------------------------------------------------------------------
    //                          Vizard Identificators
    //-------------------------------------------------------------------------

    /** The id of the New Verilog Project wizard */
//    public static final String ID_NEW_PROJECT_WIZARD = ID_VDT + ".ui.wizards.NewProjectWizard"; 
    
    /** The id of the New Verilog Module wizard */
//    public static final String ID_NEW_MODULE_WIZARD = ID_VDT + ".ui.wizards.NewModuleWizard"; 


    //-------------------------------------------------------------------------
    //                        Launch Configuration Attribute
    //-------------------------------------------------------------------------

    /**
     * Identifier for the VDT launch configuration type
     * (value <code>"com.elphel.vdt.launchConfigurationType"</code>).
     */
    public static final String ID_DEFAULT_LAUNCH_TYPE = ID_VDT + ".launchConfigurationType";
    
    /**
     * String attribute identifying the location of an external. Default value
     * is <code>null</code>. Encoding is tool specific.
     */
    public static final String ATTR_TOOL_TO_LAUNCH = ID_VDT + ".ATTR_TOOL_TO_LAUNCH";
    public static final String ATTR_TOOL_IS_SHELL = ID_VDT + ".ATTR_TOOL_IS_SHELL";

    public static final String ATTR_TOOL_ID = ID_VDT + ".ATTR_TOOL_ID";

    public static final String ATTR_TOOL_COMMAND_LINE_ARGUMENTS = ID_VDT + ".ATTR_TOOL_COMMAND_LINE";
    
    public static final String ATTR_RESOURCE_TO_LAUNCH = ID_VDT + ".ATTR_PROJECT_TO_LAUNCH";

    public static final String ATTR_WORKING_DIRECTORY = ID_VDT + ".ATTR_WORKING_DIRECTORY";
    
    public static final String ATTR_PROJECT_PATH =      ID_VDT + ".ATTR_PROJECT_PATH";

    public static final String ATTR_TOOL_ERRORS =   ID_VDT + ".ATTR_TOOL_ERRORS";
    public static final String ATTR_TOOL_WARNINGS = ID_VDT + ".ATTR_TOOL_WARNINGS";
    public static final String ATTR_TOOL_INFO =     ID_VDT + ".ATTR_TOOL_INFO";

    public static final String ATTR_LOG_BUILD_STAMP =     ID_VDT + ".ATTR_LOG_BUILD_STAMP";
    public static final String ATTR_TOOL_STATE_FILE =     ID_VDT + ".TOOL_STATE_FILE"; // name of state (snapshot)  file to restore passed at tool launch

    
    /**
     * Identifier for verilog tools launch configuration group. The verilog
     * tools launch configuration group corresponds to the verilog tools
     * category in run mode.
     */
    public static final String ID_VERILOG_TOOLS_LAUNCH_GROUP = ID_VDT +".launchGroup";


    public static final String COMMAND_OPEN_VERILOG_TOOLS_LAUNCH_DIALOG = ID_VDT +".commands.OpenVerilogToolsConfigurations";


    public static final String ATTR_LIST_OF_PARAMETERS_ATTRIBUTE_NAMES = ID_VDT + ".ATTR_LIST_OF_PARAMETERS_ATTRIBUTE_NAMES";
    
    
    public static final String VARIABLE_RESOURCE_NAME = "${folder_prompt}"; 

    
    //-------------------------------------------------------------------------
    //                        Persistent Options 
    //-------------------------------------------------------------------------

    public static final String OPTION_PROJECT_MENU = ID_VDT + ".PROJECT_DESING_MENU";

    public static final QualifiedName OPTION_XLINX_DEVICE_FAMALY  = new QualifiedName(ID_VDT, "OPTION_XLINX_DEVICE_FAMALY");
    public static final QualifiedName OPTION_XLINX_DEVICE         = new QualifiedName(ID_VDT, "OPTION_XLINX_DEVICE");
    public static final QualifiedName OPTION_XLINX_DEVICE_PACKAGE = new QualifiedName(ID_VDT, "OPTION_XLINX_DEVICE_PACKAGE");
    public static final QualifiedName OPTION_XLINX_DEVICE_SPEED_GRADE = new QualifiedName(ID_VDT, "OPTION_XLINX_DEVICE_SPEED_GRADE");
    
    //-------------------------------------------------------------------------
    //                  Build in generators for tool description 
    //-------------------------------------------------------------------------

    public static final String GENERATOR_ID_TOOL_NAME     = "ToolName"; 
    public static final String GENERATOR_ID_PROJECT_NAME  = "ProjectName"; 
    public static final String GENERATOR_ID_EXE_PATH      = "ExePath"; 
    public static final String GENERATOR_ID_PROJECT_PATH  = "ProjectPath"; 
    public static final String GENERATOR_ID_SOURCE_LIST   = "SourceList"; 
    public static final String GENERATOR_ID_FILE_LIST     = "FileList"; 
    public static final String GENERATOR_ID_TOP_MODULE    = "TopModule"; 
    public static final String GENERATOR_ID_TOP_MODULES   = "TopModules"; 
    public static final String GENERATOR_ID_OS_NAME       = "OS";
    public static final String GENERATOR_ID_SELECTED_FILE = "SelectedFile"; 
    public static final String GENERATOR_ID_CURRENT_FILE  = "CurrentFile"; 
    public static final String GENERATOR_ID_CURRENT_BASE  = "CurrentFileBase"; 
    public static final String GENERATOR_ID_CHOSEN_ACTION = "ChosenActionIndex"; 
    public static final String GENERATOR_ID_BUILD_STAMP   = "BuildStamp"; 
    public static final String GENERATOR_ID_USERNAME      = "UserName"; 
    public static final String GENERATOR_ID_BLANK         = "Blank"; 
    public static final String GENERATOR_ID_NEWLINE       = "NewLine"; 

    public static final String GENERATOR_ID_STATE_DIR     = "StateDir"; 
    public static final String GENERATOR_ID_STATE_FILE    = "StateFile"; 

    public static final String GENERATOR_PARSERS_PATH     = "ParsersPath";
    public static final String GENERATOR_PLUGIN_ROOT      = "PluginRoot";
    public static final String PATH_TO_PARSERS            = "parsers";

    public static final String TIME_STAMP_FORMAT          = "yyyyMMddHHmmssSSS";


} // class VDT
