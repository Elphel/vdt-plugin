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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IFolder;
//import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
//import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.elphel.vdt.Txt;
import com.elphel.vdt.VDT;
import com.elphel.vdt.VerilogUtils;
// import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.veditor.preference.PreferenceStrings;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.contexts.BuildParamsItem;
import com.elphel.vdt.core.tools.params.Parameter;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.ToolException;
//import com.elphel.vdt.core.verilog.VerilogUtils;
import com.elphel.vdt.ui.MessageUI;

/**
 * Utilities for Verilog development tool launch configurations.
 * 
 * Created: 23.12.2005
 * @author  Lvov Konstantin
 */

public class VDTLaunchUtil {
	public final static int CLOSE_INPUT_STREAM_DELAY = 1000; // ms
    private static VDTRunner toolRunner;

    /**
     * Returns the VDT runner.
     */
    public static VDTRunner getRunner() {
    	if (toolRunner == null) {
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
    			System.out.println ("Created new VDTRunner()");
    		}
    		toolRunner = new VDTRunner();
    	} else {
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
    			System.out.println ("Reused old VDTRunner()");
    		}
    	}
    	return toolRunner;
    }
    //runningBuilds
    /**
     * Construct attribute name for command line representations of 
     * tool parameter.
     * 
     * @return an attribute name for tool parameter
     */  
    public static String getLaunchAttributeName(Parameter toolParameter) {
        return "ATTR_" + toolParameter.getID();   
    }
    
    /**
     * Expands and returns the location attribute of the given launch
     * configuration. The location is verified to point to an existing 
     * file, in the local file system.
     * 
     * @param configuration launch configuration
     * @return an absolute path to a file in the local file system  
     * @throws CoreException if unable to retrieve the associated launch
     * configuration attribute, if unable to resolve any variables, or if the
     * resolved location does not point to an existing file in the local file
     * system
     */
    public static IPath getToolToLaunch(ILaunchConfiguration configuration) throws CoreException {
        String location = configuration.getAttribute(VDT.ATTR_TOOL_TO_LAUNCH, (String) null);
        if (location == null) {
            abort(Txt.s("Launch.Error.ToolLocation", new String[] { configuration.getName()}), null);
        } else {
            String expandedLocation = getStringVariableManager().performStringSubstitution(location);
            if (expandedLocation == null || expandedLocation.length() == 0) {
                String msg = Txt.s("Launch.Error.InvalidLocation", new Object[] { configuration.getName()});
                abort(msg, null);
            } else {
                File file = new File(expandedLocation);
                if (file.isFile()) {
                    return new Path(expandedLocation);
                } 
                String msg = Txt.s("Launch.Error.InvalidLocation", new Object[] { configuration.getName()});
                abort(msg, null);
            }
        } 
        // execution will not reach here
        return null;
    } // getToolToLaunch

    
    /**
     * Returns the arguments attribute of the given launch
     * configuration. Returns <code>null</code> if arguments are not specified.
     * 
     * @param configuration launch configuration
     * @return a list of resolved arguments, or <code>null</code> if unspecified
     * @throws CoreException if unable to retrieve the associated launch
     * configuration attribute, or if unable to resolve any variables
     */
