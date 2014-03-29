/*******************************************************************************
 * Copyright (c) 2014 Elphel, Inc.
 * Copyright (c) 2006 Elphel, Inc and Excelsior, LLC.
 * This file is a part of Eclipse/VDT plug-in.
 * Eclipse/VDT plug-in is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Eclipse/VDT plug-in is distributed in the hope that it will be useful,
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

import java.util.*;

import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.config.ConfigException;
import com.elphel.vdt.core.tools.menu.DesignMenu;


public class ContextManager {
    private Config config;
    
    private InstallationContext installationContext;
    
    private List<ProjectContext> projectContexts = new ArrayList<ProjectContext>();    
    private List<PackageContext> packageContexts = new ArrayList<PackageContext>();
    private List<Tool> toolContexts = new ArrayList<Tool>();
    
    private final String cannotAddMessage = "Cannot add a context after initialization"; 
    private boolean initialized = false;

    public void init(Config config) throws ConfigException {
        if(initialized)
            throw new ConfigException("Context layer cannot be re-initialized");
    
        this.config = config;
        
        if(installationContext != null)
            installationContext.init(config);
        
        for(PackageContext packageContext : packageContexts)
            packageContext.init(config);
        
        for(ProjectContext projectContext : projectContexts)
            projectContext.init(config);
        
        for(Tool toolContext : toolContexts)
            toolContext.init(config);
        
        sortToolContexts(); // according to priority for selecting "report" tools
        initialized = true;
    }

    public void addInstallationContexts(List<InstallationContext> contexts) throws ConfigException {
        if(initialized)
            throw new ConfigException(cannotAddMessage);

        if(contexts.isEmpty())
            return;
        
        if(installationContext != null || contexts.size() > 1)
            throw new ConfigException("There cannot be several installation contexts");
        
        installationContext = contexts.get(0);
    }

    public void addProjectContexts(List<ProjectContext> contexts) throws ConfigException {
        if(initialized)
            throw new ConfigException(cannotAddMessage);

        projectContexts.addAll(contexts);
    }

    public void addPackageContexts(List<PackageContext> contexts) throws ConfigException {
        if(initialized)
            throw new ConfigException(cannotAddMessage);

        packageContexts.addAll(contexts);
    }

    public void addToolContexts(List<Tool> contexts) throws ConfigException {
    	if(initialized)
    		throw new ConfigException(cannotAddMessage);

    	toolContexts.addAll(contexts);
    }
    
    public void sortToolContexts() throws ConfigException {
        boolean inOrder=false;
        while (!inOrder){
        	inOrder=true;
        	for (int i=0;i<(toolContexts.size()-1);i++){
        		if (toolContexts.get(i).getPriority()>toolContexts.get(i+1).getPriority()){
        			toolContexts.add(i,toolContexts.remove(i+1));
        			inOrder=false;
        		}
        	}
        }
    }
    
    
    
    public Context findContext(String contextName) {
        if(installationContext != null && installationContext.getName().equals(contextName))
            return installationContext;
        
        Context found = findContext(projectContexts, contextName);
        
        if(found != null)
            return found;
        
        found = findContext(packageContexts, contextName);
        
        if(found != null)
            return found;
        
        return findTool(contextName); 
    }
    
    public InstallationContext getInstallationContext() {
        return installationContext;
    }
   
    public ProjectContext getProjectContext(String projectName) {
        Context projectContext = findContext(projectName);

        if(projectContext != null) {
            assert projectContext instanceof ProjectContext;
            return (ProjectContext)projectContext;
        }
        
        return null;
    }
    
    public List<PackageContext> getPackageContexts() {
        return packageContexts;
    }
    
    public List<ProjectContext> getProjectContexts() {
        return projectContexts;
    }
    
    public List<Tool> getToolList() {
        return toolContexts;
    }
    
    public Tool findTool(String toolName) {
        return (Tool)findContext(toolContexts, toolName);
    }

    public DesignMenu getDesignMenu() {
        DesignMenu menu = null;
        
        //
        // TODO
        //

//        if(projectContext != null) {
//            menu = projectContext.getDesignMenu();
//            
//            if(menu != null)
//                return menu;
//        }

        if(installationContext != null) {
            menu = installationContext.getDesignMenu();
            
            if(menu != null)
                return menu;
        }
        
        return config.getDesignMenuManager().getRootDesignMenu();        
    }

    private Context findContext(List<? extends Context> contexts, String name) {
        for(Context context : contexts) {
 //       	System.out.println("Comparing "+context.getName() +" with "+name);
            if(context.getName().equals(name))
                return context;
        }
        
        return null;
    }
}
 