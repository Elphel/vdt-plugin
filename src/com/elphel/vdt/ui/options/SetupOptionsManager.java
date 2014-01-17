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
package com.elphel.vdt.ui.options;

import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.resource.ImageDescriptor;

import com.elphel.vdt.VDT;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;
import com.elphel.vdt.core.options.OptionsCore;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.VDTPluginImages;

/**
 * A preference manager maintains a hierarchy of 
 * packages and tools setup options.
 * 
 * Created: 21.04.2006
 * @author  Lvov Konstantin
 */

public class SetupOptionsManager extends PreferenceManager {

    private static final String PAGE_INSTALL_ID = VDT.ID_VDT + ".preferences.Install";

    // singleton
    private static SetupOptionsManager fDefault;
    
    private Context installContext;
    
    
    /**
     * Returns the singleton resource selection manager
     * 
     * @return SelectedResourceManager
     */
    public static SetupOptionsManager getDefault() {
        if (fDefault == null)
            fDefault = new SetupOptionsManager();
        return fDefault;
    }
    
    private SetupOptionsManager() {
        super('/');
        
        IPreferenceNode node = createInstallContextNode();
        if (node != null)
            addToRoot(node);

        for (PackageContext packageContext : ToolsCore.getContextManager().getPackageContexts())
            addToRoot(createPackageInstallNode(packageContext));

        for (Tool tool : ToolsCore.getStandaloneTools())
            addToRoot(createToolInstallNode(tool));
    }
    
    public Context getContext() {
        return installContext;
    }
    
    private IPreferenceNode createInstallContextNode() {
        installContext = ToolsCore.getContextManager().getInstallationContext();
        IPreferenceNode node = null;
        if (installContext.isVisible()) {
            node = new ContextPreferenceNode( PAGE_INSTALL_ID
                                            , VDTPluginImages.DESC_INSTALL_PROPERTIES 
                                            , installContext );
        }
        return node;
    }

    private IPreferenceNode createPackageInstallNode(PackageContext context) {
        ImageDescriptor image = VDTPluginImages.getImageDescriptor(context);
        if (image == null)
            image = VDTPluginImages.DESC_PACKAGE_PROPERTIES;
        String page_id = PAGE_INSTALL_ID+".Package."+context.getName();
        IPreferenceNode node = new PackageInstallNode( page_id 
                                                     , image
                                                     , context );
        for (Tool tool : ToolsCore.getTools(context))
            node.add(createToolInstallNode(tool));

        return node;
    }
    
    private IPreferenceNode createToolInstallNode(Tool tool) {
        ImageDescriptor image = VDTPluginImages.getImageDescriptor(tool);
        if (image == null)
            image = VDTPluginImages.DESC_RUN_TOOL;
        String page_id = PAGE_INSTALL_ID + ".Tool." + tool.getName();
        IPreferenceNode node = new ToolInstallNode( page_id
                                                  , image 
                                                  , tool );
        return node;
    }
    

    private static HashMap<String, String> pakageLocations = new HashMap<String, String>();
    
    public static String getCurrentLocation(PackageContext context) {
        String location = pakageLocations.get(context.getName());
        if (location == null) {
            IPreferenceStore store = VerilogPlugin.getDefault().getPreferenceStore();
            location = store.getString(OptionsCore.getLocationPreferenceName(context));
        }
        return location;
    }

    public static void setCurrentLocation(PackageContext context, String location) {
        pakageLocations.put(context.getName(), location);
    }


    public static void clear() {
        clearInstallationContext();
        
        for (PackageContext packageContext : ToolsCore.getContextManager().getPackageContexts())
            clearPackageInstallation(packageContext);    

        for (Tool tool : ToolsCore.getStandaloneTools())
            clearToolInstallation(tool);
    }

    private static void clearInstallationContext() {
        Context context = ToolsCore.getContextManager().getInstallationContext();
        OptionsCore.doClearContextOptions(context);
    }
    
    private static void clearPackageInstallation(PackageContext context) {
        PackageInstallPage.doClear(context);
        
        for (Tool tool : ToolsCore.getTools(context))
            clearToolInstallation(tool);
    }

    private static void clearToolInstallation(Tool tool) {
        ToolInstallPage.doClear(tool);
    }
    
} // class SetupOptionsManager