//     public static List<String> getArguments(ILaunchConfiguration configuration) throws CoreException {
       public static BuildParamsItem[] getArguments(ILaunchConfiguration configuration) throws CoreException {
        Tool tool = obtainTool(configuration);
        
        for (Iterator i = tool.getParams().iterator(); i.hasNext(); ) {
            Parameter param = (Parameter)i.next();
            String valueAttrName = LaunchCore.getValueAttributeName(param);
            
            try {
                if(param.getType().isList()) {
                    List<String> value = 
                        configuration.getAttribute(valueAttrName, param.getDefaultValue());
                    param.setCurrentValue(value);
                } else {
                    String value = configuration.getAttribute(valueAttrName, param.getDefaultValue().get(0));
                    param.setCurrentValue(value);
                }
            } catch(ToolException e) {
                MessageUI.error("Error occured during tool launch: " + e.getMessage(), e);
                return null;
            }
        }
        
        try {
            String location = getWorkingDirectory(configuration);
            tool.setWorkingDirectory(location);

            BuildParamsItem[] paramItemsArray = tool.buildParams();
    		if (VerilogPlugin.getPreferenceBoolean(PreferenceStrings.DEBUG_LAUNCHING)) {
    			System.out.println("called tool.buildParams() here (from VDTLaunchUtils.java)");
    		}
    		return paramItemsArray;

        } catch(ToolException e) {
            MessageUI.error("Error occured during tool launch: " + e.getMessage(), e);
        }

        return null;
    } // getArguments()

     public static boolean getIsShell(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         return tool.getIsShell();
     }
     
     public static String getToolProjectPath(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         return tool.getToolProjectPath();
     }
     
     
     public static String getPatternErrors(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         return tool.getPatternErrors();
     }

     public static String getToolName(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         return tool.getName();
     }

     
     public static String getPatternWarnings(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         return tool.getPatternWarnings();
     }

     public static String getPatternInfo(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         return tool.getPatternInfo();
     }
     
     public static String getToolLogDir(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         return tool.getLogDir();
     }
     
     public static List<String> getControlFiles(ILaunchConfiguration configuration) throws CoreException {
         Tool tool = obtainTool(configuration);
         
         return tool.getCreatedControlFiles();
     }
     
    /**
     * Returns the resources attribute of the given launch
     * configuration. Returns <code>null</code> if resources are not specified.
     * 
     * @param configuration launch configuration
     * @return a list of resolved resources, or <code>null</code> if unspecified
     * @throws CoreException if unable to retrieve the associated launch
     * configuration attribute, or if unable to resolve any variables
     */
    public static List<String> getResources(ILaunchConfiguration configuration) throws CoreException {
        String resourceAttr = configuration.getAttribute(VDT.ATTR_RESOURCE_TO_LAUNCH, (String) null);
        if (resourceAttr != null) {
            String resourcePath = getStringVariableManager().performStringSubstitution(resourceAttr);
//            IResource resource = VDTPlugin.getWorkspace().getRoot().findMember(resourcePath);
            IResource resource = VerilogPlugin.getWorkspace().getRoot().findMember(resourcePath);
            
            if (resource != null) {
//                List<String> resources = new ArrayList<String>(1);
//                resources.add(resource.getLocation().toOSString());
//                return resources;
//                return parseVerilogFile(resource);
                if (resource instanceof IFile) { 
                        List<String> resourcesLocation = getVerilogFileDependencies((IFile)resource);
//                        resourcesLocation.add(resource.getLocation().toOSString());
                        return resourcesLocation;
                }       
            }
        }
        return null;
    } // getResources()
    
    private static List<String> getVerilogFileDependencies(IFile file) throws CoreException {
   	
//        StackTraceElement frame = new Exception().getStackTrace()[0];
//    	System.out.println("*** Broken core/tools/generators in "+frame.getFileName()+":"+frame.getLineNumber());
//        return null;
        
        IFile[] dependencies = VerilogUtils.getDependencies(file);
        List<String> dependenciesLocation = new ArrayList<String>();
        
        if(dependencies != null) {
            for (int i=0; i < dependencies.length; i++) {
                    dependenciesLocation.add(dependencies[i].getLocation().toOSString());
//                  System.out.println("  "+dependencies[i].getName());
            }    
        }
        return dependenciesLocation;
        
    }
    

    /** 
     * Returns an array of environment variables to be used when
     * launching the given configuration or <code>null</code> if unspecified.
     * 
     * @param configuration launch configuration
     * @throws CoreException if unable to access associated attribute or if
     * unable to resolve a variable in an environment variable's value
     */
    public static String[] getEnvironment(ILaunchConfiguration configuration) throws CoreException {
        return DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);        
    }
    
    /**
     * Expands and returns the working directory attribute of the given launch
     * configuration. Returns <code>null</code> if a working directory is not
     * specified. If specified, the working is verified to point to an existing
     * directory in the local file system.
     * 
     * @param configuration launch configuration
     * @return an absolute path to a directory in the local file system, or
     * <code>null</code> if unspecified
     * @throws CoreException if unable to retrieve the associated launch
     * configuration attribute, if unable to resolve any variables, or if the
     * resolved location does not point to an existing directory in the local
     * file system
     */
    public static String getWorkingDirectory(ILaunchConfiguration configuration) throws CoreException {
        String location = configuration.getAttribute(VDT.ATTR_WORKING_DIRECTORY, (String) null);
        if (location != null) {
            String expandedLocation = getStringVariableManager().performStringSubstitution(location);
            if (expandedLocation.length() > 0) {
                File path = new File(expandedLocation);
                if (path.isDirectory()) {
                    return expandedLocation;
                } 
                String message = "The working directory \""+expandedLocation+"\"does not exist for the external tool named "+configuration.getName()+".";
                abort(message, null);
            }
        }
        return null;
    }
    public static String getProjectPath(ILaunchConfiguration configuration) throws CoreException {
        return configuration.getAttribute(VDT.ATTR_PROJECT_PATH, (String) null);
    }
    private static Tool obtainTool(ILaunchConfiguration configuration) throws CoreException {
        String toolID = configuration.getAttribute(VDT.ATTR_TOOL_ID, (String) null);
        if (toolID == null)
            abort("Tool id is undefined in launch configuration "+configuration.getName(), null);
        
        Tool tool = ToolsCore.getToolWorkingCopy(toolID);
        if (tool == null)
            abort("Tool \""+toolID+"\" is unknown", null);

        return tool;
    }
    
    private static IStringVariableManager getStringVariableManager() {
        return VariablesPlugin.getDefault().getStringVariableManager();
    }

    public static String getLogBuildStamp(ILaunchConfiguration configuration) throws CoreException{
    	return configuration.getAttribute(VDT.ATTR_LOG_BUILD_STAMP, (String) null);
    }
    
    /**
     * Throws a core exception with an error status object built from
     * the given message, lower level exception, and error code.
     * 
     * @param message the status message
     * @param exception lower level exception associated with the
     *  error, or <code>null</code> if none
     * @param code error code
     */
    protected static void abort(String message, Throwable exception) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, 
                                           VDT.ID_VDT, 
                                           IStatus.ERROR, 
                                           message, 
                                           exception));
    }
            
