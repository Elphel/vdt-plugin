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
package com.elphel.vdt.ui;


import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

// TODO: remove this import?
import org.eclipse.debug.internal.ui.DebugPluginImages;

import com.elphel.vdt.VDT;
//import com.elphel.vdt.VDTPlugin;
import com.elphel.vdt.veditor.VerilogPlugin;

import com.elphel.vdt.core.tools.contexts.Context;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.contexts.ProjectContext;
import com.elphel.vdt.core.tools.params.Tool;


/**
 * Bundle of all images used by the EFD plugin.
 * 
 * Created: 15.01.2006
 * @author  Lvov Konstantin
 */

public class VDTPluginImages {

    /** 
     * The image registry containing <code>Image</code>s.
     */
    private static ImageRegistry imageRegistry;

    public final static String ICONS_PATH = "icons"+File.separator; //$NON-NLS-1$

    public static final ImageDescriptor DESC_VERILOG_FILE    = create(ICONS_PATH, "eview16"+File.separator+"verilog_file.gif", null);

    public static final ImageDescriptor DESC_RUN_TOOL        = create(ICONS_PATH, "obj16"+File.separator+"run_tool.gif", null);
    public static final ImageDescriptor DESC_TOOL_PROPERTIES = create(ICONS_PATH, "obj16"+File.separator+"tool_prop.gif", null);
    public static final ImageDescriptor DESC_LAUNCH_CONFIG   = create(ICONS_PATH, "obj16"+File.separator+"vdt_tools.gif", null);
    public static final ImageDescriptor DESC_INSTALL_PROPERTIES = create(ICONS_PATH, "obj16"+File.separator+"install_prop.gif", null);
    public static final ImageDescriptor DESC_PACKAGE_PROPERTIES = create(ICONS_PATH, "obj16"+File.separator+"package_prop.gif", null);
    public static final ImageDescriptor DESC_PROJECT_PROPERTIES = create(ICONS_PATH, "obj16"+File.separator+"project_prop.gif", null);
    public static final ImageDescriptor DESC_DESIGM_MENU = create(ICONS_PATH, "obj16"+File.separator+"design_menu.gif", null);
    

    public static final String CHECKBOX_ON  = "CHECKBOX_ON";
    public static final String CHECKBOX_OFF = "CHECKBOX_OFF";
    public static final String CHECKBOX_ON_DISABLE  = "CHECKBOX_ON_DISABLE";
    public static final String CHECKBOX_OFF_DISABLE = "CHECKBOX_OFF_DISABLE";
    public static final String CHECKBOX_ON_DEFAULT  = "CHECKBOX_ON_DEFAULT";
    public static final String CHECKBOX_OFF_DEFAULT = "CHECKBOX_OFF_DEFAULT";
    public static final String CHECKBOX_ON_DEFAULT_DISABLE  = "CHECKBOX_ON_DEFAULT_DISABLE";
    public static final String CHECKBOX_OFF_DEFAULT_DISABLE = "CHECKBOX_OFF_DEFAULT_DISABLE";
    
    public static final ImageDescriptor DESC_CHECKBOX_ON  = create(ICONS_PATH, "obj16"+File.separator+"cb_on.gif", CHECKBOX_ON);
    public static final ImageDescriptor DESC_CHECKBOX_OFF = create(ICONS_PATH, "obj16"+File.separator+"cb_off.gif", CHECKBOX_OFF);
    public static final ImageDescriptor DESC_CHECKBOX_ON_DISABLE  = create(ICONS_PATH, "obj16"+File.separator+"cb_on_disable.gif", CHECKBOX_ON_DISABLE);
    public static final ImageDescriptor DESC_CHECKBOX_OFF_DISABLE = create(ICONS_PATH, "obj16"+File.separator+"cb_off_disable.gif", CHECKBOX_OFF_DISABLE);
    public static final ImageDescriptor DESC_CHECKBOX_ON_DEFAULT  = create(ICONS_PATH, "obj16"+File.separator+"cb_on_default.gif", CHECKBOX_ON_DEFAULT);
    public static final ImageDescriptor DESC_CHECKBOX_OFF_DEFAULT = create(ICONS_PATH, "obj16"+File.separator+"cb_off_default.gif", CHECKBOX_OFF_DEFAULT);
    public static final ImageDescriptor DESC_CHECKBOX_ON_DEFAULT_DISABLE  = create(ICONS_PATH, "obj16"+File.separator+"cb_on_disable_default.gif", CHECKBOX_ON_DEFAULT_DISABLE);
    public static final ImageDescriptor DESC_CHECKBOX_OFF_DEFAULT_DISABLE = create(ICONS_PATH, "obj16"+File.separator+"cb_off_disable_default.gif", CHECKBOX_OFF_DEFAULT_DISABLE);
    
    private static ImageDescriptor create (String prefix, String name, String key) {
        ImageDescriptor desc = ImageDescriptor.createFromURL(makeImageURL(prefix, name));
        if (key != null)
            getImageRegistry().put(key, desc);
        return desc;
    }
    
    public static void addImage(String fileName, String key, String launchType) {
        ImageDescriptor desc = null;
        File file = new File(fileName);
        if (file.isAbsolute()) {
            desc = ImageDescriptor.createFromFile(null, fileName);  
        } else {
            desc = ImageDescriptor.createFromURL(makeImageURL(ICONS_PATH, fileName));
        }
        
        if (key != null)
            getImageRegistry().put(key, desc);
        
        if ((launchType != null) && !VDT.ID_DEFAULT_LAUNCH_TYPE.equals(launchType))
            DebugPluginImages.getImageRegistry().put(launchType, desc);
            
    } // addImage()
    
    private static URL makeImageURL(String prefix, String name) {
        String path = "$nl$/" + prefix + name; //$NON-NLS-1$
        /* TODO: fix deprecation */
        URL url_try= org.eclipse.core.runtime.FileLocator.find(VerilogPlugin.getDefault().getBundle(), new Path(path), null);
        return Platform.find(VerilogPlugin.getDefault().getBundle(), new Path(path));
    }

    /**
     * Returns the <code>Image<code> identified by the given key,
     * or <code>null</code> if it does not exist.
     */
    public static Image getImage (String key) {
        return getImageRegistry().get(key);
    } // getImage()
    
    /**
     * Returns the <code>ImageDescriptor</code> identified by the given key,
     * or <code>null</code> if it does not exist.
     */
    public static ImageDescriptor getImageDescriptor(String key) {
            return getImageRegistry().getDescriptor(key);
    }
    
    /**
     * Returns the VDTPlugin ImageRegistry.
     */
    public static ImageRegistry getImageRegistry () {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry(VerilogPlugin.getStandardDisplay());
        }
        return imageRegistry;
    } // getImageRegistry()

    
    /**
     * Returns the <code>ImageDescriptor</code> identified by the given context,
     * or <code>null</code> if it does not exist.
     */
    public static ImageDescriptor getImageDescriptor(Context context) {
        String imageName = context.getIconName();
        if (imageName != null) {
            String imageKey = VDT.ID_VDT + ".ContextImage.";
            if (context instanceof PackageContext)
                imageKey = "Package.";
            else if (context instanceof ProjectContext)
                imageKey = "Project.";
            else if (context instanceof Tool)
                imageKey = "Tool.";
            imageKey += context.getName();
            
            ImageDescriptor image = VDTPluginImages.getImageDescriptor(imageKey);
            if (image == null) {
                VDTPluginImages.addImage(imageName, imageKey, null);
                image = VDTPluginImages.getImageDescriptor(imageKey);
            }
            return image;
        }
        return null;
    }
    
} // VDTPluginImages
