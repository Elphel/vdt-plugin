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
package com.elphel.vdt.ui;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
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

//    public static final ImageDescriptor DESC_RUN_TOOL        = create(ICONS_PATH, "obj16"+File.separator+"run_tool.gif", null);
    public static final ImageDescriptor DESC_RUN_TOOL         = create(ICONS_PATH, "obj16"+File.separator+"run_tool.png", null);
    public static final ImageDescriptor DESC_PLAY_BACK        = create(ICONS_PATH, "obj16"+File.separator+"play_back.png", null);
    public static final ImageDescriptor DESC_PLAY_BACK_SELECT = create(ICONS_PATH, "obj16"+File.separator+"play_back_select.png", null);
    public static final ImageDescriptor DESC_TOOL_PROPERTIES  = create(ICONS_PATH, "obj16"+File.separator+"tool_prop.gif", null);
    public static final ImageDescriptor DESC_LAUNCH_CONFIG    = create(ICONS_PATH, "obj16"+File.separator+"vdt_tools.gif", null);
    public static final ImageDescriptor DESC_INSTALL_PROPERTIES = create(ICONS_PATH, "obj16"+File.separator+"install_prop.gif", null);
    public static final ImageDescriptor DESC_PACKAGE_PROPERTIES = create(ICONS_PATH, "obj16"+File.separator+"package_prop.gif", null);
    public static final ImageDescriptor DESC_PROJECT_PROPERTIES = create(ICONS_PATH, "obj16"+File.separator+"project_prop.gif", null);
    public static final ImageDescriptor DESC_DESIGN_MENU =      create(ICONS_PATH, "obj16"+File.separator+"design_menu.gif", null);
    public static final ImageDescriptor DESC_ERASE =            create(ICONS_PATH, "obj16"+File.separator+"eraser.png", null);

    public static final ImageDescriptor DESC_TOOLS_LINKED =   create(ICONS_PATH, "obj16"+File.separator+"link.png", null);
    public static final ImageDescriptor DESC_TOOLS_UNLINKED = create(ICONS_PATH, "obj16"+File.separator+"broken_link.png", null);
    public static final ImageDescriptor DESC_TOOLS_SAVE = create(ICONS_PATH, "obj16"+File.separator+"save.png", null);
    public static final ImageDescriptor DESC_TOOLS_STOP = create(ICONS_PATH, "obj16"+File.separator+"stop.png", null);
    public static final ImageDescriptor DESC_TOOLS_PIN =            create(ICONS_PATH, "obj16"+File.separator+"pin.png", null);
    public static final ImageDescriptor DESC_TOOLS_RESTORE =        create(ICONS_PATH, "obj16"+File.separator+"restore.png", null);
    public static final ImageDescriptor DESC_TOOLS_RESTORE_SELECT = create(ICONS_PATH, "obj16"+File.separator+"restore_select.png", null);
    public static final String ICON_TOOLSTATE_NEW      = "obj16"+File.separator+"white.png";
    public static final String ICON_TOOLSTATE_BAD      = "obj16"+File.separator+"cross.png";
    public static final String ICON_TOOLSTATE_BAD_OLD  = "obj16"+File.separator+"cross_dim.png";
    public static final String ICON_TOOLSTATE_GOOD     = "obj16"+File.separator+"check.png";
    public static final String ICON_TOOLSTATE_GOOD_OLD = "obj16"+File.separator+"check_dim.png";
    public static final String ICON_TOOLSTATE_WTF     =  "obj16"+File.separator+"question.png";
    public static final String ICON_TOOLSTATE_WTF_OLD =  "obj16"+File.separator+"question_dim.png";
    public static final String ICON_TOOLSTATE_RUNNING =  "obj16"+File.separator+"spinning.gif";
    public static final String ICON_TOOLSTATE_WAITING =     "obj16"+File.separator+"hourglass.gif";
    public static final String ICON_TOOLSTATE_ALMOST_GOOD = "obj16"+File.separator+"check_almost.gif";
    public static final String ICON_TOOLSTATE_ALMOST_WTF =  "obj16"+File.separator+"question_almost.gif";
    
    public static final String ICON_TOOLSTATE_KEPT_OPEN= "obj16"+File.separator+"beat.gif";
    public static final String ICON_TOOLSTATE_PINNED  =  "obj16"+File.separator+"pinned_good.png";

    public static final String KEY_TOOLSTATE_NEW      = "TOOLSTATE_NEW";
    public static final String KEY_TOOLSTATE_BAD      = "TOOLSTATE_BAD";
    public static final String KEY_TOOLSTATE_BAD_OLD  = "TOOLSTATE_BAD_OLD";
    public static final String KEY_TOOLSTATE_GOOD     = "TOOLSTATE_GOOD";
    public static final String KEY_TOOLSTATE_GOOD_OLD = "TOOLSTATE_GOOD_OLD";
    public static final String KEY_TOOLSTATE_WTF     =  "TOOLSTATE_WTF";
    public static final String KEY_TOOLSTATE_WTF_OLD =  "TOOLSTATE_WTF_OLD";
    public static final String KEY_TOOLSTATE_RUNNING =  "TOOLSTATE_RUNNING";

    public static final String KEY_TOOLSTATE_WAITING =     "TOOLSTATE_WAITING";
    public static final String KEY_TOOLSTATE_ALMOST_GOOD = "TOOLSTATE_ALMOST_GOOD";
    public static final String KEY_TOOLSTATE_ALMOST_WTF =  "TOOLSTATE_ALMOST_WTF";
    
    public static final String KEY_TOOLSTATE_KEPT_OPEN ="TOOLSTATE_KEPT_OPEN";
    public static final String KEY_TOOLSTATE_PINNED  =  "TOOLSTATE_PINNED";


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
    
    
    private static Map<ImageDescriptor,ImageData[]> animatedGifMap = new HashMap<ImageDescriptor,ImageData[]>();
    
    private static ImageDescriptor create (String prefix, String name, String key) {
        ImageDescriptor desc = ImageDescriptor.createFromURL(makeImageURL(prefix, name));
        if (key != null)
            getImageRegistry().put(key, desc);
        return desc;
    }
    
    public static void addImage(String fileName, String key, String launchType) {
        ImageDescriptor desc = null;
        File file = new File(fileName);
        URL url=null;
        if (file.isAbsolute()) {
            desc = ImageDescriptor.createFromFile(null, fileName);  
        } else {
        	url=makeImageURL(ICONS_PATH, fileName);
            desc = ImageDescriptor.createFromURL(url);
        }
        
        if (key != null)
            getImageRegistry().put(key, desc);
        
        if ((launchType != null) && !VDT.ID_DEFAULT_LAUNCH_TYPE.equals(launchType))
            DebugPluginImages.getImageRegistry().put(launchType, desc);
//        ImageData [] imageData = new ImageLoader().load(new FileInputStream(fileName));
        ImageData [] imageData = null;
        if (url!=null){
        	try {
				imageData = new ImageLoader().load(url.openStream());
			} catch (IOException e) {
				System.out.println("Failed to open url "+url.toString());
				return;
			}
        } else {
        	try {
        	imageData = new ImageLoader().load(file.getAbsolutePath());
        	} catch (Exception e) {
				System.out.println("Failed to open absolute path "+file.getAbsolutePath());
				return;
        	}
        }
        if (imageData.length>1){
        	animatedGifMap.put(desc,imageData);
//            System.out.println("animatedGifMap.size()="+animatedGifMap.size());
//            System.out.println("imageData.length="+imageData.length);
        }
    } // addImage()
    
    private static URL makeImageURL(String prefix, String name) {
        String path = "$nl$/" + prefix + name; //$NON-NLS-1$
        /* TODO: fix deprecation */
//        URL url_try= org.eclipse.core.runtime.FileLocator.find(VerilogPlugin.getDefault().getBundle(), new Path(path), null);
//        return Platform.find(VerilogPlugin.getDefault().getBundle(), new Path(path));
        return FileLocator.find(VerilogPlugin.getDefault().getBundle(), new Path(path), null);
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
    
    public static ImageData [] getImageData(String key) {
    	ImageDescriptor imageDescriptor=getImageRegistry().getDescriptor(key);
    	return animatedGifMap.get(imageDescriptor);
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
