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
 *******************************************************************************/
package com.elphel.vdt.ui.tools;


import java.io.File;
import java.util.List;
import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

import com.elphel.vdt.VDT;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.core.tools.params.types.RunFor;
import com.elphel.vdt.ui.VDTPluginImages;

/**
 * The user interface of tool parameters
 * 
 * Created: 27.12.2005
 * @author  Lvov Konstantin
 */

public class ToolUI {

    private static final String ICON_ID_PREFIX = VDT.ID_VDT + ".Tool.Image.";
 //   private static final String ICON_ID_ACTION = ".action.";
        
    private Tool   tool;
    private Config config;
    private String imageKey = null;
//    private String [] imageKeysActions = null;
    public ToolUI(Config config, Tool tool) {
        this.tool = tool;
        this.config = config; 
        String image = tool.getIconName();
        if (image != null) {
                imageKey = ICON_ID_PREFIX + (new File(tool.getExeName())).getName();
                VDTPluginImages.addImage(image, imageKey, null/*tool.getLaunchType()*/);
        }
        /*
        List<RunFor> runFor=tool.getRunFor();
        if (runFor!=null){
        	imageKeysActions=new String [runFor.size()];
        	for (int i=0;i<imageKeysActions.length;i++){
        		imageKeysActions[i]=null;
        		image = runFor.get(i).getIconName();
        		if (image != null) {
        			imageKeysActions[i] = ICON_ID_PREFIX + (new File(tool.getExeName())).getName()+ICON_ID_ACTION+i;
        			VDTPluginImages.addImage(image, imageKeysActions[i], null);
        		}
        	}
        }
        */
       
    } // ToolUI()
    
    public ILaunchConfigurationTab[] getLaunchConfigurationTabs() {
        String[] paramGroups = null;//tool.getParamGroups();
        List<LaunchConfigurationTab> tabs = new ArrayList<LaunchConfigurationTab>(); 
        
        for(int i = 0; i < paramGroups.length; i++)
            tabs.add(new LaunchConfigurationTab(paramGroups[i], this, paramGroups[i]));
        
        return tabs.toArray(new ILaunchConfigurationTab[tabs.size()]);
    }
        
    public boolean isApplicable(IResource resource) {
        if (resource == null)
            return false;

        String extensions[] = tool.getExtensions();
        if (extensions == null)
            return true;
        
        String resourceExt = resource.getFileExtension();
        if (resourceExt == null)
            return false;
        
        for (int i=0; i < extensions.length; i++) {
            if (resourceExt.equalsIgnoreCase(extensions[i]))
                return true;    
        }
        return false;
    } // isApplicable()
    
    public Tool getTool() {
        return tool;   
    }
    
    public Config getConfig() { 
        return config; 
    }
    
    public String getToolLocation() {
        return tool.getExeName();
    }
/*
    public String getToolShell() {
        return tool.getShellName();
    }
*/    
    public String getLabel() {
        return tool.getLabel();
    }
    
    public String getLaunchType() {
        return null;//tool.getLaunchType();
    }
    
    public String getImageKey() {
        return imageKey;
    }
    /*
    public String getImageKey(int actionIndex) {
    	if (imageKeysActions==null) return null;
        return imageKeysActions[actionIndex];
    }
    */
    
} // class ToolUI
