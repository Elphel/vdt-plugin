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

import com.elphel.vdt.core.tools.*;
import com.elphel.vdt.core.tools.contexts.*;
import com.elphel.vdt.core.tools.config.*;
import com.elphel.vdt.core.tools.params.conditions.ConditionUtils;
import com.elphel.vdt.core.tools.params.recognizers.*;


public class Tool extends Context implements Cloneable, Inheritable {
    private String baseToolName;
    private String parentPackageName;
    private String parentProjectName;
    private String exeName;
    
    private String toolErrors;
    private String toolWarnings;
    private String toolInfo;


    private List<String> extensions;
    private Tool baseTool;
    private PackageContext parentPackage;
    private ProjectContext parentProject;

    private String absoluteExeName = null;
    private boolean locationSet = false;
    private boolean isShell = false; /* Tool is a shell, preserve first argument, merge all others */
    private String projectPath=null;
    private boolean initialized = false;
    
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
    }

    public void init(Config config) throws ConfigException {
        if(initialized)
            return;
        
        this.config = config;
        
        initControlInterface();
        initBaseTool();

        Checks.checkCyclicInheritance(this, "tool");

        if(baseTool != null)
            baseTool.init(config);
        
        checkBaseTool();
        
        initParentPackage();
        initParentProject();
        initParams();
        initOtherAttributes();
        initCommandLines();
        
        initialized = true;
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

    public String getExeName() {
        if(locationSet)
            return absoluteExeName;
        
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
                                            new Recognizer[] { new ContextParamRecognizer(this) });

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
        Parameter param = super.findParam(paramID);
        
        if(param != null)
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
    
    public String[] buildParams() throws ToolException {
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
                                                            new SimpleGeneratorRecognizer(),
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
        if(parentProject != null)
            inheritParams(parentProject);        

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
        
        paramContainer.init(config, this);
        
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
        EntityUtils.update(context.getParams(), paramContainer.getParams());
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
