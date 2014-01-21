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
package com.elphel.vdt.ui.views;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;

import com.elphel.vdt.VDT;
import com.elphel.vdt.core.tools.ToolsCore;
import com.elphel.vdt.core.tools.config.Config;
import com.elphel.vdt.core.tools.contexts.PackageContext;
import com.elphel.vdt.core.tools.contexts.ProjectContext;
import com.elphel.vdt.core.tools.menu.DesignMenu;
import com.elphel.vdt.core.tools.menu.DesignMenuItem;
import com.elphel.vdt.core.tools.menu.DesignMenuToolItem;
import com.elphel.vdt.core.tools.params.Tool;
import com.elphel.vdt.ui.VDTPluginImages;

/**
 * Adapter of core design menu for user interface level.
 * 
 * Created: 28.03.2006
 * @author  Lvov Konstantin
 */

public class DesignMenuModel {

    private Config config;
    
    private MenuItem root;
    
    public DesignMenuModel(DesignMenu menu) {
        config = ToolsCore.getConfig();
        root = new MenuItem(null, menu);
    }
    
    public Item[] getItems() {
        return root.getChildren();  
    }

    // ------------------------------------------------------------------------
    public abstract class Item {
        private static final String ICON_ID_PREFIX = VDT.ID_VDT + ".DesignMenu.Image.";
        private String imageKey = null;

        private DesignMenuItem source;
        private Item parent;
        
        private Item(Item parent, DesignMenuItem source) {
            this.parent = parent;
            this.source = source;

            String image = source.getIcon();
            if (image != null) {
//              imageKey = ICON_ID_PREFIX + source.getName();
                imageKey = ICON_ID_PREFIX + (new File(source.getName())).getName();
                if (VDTPluginImages.getImage(imageKey) == null)
                    VDTPluginImages.addImage(image, imageKey, null/*tool.getLaunchType()*/);
            }
        }

        public boolean isEnabled(IResource resource) {
            return false;
        }
        public boolean isEnabled(String path) {
            return false;
        }

        public Object getParent() {
            return parent;
        }

        public Item[] getChildren() {
            return new Item[0];
        }
        public boolean hasChildren() {
            return false;
        }

        public String toString() {
            String label = source.getLabel();
            return label != null ? label : "Unknown";
        }
        
        public String getLabel() {
            return toString();
        }

        public String getImageKey() {
            return imageKey;
        }
        public Tool getTool() {
            return null;   
        }

        public Config getConfig() { 
            return null; 
        }

        public PackageContext getPackageContext() { 
            return  null; 
        }

        public ProjectContext getProjectContext() { 
            return  null; 
        }
    } // class Item
    
    // ------------------------------------------------------------------------
    public class ToolItem extends Item {
        private Tool tool;
        
        private ToolItem(Item parent, DesignMenuToolItem source) {
            super(parent, source);
            tool = ToolsCore.getTool(source.getToolName());
        }

        public boolean isEnabled(IResource resource) {
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
        } // isEnabled(IResource)

        public boolean isEnabled(String path) {
            if (path == null)
                return false;

            String extensions[] = tool.getExtensions();
            if (extensions == null)
                return true;
            int index=path.indexOf(".");
            if (index<0) return false;
            String resourceExt = path.substring(index+1);
            for (int i=0; i < extensions.length; i++) {
                if (resourceExt.equalsIgnoreCase(extensions[i]))
                    return true;    
            }
            return false;
        } // isEnabled(String)

        public Tool getTool() {
            return tool;   
        }

        public Config getConfig() { 
            return config; 
        }

        public PackageContext getPackageContext() {
            PackageContext context = tool.getParentPackage();
            if ((context != null) && context.isVisible())
                return context;
            else
                return null;
        }

        public ProjectContext getProjectContext() { 
            ProjectContext context = tool.getParentProject();
            if ((context != null) && context.isVisible())
                return context;
            else
                return null;
        }
    } // class ToolItem

    // ------------------------------------------------------------------------
    public class MenuItem extends Item {
        private Item[] items;
        
        private MenuItem(Item parent, DesignMenu source) {
            super(parent, source);
            
            List<Item> itemList = new ArrayList<Item>();
            
            for(Iterator<DesignMenuItem> i = source.getItems().iterator(); i.hasNext();) {
                DesignMenuItem menuitem = (DesignMenuItem)i.next();

                if(menuitem.isVisible())
                    itemList.add(CreateItem(this, menuitem)); 
            }
        
            items = (Item[])itemList.toArray(new Item[itemList.size()]);
        }    
        
        public Item[] getChildren() {
            return items;
        }
        
        public boolean hasChildren() {
            return items.length > 0;
        }

    } // class MenuItem

    private Item CreateItem(Item parent, DesignMenuItem source) {
        if (source instanceof DesignMenuToolItem)
            return new ToolItem(parent, (DesignMenuToolItem)source);
        else if (source instanceof DesignMenu) 
            return new MenuItem(parent, (DesignMenu)source);
        else
            assert(false);
        return null;
    }
    
} // class DesignMenuModel