/*
     private static VerilogProject getVerilogProject(String projectName) {
        IProject projects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int i=0; i < projects.length; i++) {
            if ( projectName.equals(projects[i].getName()) ) {
                return new VerilogProject(projects[i]);
            }
        }
        return null;
    } // getVerilogProject()
    
    private static class VerilogProject {
        private List<String> sourceFiles = new ArrayList<String>();
        
        public VerilogProject(IProject project) {
//            System.out.println("Projects file:");
            findProjectSourceFile(project);
        } // VerilogProject()

        private void findProjectSourceFile(IProject project) {
            IResource resources[];
            try {
                resources = project.members(0);
            } catch (CoreException e) {
                MessageUI.error(e);
                return;
            }    
            for (int i=0; i < resources.length; i++) {
                IResource res = resources[i];
//                System.out.println("    "+res.getLocation().toOSString());
                switch (res.getType()) {
                case IResource.FILE:
                    if (res.getName().endsWith(".v"))
                        sourceFiles.add(res.getLocation().toOSString());    
                    break;
                case IResource.FOLDER:
                    findFolderSourceFiles((IFolder)res);
                default:
                    break;
                }
            }
        } // findProjectSourceFile()
        
        private void findFolderSourceFiles(IFolder folder) {
            IResource resources[];
            try {
                resources = folder.members(0);
            } catch (CoreException e) {
                MessageUI.error(e);
                return;
            }    
            for (int i=0; i < resources.length; i++) {
                IResource res = resources[i];
  //              System.out.println("    "+res.getLocation().toOSString());
                switch (res.getType()) {
                case IResource.FILE:
                    if (res.getName().endsWith(".v"))
                        sourceFiles.add(res.getLocation().toOSString());    
                    break;
                case IResource.FOLDER:
                    findFolderSourceFiles((IFolder)res);
                default:
                    break;
                }
            }
        } // findFolderSourceFiles()
        
        public List<String> getSourceFiles() {
            return sourceFiles;
        }
    } // class ProjectVerilogFiles
*/
} // class VDTLaunchUtil
